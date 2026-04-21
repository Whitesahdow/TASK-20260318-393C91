# City Bus Operation and Service Coordination Platform Static Audit

## 1. Verdict
- Overall conclusion: **Fail**

## 2. Scope and Static Verification Boundary
- Reviewed: `README.md`, `docs/`, root verification scripts, backend source under `repo/backend/src/main`, frontend source under `repo/frontend/src`, backend tests under `repo/backend/src/test`, frontend specs under `repo/frontend/src/app/**/*.spec.ts`, and static repository structure.
- Not reviewed: runtime startup behavior, actual browser execution, Docker/container behavior, live PostgreSQL contents, scheduled task timing, or real network interactions.
- Intentionally not executed: project startup, Docker, tests, browsers, API requests.
- Claims requiring manual verification: real first-login/register CSRF behavior in the browser, actual scheduled delivery timing, rendered UI/interaction behavior, backup generation timing, and runtime observability behavior.

## 3. Repository / Requirement Mapping Summary
- Prompt core goal: Angular passenger/dispatcher/admin platform on Spring Boot + PostgreSQL with passenger route/stop search, reminder preferences and message center, dispatcher workflow approvals with timeout escalation, admin template/weight/dictionary maintenance, HTML/JSON import and cleaning/versioning, local queue scheduling, local backup, and traceable observability.
- Main implementation areas mapped: auth/security (`repo/backend/src/main/java/com/busapp/security`, `controller/AuthController.java`), passenger search/notifications (`PassengerSearchController.java`, `PassengerNotificationController.java`, `SearchService.java`, `NotificationService.java`, `NotificationScheduler.java`), workflow (`DispatcherWorkflowController.java`, `WorkflowService.java`, `EscalationService.java`), admin/data integration (`AdminMaintenanceController.java`, `DataIntegrationController.java`, `DataCleaningService.java`, `TemplateProcessorService.java`), frontend role UIs (`repo/frontend/src/app/features/**`), docs/scripts, and test assets.

## 4. Section-by-section Review

### 1. Hard Gates
#### 1.1 Documentation and static verifiability
- Conclusion: **Fail**
- Rationale: Documentation is improved by a new `README.md`, but the verification path is still not statically reliable. `run_tests.ps1` assumes an admin user may exist with a fallback password even though the code explicitly skips admin creation when `ADMIN_INITIAL_PASSWORD` is missing, and it performs CSRF-protected login POSTs without an evident CSRF bootstrap for those sessions.
- Evidence: `README.md:24-29`; `run_tests.ps1:82-88`; `run_tests.ps1:135-144`; `run_tests.ps1:194-205`; `repo/backend/src/main/java/com/busapp/infra/config/DataInitializer.java:64-74`; `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:35-42`
- Manual verification note: A reviewer would need to rewrite or repair core verification steps before using the provided script as evidence.

#### 1.2 Whether the delivered project materially deviates from the Prompt
- Conclusion: **Partial Pass**
- Rationale: The implementation is clearly centered on the requested business problem and now enforces safer public registration. Remaining deviations are material but not wholesale replacements of the prompt: the register UI still offers dispatcher self-selection even though the backend silently forces passenger, and the default area unit is `sqm` rather than the prompt example `㎡`.
- Evidence: `repo/frontend/src/app/features/auth/register.component.ts:20-26`; `repo/frontend/src/app/features/auth/register.component.html:15-20`; `repo/backend/src/main/java/com/busapp/service/AuthService.java:34-40`; `repo/backend/src/main/java/com/busapp/service/AdminConfigService.java:25-32`

### 2. Delivery Completeness
#### 2.1 Whether the delivered project fully covers explicit core requirements
- Conclusion: **Partial Pass**
- Rationale: The repository includes implementations for search, notification preferences, message center, dispatcher workflows, admin maintenance, import/versioning, backup configuration, and observability. Gaps remain in static acceptance evidence and user flow semantics: the passenger must manually enter `arrivalEta` on the search results grid, and the register UI exposes a dispatcher role that the backend rejects by downgrading.
- Evidence: `repo/frontend/src/app/features/passenger/passenger.component.html:25-47`; `repo/frontend/src/app/features/passenger/passenger.component.ts:118-142`; `repo/backend/src/main/java/com/busapp/service/NotificationService.java:52-80`; `repo/frontend/src/app/features/auth/register.component.html:15-20`; `repo/backend/src/main/java/com/busapp/service/AuthService.java:34-40`

