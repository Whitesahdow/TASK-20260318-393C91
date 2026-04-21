# Delivery Acceptance and Project Architecture Audit

## 1. Verdict
- Overall conclusion: Fail

## 2. Scope and Static Verification Boundary
- What was reviewed:
  - Project docs and API notes in [docs/design.md](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/docs/design.md:1), [docs/api-spec.md](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/docs/api-spec.md:1), and [docs/questions.md](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/docs/questions.md:1)
  - Backend Spring Boot source, config, controllers, services, repositories, security, and models under `repo/backend/src/main`
  - Frontend Angular routes, guards, services, interceptors, and role UIs under `repo/frontend/src`
  - Backend tests under `repo/backend/src/test/java`
  - Build/config manifests including [repo/backend/pom.xml](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/pom.xml:1) and [repo/backend/src/main/resources/application.yml](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/resources/application.yml:1)
- What was not reviewed:
  - Runtime behavior, browser rendering, real DB connectivity, session persistence in a running server, Docker/container behavior, scheduler execution, and real backup execution
- What was intentionally not executed:
  - Project startup, tests, Docker, database, browser flows, external services
- Which claims require manual verification:
  - Actual authentication/session persistence after login
  - Actual startup success with the current dependency set
  - Real Angular-to-backend interoperability in browser
  - Live scheduler behavior, queue backlog alerts, and P95 diagnostic generation

## 3. Repository / Requirement Mapping Summary
- Prompt core goal:
  - Deliver a full-stack offline LAN bus operations platform with Angular passenger UI, dispatcher workflow dashboard, administrator maintenance console, Spring Boot REST APIs, PostgreSQL storage and backup, local username/password auth, in-platform queued notifications, HTML/JSON data import and cleaning, and observability with trace IDs, health checks, metrics, and alerts.
- Main implementation areas mapped:
  - Passenger search and notifications: [repo/backend/src/main/java/com/busapp/controller/PassengerSearchController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/PassengerSearchController.java:13), [repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java:18), [repo/frontend/src/app/features/passenger/passenger.component.ts](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/src/app/features/passenger/passenger.component.ts:40)
  - Dispatcher workflow: [repo/backend/src/main/java/com/busapp/controller/DispatcherWorkflowController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/DispatcherWorkflowController.java:16), [repo/backend/src/main/java/com/busapp/service/WorkflowService.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/service/WorkflowService.java:13), [repo/frontend/src/app/features/dispatcher/dispatcher.component.ts](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/src/app/features/dispatcher/dispatcher.component.ts:20)
  - Admin maintenance and import/cleaning: [repo/backend/src/main/java/com/busapp/controller/AdminMaintenanceController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/AdminMaintenanceController.java:17), [repo/backend/src/main/java/com/busapp/controller/DataIntegrationController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/DataIntegrationController.java:26), [repo/backend/src/main/java/com/busapp/service/DataCleaningService.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/service/DataCleaningService.java:20)
  - Auth and observability: [repo/backend/src/main/java/com/busapp/controller/AuthController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/AuthController.java:21), [repo/backend/src/main/java/com/busapp/security/SecurityConfig.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:18), [repo/backend/src/main/java/com/busapp/infra/TraceIdFilter.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/infra/TraceIdFilter.java:15), [repo/backend/src/main/java/com/busapp/infra/monitoring/ApiTimingInterceptor.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/infra/monitoring/ApiTimingInterceptor.java:12)
- Summary:
  - The repository is materially aligned with the business domain, but it fails acceptance because the backend security layer is statically inconsistent with the Maven dependencies, the admin frontend is wired to obsolete endpoints, workflow delivery remains partial, and the current tests do not cover the highest-risk paths and no longer match the API surface.

## 4. Section-by-section Review

### 1. Hard Gates
- 1.1 Documentation and static verifiability
  - Conclusion: Fail
  - Rationale: There is no README or equivalent startup/run/test guide. The docs describe an `/api/v1` contract, but frontend admin calls still target `/api/admin/...` while backend controllers expose `/api/v1/admin/...`, so documentation/code/UI are not statically consistent enough for straightforward verification.
  - Evidence: [docs/api-spec.md](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/docs/api-spec.md:2), [repo/frontend/src/app/features/admin/console/console.component.ts](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/src/app/features/admin/console/console.component.ts:39), [repo/frontend/src/app/features/admin/dictionary/dictionary.component.ts](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/src/app/features/admin/dictionary/dictionary.component.ts:79), [repo/backend/src/main/java/com/busapp/controller/AdminMaintenanceController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/AdminMaintenanceController.java:18), [repo/backend/src/main/java/com/busapp/controller/DataIntegrationController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/DataIntegrationController.java:27)
  - Manual verification note: Startup success and real integration cannot be assumed because the current dependency/config state is not statically coherent.
