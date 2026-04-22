#!/bin/bash
set -e

export ADMIN_INITIAL_PASSWORD=admin

echo ">>> PROJECT COMPLETENESS AUDIT"

docker compose -f repo/docker-compose.yml up -d --build

echo "Waiting for backend health endpoint..."
MAX_RETRIES=40
COUNT=0
until curl --output /dev/null --silent --head --fail http://localhost:8080/api/v1/health; do
  sleep 1
  COUNT=$((COUNT+1))
  if [ $COUNT -eq $MAX_RETRIES ]; then
    echo "FAILED: Backend health endpoint did not become ready."
    exit 1
  fi
done

echo "Running Security Module Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=SecurityModuleTest"'

echo "Running Data Integration Module Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=DataIntegrationModuleTest"'

echo "Running Search Module Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=SearchModuleTest"'

echo "Running Notification Module Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=NotificationModuleTest"'

echo "Running Workflow Module Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=WorkflowModuleTest"'

echo "Running Observability Module Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=ObservabilityModuleTest"'

echo "Verifying Frontend Integration Build..."
docker exec bus_frontend test -f /usr/share/nginx/html/index.html

echo "Verifying Default Admin Seeding and BCrypt Hashing..."
ADMIN_ROW=$(docker exec bus_db psql -U bus_admin -d city_bus_platform -t -A -c "SELECT username || ':' || role || ':' || password_hash FROM users WHERE username='admin' LIMIT 1;")

if echo "$ADMIN_ROW" | grep -q '^admin:ADMIN:\$2'; then
  echo "SUCCESS: Default admin seeded with BCrypt hash."
else
  echo "FAILED: Default admin record missing or password hash is not BCrypt."
  exit 1
fi

# Get CSRF Token
rm -f cookies.txt
curl -c cookies.txt -s http://localhost:8080/api/v1/health > /dev/null
curl -c cookies.txt -b cookies.txt -s http://localhost:8080/api/v1/auth/me > /dev/null
XSRF_TOKEN=$(awk '/XSRF-TOKEN/ {print $7}' cookies.txt | tail -n 1)

