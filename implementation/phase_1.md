
This Markdown file is designed as the master blueprint for Phase 1. It is structured for an AI Co-pilot or an expert developer to execute with 100% precision. It establishes the "Contract" for the entire project.
Phase 1: Environment Scaffold & Dockerization
Project Name: TASK-20260318-393C91
Focus: Infrastructure, Container Orchestration, and Observability Baseline.
1. Project Directory Structure
The root of the workspace must follow this structure. All code implementation happens inside repo/.
code
Text
TASK-20260318-393C91/
├── repo/
│   ├── backend/             # Spring Boot (Java 17)
│   ├── frontend/            # Angular 17+
│   └── docker-compose.yml   # Orchestration
├── docs/                    # Design & API Docs
├── run_tests.sh             # Standard Test Entrypoint (Gate 1.1)
└── metadata.json            # Project Metadata
2. Infrastructure Layer (The Core Contract)
2.1 repo/docker-compose.yml
This configuration ensures the backend waits for the database to be "Healthy" before starting, preventing startup race conditions.
code
Yaml
services:
  db:
    image: postgres:15-alpine
    container_name: bus_db
    environment:
      POSTGRES_USER: bus_admin
      POSTGRES_PASSWORD: salted_password_123
      POSTGRES_DB: city_bus_platform
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U bus_admin -d city_bus_platform"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    build: ./backend
    container_name: bus_backend
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/city_bus_platform
      SPRING_DATASOURCE_USERNAME: bus_admin
      SPRING_DATASOURCE_PASSWORD: salted_password_123

  frontend:
    build: ./frontend
    container_name: bus_frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  postgres_data:
3. Backend Implementation (Spring Boot)
3.1 Trace ID Observability
We implement the TraceIdFilter immediately. This ensures every log entry across the platform is tracked by a unique traceId (Requirement: Observability).
Location: repo/backend/src/main/java/com/busapp/infra/TraceIdFilter.java
code
Java
package com.busapp.infra;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.UUID;

@Component
public class TraceIdFilter implements Filter {
    private static final String TRACE_ID_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID_KEY, traceId);
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setHeader("X-Trace-ID", traceId); // For static audit verification
        
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }
}
3.2 Health Check Controller
Location: repo/backend/src/main/java/com/busapp/controller/HealthController.java
code
Java
package com.busapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "City Bus Platform Backend");
    }
}
4. Frontend Implementation (Angular)
4.1 Proxy Configuration
To allow the English interface to communicate with the Spring API in an offline LAN without CORS issues.
Location: repo/frontend/proxy.conf.json
code
JSON
{
  "/api": {
    "target": "http://backend:8080",
    "secure": false
  }
}
5. Verification Script (The Entrypoint)
This script is the Standard Test Entrypoint. If this script fails, the project fails the audit.
Location: ./run_tests.sh
code
Bash
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
# Check if backend successfully connected to Postgres
if docker logs bus_backend 2>&1 | grep -i "HikariPool-1 - Start completed" > /dev/null; then
    echo "SUCCESS: Database Connection Established."
else
    echo "FAILED: Database connection logs not found."
    exit 1
fi

echo "=========================================="
echo "PHASE 1 VERIFICATION COMPLETED SUCCESSFULLY"
echo "=========================================="
6. Phase 1 Exit Criteria
Hard Gate 1.1: docker compose up starts DB, Backend, and Frontend without errors.
Hard Gate 4.1: All requests produce an X-Trace-ID header.
Audit Gate: run_tests.sh returns exit code 0.
Instructions for Co-pilot:
Implement all file contents exactly as written.
Ensure run_tests.sh is given execution permissions (chmod +x).
Verify that the logs in bus_backend show the Spring Boot banner.