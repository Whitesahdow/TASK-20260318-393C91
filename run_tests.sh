#!/bin/bash
# TASK-20260318-393C91: Phase 1 Verification Script
set -e

echo ">>> [1/4] Starting Environment via Docker Compose..."
docker compose -f repo/docker-compose.yml up -d --build

echo ">>> [2/4] Waiting for Backend API..."
MAX_RETRIES=30
COUNT=0
until $(curl --output /dev/null --silent --head --fail http://localhost:8080/api/health); do
    printf '.'
    sleep 1
    COUNT=$((COUNT+1))
    if [ $COUNT -eq $MAX_RETRIES ]; then
        echo "Timeout reached!"
        exit 1
    fi
done
echo " Backend is UP."

echo ">>> [3/4] Verifying Trace ID in Headers..."
TRACE_ID=$(curl -sI http://localhost:8080/api/health | grep -i X-Trace-ID)
if [ -z "$TRACE_ID" ]; then
    echo "FAILED: Observability Header (X-Trace-ID) missing!"
    exit 1
else
    echo "SUCCESS: Trace ID found: $TRACE_ID"
fi

echo ">>> [4/4] Verifying Database Connectivity..."
if docker logs bus_backend 2>&1 | grep -i "HikariPool-1 - Start completed" > /dev/null; then
    echo "SUCCESS: Database Connection Established."
else
    echo "FAILED: Database connection logs not found."
    exit 1
fi

echo "=========================================="
echo "PHASE 1 VERIFICATION COMPLETED SUCCESSFULLY"
echo "=========================================="