#### 2.2 Whether the delivered project represents a basic end-to-end deliverable from 0 to 1
- Conclusion: **Partial Pass**
- Rationale: This is a full-stack repository with backend, frontend, docs, configs, and test assets rather than a single-file demo. It still falls short of a clean end-to-end deliverable because the included verification assets are stale/inconsistent and some committed artifacts (`node_modules`, `dist`, backup dump) add delivery noise.
- Evidence: `README.md:31-35`; `repo/backend/pom.xml:23-74`; `repo/frontend/package.json:5-36`; committed artifacts under `repo/frontend/node_modules/**`, `repo/frontend/dist/**`, `repo/backups/backup_20260421.sql`

### 3. Engineering and Architecture Quality
#### 3.1 Whether the project adopts a reasonable engineering structure and module decomposition
- Conclusion: **Partial Pass**
- Rationale: The backend follows a conventional controller/service/repository/model split and the frontend is separated by role features. The structure is acceptable, but the repository contains unnecessary generated/vendor artifacts that should not normally be part of a clean delivery.
- Evidence: `repo/backend/src/main/java/com/busapp/**`; `repo/frontend/src/app/features/**`; `repo/frontend/node_modules/**`; `repo/frontend/dist/**`

#### 3.2 Whether the project shows maintainability and extensibility
- Conclusion: **Partial Pass**
- Rationale: Most core logic is modularized, but there are maintainability concerns from stale tests, docs that lag the implementation, and business semantics split awkwardly between frontend and backend, such as passenger-side `arrivalEta` entry and backend-side role downgrading.
- Evidence: `docs/api-spec.md:31-53`; `repo/frontend/src/app/features/passenger/passenger.component.ts:118-142`; `repo/backend/src/main/java/com/busapp/service/AuthService.java:34-40`; `repo/backend/src/test/java/com/busapp/SearchModuleTest.java:39-71`

### 4. Engineering Details and Professionalism
#### 4.1 Whether engineering details reflect professional practice
- Conclusion: **Fail**
- Rationale: The project has exception handling, trace IDs, metrics, and input validation, but the delivery still includes stale verification and test code that does not align with the implementation. That undercuts confidence in the API contract and in the claimed verification path.
- Evidence: `repo/backend/src/main/java/com/busapp/controller/ApiExceptionHandler.java:12-27`; `repo/backend/src/main/java/com/busapp/infra/TraceIdFilter.java:16-32`; `run_tests.ps1:135-144`; `repo/backend/src/test/java/com/busapp/SearchModuleTest.java:60-64`; `repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java:59-61`

#### 4.2 Whether the project is organized like a real product or service
- Conclusion: **Partial Pass**
- Rationale: The codebase resembles a real product more than a toy example, but the acceptance assets and part of the test suite are not maintained to the same standard as the application code.
- Evidence: `README.md:10-35`; `repo/backend/src/main/java/com/busapp/BackendApplication.java:7-12`; `run_tests.ps1:1-235`; `repo/backend/src/test/java/com/busapp/*.java`

### 5. Prompt Understanding and Requirement Fit
#### 5.1 Whether the project accurately understands and responds to the business goal and implicit constraints
- Conclusion: **Partial Pass**
- Rationale: The repository now protects admin/internal routes and forces public registration to passenger, which aligns better with the prompt. However, the register UI still suggests dispatcher self-registration is valid, and the cleaning defaults use `sqm` instead of the prompt’s example `㎡`, indicating requirement interpretation drift.
- Evidence: `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:37-42`; `repo/backend/src/main/java/com/busapp/service/AuthService.java:34-40`; `repo/frontend/src/app/features/auth/register.component.html:15-20`; `repo/backend/src/main/java/com/busapp/service/AdminConfigService.java:25-32`

### 6. Aesthetics
#### 6.1 Whether the visual and interaction design fits the scenario
- Conclusion: **Partial Pass**
- Rationale: Static templates/CSS show separated functional areas, coherent card layouts, and reasonable hierarchy. Actual rendering, interaction feel, and correctness under runtime data require manual verification.
- Evidence: `repo/frontend/src/app/features/passenger/passenger.component.html:1-88`; `repo/frontend/src/app/features/passenger/passenger.component.css:1-146`; `repo/frontend/src/app/features/dispatcher/dispatcher.component.html:1-35`; `repo/frontend/src/app/features/dispatcher/dispatcher.component.css:1-123`
- Manual verification note: Browser execution is required to confirm rendered quality and actual interaction feedback.

