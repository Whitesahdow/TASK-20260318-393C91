# City Bus Platform Static Audit

## 1. Verdict
- Overall conclusion: **Fail**

## 2. Scope and Static Verification Boundary
- Reviewed: `docs/`, root manifests/scripts, backend source under `repo/backend/src/main`, frontend source under `repo/frontend/src`, backend tests under `repo/backend/src/test`, and static repository structure.
- Not reviewed: runtime behavior, browser rendering, actual HTTP execution, DB contents at runtime, Docker/container behavior, scheduler timing, or external/network behavior.
- Intentionally not executed: project startup, Docker, tests, browsers, API requests.
- Manual verification required for: first-login/register CSRF flow, actual Angular rendering, runtime queue consumption timing, scheduled escalation timing, and real backup generation.

## 3. Repository / Requirement Mapping Summary
- Prompt core goal: Angular passenger/dispatcher/admin platform on Spring Boot + PostgreSQL for route/stop search, notification preferences and message center, dispatcher workflow approvals with escalation, admin template/weight/dictionary maintenance, HTML/JSON import and cleaning, local queue scheduling, backup, and observability.
- Main implementation areas mapped: auth/security (`repo/backend/src/main/java/com/busapp/security`, `controller/AuthController.java`), passenger search and notifications (`PassengerSearchController.java`, `PassengerNotificationController.java`, `SearchService.java`, `NotificationService.java`, `NotificationScheduler.java`), dispatcher workflow (`DispatcherWorkflowController.java`, `WorkflowService.java`, `EscalationService.java`), admin/data integration (`AdminMaintenanceController.java`, `DataIntegrationController.java`, `DataCleaningService.java`, `TemplateProcessorService.java`), frontend role views (`repo/frontend/src/app/features/**`), docs/manifests, and backend tests.

## 4. Section-by-section Review

### 1. Hard Gates
#### 1.1 Documentation and static verifiability
- Conclusion: **Fail**
- Rationale: There is still no README or coherent startup/test/configuration guide. The supplied `run_tests.ps1` is not statically consistent with the secured API it targets: it posts to CSRF-protected endpoints without token handling and calls admin-only endpoints without authenticated admin context.
- Evidence: `docs/api-spec.md:1-52`; `docs/design.md:1-55`; `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:35-42`; `run_tests.ps1:91-96`; `run_tests.ps1:122-123`; `run_tests.ps1:170-177`; `run_tests.ps1:195-196`
- Manual verification note: A human reviewer would need to rewrite parts of the verification path before attempting validation.

#### 1.2 Material deviation from the Prompt
- Conclusion: **Fail**
- Rationale: The implementation remains centered on the prompt, but material deviations remain in core flows: the quiet-hours toggle is saved but not enforced, the frontend schedules reminders with a mock ETA instead of real service timing, and message-center responses do not use the queue-processed `finalContent`/sensitivity logic.
- Evidence: `repo/backend/src/main/java/com/busapp/service/NotificationService.java:41-49`; `repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java:41-69`; `repo/frontend/src/app/features/passenger/passenger.component.ts:122-127`; `repo/backend/src/main/java/com/busapp/model/MessageResponse.java:11-17`

### 2. Delivery Completeness
#### 2.1 Coverage of explicit core requirements
- Conclusion: **Fail**
- Rationale: Search, workflow, admin maintenance, import/versioning, backup configuration, trace IDs, and queue processing are implemented. However, explicit prompt requirements are still incomplete or weakened: quiet-hours toggle behavior is not honored, reminder scheduling on the passenger side is hardcoded to a fake one-hour ETA, and message desensitization is not consistently driven by configured sensitivity levels.
- Evidence: implemented areas in `repo/backend/src/main/java/com/busapp/service/SearchService.java:21-67`, `repo/backend/src/main/java/com/busapp/service/EscalationService.java:22-33`, `repo/backend/src/main/java/com/busapp/service/DataCleaningService.java:37-191`, `repo/docker-compose.yml:39-56`; incomplete areas in `repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java:48-69`, `repo/frontend/src/app/features/passenger/passenger.component.ts:122-127`, `repo/backend/src/main/java/com/busapp/model/MessageResponse.java:11-17`

