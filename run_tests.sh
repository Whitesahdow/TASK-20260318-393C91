#!/bin/bash
set -e

echo ">>> PHASE 2: AUTH & INTEGRATION START"

docker compose -f repo/docker-compose.yml up -d --build

echo "Running Backend Security Tests..."
docker exec bus_backend mvn test -Dtest=AuthIntegrationTest

echo "Verifying Frontend Integration Build..."
docker exec bus_frontend test -f /usr/share/nginx/html/index.html

echo "Testing End-to-End Auth API (Negative Case)..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123"}' \
  http://localhost:8080/api/auth/login)

if [ "$HTTP_CODE" -eq 400 ]; then
    echo "SUCCESS: Server rejected weak password (8-char rule active)."
else
    echo "FAILED: Server accepted weak password or returned $HTTP_CODE"
    exit 1
fi

echo "PHASE 2 COMPLETE: Full-stack Security Scaffold is functional."