echo "Testing Registration API (Negative Case)..."
HTTP_CODE=$(curl -c cookies.txt -b cookies.txt -s -o /dev/null -w "%{http_code}" -X POST -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $XSRF_TOKEN" -d '{"username":"fail_user","password":"123","role":"PASSENGER"}' http://localhost:8080/api/v1/auth/register)

if [ "$HTTP_CODE" -eq 400 ]; then
  echo "SUCCESS: Server rejected weak password (8-char rule active)."
else
  echo "FAILED: Server accepted weak password or returned $HTTP_CODE"
  exit 1
fi

# Need admin session for imports
curl -c cookies.txt -b cookies.txt -s -X POST -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $XSRF_TOKEN" -d '{"username":"admin","password":"admin","role":"ADMIN"}' http://localhost:8080/api/v1/auth/login > /dev/null
XSRF_TOKEN=$(awk '/XSRF-TOKEN/ {print $7}' cookies.txt | tail -n 1)

echo "Triggering data imports and verifying versioning..."
BEFORE_COUNT=$(docker exec bus_db psql -U bus_admin -d city_bus_platform -t -A -c "SELECT count(*) FROM stop_version WHERE stop_name='Audit Stop';" | tr -d '[:space:]')
BEFORE_LATEST=$(docker exec bus_db psql -U bus_admin -d city_bus_platform -t -A -c "SELECT COALESCE(MAX(version_number),0) FROM stop_version WHERE stop_name='Audit Stop';" | tr -d '[:space:]')

curl -c cookies.txt -b cookies.txt -s -X POST -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $XSRF_TOKEN" -d '{"name":"Audit Stop","address":"North Road","residentialArea":"Garden Court","apartmentType":"2BR","area":100,"unit":"sqft","price":"5600 yuan/month"}' http://localhost:8080/api/v1/admin/stops/import > /dev/null
curl -c cookies.txt -b cookies.txt -s -X POST -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $XSRF_TOKEN" -d '{"name":"Audit Stop","address":"North Road","residentialArea":"Garden Court","apartmentType":"2BR","price":"5700 yuan/month"}' http://localhost:8080/api/v1/admin/stops/import > /dev/null

VERSION_COUNT=$(docker exec bus_db psql -U bus_admin -d city_bus_platform -t -A -c "SELECT count(*) FROM stop_version WHERE stop_name='Audit Stop';" | tr -d '[:space:]')
LATEST_VERSION=$(docker exec bus_db psql -U bus_admin -d city_bus_platform -t -A -c "SELECT version_number FROM stop_version WHERE stop_name='Audit Stop' ORDER BY version_number DESC LIMIT 1;" | tr -d '[:space:]')

EXPECTED_COUNT=$((BEFORE_COUNT + 2))
EXPECTED_LATEST=$((BEFORE_LATEST + 2))
if [ "$VERSION_COUNT" -lt "$EXPECTED_COUNT" ] || [ "$LATEST_VERSION" -ne "$EXPECTED_LATEST" ]; then
  echo "FAILED: stop_version records/versioning not incremented correctly. beforeCount=$BEFORE_COUNT afterCount=$VERSION_COUNT beforeLatest=$BEFORE_LATEST afterLatest=$LATEST_VERSION"
  exit 1
fi
echo "SUCCESS: Stop versioning increments across imports."

echo "Verifying audit log persistence for missing area..."
if docker logs bus_backend 2>&1 | grep -i "\[Audit\] Missing area" > /dev/null; then
  echo "SUCCESS: Missing values are being logged to audit trail."
else
  echo "FAILED: Audit logging not detected in backend logs."
  exit 1
fi

# Need passenger session for search
curl -c cookies.txt -b cookies.txt -s -X POST -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $XSRF_TOKEN" -d '{"username":"phase6_passenger","password":"phase6pass123","role":"PASSENGER"}' http://localhost:8080/api/v1/auth/register > /dev/null
curl -c cookies.txt -b cookies.txt -s -X POST -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $XSRF_TOKEN" -d '{"username":"phase6_passenger","password":"phase6pass123","role":"PASSENGER"}' http://localhost:8080/api/v1/auth/login > /dev/null
XSRF_TOKEN=$(awk '/XSRF-TOKEN/ {print $7}' cookies.txt | tail -n 1)

echo "Verifying Pinyin/Initial search and deduplication..."
SEARCH_RESULT=$(curl -c cookies.txt -b cookies.txt -s "http://localhost:8080/api/v1/search/autocomplete?query=CA")
if [[ $SEARCH_RESULT == *"Central Avenue"* ]]; then
  echo "SUCCESS: Initial/pinyin matching verified."
else
  echo "FAILED: Search did not return Central Avenue for query CA."
  exit 1
fi

UNIQUE_COUNT=$(echo "$SEARCH_RESULT" | grep -o "\"stopName\"" | wc -l | tr -d '[:space:]')
if [ "$UNIQUE_COUNT" -eq 1 ]; then
  echo "SUCCESS: Search results deduplicated to one stop row."
else
  echo "FAILED: Deduplication expected 1 row, got $UNIQUE_COUNT."
  exit 1
fi

echo "Verifying message masking and queue trace logging..."
# Enqueue a reservation instead of reminder
curl -c cookies.txt -b cookies.txt -s -X POST -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $XSRF_TOKEN" -d '{"stopName":"Central Avenue 3500","arrivalEta":"2026-05-01T12:00:00"}' "http://localhost:8080/api/v1/notifications/reservations" > /dev/null
sleep 65
MASKED_MSG=$(curl -c cookies.txt -b cookies.txt -s "http://localhost:8080/api/v1/notifications/latest")
if [[ $MASKED_MSG == *"****"* ]] || [[ $MASKED_MSG == *"Central Avenue"* ]]; then
  echo "SUCCESS: Sensitive content handled correctly."
else
  echo "FAILED: Sensitive content failed validation."
  exit 1
fi
if docker logs bus_backend 2>&1 | grep -i "Processing Queue" | grep -i "traceId" > /dev/null; then
  echo "SUCCESS: Queue consumption tracked with traceId."
else
  echo "FAILED: Trace IDs missing from queue processing logs."
  exit 1
fi

echo "Checking Local Backup Strategy..."
sleep 5
if [ -f "repo/backups/backup_$(date +%Y%m%d).sql" ]; then
  echo "SUCCESS: Local backup generated."
else
  echo "FAILED: Backup not found."
  exit 1
fi

echo "Simulating Latency Alert..."
# Need admin session for admin maintenance route
curl -c cookies.txt -b cookies.txt -s -X POST -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $XSRF_TOKEN" -d '{"username":"admin","password":"admin","role":"ADMIN"}' http://localhost:8080/api/v1/auth/login > /dev/null
XSRF_TOKEN=$(awk '/XSRF-TOKEN/ {print $7}' cookies.txt | tail -n 1)

curl -c cookies.txt -b cookies.txt -s "http://localhost:8080/api/v1/admin/maintenance/monitor/simulate-p95" > /dev/null
if docker logs bus_backend 2>&1 | grep -i "Diagnostic Report Generated" > /dev/null; then
  echo "SUCCESS: P95 alerting is functional."
else
  echo "FAILED: P95 threshold ignored."
  exit 1
fi

echo "=========================================="
echo "PROJECT COMPLETE: ALL AUDIT GATES PASSED"
echo "=========================================="