- 1.2 Whether the delivered project materially deviates from the Prompt
  - Conclusion: Partial Pass
  - Rationale: The implementation is centered on the prompt domain and now includes most major modules, but key requirements remain weakened or incomplete: workflow parallel approvals are only partially modeled, admin UI is statically broken against the backend, and observability/health delivery remains incomplete.
  - Evidence: [repo/backend/src/main/java/com/busapp/service/WorkflowService.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/service/WorkflowService.java:45), [repo/frontend/src/app/features/admin/console/console.component.ts](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/src/app/features/admin/console/console.component.ts:39), [repo/backend/src/main/java/com/busapp/controller/HealthController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/HealthController.java:10)

### 2. Delivery Completeness
- 2.1 Whether the delivered project fully covers the core requirements explicitly stated in the Prompt
  - Conclusion: Partial Pass
  - Rationale: Present statically: passenger search with autocomplete, dedupe, pinyin initial/full and keyword matching, notification preferences with DND, message center, reservation and missed-check-in queue writes, dispatcher task dashboard with progress and batch approval, admin templates/weights/dictionaries/import, HTML/JSON parsing, cleaning/versioning, trace IDs, queue backlog warning, and API latency monitoring hook. Incomplete or weak: workflow joint/parallel approval is represented only by two booleans and no true multi-actor task model; admin frontend paths do not match backend; custom health endpoint path differs from the security contract and docs; metrics/alerts are minimal.
  - Evidence: [repo/backend/src/main/java/com/busapp/repository/SearchRepository.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/repository/SearchRepository.java:23), [repo/backend/src/main/java/com/busapp/service/NotificationService.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/service/NotificationService.java:52), [repo/backend/src/main/java/com/busapp/service/WorkflowService.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/service/WorkflowService.java:54), [repo/frontend/src/app/features/admin/console/console.component.ts](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/src/app/features/admin/console/console.component.ts:39)
- 2.2 Whether the delivered project represents a basic end-to-end deliverable from 0 to 1
  - Conclusion: Partial Pass
  - Rationale: The repository has a complete project structure and non-trivial implementation, but the missing operator guide, stale frontend admin API wiring, and security dependency inconsistency keep it from being a statically verifiable end-to-end deliverable.
  - Evidence: [repo/frontend/src/main.ts](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/src/main.ts:31), [repo/backend/src/main/java/com/busapp/BackendApplication.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/BackendApplication.java:7), [repo/backend/pom.xml](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/pom.xml:23)

### 3. Engineering and Architecture Quality
- 3.1 Whether the project adopts a reasonable engineering structure and module decomposition
  - Conclusion: Pass
  - Rationale: The repository is separated into backend controllers/services/repositories/models and frontend feature modules by role and concern. There is no single-file collapse.
  - Evidence: [repo/backend/src/main/java/com/busapp/controller/AuthController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/AuthController.java:1), [repo/backend/src/main/java/com/busapp/service/AuthService.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/service/AuthService.java:1), [repo/frontend/src/app/features/passenger/passenger.component.ts](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/src/app/features/passenger/passenger.component.ts:1)
- 3.2 Whether the project shows maintainability and extensibility
  - Conclusion: Partial Pass
  - Rationale: The code has reasonable layering, but workflow approvals are modeled in a narrow, hard-coded way, and the frontend/backend API drift shows weak contract discipline. The security stack is also introduced without the matching starter dependency.
  - Evidence: [repo/backend/src/main/java/com/busapp/service/WorkflowService.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/service/WorkflowService.java:45), [repo/frontend/src/app/features/admin/console/console.component.ts](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/src/app/features/admin/console/console.component.ts:39), [repo/backend/pom.xml](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/pom.xml:40)

### 4. Engineering Details and Professionalism
- 4.1 Whether engineering details reflect professional software practice
  - Conclusion: Partial Pass
  - Rationale: There is centralized exception handling, validation, trace IDs, and some observability. However, `SecurityException` is still mapped to `400` instead of `401`, frontend admin API paths are wrong, and the backend security configuration depends on classes not declared in the Maven dependencies.
  - Evidence: [repo/backend/src/main/java/com/busapp/controller/ApiExceptionHandler.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/ApiExceptionHandler.java:24), [repo/frontend/src/app/features/admin/dictionary/dictionary.component.ts](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/src/app/features/admin/dictionary/dictionary.component.ts:79), [repo/backend/pom.xml](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/pom.xml:23)
