# City Bus Operation and Service Coordination Platform Static Audit

## 1. Verdict
- Overall conclusion: **Fail**

## 2. Scope and Static Verification Boundary
- Reviewed: repository docs, backend/frontend source, security config, controllers, services, models, repositories, tests, manifests, and the supplied verification scripts under `repo/`, `docs/`, and root.
- Not reviewed: runtime behavior, browser behavior, container orchestration, DB state at execution time, scheduled task execution timing, actual build/test execution results.
- Intentionally not executed: project startup, Docker, tests, external services, browser interaction.
- Manual verification required for: actual runtime startup, real queue scheduling behavior over time, health/metrics endpoint behavior, DB migrations/data persistence, Angular rendering, and any claim dependent on HTTP execution.

## 3. Repository / Requirement Mapping Summary
- Prompt core goal: Angular passenger/dispatcher/admin platform on Spring Boot + PostgreSQL for route search, reminders/message center, dispatcher workflows, admin maintenance, offline/local observability, local queue scheduling, and HTML/JSON import/cleaning/versioning.
- Main implementation areas reviewed: auth/security (`repo/backend/src/main/java/com/busapp/security`, `controller/AuthController.java`), passenger search/notifications (`PassengerSearchController.java`, `PassengerNotificationController.java`, `SearchService.java`, `NotificationService.java`), workflow (`DispatcherWorkflowController.java`, `WorkflowService.java`, `EscalationService.java`), admin/data integration (`AdminMaintenanceController.java`, `DataIntegrationController.java`, `DataCleaningService.java`, `TemplateProcessorService.java`), frontend role UIs (`repo/frontend/src/app/features/**`), observability (`TraceIdFilter.java`, `PerformanceMonitor.java`), and tests (`repo/backend/src/test/java/com/busapp/*.java`).

## 4. Section-by-section Review

### 1. Hard Gates
#### 1.1 Documentation and static verifiability
- Conclusion: **Fail**
- Rationale: Available docs describe architecture and API shape, but they do not provide a coherent human verification path for startup/run/test/configuration. The supplied verification script is statically inconsistent with the implemented routes, so even the included validation path cannot be trusted as written.
- Evidence: `docs/design.md:1-55`; `docs/api-spec.md:1-52`; `run_tests.ps1:14-15`, `run_tests.ps1:74-79`, `run_tests.ps1:91-95`, `run_tests.ps1:122-123`, `run_tests.ps1:145-146`, `run_tests.ps1:170-172`; implemented routes in `repo/backend/src/main/java/com/busapp/controller/HealthController.java:10-12`, `AuthController.java:22-56`, `DataIntegrationController.java:27-97`, `PassengerSearchController.java:22-35`, `PassengerNotificationController.java:19-67`
- Manual verification note: A human can inspect the code, but not follow the provided script/docs without first rewriting commands and endpoints.

#### 1.2 Material deviation from the Prompt
- Conclusion: **Fail**
- Rationale: The repository targets the stated domain, but several core prompt requirements are weakened or replaced: public callers can self-assign privileged roles, reminders are queued immediately instead of being scheduled around trip time, dispatcher UI does not expose the approval action needed for risky/joint approvals, and notification APIs return raw queue entities instead of only desensitized message-center content.
- Evidence: `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:37-42`; `repo/backend/src/main/java/com/busapp/service/AuthService.java:34-40`; `repo/backend/src/main/java/com/busapp/service/NotificationService.java:56-75`; `repo/frontend/src/app/features/dispatcher/dispatcher.component.html:7-29`; `repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java:43-67`; `repo/backend/src/main/java/com/busapp/model/MessageTask.java:20-45`

### 2. Delivery Completeness
#### 2.1 Coverage of explicit core requirements
- Conclusion: **Fail**
- Rationale: Search, message center, workflow, admin maintenance, import/cleaning, backup, and observability are present in some form, but important explicit requirements are not fully implemented: future lead-time reminder scheduling, secure role handling, dispatcher approval interaction, and strict desensitized message delivery.
- Evidence: implemented pieces in `repo/frontend/src/app/features/passenger/passenger.component.ts:63-175`, `repo/backend/src/main/java/com/busapp/service/SearchService.java:21-67`, `repo/backend/src/main/java/com/busapp/service/EscalationService.java:22-33`, `repo/backend/src/main/java/com/busapp/service/DataCleaningService.java:37-191`, `repo/docker-compose.yml:39-56`; missing/weak areas in `repo/backend/src/main/java/com/busapp/service/NotificationService.java:56-75`, `repo/frontend/src/app/features/dispatcher/dispatcher.component.html:7-29`, `repo/backend/src/main/java/com/busapp/service/AuthService.java:34-40`