## 5. Issues / Suggestions (Severity-Rated)

### High
#### 1. Provided verification path is still inconsistent with the actual secured startup flow
- Severity: **High**
- Conclusion: `run_tests.ps1` assumes an admin user may be available with a fallback password and performs login POSTs without an evident CSRF bootstrap for those login sessions, while the code only creates `admin` when `ADMIN_INITIAL_PASSWORD` is set and CSRF is enabled globally.
- Evidence: `run_tests.ps1:82-88`; `run_tests.ps1:135-144`; `run_tests.ps1:194-205`; `repo/backend/src/main/java/com/busapp/infra/config/DataInitializer.java:64-74`; `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:35-42`
- Impact: The delivery’s own documented verification path is not trustworthy; reviewers must rewrite core verification steps.
- Minimum actionable fix: Align the script with the actual auth/bootstrap contract by explicitly creating/ensuring admin credentials, fetching CSRF tokens for each new session before POSTs, and documenting the required environment variables.

#### 2. Backend test suite contains stale code that does not match the current implementation
- Severity: **High**
- Conclusion: `SearchModuleTest` references nonexistent repository/service methods, and `WorkflowModuleTest` uses `any(...)` and `List.of(...)` without corresponding imports.
- Evidence: `repo/backend/src/test/java/com/busapp/SearchModuleTest.java:60-64`; actual repository/service APIs in `repo/backend/src/main/java/com/busapp/repository/SearchRepository.java:11-35`, `repo/backend/src/main/java/com/busapp/service/ConfigService.java:14-33`; `repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java:15-18`; `repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java:59-61`
- Impact: The backend test suite cannot be treated as reliable acceptance evidence and may not compile/run as delivered.
- Minimum actionable fix: Update tests to the current APIs, add the missing imports, and re-baseline the coverage matrix against the implemented code.

### Medium
#### 3. Register UI exposes dispatcher self-registration even though the backend silently forces passenger
- Severity: **Medium**
- Conclusion: The frontend presents `DISPATCHER` as a selectable role, but the backend ignores it and persists `PASSENGER`.
- Evidence: `repo/frontend/src/app/features/auth/register.component.ts:20-26`; `repo/frontend/src/app/features/auth/register.component.html:15-20`; `repo/backend/src/main/java/com/busapp/service/AuthService.java:34-40`
- Impact: The user-facing flow is misleading and does not reflect actual system behavior.
- Minimum actionable fix: Remove dispatcher from public registration UI or replace it with explanatory text about admin-created non-passenger accounts.

#### 4. API documentation is still partially stale relative to implemented admin endpoints
- Severity: **Medium**
- Conclusion: The API spec documents `/admin/audit-logs`, `/admin/dictionaries`, and `/admin/ranking-weights`, while the implementation exposes `/api/v1/admin/stops/...` and `/api/v1/admin/maintenance/...`.
- Evidence: `docs/api-spec.md:47-53`; implemented endpoints in `repo/backend/src/main/java/com/busapp/controller/DataIntegrationController.java:46-97`; `repo/backend/src/main/java/com/busapp/controller/AdminMaintenanceController.java:34-72`
- Impact: Reviewers and integrators are given an inaccurate API contract.
- Minimum actionable fix: Update `docs/api-spec.md` to match the current paths, verbs, and auth constraints exactly.

#### 5. Cleaning defaults use `sqm` instead of the prompt’s example unit `㎡`
- Severity: **Medium**
- Conclusion: Default field dictionary normalization uses `sqm` as the area unit suffix.
- Evidence: `repo/backend/src/main/java/com/busapp/service/AdminConfigService.java:25-32`
- Impact: The delivered normalization behavior does not fully match the prompt’s stated example formatting.
- Minimum actionable fix: Decide on the intended canonical unit format and align both implementation and documentation to it.

