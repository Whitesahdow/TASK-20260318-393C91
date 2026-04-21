# City Bus Operation and Service Coordination Platform Static Audit

## 1. Verdict
- Overall conclusion: **Fail**

## 2. Scope and Static Verification Boundary
- Reviewed: `docs/`, root manifests/scripts, backend source under `repo/backend/src/main`, frontend source under `repo/frontend/src`, backend tests under `repo/backend/src/test`, and static repository structure.
- Not reviewed: runtime startup, browser behavior, actual HTTP execution, Docker/container behavior, live PostgreSQL state, scheduled timing behavior, or network interactions.
- Intentionally not executed: project startup, Docker, tests, browser interaction, external services.
- Claims requiring manual verification: actual CSRF bootstrap/login flow, Angular rendering and user interaction, queue processing timing, escalation scheduling, and backup generation.

## 3. Repository / Requirement Mapping Summary
- Prompt core goal: Angular passenger/dispatcher/admin platform on Spring Boot + PostgreSQL for route/stop search, notification preferences and message center, dispatcher workflow approvals with timeout escalation, admin maintenance of templates/weights/dictionaries, HTML/JSON import and cleaning/versioning, local queue scheduling, local backup, and observability with trace IDs.
- Main implementation areas mapped: authentication and authorization (`repo/backend/src/main/java/com/busapp/security`, `controller/AuthController.java`), passenger search/notifications (`PassengerSearchController.java`, `PassengerNotificationController.java`, `SearchService.java`, `NotificationService.java`, `NotificationScheduler.java`), workflow (`DispatcherWorkflowController.java`, `WorkflowService.java`, `EscalationService.java`), admin/data integration (`AdminMaintenanceController.java`, `DataIntegrationController.java`, `DataCleaningService.java`, `TemplateProcessorService.java`), frontend role views (`repo/frontend/src/app/features/**`), docs/scripts, and backend tests.

## 4. Section-by-section Review

### 1. Hard Gates
#### 1.1 Documentation and static verifiability
- Conclusion: **Fail**
- Rationale: There is still no top-level README or coherent human verification guide. The supplied `run_tests.ps1` is statically inconsistent with the secured API because it posts to CSRF-protected endpoints without token handling and invokes admin-only routes without authenticated admin context.
- Evidence: `docs/api-spec.md:1-52`; `docs/design.md:1-55`; `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:35-42`; `run_tests.ps1:91-96`; `run_tests.ps1:122-123`; `run_tests.ps1:170-177`; `run_tests.ps1:195-196`
- Manual verification note: A reviewer would need to rewrite key verification steps before attempting static-to-manual validation.

#### 1.2 Whether the delivered project materially deviates from the Prompt
- Conclusion: **Fail**
- Rationale: The codebase is still centered on the requested business scenario, but key prompt semantics remain weakened. Quiet hours are modeled in the UI and entity but not enforced by the scheduler, message-center responses bypass the queue’s own `finalContent`/sensitivity handling, and passenger reminder scheduling still depends on a frontend mock ETA.
- Evidence: `repo/backend/src/main/java/com/busapp/model/NotificationPreference.java:15-28`; `repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java:48-69`; `repo/backend/src/main/java/com/busapp/model/MessageResponse.java:11-17`; `repo/frontend/src/app/features/passenger/passenger.component.ts:122-127`

### 2. Delivery Completeness
#### 2.1 Whether the delivered project fully covers explicit core requirements
- Conclusion: **Fail**
- Rationale: Search, dispatcher workflow, admin maintenance, import/versioning, local backup configuration, health/metrics, and trace IDs are present. Core prompt requirements are still not fully satisfied because quiet-hours toggles do not control suppression behavior, the arrival reminder flow uses mock ETA logic, and message desensitization is not consistently driven by the queue’s processed content.
- Evidence: implemented areas in `repo/backend/src/main/java/com/busapp/service/SearchService.java:21-67`, `repo/backend/src/main/java/com/busapp/service/WorkflowService.java:25-88`, `repo/backend/src/main/java/com/busapp/service/EscalationService.java:22-33`, `repo/backend/src/main/java/com/busapp/service/DataCleaningService.java:37-191`, `repo/docker-compose.yml:39-56`; incomplete areas in `repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java:48-69`, `repo/backend/src/main/java/com/busapp/model/MessageResponse.java:11-17`, `repo/frontend/src/app/features/passenger/passenger.component.ts:122-127`