- 4.2 Whether the project is organized like a real product or service
  - Conclusion: Partial Pass
  - Rationale: It is clearly beyond demo level structurally, but several production-critical defects remain in auth/build consistency, tests, and UI/API integration.
  - Evidence: [repo/backend/src/main/java/com/busapp/infra/monitoring/ApiTimingInterceptor.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/infra/monitoring/ApiTimingInterceptor.java:12), [repo/backend/src/main/java/com/busapp/security/SecurityConfig.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:31)

### 5. Prompt Understanding and Requirement Fit
- 5.1 Whether the project accurately understands and responds to the business goal and constraints
  - Conclusion: Partial Pass
  - Rationale: The repo now reflects the intended system more closely than before, including `/api/v1`, `/auth/me`, reservations, pinyin full/keywords, and session-oriented auth intent. Remaining problems are mostly implementation completeness and integration defects rather than misunderstanding of the business domain.
  - Evidence: [repo/backend/src/main/java/com/busapp/controller/AuthController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/AuthController.java:44), [repo/backend/src/main/java/com/busapp/repository/SearchRepository.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/repository/SearchRepository.java:25), [repo/backend/src/main/java/com/busapp/service/NotificationService.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/service/NotificationService.java:58)

### 6. Aesthetics
- 6.1 Whether the visual and interaction design fits the scenario
  - Conclusion: Partial Pass
  - Rationale: The frontend has distinct passenger, dispatcher, and admin views with structured panels and interaction controls. Actual rendering quality cannot be confirmed statistically. There are visible encoding artifacts in templates.
  - Evidence: [repo/frontend/src/app/features/passenger/passenger.component.html](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/src/app/features/passenger/passenger.component.html:1), [repo/frontend/src/app/features/dispatcher/dispatcher.component.html](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/src/app/features/dispatcher/dispatcher.component.html:1), [repo/frontend/src/app/features/admin/admin.component.html](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/src/app/features/admin/admin.component.html:1), [repo/frontend/src/app/features/passenger/passenger.component.html](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/src/app/features/passenger/passenger.component.html:75)
  - Manual verification note: Browser verification is required for final visual acceptance.

## 5. Issues / Suggestions (Severity-Rated)
- Severity: Blocker
  - Title: Backend security layer is statically inconsistent with Maven dependencies
  - Conclusion: Fail
  - Evidence: [repo/backend/pom.xml](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/pom.xml:40), [repo/backend/src/main/java/com/busapp/security/SecurityConfig.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:5), [repo/backend/src/main/java/com/busapp/security/CustomUserDetailsService.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/security/CustomUserDetailsService.java:5)
  - Impact: The code depends on `AuthenticationManager`, `HttpSecurity`, `SecurityFilterChain`, `UserDetailsService`, and related Spring Security web/config classes, but `pom.xml` declares only `spring-security-crypto`, not `spring-boot-starter-security`. The current backend is not statically reliable as a buildable deliverable.
  - Minimum actionable fix: Add `spring-boot-starter-security` and align the security configuration with declared dependencies.
- Severity: High
  - Title: Admin frontend is wired to obsolete `/api/admin/...` endpoints instead of the implemented `/api/v1/admin/...` endpoints
  - Conclusion: Fail
  - Evidence: [repo/frontend/src/app/features/admin/console/console.component.ts](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/src/app/features/admin/console/console.component.ts:39), [repo/frontend/src/app/features/admin/dictionary/dictionary.component.ts](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/src/app/features/admin/dictionary/dictionary.component.ts:79), [repo/backend/src/main/java/com/busapp/controller/AdminMaintenanceController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/AdminMaintenanceController.java:18), [repo/backend/src/main/java/com/busapp/controller/DataIntegrationController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/DataIntegrationController.java:27)
  - Impact: The admin console and dictionary/import screens are statically unable to call the current backend API surface.
  - Minimum actionable fix: Update all admin frontend HTTP calls to `/api/v1/admin/...`.