#### 2.2 Basic end-to-end deliverable vs partial/demo
- Conclusion: **Partial Pass**
- Rationale: The repository is a full-stack deliverable with backend, frontend, DB config, admin flows, and tests. It still contains demo-grade shortcuts in user-critical flows, especially the frontend’s mock arrival ETA and the lack of a statically usable verification path.
- Evidence: `repo/backend/pom.xml:23-68`; `repo/frontend/package.json:5-29`; `repo/frontend/src/app/features/passenger/passenger.component.ts:122-127`; `run_tests.ps1:1-205`

### 3. Engineering and Architecture Quality
#### 3.1 Structure and module decomposition
- Conclusion: **Partial Pass**
- Rationale: Backend modules are reasonably separated into controller/service/repository/model layers and the frontend is split by role. The repository still includes unnecessary committed artifacts such as `node_modules`, `dist`, and a backup SQL dump, which adds delivery noise.
- Evidence: `repo/backend/src/main/java/com/busapp/**`; `repo/frontend/src/app/features/**`; committed artifacts under `repo/frontend/node_modules/**`, `repo/frontend/dist/**`, `repo/backups/backup_20260421.sql`

#### 3.2 Maintainability and extensibility
- Conclusion: **Fail**
- Rationale: Core logic still contains hard-coded behavior that weakens extension: passenger reminder scheduling uses a frontend-generated mock ETA, missed check-in content hardcodes `"Central Avenue"`, and the scheduler ignores the persisted `quietHoursEnabled` switch entirely.
- Evidence: `repo/frontend/src/app/features/passenger/passenger.component.ts:122-127`; `repo/backend/src/main/java/com/busapp/service/NotificationService.java:88-96`; `repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java:41-69`

### 4. Engineering Details and Professionalism
#### 4.1 Error handling, logging, validation, API design
- Conclusion: **Fail**
- Rationale: There is structured exception handling, trace IDs, health checks, and validation for some inputs. Professionalism is undercut by a broken static verification path, CSRF-protected POST flows without corresponding documented token handling, and message DTO construction that bypasses queue-produced `finalContent` and sensitivity-based masking.
- Evidence: `repo/backend/src/main/java/com/busapp/controller/ApiExceptionHandler.java:12-27`; `repo/backend/src/main/java/com/busapp/infra/TraceIdFilter.java:16-32`; `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:35-42`; `repo/backend/src/main/java/com/busapp/model/MessageResponse.java:11-17`; `run_tests.ps1:91-96`; `run_tests.ps1:170-177`

#### 4.2 Real product/service shape vs demo/example
- Conclusion: **Partial Pass**
- Rationale: The system is shaped like a real application, but important passenger notification behavior still relies on mock/demo assumptions rather than business data, and the delivery’s own verification assets are not production-shaped.
- Evidence: `repo/frontend/src/app/features/passenger/passenger.component.ts:122-127`; `run_tests.ps1:1-205`

### 5. Prompt Understanding and Requirement Fit
#### 5.1 Business goal, usage scenario, and implicit constraints
- Conclusion: **Fail**
- Rationale: The repository reflects the requested domain and now enforces passenger-only public registration. It still misses the prompt’s semantics for quiet-hour toggles and sensitivity-driven desensitization, and it substitutes a mock passenger ETA for actual schedule-derived reminder timing.
- Evidence: corrected registration in `repo/backend/src/main/java/com/busapp/service/AuthService.java:34-40`; unmet semantics in `repo/backend/src/main/java/com/busapp/model/NotificationPreference.java:15-28`, `repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java:48-69`, `repo/backend/src/main/java/com/busapp/model/MessageResponse.java:11-17`, `repo/frontend/src/app/features/passenger/passenger.component.ts:122-127`

### 6. Aesthetics
#### 6.1 Visual and interaction design fit
- Conclusion: **Partial Pass**
- Rationale: The static markup/CSS show clear separation of functional zones and consistent card-based layouts. Actual rendering, interaction polish, and runtime feedback cannot be confirmed without execution.
- Evidence: `repo/frontend/src/app/features/passenger/passenger.component.html:1-87`; `repo/frontend/src/app/features/passenger/passenger.component.css:1-146`; `repo/frontend/src/app/features/dispatcher/dispatcher.component.html:1-35`; `repo/frontend/src/app/features/dispatcher/dispatcher.component.css:1-123`
- Manual verification note: Browser verification is required for render correctness and real interaction behavior.

## 5. Issues / Suggestions (Severity-Rated)