#### 2.2 Whether the project is a basic end-to-end deliverable from 0 to 1
- Conclusion: **Partial Pass**
- Rationale: This is a multi-module full-stack delivery with backend, frontend, DB config, admin interfaces, and backend tests. It still contains demo-style shortcuts in critical passenger flows and lacks a statically reliable verification path.
- Evidence: `repo/backend/pom.xml:23-68`; `repo/frontend/package.json:5-29`; `repo/frontend/src/app/features/passenger/passenger.component.ts:122-127`; `run_tests.ps1:1-205`

### 3. Engineering and Architecture Quality
#### 3.1 Whether the project adopts a reasonable engineering structure and module decomposition
- Conclusion: **Partial Pass**
- Rationale: The backend uses a conventional layered structure and the frontend is segmented by role. The repository still ships unnecessary generated/vendor artifacts such as `node_modules`, frontend `dist`, and a backup dump, which adds acceptance noise.
- Evidence: `repo/backend/src/main/java/com/busapp/**`; `repo/frontend/src/app/features/**`; committed artifacts under `repo/frontend/node_modules/**`, `repo/frontend/dist/**`, `repo/backups/backup_20260421.sql`

#### 3.2 Whether the project shows maintainability and extensibility
- Conclusion: **Fail**
- Rationale: Important business behavior is still hard-coded or coupled to placeholders: the passenger frontend fabricates an ETA, missed check-in content hardcodes a stop name, and the scheduler ignores the persisted quiet-hours switch.
- Evidence: `repo/frontend/src/app/features/passenger/passenger.component.ts:122-127`; `repo/backend/src/main/java/com/busapp/service/NotificationService.java:88-96`; `repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java:48-69`

### 4. Engineering Details and Professionalism
#### 4.1 Whether engineering details reflect professional practice
- Conclusion: **Fail**
- Rationale: The project has exception handling, trace IDs, metrics, and some validation, but professionalism is undercut by the broken delivery verifier, incomplete CSRF/documentation alignment, and a message DTO that ignores the queue’s processed output and sensitivity logic.
- Evidence: `repo/backend/src/main/java/com/busapp/controller/ApiExceptionHandler.java:12-27`; `repo/backend/src/main/java/com/busapp/infra/TraceIdFilter.java:16-32`; `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:35-42`; `repo/backend/src/main/java/com/busapp/model/MessageResponse.java:11-17`; `run_tests.ps1:91-96`; `run_tests.ps1:170-177`

#### 4.2 Whether the project is organized like a real product or service
- Conclusion: **Partial Pass**
- Rationale: The repository resembles a real application rather than a single-file sample, but important user-facing flows still rely on mock behavior and the acceptance assets are not maintained to match the code.
- Evidence: `repo/backend/pom.xml:23-68`; `repo/frontend/package.json:5-29`; `repo/frontend/src/app/features/passenger/passenger.component.ts:122-127`; `docs/api-spec.md:31-52`

### 5. Prompt Understanding and Requirement Fit
#### 5.1 Whether the project accurately understands and responds to the business goal and constraints
- Conclusion: **Fail**
- Rationale: The implementation now enforces passenger-only public registration and protects admin/actuator routes, which is a clear improvement. It still misunderstands the quiet-hours requirement and the message desensitization requirement by not tying delivery behavior and displayed content to the stored quiet-hours toggle and queue-produced masked content.
- Evidence: corrected registration in `repo/backend/src/main/java/com/busapp/service/AuthService.java:34-40`; remaining semantic mismatch in `repo/backend/src/main/java/com/busapp/model/NotificationPreference.java:15-28`, `repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java:48-69`, `repo/backend/src/main/java/com/busapp/model/MessageResponse.java:11-17`

### 6. Aesthetics
#### 6.1 Whether the visual and interaction design fits the scenario
- Conclusion: **Partial Pass**
- Rationale: Static templates and CSS show distinct functional regions, consistent card layouts, and basic responsive structure. Runtime rendering correctness and interaction quality cannot be confirmed statically.
- Evidence: `repo/frontend/src/app/features/passenger/passenger.component.html:1-87`; `repo/frontend/src/app/features/passenger/passenger.component.css:1-146`; `repo/frontend/src/app/features/dispatcher/dispatcher.component.html:1-35`; `repo/frontend/src/app/features/dispatcher/dispatcher.component.css:1-123`
- Manual verification note: Browser verification is required for actual rendering, hover/click feedback, and form interaction behavior.