- Severity: High
  - Title: Tests are stale against the current API surface and do not protect core auth/authorization behavior
  - Conclusion: Fail
  - Evidence: [repo/backend/src/test/java/com/busapp/SecurityModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:53), [repo/backend/src/main/java/com/busapp/controller/AuthController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/AuthController.java:22), [repo/backend/src/main/java/com/busapp/security/SecurityConfig.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:38)
  - Impact: The only endpoint-style security test still targets `/api/auth/login` instead of `/api/v1/auth/login`, and there is no meaningful coverage for `401`, `403`, session-authenticated access, object-level isolation, or admin/dispatcher protections.
  - Minimum actionable fix: Rewrite API tests around the current `/api/v1` surface and add coverage for unauthenticated, unauthorized, and role-specific flows.
- Severity: High
  - Title: Workflow engine only partially implements required joint/parallel approvals
  - Conclusion: Partial Fail
  - Evidence: [docs/design.md](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/docs/design.md:33), [repo/backend/src/main/java/com/busapp/service/WorkflowService.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/service/WorkflowService.java:47), [repo/backend/src/main/java/com/busapp/model/WorkflowTask.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/model/WorkflowTask.java:42)
  - Impact: “Parallel approval” is reduced to two booleans on a single row with no participant model, no task ownership, no explicit joint approval sub-steps, and no visual distinction in the API model beyond comment/progress updates.
  - Minimum actionable fix: Introduce explicit approval participants/subtasks or a richer workflow state model with auditable actor transitions.
- Severity: Medium
  - Title: Authentication/session persistence after controller-driven login cannot be confirmed statically
  - Conclusion: Cannot Confirm Statistically
  - Evidence: [repo/backend/src/main/java/com/busapp/controller/AuthController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/AuthController.java:30), [repo/backend/src/main/java/com/busapp/service/AuthService.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/service/AuthService.java:48), [repo/frontend/src/app/core/interceptors/credentials.interceptor.ts](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/src/app/core/interceptors/credentials.interceptor.ts:4)
  - Impact: The system appears intended to use session cookies, but because login is handled in a controller rather than a standard Spring Security authentication flow, successful persistence of the authenticated context across subsequent requests is not provable statically.
  - Minimum actionable fix: Use a standard Spring Security login mechanism or explicitly persist the security context via a `SecurityContextRepository`, then add integration tests.
- Severity: Medium
  - Title: Health endpoint contract is inconsistent and may not match the secured API surface
  - Conclusion: Partial Fail
  - Evidence: [docs/design.md](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/docs/design.md:19), [repo/backend/src/main/java/com/busapp/controller/HealthController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/HealthController.java:10), [repo/backend/src/main/java/com/busapp/security/SecurityConfig.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:38)
  - Impact: The backend exposes `/api/health`, while the versioned API uses `/api/v1/...` and the security config only explicitly permits `/actuator/**`. This makes the intended health-check contract unclear.
  - Minimum actionable fix: Standardize on actuator health or a versioned public health endpoint and document it.
- Severity: Medium
  - Title: `SecurityException` is still translated to HTTP 400 instead of an auth-specific status
  - Conclusion: Fail
  - Evidence: [repo/backend/src/main/java/com/busapp/controller/ApiExceptionHandler.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/ApiExceptionHandler.java:24)
  - Impact: Authentication-related failures can be reported with the wrong status, which weakens API correctness and testability.
  - Minimum actionable fix: Return `401 Unauthorized` for authentication failures and reserve `403` for access denial.
- Severity: Low
  - Title: Visible encoding artifacts remain in docs and frontend templates
  - Conclusion: Partial Fail
  - Evidence: [docs/design.md](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/docs/design.md:49), [docs/api-spec.md](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/docs/api-spec.md:51), [repo/frontend/src/app/features/passenger/passenger.component.html](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/src/app/features/passenger/passenger.component.html:75)
  - Impact: Reduces documentation and UI polish.
  - Minimum actionable fix: Normalize source files to UTF-8 and correct malformed characters.

## 6. Security Review Summary
- authentication entry points
  - Conclusion: Partial Pass
  - Evidence: [repo/backend/src/main/java/com/busapp/controller/AuthController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/AuthController.java:30), [repo/backend/src/main/java/com/busapp/controller/AuthController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/AuthController.java:44)
  - Reasoning: `/login`, `/register`, and `/me` now exist and the backend derives protected passenger identity from the security context. However, actual session persistence after login cannot be confirmed statically, and the security dependency set is incomplete.
- route-level authorization
  - Conclusion: Partial Pass
  - Evidence: [repo/backend/src/main/java/com/busapp/security/SecurityConfig.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:37)
  - Reasoning: Role-based route restrictions are defined for passenger, dispatch, and admin endpoints. Static confidence is reduced because the required security starter is missing from Maven dependencies.