#### 2.2 Basic end-to-end deliverable vs partial/demo
- Conclusion: **Partial Pass**
- Rationale: This is a multi-module full-stack repository with frontend, backend, database config, docs, and backend tests. However, demo-style shortcuts remain material: seeded sample data, immediate reminder scheduling, canned template payloads, and broken supplied verification commands reduce confidence in it as a production-shaped 0-to-1 delivery.
- Evidence: `repo/frontend/src/app/features/admin/dictionary/dictionary.component.ts:39-58`; `repo/backend/src/main/java/com/busapp/infra/config/DataInitializer.java:76-160`; `repo/backend/src/main/java/com/busapp/service/NotificationService.java:56-75`; `run_tests.ps1:1-200`

### 3. Engineering and Architecture Quality
#### 3.1 Structure and module decomposition
- Conclusion: **Partial Pass**
- Rationale: Backend concerns are separated into controllers/services/repositories/models and the frontend is role-segmented. The structure is reasonable, but there is avoidable repository noise (`node_modules`, built `dist`, backup SQL dump) and some APIs return persistence entities directly instead of boundary DTOs.
- Evidence: module layout under `repo/backend/src/main/java/com/busapp/**`; role layout under `repo/frontend/src/app/features/**`; direct entity responses in `repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java:31-67`, `PassengerSearchController.java:32-35`; repository noise from `repo/frontend/node_modules/**`, `repo/frontend/dist/**`, `repo/backups/backup_20260421.sql`

#### 3.2 Maintainability and extensibility
- Conclusion: **Fail**
- Rationale: Several core behaviors are hard-coded or coupled to demo assumptions: reservations do not carry arrival time, missed check-in content hard-codes a stop name, search seed data is baked in, and frontend labels do not match backend semantics. These choices make extension risky and encourage drift between prompt, docs, and implementation.
- Evidence: `repo/backend/src/main/java/com/busapp/service/NotificationService.java:57-75`, `NotificationService.java:87-95`; `repo/backend/src/main/java/com/busapp/infra/config/DataInitializer.java:81-116`; `repo/frontend/src/app/features/passenger/passenger.component.html:50-66`; `repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java:53-67`

### 4. Engineering Details and Professionalism
#### 4.1 Error handling, logging, validation, API design
- Conclusion: **Fail**
- Rationale: There is some exception mapping and structured trace logging, but critical API/security details are weak: session auth has CSRF disabled, registration trusts caller-supplied roles, controllers accept loose `Map<String,String>` bodies, message APIs serialize full entities, and frontend tracing uses `console.log` instead of structured telemetry.
- Evidence: `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:33-51`; `repo/backend/src/main/java/com/busapp/service/AuthService.java:27-40`; `repo/backend/src/main/java/com/busapp/controller/AuthController.java:30-45`; `repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java:53-67`; `repo/backend/src/main/java/com/busapp/controller/ApiExceptionHandler.java:12-27`; `repo/frontend/src/app/core/interceptors/trace.interceptor.ts:5-10`

#### 4.2 Real product/service shape vs demo/example
- Conclusion: **Partial Pass**
- Rationale: The project is shaped like a real service, not a single-file sample. Still, several user-critical behaviors remain simplistic enough to look demo-grade rather than delivery-grade, especially reminder scheduling, dispatcher task handling, and verification automation drift.
- Evidence: `repo/backend/pom.xml:23-68`; `repo/frontend/package.json:5-29`; `repo/frontend/src/app/features/dispatcher/dispatcher.component.ts:32-73`; `repo/backend/src/main/java/com/busapp/service/NotificationService.java:56-75`; `run_tests.ps1:1-200`