## 5. Issues / Suggestions (Severity-Rated)

### High
#### 1. Quiet-hours toggle is persisted but not enforced by queue processing
- Severity: **High**
- Conclusion: `quietHoursEnabled` is saved from the passenger UI, but the scheduler suppresses solely on DND times and ignores the toggle.
- Evidence: `repo/backend/src/main/java/com/busapp/model/NotificationPreference.java:18-28`; `repo/backend/src/main/java/com/busapp/service/NotificationService.java:44-49`; `repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java:48-69`; `repo/frontend/src/app/features/passenger/passenger.component.html:55-70`
- Impact: Passenger notification preferences do not behave as specified; disabling quiet hours in the UI does not stop suppression.
- Minimum actionable fix: Condition DND suppression on `pref.isQuietHoursEnabled()` and add scheduler tests for enabled/disabled quiet-hours cases.

#### 2. Message-center DTO bypasses queue-produced `finalContent` and sensitivity-based masking
- Severity: **High**
- Conclusion: `MessageResponse` is built from `rawContent` using a custom regex mask instead of using the scheduler’s `finalContent`/`MaskingUtils`.
- Evidence: `repo/backend/src/main/java/com/busapp/model/MessageResponse.java:11-17`; `repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java:48-57`; `repo/backend/src/main/java/com/busapp/service/MaskingUtils.java:8-21`
- Impact: Message-center output can diverge from actual queued delivery behavior and sensitivity rules, weakening the prompt’s desensitization requirement.
- Minimum actionable fix: Populate DTOs from persisted `finalContent` after queue processing and align display masking with `MaskingUtils`.

#### 3. Passenger reminder scheduling still depends on a frontend mock ETA
- Severity: **High**
- Conclusion: The frontend fabricates `arrivalEta` as `now + 1 hour` and labels it as a mock.
- Evidence: `repo/frontend/src/app/features/passenger/passenger.component.ts:122-127`
- Impact: A core passenger feature is implemented with placeholder timing rather than route/service data or actual reservation context.
- Minimum actionable fix: Derive ETA from real bus/route data or require reservation-specific time input from a true reservation flow; remove the mock timestamp generation.

#### 4. Delivery verification script is statically incompatible with the secured API
- Severity: **High**
- Conclusion: `run_tests.ps1` posts to CSRF-protected endpoints without token handling and invokes admin-only endpoints without authenticated admin context.
- Evidence: `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:35-42`; `run_tests.ps1:91-96`; `run_tests.ps1:122-123`; `run_tests.ps1:170-177`; `run_tests.ps1:195-196`
- Impact: The included acceptance path cannot be relied on; a reviewer must rewrite core verification steps before using it.
- Minimum actionable fix: Maintain a verifier that explicitly obtains CSRF tokens, authenticates the required roles, and targets only the implemented routes/methods.

#### 5. Security/integration test assets are internally inconsistent
- Severity: **High**
- Conclusion: `SecurityModuleTest` references `WithMockUser` without a corresponding `spring-security-test` dependency and includes a route assertion against `/api/v1/passenger/messages`, which is not implemented.
- Evidence: `repo/backend/pom.xml:54-58`; `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:117-133`; implemented notification routes in `repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java:31-79`
- Impact: The security test suite is not trustworthy as delivery evidence and may be non-runnable or partially stale.
- Minimum actionable fix: Add `spring-security-test` explicitly, align tests to actual routes, and expand coverage for CSRF, authz, and object isolation.

### Medium
#### 6. API documentation is still inconsistent with implemented paths and methods
- Severity: **Medium**
- Conclusion: The API spec documents `PATCH` methods and simplified admin paths that do not match the controllers.
- Evidence: `docs/api-spec.md:31-52`; `repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java:31-79`; `repo/backend/src/main/java/com/busapp/controller/DispatcherWorkflowController.java:25-42`; `repo/backend/src/main/java/com/busapp/controller/DataIntegrationController.java:46-97`
- Impact: Reviewers and integrators are given an inaccurate contract.
- Minimum actionable fix: Update the API spec to exactly match implemented paths, verbs, and auth expectations.

