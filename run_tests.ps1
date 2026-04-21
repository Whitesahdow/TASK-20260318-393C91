$ErrorActionPreference = "Stop"

Write-Host ">>> [1/17] Starting Environment via Docker Compose..."
docker compose -f repo/docker-compose.yml up -d --build
if ($LASTEXITCODE -ne 0) {
    throw "Docker Compose failed during startup/build."
}

Write-Host ">>> [2/17] Waiting for Backend API..."
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
Write-Host ">>> [3/18] Running Security Module Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=SecurityModuleTest"'
if ($LASTEXITCODE -ne 0) {
    throw "Security module tests failed."
}

Write-Host ">>> [4/18] Running Data Integration Module Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=DataIntegrationModuleTest"'
if ($LASTEXITCODE -ne 0) {
    throw "Data integration tests failed."
}

Write-Host ">>> [5/18] Running Search Module Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=SearchModuleTest"'
if ($LASTEXITCODE -ne 0) {
    throw "Search module tests failed."
}

Write-Host ">>> [6/18] Running Notification Module Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=NotificationModuleTest"'
if ($LASTEXITCODE -ne 0) {
    throw "Notification module tests failed."
}

Write-Host ">>> [7/18] Running Workflow Module Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=WorkflowModuleTest"'
if ($LASTEXITCODE -ne 0) {
    throw "Workflow module tests failed."
}

Write-Host ">>> [8/18] Running Observability Module Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=ObservabilityModuleTest"'
if ($LASTEXITCODE -ne 0) {
    throw "Observability module tests failed."
}

Write-Host ">>> [9/18] Verifying Frontend Integration Build..."
docker exec bus_frontend test -f /usr/share/nginx/html/index.html
if ($LASTEXITCODE -ne 0) {
    throw "Frontend integration artifact missing."
}

Write-Host ">>> [10/18] Verifying Trace ID in Headers..."
$headersResp = Invoke-WebRequest -Uri "http://localhost:8080/api/health" -Method Head -UseBasicParsing
$traceId = $headersResp.Headers["X-Trace-ID"]

if ([string]::IsNullOrWhiteSpace($traceId)) {
    throw "FAILED: Observability Header (X-Trace-ID) missing!"
}
Write-Host "SUCCESS: Trace ID found: X-Trace-ID=$traceId"

Write-Host ">>> [11/18] Verifying Default Admin Seeding and BCrypt Hashing..."
$adminRow = docker exec bus_db psql -U bus_admin -d city_bus_platform -t -A -c "SELECT username || ':' || role || ':' || password_hash FROM users WHERE username='admin' LIMIT 1;"
if ($adminRow -match '^admin:ADMIN:\$2') {
    Write-Host "SUCCESS: Default admin seeded with BCrypt hash."
} else {
    throw "FAILED: Default admin record missing or password hash is not BCrypt."
}

Write-Host ">>> [12/18] Verifying Registration Boundary..."
$registerCode = curl.exe -s -o NUL -w "%{http_code}" -X POST -H "Content-Type: application/json" -d '{"username":"fail_user","password":"123","role":"PASSENGER"}' http://localhost:8080/api/auth/register
if ($registerCode -eq "400") {
    Write-Host "SUCCESS: Server rejected weak password (8-char rule active)."
} else {
    throw "FAILED: Server accepted weak password or returned unexpected status."
}

Write-Host ">>> [13/18] Triggering stop imports and verifying versioning..."
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

Write-Host ">>> [14/18] Verifying audit log persistence..."
$logHit = docker logs bus_backend 2>&1 | Select-String -Pattern "\[Audit\] Missing area"
if (-not $logHit) {
    throw "FAILED: Audit logging not detected in backend logs."
}
Write-Host "SUCCESS: Missing values are being logged to audit trail."

Write-Host ">>> [15/18] Verifying search API initials matching and deduplication..."
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

Write-Host ">>> [16/18] Verifying message masking and queue trace logs..."
$phase6User = "phase6_passenger"
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -ContentType "application/json" -Body (@{
        username = "phase6_passenger"
        password = "phase6pass123"
        role = "PASSENGER"
    } | ConvertTo-Json) | Out-Null
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -ne 409) {
        throw "FAILED: Could not ensure passenger test user exists (HTTP $statusCode)."
    }
}
Invoke-RestMethod -Uri "http://localhost:8080/api/passenger/messages/reminder?username=$phase6User" -Method Post -ContentType "application/json" -Body '{"stopName":"Central Avenue 3500"}' | Out-Null
Start-Sleep -Seconds 65
$maskedMsg = Invoke-RestMethod -Uri "http://localhost:8080/api/passenger/messages/latest?username=$phase6User" -Method Get
if (($maskedMsg.finalContent | Out-String) -notmatch "\*\*\*\*") {
    throw "FAILED: Sensitive content not masked in latest message."
}
$queueLog = docker logs bus_backend 2>&1 | Select-String -Pattern "Processing Queue" | Select-String -Pattern "traceId"
if (-not $queueLog) {
    throw "FAILED: Queue processing logs missing traceId."
}
Write-Host "SUCCESS: Message masking and queue trace logging verified."

Write-Host ">>> [17/18] Verifying backup file creation..."
Start-Sleep -Seconds 5
$backupFile = "repo/backups/backup_$(Get-Date -Format yyyyMMdd).sql"
if (-not (Test-Path $backupFile)) {
    throw "FAILED: Backup file not generated at $backupFile"
}
Write-Host "SUCCESS: Local backup generated."

Write-Host ">>> [18/18] Simulating P95 alert diagnostic..."
Invoke-RestMethod -Uri "http://localhost:8080/api/admin/maintenance/monitor/simulate-p95" -Method Get | Out-Null
$diagLog = docker logs bus_backend 2>&1 | Select-String -Pattern "Diagnostic Report Generated"
if (-not $diagLog) {
    throw "FAILED: P95 diagnostic report log not found."
}
Write-Host "SUCCESS: P95 diagnostic alert functional."

Write-Host "=========================================="
Write-Host "PROJECT COMPLETE: ALL AUDIT GATES PASSED"
Write-Host "=========================================="