- object-level authorization
  - Conclusion: Pass
  - Evidence: [repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java:27)
  - Reasoning: Passenger notification endpoints no longer trust a caller-supplied `username`; they derive the subject from the authenticated principal.
- function-level authorization
  - Conclusion: Partial Pass
  - Evidence: [repo/backend/src/main/java/com/busapp/security/SecurityConfig.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:39), [repo/backend/src/main/java/com/busapp/controller/AdminMaintenanceController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/AdminMaintenanceController.java:18)
  - Reasoning: Sensitive functions are meant to be role-gated by URL patterns, but there are no finer-grained method-level checks and the static dependency inconsistency remains.
- tenant / user isolation
  - Conclusion: Partial Pass
  - Evidence: [repo/backend/src/main/java/com/busapp/service/NotificationService.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/service/NotificationService.java:35), [repo/backend/src/main/java/com/busapp/service/NotificationService.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/service/NotificationService.java:136)
  - Reasoning: User-scoped notification data is now resolved from the authenticated username, but tests do not verify cross-user isolation.
- admin / internal / debug protection
  - Conclusion: Partial Pass
  - Evidence: [repo/backend/src/main/java/com/busapp/security/SecurityConfig.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:41), [repo/backend/src/main/java/com/busapp/controller/AdminMaintenanceController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/AdminMaintenanceController.java:68)
  - Reasoning: Admin and debug endpoints are intended to be admin-only by URL policy. Whether that protection is effective at runtime cannot be fully confirmed because the security starter is missing and tests do not cover it.

## 7. Tests and Logging Review
- Unit tests
  - Conclusion: Partial Pass
  - Evidence: [repo/backend/src/test/java/com/busapp/SearchModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/SearchModuleTest.java:24), [repo/backend/src/test/java/com/busapp/DataIntegrationModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/DataIntegrationModuleTest.java:35), [repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java:28)
  - Reasoning: Core utility logic has some unit coverage, but the tests are thin and do not cover many failure paths.
- API / integration tests
  - Conclusion: Fail
  - Evidence: [repo/backend/src/test/java/com/busapp/SecurityModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:50), [repo/backend/src/main/java/com/busapp/controller/AuthController.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/controller/AuthController.java:22)
  - Reasoning: Endpoint-style testing is stale and not aligned with the current `/api/v1` routes; there is no meaningful integration coverage for auth/session, admin, dispatcher, or object isolation.
- Logging categories / observability
  - Conclusion: Partial Pass
  - Evidence: [repo/backend/src/main/resources/application.yml](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/resources/application.yml:34), [repo/backend/src/main/java/com/busapp/infra/TraceIdFilter.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/infra/TraceIdFilter.java:22), [repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java:35)
  - Reasoning: Trace IDs, console pattern logging, queue backlog warning, and latency monitoring hooks exist. The implementation is still lightweight and not fully validated by tests.
- Sensitive-data leakage risk in logs / responses
  - Conclusion: Partial Pass
  - Evidence: [repo/backend/src/main/java/com/busapp/infra/config/DataInitializer.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/infra/config/DataInitializer.java:65), [repo/backend/src/main/java/com/busapp/service/NotificationService.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/main/java/com/busapp/service/NotificationService.java:140)
  - Reasoning: The previous plain-text admin password log has been removed. Remaining leakage risk is lower, though user-specific validation messages still expose looked-up usernames and frontend trace logging uses `console.log`.

## 8. Test Coverage Assessment (Static Audit)

### 8.1 Test Overview
- Unit tests exist:
  - JUnit 5 + Mockito tests are present under `repo/backend/src/test/java/com/busapp`
  - Evidence: [repo/backend/src/test/java/com/busapp/SearchModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/SearchModuleTest.java:17), [repo/backend/src/test/java/com/busapp/NotificationModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/NotificationModuleTest.java:19)
- API / integration tests exist:
  - One `@SpringBootTest`/`MockMvc` class exists, but it is stale against current routes
  - Evidence: [repo/backend/src/test/java/com/busapp/SecurityModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:30)
- Test frameworks:
  - JUnit 5, Mockito, Spring Boot Test, MockMvc
  - Evidence: [repo/backend/src/test/java/com/busapp/SecurityModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:10), [repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java:10)
- Test entry points:
  - Maven test lifecycle is implied by Spring Boot Maven setup; frontend `ng test` script exists
  - Evidence: [repo/backend/pom.xml](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/pom.xml:61), [repo/frontend/package.json](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/package.json:5)
