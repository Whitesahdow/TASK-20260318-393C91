# City Bus Operation and Service Coordination Platform

This repository contains the backend and frontend services for the City Bus Operation Platform.

## Prerequisites
- Docker and Docker Compose
- Node.js 18+ (for frontend development)
- Java 17+ and Maven (for backend development)

## Getting Started

To run the full stack locally via Docker Compose:

```bash
docker compose -f repo/docker-compose.yml up -d --build
```

This will start:
- PostgreSQL Database (`db` on port 5432)
- Spring Boot Backend (`backend` on port 8080)
- Angular Frontend (`frontend` on port 80)
- Backup Worker (`db_backup`)

## Testing & Verification
A static verification and end-to-end testing script is provided. You can run it on Windows:
```powershell
.\run_tests.ps1
```
This script will start the environment, wait for readiness, run all backend module tests via Maven, and execute a series of HTTP integration tests verifying security boundaries and workflow logic.

## Project Structure
- `repo/backend`: Spring Boot Java application
- `repo/frontend`: Angular application
- `repo/backups`: Database backup dumps
- `docs/`: Architecture and API specifications