### 5. Prompt Understanding and Requirement Fit
#### 5.1 Business goal, usage scenario, and implicit constraints
- Conclusion: **Fail**
- Rationale: The repository clearly aims at the requested platform, but important semantics are misunderstood or ignored: privileged roles are self-selectable at registration, reminder timing is not tied to trip time, quiet-hours UI controls the wrong backend flag, and message desensitization is undermined by returning raw queue fields.
- Evidence: `repo/backend/src/main/java/com/busapp/model/UserRole.java:3-6`; `repo/backend/src/main/java/com/busapp/service/AuthService.java:34-40`; `repo/frontend/src/app/features/auth/register.component.ts:20-25`; `repo/backend/src/main/java/com/busapp/service/NotificationService.java:56-75`; `repo/frontend/src/app/features/passenger/passenger.component.html:50-66`; `repo/backend/src/main/java/com/busapp/model/MessageTask.java:20-45`

### 6. Aesthetics
#### 6.1 Visual and interaction design fit
- Conclusion: **Partial Pass**
- Rationale: Static markup/CSS show separated functional areas, responsive layouts, and consistent card-based styling. However, there are visible encoding defects (`Â·`, `ãŽ¡`), hover/interaction feedback is minimal, and actual rendering quality cannot be confirmed without execution.
- Evidence: `repo/frontend/src/app/features/passenger/passenger.component.html:1-81`, `repo/frontend/src/app/features/passenger/passenger.component.css:1-146`; `repo/frontend/src/app/features/dispatcher/dispatcher.component.html:24-28`; `repo/backend/src/main/java/com/busapp/service/AdminConfigService.java:25-32`
- Manual verification note: Actual rendered spacing, fonts, and runtime interaction states require browser verification.

## 5. Issues / Suggestions (Severity-Rated)

### Blocker
#### 1. Public registration allows self-assigned privileged roles
- Severity: **Blocker**
- Conclusion: Unauthenticated callers can register as `DISPATCHER` or `ADMIN`.
- Evidence: `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:37-39`; `repo/backend/src/main/java/com/busapp/controller/AuthController.java:43-45`; `repo/backend/src/main/java/com/busapp/service/AuthService.java:34-40`, `AuthService.java:72-74`; `repo/backend/src/main/java/com/busapp/model/UserRole.java:3-6`; frontend also exposes dispatcher signup in `repo/frontend/src/app/features/auth/register.component.ts:20-25`
- Impact: Immediate privilege escalation across dispatcher/admin-only APIs and workflows.
- Minimum actionable fix: Restrict public registration to passengers only; move dispatcher/admin creation to an authenticated admin-only flow; enforce the role restriction server-side regardless of frontend behavior.

### High
#### 2. Session-based authentication is deployed with CSRF disabled
- Severity: **High**
- Conclusion: The app uses cookie-backed sessions for authenticated requests but disables CSRF protection.
- Evidence: `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:33-36`; `repo/backend/src/main/java/com/busapp/controller/AuthController.java:36-39`; `repo/frontend/src/app/core/interceptors/credentials.interceptor.ts:4-8`
- Impact: Cross-site requests can potentially perform authenticated state-changing actions in the user’s session.
- Minimum actionable fix: Enable CSRF protection for session auth, or switch to a non-cookie auth model with explicit bearer tokens and proper rotation.

#### 3. Notification APIs expose raw queue entities, undermining desensitization
- Severity: **High**
- Conclusion: Message-center endpoints return `MessageTask` entities containing `rawContent`, `userId`, `sensitivity`, and `traceId`.
- Evidence: `repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java:43-50`; `repo/backend/src/main/java/com/busapp/model/MessageTask.java:20-45`, `MessageTask.java:46-63`
- Impact: Sensitive unmasked content and internal metadata are exposed to clients even though the prompt requires desensitized message content.
- Minimum actionable fix: Introduce response DTOs that expose only approved fields such as masked/final content, type, status, and display time.

