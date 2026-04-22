# City Bus Operation and Service Coordination Platform

This repository contains the backend and frontend services for the City Bus Operation Platform.

## Prerequisites
- Docker and Docker Compose
- Node.js 18+ (for frontend development)
- Java 17+ and Maven (for backend development)

## Getting Started

To run the full stack locally via Docker Compose, you must set required environment variables:

```bash
export POSTGRES_PASSWORD=secure_db_pass
export ADMIN_INITIAL_PASSWORD=admin
docker compose -f repo/docker-compose.yml up -d --build
```

This will start:
- PostgreSQL Database (`db` on port 5432)
- Spring Boot Backend (`backend` on port 8080)
- Angular Frontend (`frontend` on port 80)
- Backup Worker (`db_backup`)

If you want to run the application without Docker, you will need to manually start a PostgreSQL instance, provide the connection string to the Spring Boot application via `SPRING_DATASOURCE_URL`, and serve the Angular app using `ng serve`.

## Testing & Verification
A static verification and end-to-end testing script is provided. You can run it using bash:
```bash
bash run_tests.sh
```
This script will start the Docker environment, wait for readiness, run all backend module tests via Maven, and execute a series of HTTP integration tests verifying security boundaries and workflow logic.

## Project Structure
- `repo/backend`: Spring Boot Java application
- `repo/frontend`: Angular application
- `repo/backups`: Database backup dumps
- `docs/`: Architecture and API specifications