#### 6. Frontend test coverage exists but is almost entirely smoke-level
- Severity: **Medium**
- Conclusion: The frontend specs only verify component creation and do not cover business flows, validation, auth, or interaction outcomes.
- Evidence: `repo/frontend/src/app/features/passenger/passenger.component.spec.ts:10-23`; `repo/frontend/src/app/features/dispatcher/dispatcher.component.spec.ts:10-23`; `repo/frontend/src/app/features/auth/login.component.spec.ts:10-23`; `repo/frontend/src/app/features/auth/register.component.spec.ts:10-23`
- Impact: Severe UI regressions or flow breakages could go undetected even if the frontend test suite passes.
- Minimum actionable fix: Add focused interaction tests for login/register, reminder scheduling, dispatcher approval actions, and admin configuration flows.

## 6. Security Review Summary
- Authentication entry points: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/controller/AuthController.java:30-66`; `repo/backend/src/main/java/com/busapp/service/AuthService.java:27-57`
  - Reasoning: Local username/password auth, BCrypt hashing, passenger-only public registration, and logout endpoint are present. The static verifier/front-end bootstrap for first CSRF-protected POSTs is not convincingly aligned.
- Route-level authorization: **Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:37-42`
  - Reasoning: Passenger, dispatcher/admin, admin, and actuator route groups are explicitly segmented in security config.
- Object-level authorization: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java:27-80`; `repo/backend/src/main/java/com/busapp/service/NotificationService.java:35-38`; `repo/backend/src/main/java/com/busapp/service/NotificationService.java:106-113`
  - Reasoning: Notification reads/writes are scoped from the authenticated username to the current user id. There is no meaningful static test proof of cross-user isolation.
- Function-level authorization: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/service/WorkflowService.java:47-71`
  - Reasoning: Risky workflow approval branches on caller authority inside the service, but tests do not meaningfully validate role-specific workflow behavior.
- Tenant / user isolation: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/repository/MessageQueueRepository.java:9-12`; `repo/backend/src/main/java/com/busapp/service/NotificationService.java:106-113`
  - Reasoning: Notification retrieval is user-scoped. Multi-user isolation is not covered by tests.
- Admin / internal / debug protection: **Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:41-42`; `repo/backend/src/main/resources/application.yml:24-32`
  - Reasoning: Admin endpoints and actuator endpoints are protected by admin role rather than being public.

## 7. Tests and Logging Review
- Unit tests: **Fail**
  - Evidence: `repo/backend/src/test/java/com/busapp/SearchModuleTest.java:60-64`; `repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java:59-61`
  - Reasoning: Backend test files exist, but at least part of the suite is stale enough to be internally inconsistent with the current codebase.
- API / integration tests: **Fail**
  - Evidence: `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:50-134`; frontend specs in `repo/frontend/src/app/**/*.spec.ts`
  - Reasoning: Security/integration coverage is narrow, and the supplied end-to-end verifier is not a trustworthy static acceptance asset.
- Logging categories / observability: **Partial Pass**
  - Evidence: `repo/backend/src/main/resources/application.yml:24-36`; `repo/backend/src/main/java/com/busapp/infra/TraceIdFilter.java:16-32`; `repo/backend/src/main/java/com/busapp/infra/monitoring/PerformanceMonitor.java:14-35`; `repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java:35-57`
  - Reasoning: Trace IDs, metrics exposure, backlog warnings, and latency diagnostics are present statically.
- Sensitive-data leakage risk in logs / responses: **Partial Pass**
  - Evidence: `repo/backend/src/main/java/com/busapp/model/MessageResponse.java:11-16`; `repo/backend/src/main/java/com/busapp/service/MaskingUtils.java:8-21`; `repo/backend/src/main/java/com/busapp/service/DataCleaningService.java:137-139`
  - Reasoning: Message responses now use `finalContent`, which is an improvement, but raw invalid price strings are still logged on parse failure.

## 8. Test Coverage Assessment (Static Audit)

### 8.1 Test Overview
- Unit tests and service tests exist under `repo/backend/src/test/java/com/busapp/*.java`.
- Frontend component specs exist under `repo/frontend/src/app/features/**/*.spec.ts`.
- Test frameworks: JUnit 5, Mockito, Spring Boot Test, Spring Security Test, Jasmine/Karma.
- Test entry points: Maven/Surefire via `repo/backend/pom.xml`; Angular `ng test` via `repo/frontend/package.json`.
- Documentation provides test commands through `README.md` and `run_tests.ps1`, but the shipped verifier is not statically reliable.
- Evidence: `repo/backend/pom.xml:54-63`; `repo/frontend/package.json:5-35`; `README.md:24-29`; `run_tests.ps1:1-235`

