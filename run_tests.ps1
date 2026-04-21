$ErrorActionPreference = "Stop"

Write-Host ">>> [1/14] Starting Environment via Docker Compose..."
docker compose -f repo/docker-compose.yml up -d --build
if ($LASTEXITCODE -ne 0) {
    throw "Docker Compose failed during startup/build."
}

Write-Host ">>> [2/14] Waiting for Backend API..."
$maxRetries = 60
$count = 0
while ($count -lt $maxRetries) {
    try {
        $resp = Invoke-WebRequest -Uri "http://localhost:8080/api/health" -Method Head -UseBasicParsing
        if ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 400) {
            break
        }
    } catch {
    }

    Write-Host -NoNewline "."
    Start-Sleep -Seconds 1
    $count++
}

if ($count -eq $maxRetries) {
    throw "Timeout reached waiting for backend health endpoint"
}

Write-Host " Backend is UP."
Write-Host ">>> [3/14] Running Backend Security Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=AuthIntegrationTest,com.busapp.service.AuthServiceTest"'
if ($LASTEXITCODE -ne 0) {
    throw "Backend security tests failed."
}

Write-Host ">>> [4/14] Running Data Cleaning Unit Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=DataCleaningTest"'
if ($LASTEXITCODE -ne 0) {
    throw "Data cleaning tests failed."
}

Write-Host ">>> [5/14] Running Intelligent Search Unit Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=SearchRankingTest,AutocompleteTest"'
if ($LASTEXITCODE -ne 0) {
    throw "Intelligent search tests failed."
}

Write-Host ">>> [6/14] Running Notification Logic Unit Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=DNDLogicTest,CheckInTriggerTest"'
if ($LASTEXITCODE -ne 0) {
    throw "Notification tests failed."
}

Write-Host ">>> [7/14] Verifying Frontend Integration Build..."
docker exec bus_frontend test -f /usr/share/nginx/html/index.html
if ($LASTEXITCODE -ne 0) {
    throw "Frontend integration artifact missing."
}

Write-Host ">>> [8/14] Verifying Trace ID in Headers..."
$headersResp = Invoke-WebRequest -Uri "http://localhost:8080/api/health" -Method Head -UseBasicParsing
$traceId = $headersResp.Headers["X-Trace-ID"]

if ([string]::IsNullOrWhiteSpace($traceId)) {
    throw "FAILED: Observability Header (X-Trace-ID) missing!"
}
Write-Host "SUCCESS: Trace ID found: X-Trace-ID=$traceId"

Write-Host ">>> [9/14] Verifying Default Admin Seeding and BCrypt Hashing..."
$adminRow = docker exec bus_db psql -U bus_admin -d city_bus_platform -t -A -c "SELECT username || ':' || role || ':' || password_hash FROM users WHERE username='admin' LIMIT 1;"
if ($adminRow -match '^admin:ADMIN:\$2') {
    Write-Host "SUCCESS: Default admin seeded with BCrypt hash."
} else {
    throw "FAILED: Default admin record missing or password hash is not BCrypt."
}

Write-Host ">>> [10/14] Verifying Registration Boundary..."
$registerCode = curl.exe -s -o NUL -w "%{http_code}" -X POST -H "Content-Type: application/json" -d '{"username":"fail_user","password":"123","role":"PASSENGER"}' http://localhost:8080/api/auth/register
if ($registerCode -eq "400") {
    Write-Host "SUCCESS: Server rejected weak password (8-char rule active)."
} else {
    throw "FAILED: Server accepted weak password or returned unexpected status."
}

Write-Host ">>> [11/14] Triggering stop imports and verifying versioning..."
$beforeCount = docker exec bus_db psql -U bus_admin -d city_bus_platform -t -A -c "SELECT count(*) FROM stop_version WHERE stop_name='Audit Stop';"
$beforeLatest = docker exec bus_db psql -U bus_admin -d city_bus_platform -t -A -c "SELECT COALESCE(MAX(version_number), 0) FROM stop_version WHERE stop_name='Audit Stop';"
$beforeCount = $beforeCount.Trim()
$beforeLatest = $beforeLatest.Trim()