### High
#### 1. Quiet-hours toggle is persisted but never enforced by queue processing
- Severity: **High**
- Conclusion: `quietHoursEnabled` exists in the model/UI and is saved, but the scheduler suppresses messages solely based on the DND time window and ignores the toggle.
- Evidence: `repo/backend/src/main/java/com/busapp/model/NotificationPreference.java:18-28`; `repo/backend/src/main/java/com/busapp/service/NotificationService.java:44-49`; `repo/frontend/src/app/features/passenger/passenger.component.html:55-70`; `repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java:48-69`
- Impact: Passenger notification preferences do not behave as specified; users can disable quiet hours in the UI and still have messages suppressed.
- Minimum actionable fix: Gate DND suppression on `pref.isQuietHoursEnabled()` before evaluating time-window logic, and add tests for enabled/disabled quiet-hour behavior.

#### 2. Message-center DTO bypasses sensitivity-based masking and queue-produced final content
- Severity: **High**
- Conclusion: `MessageResponse` masks `rawContent` with a simple positional regex instead of using the scheduler’s `finalContent` and `MaskingUtils`.
- Evidence: `repo/backend/src/main/java/com/busapp/model/MessageResponse.java:11-17`; `repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java:48-57`; `repo/backend/src/main/java/com/busapp/service/MaskingUtils.java:8-21`
- Impact: Sensitive values can remain visible, and suppressed/delivered queue outcomes are not faithfully represented in the message center.
- Minimum actionable fix: Build response DTOs from `finalContent` after queue processing and apply the same sensitivity masking policy used by `NotificationScheduler`.

#### 3. Passenger reminder scheduling still depends on a frontend-generated mock ETA
- Severity: **High**
- Conclusion: The frontend invents `arrivalEta` as `now + 1 hour` with an inline `Mock 1 hour ETA` comment when a user sets a reminder.
- Evidence: `repo/frontend/src/app/features/passenger/passenger.component.ts:122-127`
- Impact: Reminder timing is not tied to route/service data, so a core passenger feature is implemented with hardcoded behavior instead of meaningful business logic.
- Minimum actionable fix: Derive ETA from actual route/stop schedule data or require the reservation flow to provide a real trip time, then remove the mock frontend timestamp generation.

#### 4. Static verification assets are not runnable against the current secured API
- Severity: **High**
- Conclusion: The shipped verification script posts to CSRF-protected endpoints without token handling and calls admin-only endpoints without authenticated admin context.
- Evidence: `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:35-42`; `run_tests.ps1:91-96`; `run_tests.ps1:122-123`; `run_tests.ps1:170-177`; `run_tests.ps1:195-196`
- Impact: A reviewer cannot rely on the delivery’s own verification path; the acceptance workflow requires rewriting core steps first.
- Minimum actionable fix: Provide a consistent documented test flow that obtains CSRF tokens, authenticates correctly, and uses the required role/session for admin-only endpoints.

#### 5. Backend security tests are statically inconsistent with declared test dependencies and routes
- Severity: **High**
- Conclusion: `SecurityModuleTest` uses `WithMockUser` without a `spring-security-test` dependency and includes a request to a non-existent `/api/v1/passenger/messages` route.
- Evidence: `repo/backend/pom.xml:54-58`; `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:117-133`
- Impact: The security test suite cannot be trusted as delivery evidence; it is likely incomplete or non-runnable as delivered.
- Minimum actionable fix: Add `spring-security-test` explicitly, align tests to real routes, and cover real 401/403/CSRF/object-isolation paths.

### Medium
#### 6. API documentation remains inconsistent with implemented methods and paths
- Severity: **Medium**
- Conclusion: The API spec still documents paths/methods such as `PATCH /notifications/preferences`, `PATCH /dispatch/tasks/{taskId}/approve`, and `/admin/import`, while the implementation uses different endpoints and verbs.
- Evidence: `docs/api-spec.md:31-52`; `repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java:31-79`; `repo/backend/src/main/java/com/busapp/controller/DispatcherWorkflowController.java:25-42`; `repo/backend/src/main/java/com/busapp/controller/DataIntegrationController.java:46-97`
- Impact: Reviewers and integrators are given a misleading contract.
- Minimum actionable fix: Update the API documentation to match implemented routes and HTTP methods exactly.

#### 7. No top-level delivery README or static reviewer guide is present
- Severity: **Medium**
- Conclusion: The repository provides design/API notes but no single startup/run/test/configuration guide.
- Evidence: `docs/design.md:1-55`; `docs/api-spec.md:1-52`; root file list shows no `README*`
- Impact: Hard-gate static verifiability is reduced because the reviewer must infer verification steps from scattered files.
- Minimum actionable fix: Add a top-level README with configuration prerequisites, entry points, authentication/test flow, and static verification instructions.