### 8.2 Coverage Mapping Table
| Requirement / Risk Point | Mapped Test Case(s) | Key Assertion / Fixture / Mock | Coverage Assessment | Gap | Minimum Test Addition |
|---|---|---|---|---|---|
| Password minimum length | `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:50-68` | Short password rejected; short registration password throws | basically covered | No confirmed full browser/bootstrap coverage | Add integration tests for first-login/register flow with CSRF bootstrap |
| Public registration cannot create privileged roles | `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:100-115` | ADMIN role request is forced to PASSENGER | basically covered | UI still exposes dispatcher signup and no end-to-end assertion covers it | Add frontend + controller test ensuring public registration UX matches backend policy |
| Route authorization 401/403 | `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:117-134` | Passenger blocked from admin; unauthenticated access rejected | basically covered | No wider route/role matrix and no object-level auth assertions | Add role-matrix tests for passenger/dispatcher/admin across major route groups |
| Search min-length / ranking | `repo/backend/src/test/java/com/busapp/SearchModuleTest.java:24-35` | 2-char trigger and score math | insufficient | Deduplication test is stale and mismatched to current APIs | Rewrite search tests against `SearchRepository.searchStops(...)` and current DTO semantics |
| Quiet-hours suppression | `repo/backend/src/test/java/com/busapp/NotificationModuleTest.java:28-57` | DND suppression and quiet-hours-disabled case | basically covered | No full queue-processing assertions or response DTO checks | Add scheduler tests for delivered vs suppressed `finalContent` |
| Missed check-in threshold | `repo/backend/src/test/java/com/busapp/NotificationModuleTest.java:39-45` | 6-minute-old start time triggers threshold | insufficient | No controller/content test for stop-specific missed check-in | Add service/controller tests for stopName propagation and response content |
| Workflow risky branching and routine batch approval | `repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java:28-67` | Intended risky branch and routine batch approval assertions | missing | Current test file is stale/incomplete and likely non-compilable | Repair the file and add tests for joint approvals, returns, and batch rejection of risky tasks |
| Data cleaning and version increment | `repo/backend/src/test/java/com/busapp/DataIntegrationModuleTest.java:35-76` | sqft conversion, NULL fallback, version bump | basically covered | No controller/auth coverage for admin import/config routes | Add admin-only controller tests for import/history/config endpoints |
| Observability / P95 | `repo/backend/src/test/java/com/busapp/ObservabilityModuleTest.java:10-16` | Basic `calculateP95()` assertion | insufficient | No `X-Trace-ID` header or protected actuator coverage | Add MockMvc tests for trace headers and admin-protected actuator access |
| Frontend login/register/passenger/dispatcher flows | `repo/frontend/src/app/features/**/*.spec.ts` | Only component creation | insufficient | No business-flow assertions | Add interaction tests for forms, reminders, approvals, and error states |

### 8.3 Security Coverage Audit
- Authentication: **Partial Pass**
  - Tests cover password validation and role downgrading on registration, but do not convincingly cover the full CSRF-protected startup/login contract.
- Route authorization: **Partial Pass**
  - 401/403 tests exist for a few routes, but the matrix is narrow.
- Object-level authorization: **Fail**
  - No tests verify that one user cannot read or mutate another user’s notifications/preferences.
- Tenant / data isolation: **Fail**
  - No multi-user isolation tests exist.
- Admin / internal protection: **Partial Pass**
  - Protection is configured and partially asserted, but not deeply exercised across admin/internal endpoints.

### 8.4 Final Coverage Judgment
- **Fail**
- Major risks covered: password validation basics, role downgrade on public registration, some 401/403 checks, DND window logic, missed-checkin threshold math, data normalization/version increment basics, and P95 calculation.
- Major uncovered risks: reliable end-to-end auth bootstrap, object-level isolation, full workflow authorization behavior, current search behavior, frontend business flows, and trustworthy execution of the shipped verifier. Part of the backend test suite is stale enough that passing tests would still not prove the critical flows are protected.

## 9. Final Notes
- This audit is static-only and does not claim runtime success or failure.
- The repository improved materially with a README, frontend specs, safer registration handling, and protected internal routes, but it still fails acceptance because the shipped verification path and part of the backend test suite are not trustworthy.
- Report location: `.tmp/city-bus-static-audit-fresh.md`