- Documentation provides test commands:
  - Conclusion: Partial
  - Evidence: [repo/frontend/package.json](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/frontend/package.json:9)
  - Reasoning: Scripts exist, but there is no review-friendly documentation describing test scope or how to verify the system.

### 8.2 Coverage Mapping Table
| Requirement / Risk Point | Mapped Test Case(s) | Key Assertion / Fixture / Mock | Coverage Assessment | Gap | Minimum Test Addition |
|---|---|---|---|---|---|
| Password minimum length | [SecurityModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:50) | Expects `400` for short password on `/api/auth/login` | insufficient | Targets obsolete route and does not validate current `/api/v1` auth flow | Add MockMvc tests for `/api/v1/auth/login`, `/api/v1/auth/me`, `401`, and successful session reuse |
| Password hashing on registration | [SecurityModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:69) | Captures saved `passwordHash` | basically covered | No duplicate registration endpoint coverage | Add conflict test and response contract test |
| Search short-query suppression | [SearchModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/SearchModuleTest.java:31) | `getAutocomplete("a")` returns empty | basically covered | No endpoint test, no pinyin full, keyword, metadata, or dedupe assertions | Add controller/service tests for `/search/autocomplete`, `/search/results`, `/stops/{id}/metadata` |
| Search weighting | [SearchModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/SearchModuleTest.java:24) | Arithmetic score `22.0` | insufficient | Does not verify repository ordering, dedupe, or configurable weights | Add repository-backed sorting/deduplication tests |
| DND logic | [NotificationModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/NotificationModuleTest.java:28) | `isInsideDND(23:00)` is true | basically covered | No queue processing, suppression status, or final content assertions | Add scheduler tests for delivered vs suppressed queue items |
| Missed check-in threshold | [NotificationModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/NotificationModuleTest.java:39) | `minusMinutes(6)` triggers true | basically covered | No exact boundary or endpoint coverage | Add `5-minute` boundary and endpoint tests |
| Cleaning, NULL handling, versioning | [DataIntegrationModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/DataIntegrationModuleTest.java:35) | sqft conversion, NULL fallback, version increment | basically covered | No HTML template parsing test and no audit-log assertions | Add `TemplateProcessorService` tests and import audit persistence tests |
| Workflow branching | [WorkflowModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java:28) | High-impact route sets `RISKY` | insufficient | No tests for joint approval, batch rejection of risky tasks, or escalation | Add dispatcher/admin approval sequence tests and escalation tests |
| Observability / P95 | [ObservabilityModuleTest.java](/C:/Users/BEKI/Documents/Flutter/Work/We%20venture/TASK-20260318-393C91/repo/backend/src/test/java/com/busapp/ObservabilityModuleTest.java:11) | Standalone P95 calculation | insufficient | No interceptor, trace header, backlog alert, or diagnostics coverage | Add interceptor and scheduler alert tests |
| Route authorization and isolation | None meaningfully aligned | N/A | missing | Severe auth defects could remain undetected while current tests pass | Add role-based `401/403` and per-user isolation integration tests |

### 8.3 Security Coverage Audit
- authentication
  - Conclusion: insufficient
  - Reasoning: Validation and hashing are partially covered, but current login/session behavior on `/api/v1` is not.
- route authorization
  - Conclusion: missing
  - Reasoning: No meaningful tests verify passenger/dispatcher/admin authorization outcomes.
- object-level authorization
  - Conclusion: missing
  - Reasoning: No tests verify that one authenticated user cannot access another user’s notification data.
- tenant / data isolation
  - Conclusion: missing
  - Reasoning: No tests verify per-user message/preference isolation.
- admin / internal protection
  - Conclusion: missing
  - Reasoning: No tests cover rejection of unauthorized access to admin maintenance or dispatcher workflow endpoints.

### 8.4 Final Coverage Judgment
- Fail
- Major low-level logic is tested, but the highest-risk paths are either untested or stale against the current implementation: `/api/v1` auth/session behavior, route authorization, object-level isolation, admin protection, workflow approval sequencing, and observability hooks. The current tests could pass while severe defects remain.

## 9. Final Notes
- This audit is static-only and evidence-based.
- The most material current blockers are:
  - the backend security implementation is not statically consistent with the declared Maven dependencies
  - the admin frontend is still pointed at obsolete endpoints
  - the test suite does not validate the current secured API surface