#### 8. Missed check-in content remains hardcoded to a single stop name
- Severity: **Medium**
- Conclusion: Missed check-in notifications always embed `"Central Avenue"` instead of using reservation-specific data.
- Evidence: `repo/backend/src/main/java/com/busapp/service/NotificationService.java:88-96`
- Impact: User-facing notification content is not data-driven and does not generalize to actual reservations.
- Minimum actionable fix: Carry stop/trip metadata into the missed check-in flow and render the message from real reservation data or templates.

## 6. Security Review Summary
- Authentication entry points: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/controller/AuthController.java:30-66`; `repo/backend/src/main/java/com/busapp/service/AuthService.java:27-57`
  - Reasoning: Local username/password auth, BCrypt hashing, passenger-only public registration, and logout endpoint exist. Static evidence for correct CSRF bootstrapping on first login/register is still missing.
- Route-level authorization: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:37-42`
  - Reasoning: Passenger/dispatcher/admin and actuator paths are now role-protected appropriately, but test coverage is still thin and verification assets do not exercise the secured contract correctly.
- Object-level authorization: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java:27-79`; `repo/backend/src/main/java/com/busapp/service/NotificationService.java:35-38`, `NotificationService.java:106-113`
  - Reasoning: Notification reads/writes are scoped via the authenticated username to the current user id. Static tests do not prove cross-user isolation.
- Function-level authorization: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/service/WorkflowService.java:47-71`
  - Reasoning: Risky workflow approvals branch on caller role within the service. There is still no strong test coverage for role-specific workflow behavior.
- Tenant / user isolation: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/repository/MessageQueueRepository.java:9-12`; `repo/backend/src/main/java/com/busapp/service/NotificationService.java:106-113`
  - Reasoning: Notification data access is user-scoped by repository query. No multi-user isolation tests are present.
- Admin / internal / debug protection: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:38-42`; `repo/backend/src/main/resources/application.yml:24-32`
  - Reasoning: Admin and actuator endpoints are now protected by admin role. The shipped verifier still targets admin endpoints without admin auth, so the delivery evidence remains inconsistent.

## 7. Tests and Logging Review
- Unit tests: **Partial Pass**
  - Evidence: `repo/backend/src/test/java/com/busapp/SearchModuleTest.java:17-36`; `repo/backend/src/test/java/com/busapp/NotificationModuleTest.java:19-45`; `repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java:20-41`; `repo/backend/src/test/java/com/busapp/DataIntegrationModuleTest.java:22-76`; `repo/backend/src/test/java/com/busapp/ObservabilityModuleTest.java:10-16`
  - Reasoning: Backend service-level tests exist, but they remain narrow and do not cover many critical failure paths.
- API / integration tests: **Fail**
  - Evidence: `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:30-133`; no frontend spec files present under `repo/frontend/src`
  - Reasoning: Security/integration coverage is weak, partly inconsistent with real routes, and missing frontend tests entirely.
- Logging categories / observability: **Partial Pass**
  - Evidence: `repo/backend/src/main/resources/application.yml:24-36`; `repo/backend/src/main/java/com/busapp/infra/TraceIdFilter.java:16-32`; `repo/backend/src/main/java/com/busapp/infra/monitoring/PerformanceMonitor.java:14-35`; `repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java:35-57`
  - Reasoning: Structured trace IDs, metrics exposure, queue backlog warnings, and latency diagnostics are present statically.
- Sensitive-data leakage risk in logs / responses: **Fail**
  - Evidence: `repo/backend/src/main/java/com/busapp/model/MessageResponse.java:11-17`; `repo/backend/src/main/java/com/busapp/service/DataCleaningService.java:137-139`
  - Reasoning: Response masking is not aligned with the configured sensitivity rules, and raw price values are logged on parse failure.

## 8. Test Coverage Assessment (Static Audit)

### 8.1 Test Overview
- Unit/service tests exist in `repo/backend/src/test/java/com/busapp/*.java`.
- Test frameworks: JUnit 5, Mockito, Spring Boot Test.
- Test entry points: Maven/Surefire via `repo/backend/pom.xml:54-66`; frontend `npm test` script exists in `repo/frontend/package.json:5-10`.
- Documentation does not provide a reliable static test guide; the only supplied automation is `run_tests.ps1`, which is inconsistent with the current secured API.
- Evidence: `repo/backend/pom.xml:54-66`; `repo/frontend/package.json:5-10`; `run_tests.ps1:1-205`