#### 4. Arrival reminders are queued immediately instead of being scheduled for the configured lead time
- Severity: **High**
- Conclusion: Reservation success and reminder tasks are both created with `scheduledAt = now - 1 second`; the reservation API does not accept trip/arrival time.
- Evidence: `repo/backend/src/main/java/com/busapp/service/NotificationService.java:56-75`; `repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java:53-59`
- Impact: The prompt’s “upcoming reminders (default 10 minutes in advance)” is not implemented; notifications will be processed immediately by the local scheduler.
- Minimum actionable fix: Capture reservation trip time or arrival ETA, compute `scheduledAt = eventTime - leadTimeMinutes`, and persist only the reservation-success message for immediate delivery.

#### 5. Dispatcher UI lacks the individual approval action required for risky/joint workflows
- Severity: **High**
- Conclusion: The backend exposes `/tasks/{taskId}/approve`, but the dispatcher page only supports return and batch approve.
- Evidence: `repo/backend/src/main/java/com/busapp/controller/DispatcherWorkflowController.java:30-42`; `repo/frontend/src/app/features/dispatcher/dispatcher.component.html:7-29`; `repo/frontend/src/app/features/dispatcher/dispatcher.component.ts:55-73`; risky tasks are blocked from batch approval in `repo/backend/src/main/java/com/busapp/service/WorkflowService.java:80-85`
- Impact: Dispatchers cannot complete the risky parallel-approval path from the delivered UI, so a core workflow is incomplete.
- Minimum actionable fix: Add an explicit per-task approve action in the UI and test risky/admin-dispatcher joint approval behavior end to end.

#### 6. Supplied verification script does not match the implemented API surface
- Severity: **High**
- Conclusion: The project’s own verification path targets routes that do not exist in the codebase.
- Evidence: `run_tests.ps1:14-15`, `run_tests.ps1:74-79`, `run_tests.ps1:91-95`, `run_tests.ps1:122-123`, `run_tests.ps1:145-146`, `run_tests.ps1:170-172`; implemented routes in `repo/backend/src/main/java/com/busapp/controller/HealthController.java:10-12`, `AuthController.java:22-56`, `DataIntegrationController.java:27-97`, `PassengerSearchController.java:22-35`, `PassengerNotificationController.java:19-67`
- Impact: Delivery acceptance cannot rely on the included script; a reviewer must rewrite core verification steps before checking the project.
- Minimum actionable fix: Align verification scripts and docs with actual route prefixes and endpoint names, then keep them versioned with the code.

### Medium
#### 7. Internal metrics and actuator endpoints are publicly exposed
- Severity: **Medium**
- Conclusion: `/actuator/**` is unauthenticated while metrics/prometheus are exposed.
- Evidence: `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:38-42`; `repo/backend/src/main/resources/application.yml:24-32`
- Impact: Operational metadata can be accessed without authentication.
- Minimum actionable fix: Limit public exposure to a minimal health endpoint or secure management endpoints separately.

#### 8. Logout is frontend-only and does not invalidate the backend session
- Severity: **Medium**
- Conclusion: Client logout clears local storage only; no backend logout/session invalidation path exists.
- Evidence: `repo/frontend/src/app/core/services/auth.service.ts:33-36`; `repo/backend/src/main/java/com/busapp/controller/AuthController.java:22-56`
- Impact: A browser session may remain authenticated server-side after “logout.”
- Minimum actionable fix: Add a backend logout endpoint that invalidates the HTTP session and clear the cookie on logout.

#### 9. Passenger notification preference UI is semantically incorrect
- Severity: **Medium**
- Conclusion: The checkbox labeled “Enable quiet hours” is bound to `arrivalRemindersEnabled`, which the backend uses to decide whether to create reminder tasks at all.
- Evidence: `repo/frontend/src/app/features/passenger/passenger.component.html:50-66`; `repo/backend/src/main/java/com/busapp/service/NotificationService.java:66-76`
- Impact: Users can accidentally disable reminders instead of only configuring do-not-disturb behavior, deviating from the prompt.
- Minimum actionable fix: Separate “arrival reminders enabled” from “quiet hours enabled” in both UI and backend DTO semantics.

#### 10. Encoding defects leak into business data and UI text
- Severity: **Medium**
- Conclusion: The seeded area unit is corrupted (`ãŽ¡`) and dispatcher UI text contains `Â·`.
- Evidence: `repo/backend/src/main/java/com/busapp/service/AdminConfigService.java:25-32`; `repo/frontend/src/app/features/dispatcher/dispatcher.component.html:27`
- Impact: Standard dictionary output and rendered UI text do not match the prompt’s required unit standard and polish.
- Minimum actionable fix: Normalize source file encoding to UTF-8 and replace corrupted literals with correct text.

