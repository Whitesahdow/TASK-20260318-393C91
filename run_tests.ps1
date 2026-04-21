$ErrorActionPreference = "Stop"

Write-Host ">>> [1/4] Starting Environment via Docker Compose..."
docker compose -f repo/docker-compose.yml up -d --build
if ($LASTEXITCODE -ne 0) {
    throw "Docker Compose failed during startup/build."
}

Write-Host ">>> [2/4] Waiting for Backend API..."
$maxRetries = 30
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
Write-Host ">>> [3/4] Verifying Trace ID in Headers..."
$headersResp = Invoke-WebRequest -Uri "http://localhost:8080/api/health" -Method Head -UseBasicParsing
$traceId = $headersResp.Headers["X-Trace-ID"]

if ([string]::IsNullOrWhiteSpace($traceId)) {
    throw "FAILED: Observability Header (X-Trace-ID) missing!"
}
Write-Host "SUCCESS: Trace ID found: X-Trace-ID=$traceId"

Write-Host ">>> [4/4] Verifying Database Connectivity..."
$logs = docker logs bus_backend 2>&1
if ($logs -match "HikariPool-1 - Start completed") {
    Write-Host "SUCCESS: Database Connection Established."
} else {
    throw "FAILED: Database connection logs not found."
}

Write-Host "=========================================="
Write-Host "PHASE 1 VERIFICATION COMPLETED SUCCESSFULLY"
Write-Host "=========================================="