#### 7. No top-level README or reviewer guide is provided
- Severity: **Medium**
- Conclusion: The delivery still lacks a central startup/run/test/configuration guide.
- Evidence: root file list contains no `README*`; only `docs/design.md:1-55` and `docs/api-spec.md:1-52` are present
- Impact: Hard-gate static verifiability is weakened because the reviewer must infer verification steps from scattered files.
- Minimum actionable fix: Add a top-level README with prerequisites, configuration, entry points, security assumptions, and a maintained static verification path.

#### 8. Missed check-in notification content is hard-coded to one stop
- Severity: **Medium**
- Conclusion: Missed check-in messages always reference `"Central Avenue"`.
- Evidence: `repo/backend/src/main/java/com/busapp/service/NotificationService.java:88-96`
- Impact: User-facing content is not reservation-specific and does not generalize across actual trips.
- Minimum actionable fix: Pass stop/trip context into missed check-in generation and render content from real reservation data or templates.

## 6. Security Review Summary
- Authentication entry points: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/controller/AuthController.java:30-66`; `repo/backend/src/main/java/com/busapp/service/AuthService.java:27-57`
  - Reasoning: Local username/password auth, BCrypt hashing, passenger-only public registration, and logout endpoint are present. Static evidence is still insufficient to confirm the first-request CSRF bootstrap path for public POSTs.
- Route-level authorization: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:37-42`
  - Reasoning: Passenger/dispatcher/admin and actuator routes are properly segmented in config. Tests and verification assets are too weak/inconsistent to treat this as fully evidenced.
- Object-level authorization: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java:27-79`; `repo/backend/src/main/java/com/busapp/service/NotificationService.java:35-38`, `NotificationService.java:106-113`
  - Reasoning: Notification reads/writes are derived from the authenticated username to the current user id. No tests prove cross-user isolation.
- Function-level authorization: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/service/WorkflowService.java:47-71`
  - Reasoning: Risky workflow approval branches on caller authority inside the service, but role-specific workflow behavior is not thoroughly tested.
- Tenant / user isolation: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/repository/MessageQueueRepository.java:9-12`; `repo/backend/src/main/java/com/busapp/service/NotificationService.java:106-113`
  - Reasoning: Notification retrieval is scoped by `userId`. There is no static test evidence for isolation under multi-user conditions.
- Admin / internal / debug protection: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:38-42`; `repo/backend/src/main/resources/application.yml:24-32`
  - Reasoning: Admin and actuator endpoints are now role-protected. The delivery’s own verifier still contradicts this contract by calling admin endpoints without admin auth.

## 7. Tests and Logging Review
- Unit tests: **Partial Pass**
  - Evidence: `repo/backend/src/test/java/com/busapp/SearchModuleTest.java:17-36`; `repo/backend/src/test/java/com/busapp/NotificationModuleTest.java:19-45`; `repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java:20-41`; `repo/backend/src/test/java/com/busapp/DataIntegrationModuleTest.java:22-76`; `repo/backend/src/test/java/com/busapp/ObservabilityModuleTest.java:10-16`
  - Reasoning: Backend service-level tests exist, but they cover narrow slices of behavior.
- API / integration tests: **Fail**
  - Evidence: `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:30-133`; no frontend spec files present under `repo/frontend/src`
  - Reasoning: Security/integration tests are shallow, partly stale, and not reliable evidence for the secured API as delivered.
- Logging categories / observability: **Partial Pass**
  - Evidence: `repo/backend/src/main/resources/application.yml:24-36`; `repo/backend/src/main/java/com/busapp/infra/TraceIdFilter.java:16-32`; `repo/backend/src/main/java/com/busapp/infra/monitoring/PerformanceMonitor.java:14-35`; `repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java:35-57`
  - Reasoning: Trace IDs, metrics exposure, queue backlog warnings, and latency diagnostics exist statically.
- Sensitive-data leakage risk in logs / responses: **Fail**
  - Evidence: `repo/backend/src/main/java/com/busapp/model/MessageResponse.java:11-17`; `repo/backend/src/main/java/com/busapp/service/DataCleaningService.java:137-139`
  - Reasoning: Response masking is not aligned with the declared sensitivity mechanism, and raw price strings are logged on parse failure.

## 8. Test Coverage Assessment (Static Audit)

### 8.1 Test Overview
- Unit/service tests exist in `repo/backend/src/test/java/com/busapp/*.java`.
- Test frameworks: JUnit 5, Mockito, Spring Boot Test.
- Test entry points: Maven/Surefire through `repo/backend/pom.xml:54-66`; frontend `npm test` exists in `repo/frontend/package.json:5-10`.
- Documentation does not provide a reliable static test guide. The shipped automation in `run_tests.ps1` is inconsistent with the current secured API.
- Evidence: `repo/backend/pom.xml:54-66`; `repo/frontend/package.json:5-10`; `run_tests.ps1:1-205`