$firstImportPayload = @{
    name = "Audit Stop"
    address = "North Road"
    residentialArea = "Garden Court"
    apartmentType = "2BR"
    area = 100
    unit = "sqft"
    price = "5600 yuan/month"
} | ConvertTo-Json

$secondImportPayload = @{
    name = "Audit Stop"
    address = "North Road"
    residentialArea = "Garden Court"
    apartmentType = "2BR"
    price = "5700 yuan/month"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/admin/stops/import" -Method Post -ContentType "application/json" -Body $firstImportPayload | Out-Null
Invoke-RestMethod -Uri "http://localhost:8080/api/admin/stops/import" -Method Post -ContentType "application/json" -Body $secondImportPayload | Out-Null

$versionCount = docker exec bus_db psql -U bus_admin -d city_bus_platform -t -A -c "SELECT count(*) FROM stop_version WHERE stop_name='Audit Stop';"
$latestVersion = docker exec bus_db psql -U bus_admin -d city_bus_platform -t -A -c "SELECT version_number FROM stop_version WHERE stop_name='Audit Stop' ORDER BY version_number DESC LIMIT 1;"
$versionCount = $versionCount.Trim()
$latestVersion = if ($null -eq $latestVersion) { "" } else { $latestVersion.Trim() }
if ([string]::IsNullOrWhiteSpace($latestVersion)) {
    throw "FAILED: stop_version query returned no rows for Audit Stop."
}
if ([int]$versionCount -lt ([int]$beforeCount + 2) -or [int]$latestVersion -ne ([int]$beforeLatest + 2)) {
    throw "FAILED: stop_version records/versioning not incremented correctly. beforeCount=$beforeCount afterCount=$versionCount beforeLatest=$beforeLatest afterLatest=$latestVersion"
}
Write-Host "SUCCESS: Stop versioning increments across imports."

Write-Host ">>> [12/14] Verifying audit log persistence..."
$logHit = docker logs bus_backend 2>&1 | Select-String -Pattern "\[Audit\] Missing area"
if (-not $logHit) {
    throw "FAILED: Audit logging not detected in backend logs."
}
Write-Host "SUCCESS: Missing values are being logged to audit trail."

Write-Host ">>> [13/14] Verifying search API initials matching and deduplication..."
$searchResultRaw = Invoke-RestMethod -Uri "http://localhost:8080/api/passenger/search?query=CA" -Method Get
$searchResultJson = $searchResultRaw | ConvertTo-Json -Depth 10
if ($searchResultJson -notmatch "Central Avenue") {
    throw "FAILED: Search did not return Central Avenue for query CA."
}
$uniqueStops = @($searchResultRaw | Select-Object -ExpandProperty stopName -Unique)
if ($uniqueStops.Count -ne 1) {
    throw "FAILED: Deduplication expected 1 unique stop row, got $($uniqueStops.Count)."
}
Write-Host "SUCCESS: Pinyin/initial matching and deduplication verified."

Write-Host ">>> [14/14] Verifying message masking and queue trace logs..."
Invoke-RestMethod -Uri "http://localhost:8080/api/passenger/messages/reminder?username=admin" -Method Post -ContentType "application/json" -Body '{"stopName":"Central Avenue 3500"}' | Out-Null
Start-Sleep -Seconds 65
$maskedMsg = Invoke-RestMethod -Uri "http://localhost:8080/api/passenger/messages/latest?username=admin" -Method Get
if (($maskedMsg.finalContent | Out-String) -notmatch "\*\*\*\*") {
    throw "FAILED: Sensitive content not masked in latest message."
}
$queueLog = docker logs bus_backend 2>&1 | Select-String -Pattern "Processing Queue" | Select-String -Pattern "traceId"
if (-not $queueLog) {
    throw "FAILED: Queue processing logs missing traceId."
}
Write-Host "SUCCESS: Message masking and queue trace logging verified."

Write-Host "=========================================="
Write-Host "PHASE 6 COMPLETE: Notification + message center + prior phases verified"
Write-Host "=========================================="