## 6. Security Review Summary
- Authentication entry points: **Fail**
  - Evidence: `repo/backend/src/main/java/com/busapp/controller/AuthController.java:30-56`; `repo/backend/src/main/java/com/busapp/service/AuthService.java:27-57`
  - Reasoning: Local username/password with BCrypt is present, but public registration trusts caller-selected roles, which is a critical auth flaw.
- Route-level authorization: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:37-42`
  - Reasoning: Passenger/dispatcher/admin path prefixes are protected, but `/actuator/**` is public and route tests are very thin.
- Object-level authorization: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java:27-45`; `repo/backend/src/main/java/com/busapp/service/NotificationService.java:105-113`
  - Reasoning: Passenger messages/preferences are scoped by the authenticated username to a user id. No stronger object-level policy exists for other domains, and coverage is not proven by tests.
- Function-level authorization: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/service/WorkflowService.java:47-71`
  - Reasoning: Risky workflow approval checks current authorities inside the service, but most authorization relies only on URL-prefix security and lacks defense-in-depth.
- Tenant / user isolation: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/service/NotificationService.java:35-48`, `NotificationService.java:105-113`; `repo/backend/src/main/java/com/busapp/repository/MessageQueueRepository.java:9-12`
  - Reasoning: Notification reads are filtered by `userId`; no multi-tenant model is present. Static review did not find an explicit cross-user access path, but there are no tests proving isolation.
- Admin / internal / debug protection: **Fail**
  - Evidence: `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:38-42`; `repo/backend/src/main/resources/application.yml:24-32`; protected admin endpoints in `repo/backend/src/main/java/com/busapp/controller/AdminMaintenanceController.java:17-73`, `DataIntegrationController.java:26-97`
  - Reasoning: Admin APIs are role-guarded, but internal actuator/metrics endpoints are still unauthenticated.

## 7. Tests and Logging Review
- Unit tests: **Partial Pass**
  - Evidence: `repo/backend/src/test/java/com/busapp/SearchModuleTest.java:17-36`, `NotificationModuleTest.java:19-45`, `WorkflowModuleTest.java:20-41`, `DataIntegrationModuleTest.java:22-76`, `ObservabilityModuleTest.java:10-16`
  - Reasoning: Backend unit/service tests exist, but they cover only narrow happy-path fragments.
- API / integration tests: **Fail**
  - Evidence: only limited MockMvc coverage in `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:30-56`; no frontend tests found; no meaningful authz/object-isolation integration tests
  - Reasoning: Core HTTP flows, 401/403 behavior, role escalation, object isolation, admin protection, and UI behavior are not meaningfully covered.
- Logging categories / observability: **Partial Pass**
  - Evidence: `repo/backend/src/main/resources/application.yml:34-36`; `repo/backend/src/main/java/com/busapp/infra/TraceIdFilter.java:16-32`; `repo/backend/src/main/java/com/busapp/infra/monitoring/PerformanceMonitor.java:14-35`; `repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java:30-58`
  - Reasoning: Trace IDs, health, metrics, and latency monitoring exist statically. Diagnostic behavior is log-based only, and frontend tracing is just `console.log`.
- Sensitive-data leakage risk in logs / responses: **Fail**
  - Evidence: response leakage in `repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java:43-50` and `repo/backend/src/main/java/com/busapp/model/MessageTask.java:20-45`; raw input values logged in `repo/backend/src/main/java/com/busapp/service/DataCleaningService.java:137-139`
  - Reasoning: The most serious leak is in API responses, where raw queue content is exposed directly.

## 8. Test Coverage Assessment (Static Audit)

### 8.1 Test Overview
- Unit tests and service-level tests exist in `repo/backend/src/test/java/com/busapp/*.java`.
- Test frameworks: JUnit 5, Mockito, Spring Boot Test.
- Test entry points: Maven/Surefire backend tests implied by `repo/backend/pom.xml:54-66`; frontend `npm test` script exists in `repo/frontend/package.json:5-10` but no frontend spec files were found.
- Documentation provides no reliable human test instructions; the only supplied script is `run_tests.ps1`, which requires Docker and contains route mismatches.
- Evidence: `repo/backend/pom.xml:54-66`; `repo/frontend/package.json:5-10`; `run_tests.ps1:1-200`; frontend test absence check result: `NO_FRONTEND_TESTS_FOUND`

### 8.2 Coverage Mapping Table
| Requirement / Risk Point | Mapped Test Case(s) | Key Assertion / Fixture / Mock | Coverage Assessment | Gap | Minimum Test Addition |
|---|---|---|---|---|---|
| Password minimum length | `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:50-67` | Short password causes bad request / `ValidationException` | basically covered | No coverage for missing username, invalid role, duplicate user HTTP path | Add controller tests for invalid/missing auth fields and duplicate registration |
| Password hashing on registration | `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:70-85` | Captures saved `UserEntity` and checks hashed password | basically covered | No test for role restriction or public registration abuse | Add tests proving public registration cannot create dispatcher/admin accounts |
| Search min-length and weighted score | `repo/backend/src/test/java/com/busapp/SearchModuleTest.java:24-35` | Score calculation and query length guard | insufficient | No test for deduplication, route-number match, pinyin/full keyword match, result ordering | Add repository/service tests for deduped ordered search result sets |
| DND suppression and missed check-in threshold | `repo/backend/src/test/java/com/busapp/NotificationModuleTest.java:28-45` | `isInsideDND()` and `checkMissedCheckIn()` | insufficient | No test for real queue processing, masking, message DTO exposure, or lead-time scheduling | Add scheduler/service tests for message creation time, masked output, and response DTOs |
| Workflow risky branch selection | `repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java:28-41` | High-impact route change sets `RISKY` on return path | insufficient | No test for individual approve path, joint admin+dispatcher approvals, batch rejection HTTP behavior, or 403s | Add service and controller tests for risky parallel approvals and access control |
| Data cleaning unit normalization and version increment | `repo/backend/src/test/java/com/busapp/DataIntegrationModuleTest.java:35-76` | sqft conversion, NULL fallback, version bump | basically covered | No controller tests for template import, HTML parsing, dictionaries/rules auth, or audit history endpoints | Add controller tests for admin-only import/config endpoints and HTML mapping |
| P95 calculation | `repo/backend/src/test/java/com/busapp/ObservabilityModuleTest.java:10-16` | Checks `calculateP95()` result | insufficient | No tests for `X-Trace-ID` header, queue backlog alerting, or actuator exposure | Add MockMvc tests for trace header and protected/unprotected ops endpoints |
| Authentication / authorization / isolation | none meaningful beyond one login boundary test | N/A | missing | No tests for 401/403, public actuator exposure, role escalation, object-level message isolation, admin/internal protection | Add route-security integration suite covering authn/authz/isolation failure paths |

### 8.3 Security Coverage Audit
- Authentication: **Fail**
  - Existing tests check password length and hashing, but not privileged-role registration abuse, failed login handling, or session invalidation. Severe auth defects could pass the current suite.
- Route authorization: **Fail**
  - There are no meaningful tests asserting 401/403 behavior on passenger/dispatcher/admin routes.
- Object-level authorization: **Fail**
  - No tests verify that one user cannot read another user’s notifications/preferences.
- Tenant / data isolation: **Fail**
  - No multi-user isolation tests exist; current tests are single-object service checks only.
- Admin / internal protection: **Fail**
  - No tests cover actuator exposure or admin-only endpoint protection.

### 8.4 Final Coverage Judgment
- **Fail**
- Major risks covered: password length/hash basics, small parts of search scoring, DND window calculation, one workflow branch rule, area normalization/version increment, and P95 math.
- Major uncovered risks: privileged-role escalation, 401/403 enforcement, object-level isolation, raw message leakage, real reminder scheduling, risky joint approvals, admin/internal exposure, and frontend behavior. The current tests could all pass while severe security and requirement-fit defects remain.

## 9. Final Notes
- This audit is static-only and does not claim runtime success or failure.
- The most material blockers are security and requirement-fit issues, not styling or minor refactors.
- Report location: `.tmp/city-bus-static-audit.md`