### 8.2 Coverage Mapping Table
| Requirement / Risk Point | Mapped Test Case(s) | Key Assertion / Fixture / Mock | Coverage Assessment | Gap | Minimum Test Addition |
|---|---|---|---|---|---|
| Password minimum length | `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:50-67` | Short password rejected for login/registration path | basically covered | No verified CSRF-protected success flow | Add MockMvc tests with CSRF token handling for login/register success and failure |
| Public registration cannot escalate privileges | `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:99-114` | ADMIN role request forced to PASSENGER | basically covered | No full HTTP-level regression around posted role values | Add controller-level test for role downgrade on public registration |
| Route authorization 401/403 | `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:116-133` | Passenger blocked from admin; unauthenticated requests rejected | insufficient | One tested route is non-existent; no full route/role matrix | Add real route coverage for passenger/dispatcher/admin/actuator access |
| Search autocomplete trigger and weighted score | `repo/backend/src/test/java/com/busapp/SearchModuleTest.java:24-35` | 2-char trigger and score math | insufficient | No deduplication, ordering, route-number, or pinyin match assertions | Add service/repository tests for deduped ordered search results |
| Quiet-hours suppression | `repo/backend/src/test/java/com/busapp/NotificationModuleTest.java:28-37` | `isInsideDND()` true for 22:00-07:00 | insufficient | No coverage for `quietHoursEnabled=false` and no queue processing assertions | Add scheduler tests for quiet-hours enabled/disabled behavior |
| Missed check-in threshold | `repo/backend/src/test/java/com/busapp/NotificationModuleTest.java:39-45` | 6-minute-old start time triggers threshold | insufficient | No test for actual notification content or reservation context | Add service/controller tests for missed-checkin payload and content generation |
| Workflow risky branching | `repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java:28-41` | High-impact route change becomes `RISKY` | insufficient | No joint approval, batch rejection, or role-specific workflow coverage | Add workflow tests for parallel approvals and forbidden paths |
| Data cleaning and version increment | `repo/backend/src/test/java/com/busapp/DataIntegrationModuleTest.java:35-76` | sqft conversion, NULL fallback, version increment | basically covered | No controller/auth coverage for import/config endpoints | Add admin-only controller tests for HTML/JSON import and configuration APIs |
| Observability / P95 | `repo/backend/src/test/java/com/busapp/ObservabilityModuleTest.java:10-16` | Basic `calculateP95()` assertion | insufficient | No `X-Trace-ID` header or protected actuator coverage | Add MockMvc tests for trace header and admin-protected management routes |
| Sensitivity-based message masking | none meaningful | N/A | missing | No tests cover `MessageResponse`, `finalContent`, or sensitivity leakage | Add DTO/service tests for masked message-center output based on queue-processed content |

### 8.3 Security Coverage Audit
- Authentication: **Partial Pass**
  - Tests cover password validation and forced passenger role on registration, but do not meaningfully cover the live CSRF-protected login/register contract.
- Route authorization: **Fail**
  - Some 401/403 assertions exist, but coverage is shallow and partially stale, so severe authorization defects could still go undetected.
- Object-level authorization: **Fail**
  - No tests verify that one user cannot read another user’s notifications/preferences.
- Tenant / data isolation: **Fail**
  - No multi-user isolation tests exist.
- Admin / internal protection: **Fail**
  - There is no trustworthy integration coverage proving admin/actuator protections under real request conditions.

### 8.4 Final Coverage Judgment
- **Fail**
- Major risks covered: password validation basics, role downgrade on public registration, simple DND time-window math, one workflow branch rule, unit conversion/version increment, and P95 math.
- Major uncovered risks: actual CSRF-protected auth flow, quiet-hours toggle enforcement, route-authorization matrix, object isolation, sensitivity-based message masking, frontend reminder behavior, and admin/internal endpoint protection. The current tests could pass while severe delivery and security defects remain.

## 9. Final Notes
- This audit is static-only and does not claim runtime success or failure.
- The repository has improved in registration hardening and admin/actuator protection, but acceptance still fails because the delivered verification path is broken and key notification semantics remain incorrect.
- Report location: `.tmp/city-bus-static-audit-current.md`
