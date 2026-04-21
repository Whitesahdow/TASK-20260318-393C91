#!/bin/bash
set -e

echo ">>> PHASE 6: FULL PLATFORM VERIFICATION (PHASE 1-6)"

docker compose -f repo/docker-compose.yml up -d --build

echo "Waiting for backend health endpoint..."
MAX_RETRIES=40
COUNT=0
until curl --output /dev/null --silent --head --fail http://localhost:8080/api/health; do
  sleep 1
  COUNT=$((COUNT+1))
  if [ $COUNT -eq $MAX_RETRIES ]; then
    echo "FAILED: Backend health endpoint did not become ready."
    exit 1
  fi
done

echo "Running Backend Security Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=AuthIntegrationTest,com.busapp.service.AuthServiceTest"'

echo "Running Data Cleaning Unit Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=DataCleaningTest"'

echo "Running Intelligent Search Unit Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=SearchRankingTest,AutocompleteTest"'

echo "Running Notification Logic Unit Tests..."
docker exec -e HOME=/root -e MAVEN_CONFIG=/root/.m2 -w /app bus_backend sh -lc 'mvn -o test "-Dtest=DNDLogicTest,CheckInTriggerTest"'

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

echo "Testing Registration API (Negative Case)..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST -H "Content-Type: application/json" -d '{"username":"fail_user","password":"123","role":"PASSENGER"}' http://localhost:8080/api/auth/register)

if [ "$HTTP_CODE" -eq 400 ]; then
  echo "SUCCESS: Server rejected weak password (8-char rule active)."
else
  echo "FAILED: Server accepted weak password or returned $HTTP_CODE"
  exit 1
fi

echo "Triggering data imports and verifying versioning..."
BEFORE_COUNT=$(docker exec bus_db psql -U bus_admin -d city_bus_platform -t -A -c "SELECT count(*) FROM stop_version WHERE stop_name='Audit Stop';" | tr -d '[:space:]')
BEFORE_LATEST=$(docker exec bus_db psql -U bus_admin -d city_bus_platform -t -A -c "SELECT COALESCE(MAX(version_number),0) FROM stop_version WHERE stop_name='Audit Stop';" | tr -d '[:space:]')
curl -s -X POST -H "Content-Type: application/json" -d '{"name":"Audit Stop","address":"North Road","residentialArea":"Garden Court","apartmentType":"2BR","area":100,"unit":"sqft","price":"5600 yuan/month"}' http://localhost:8080/api/admin/stops/import > /dev/null
curl -s -X POST -H "Content-Type: application/json" -d '{"name":"Audit Stop","address":"North Road","residentialArea":"Garden Court","apartmentType":"2BR","price":"5700 yuan/month"}' http://localhost:8080/api/admin/stops/import > /dev/null

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
if docker logs bus_backend 2>&1 | rg -i "\\[Audit\\] Missing area" > /dev/null; then
  echo "SUCCESS: Missing values are being logged to audit trail."
else
  echo "FAILED: Audit logging not detected in backend logs."
  exit 1
fi

echo "Verifying Pinyin/Initial search and deduplication..."
SEARCH_RESULT=$(curl -s "http://localhost:8080/api/passenger/search?query=CA")
if [[ $SEARCH_RESULT == *"Central Avenue"* ]]; then
  echo "SUCCESS: Initial/pinyin matching verified."
else
  echo "FAILED: Search did not return Central Avenue for query CA."
  exit 1
fi

UNIQUE_COUNT=$(echo "$SEARCH_RESULT" | rg -o "\"stopName\"" | wc -l | tr -d '[:space:]')
if [ "$UNIQUE_COUNT" -eq 1 ]; then
  echo "SUCCESS: Search results deduplicated to one stop row."
else
  echo "FAILED: Deduplication expected 1 row, got $UNIQUE_COUNT."
  exit 1
fi

echo "Verifying message masking and queue trace logging..."
curl -s -X POST -H "Content-Type: application/json" -d '{"stopName":"Central Avenue 3500"}' "http://localhost:8080/api/passenger/messages/reminder?username=admin" > /dev/null
sleep 65
MASKED_MSG=$(curl -s "http://localhost:8080/api/passenger/messages/latest?username=admin")
if [[ $MASKED_MSG == *"****"* ]]; then
  echo "SUCCESS: Sensitive content masked for passenger/admin view policy."
else
  echo "FAILED: Sensitive content exposed."
  exit 1
fi
if docker logs bus_backend 2>&1 | rg -i "Processing Queue" | rg -i "traceId" > /dev/null; then
  echo "SUCCESS: Queue consumption tracked with traceId."
else
  echo "FAILED: Trace IDs missing from queue processing logs."
  exit 1
fi

echo "=========================================="
echo "PHASE 6 COMPLETE: Notification + message center + prior phases verified"
echo "=========================================="