### 8.2 Coverage Mapping Table
| Requirement / Risk Point | Mapped Test Case(s) | Key Assertion / Fixture / Mock | Coverage Assessment | Gap | Minimum Test Addition |
|---|---|---|---|---|---|
| Password minimum length | `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:50-67` | Short password rejected, short registration password throws | basically covered | No verified CSRF/login integration path | Add MockMvc tests with CSRF token handling for login/register success and failure |
| Public registration cannot escalate privileges | `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:99-114` | Admin role request is forced to `PASSENGER` | basically covered | No full HTTP-level regression for registration payload role abuse | Add controller test asserting posted `ADMIN` role still returns/stores passenger role |
| Route authorization 401/403 | `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:116-133` | Passenger blocked from admin, unauthenticated blocked | insufficient | One tested route is non-existent and there is no CSRF or dispatcher/admin matrix | Add route-auth coverage for real endpoints across passenger/dispatcher/admin roles |
| Search minimum length / ranking | `repo/backend/src/test/java/com/busapp/SearchModuleTest.java:24-35` | Score math and 2-char trigger | insufficient | No deduplication, route-number, pinyin/full-text, or ordering assertions | Add service/repository tests for search result ordering and deduplication |
| Quiet-hours suppression | `repo/backend/src/test/java/com/busapp/NotificationModuleTest.java:28-37` | `isInsideDND()` returns true for 22:00-07:00 | insufficient | No test for `quietHoursEnabled=false`, no queue processing assertions | Add scheduler tests covering toggle-enabled and toggle-disabled behavior |
| Missed check-in threshold | `repo/backend/src/test/java/com/busapp/NotificationModuleTest.java:39-45` | 6-minute-old start time triggers check | insufficient | No end-to-end notification creation/content test | Add service/controller tests for missed-checkin payload handling and message content |
| Workflow risky branching | `repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java:28-41` | High-impact route change marked `RISKY` on return | insufficient | No joint approval, batch rejection, or role-specific approval coverage | Add workflow tests for dispatcher/admin parallel approvals and batch rejection |
| Data cleaning and version increment | `repo/backend/src/test/java/com/busapp/DataIntegrationModuleTest.java:35-76` | sqft conversion, NULL fallback, version increment | basically covered | No template import/controller/auth tests | Add admin-only controller tests for HTML/JSON import and config endpoints |
| Observability / P95 | `repo/backend/src/test/java/com/busapp/ObservabilityModuleTest.java:10-16` | `calculateP95()` basic assertion | insufficient | No trace-header or protected actuator coverage | Add MockMvc tests for `X-Trace-ID` and actuator/admin protection |
| Sensitive message masking | none meaningful | N/A | missing | No tests cover `MessageResponse`, `finalContent`, or sensitivity leakage | Add DTO/service tests proving masked content follows sensitivity rules and suppression outcome |

### 8.3 Security Coverage Audit
- Authentication: **Partial Pass**
  - Tests cover password validation and forced passenger role on registration, but not the real CSRF-protected login/register HTTP flow.
- Route authorization: **Fail**
  - Some 401/403 assertions exist, but coverage is shallow and includes a non-existent route, so severe defects could remain undetected.
- Object-level authorization: **Fail**
  - No tests prove one user cannot access another user’s notifications or preferences.
- Tenant / data isolation: **Fail**
  - No multi-user isolation tests exist.
- Admin / internal protection: **Fail**
  - There is no meaningful test coverage proving actuator/admin endpoints stay protected under real request conditions.

### 8.4 Final Coverage Judgment
- **Fail**
- Major risks covered: password validation basics, role downgrade on public registration, simple DND time-window math, one workflow branch rule, unit conversion/version increment, and P95 math.
- Major uncovered risks: CSRF-protected auth flow, quiet-hours toggle enforcement, real route-authorization matrix, object isolation, sensitivity-based message masking, frontend reminder behavior, and admin/internal endpoint protection. Current tests could pass while severe delivery and security defects remain.

## 9. Final Notes
- This audit is static-only and does not claim runtime success or failure.
- The repository improved materially in registration hardening and admin/actuator protection, but acceptance still fails on delivery verifiability and unresolved core notification-flow defects.
- Report location: `.tmp/city-bus-static-audit-2026-04-22.md`
