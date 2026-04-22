# Delivery Acceptance and Project Architecture Audit

## 1. Verdict
- Overall conclusion: Fail

## 2. Scope and Static Verification Boundary
- Reviewed: `README.md`, `docs/*.md`, `repo/docker-compose.yml`, backend Spring Boot source and tests under `repo/backend/src`, frontend Angular source and specs under `repo/frontend/src`, build manifests (`pom.xml`, `package.json`, `angular.json`), and included backup artifact.
- Not reviewed: runtime behavior, database state beyond static schema/model code, browser rendering, container orchestration behavior, actual API responses, and scheduler timing.
- Intentionally not executed: project startup, Docker, tests, Maven, npm, HTTP calls, database commands.
- Manual verification required for: actual startup/build success, CSRF/session behavior in browser, scheduler execution, queue consumption timing, backup generation, real P95 alert generation, and real UI rendering fidelity.

## 3. Repository / Requirement Mapping Summary
- Prompt core goal: a LAN-local Angular + Spring Boot + PostgreSQL platform for passengers, dispatchers, and administrators with search, notifications, workflow approvals, admin maintenance, data cleaning/versioning, local auth, local queueing, backups, and observability.
- Main implementation areas mapped: auth/security (`AuthController`, `SecurityConfig`, `AuthService`), passenger search/notifications (`PassengerSearchController`, `PassengerNotificationController`, `SearchService`, `NotificationService`), dispatcher workflow (`DispatcherWorkflowController`, `WorkflowService`, `EscalationService`), admin/config/data cleaning (`AdminMaintenanceController`, `DataIntegrationController`, `DataCleaningService`, `AdminConfigService`, `TemplateProcessorService`), observability (`TraceIdFilter`, `ApiTimingInterceptor`, `PerformanceMonitor`), Angular role dashboards, and tests/specs.

## 4. Section-by-section Review

### 1. Hard Gates
#### 1.1 Documentation and static verifiability
- Conclusion: Fail
- Rationale: The README documents Docker-only startup and a Windows `run_tests.ps1` file that is not present, while required secrets are referenced but not documented. API docs also do not match implemented admin endpoints.
- Evidence: `README.md:12`, `README.md:24`, `README.md:27`, `run_tests.sh:1`, `repo/docker-compose.yml:7`, `repo/docker-compose.yml:29`, `repo/backend/src/main/java/com/busapp/infra/config/DataInitializer.java:65`, `docs/api-spec.md:49`, `docs/api-spec.md:51`, `docs/api-spec.md:52`, `docs/api-spec.md:53`, `repo/backend/src/main/java/com/busapp/controller/DataIntegrationController.java:74`, `repo/backend/src/main/java/com/busapp/controller/DataIntegrationController.java:79`, `repo/backend/src/main/java/com/busapp/controller/AdminMaintenanceController.java:34`
- Manual verification note: Manual setup would require reconstructing undocumented env values and correcting endpoint names first.

#### 1.2 Material deviation from the Prompt
- Conclusion: Fail
- Rationale: Core prompt features are weakened or bypassed: passenger search stops at autocomplete selection instead of a true results page, notification templates are maintained but not used, and dispatcher role provisioning is absent.
- Evidence: `repo/frontend/src/app/features/passenger/passenger.component.ts:86`, `repo/frontend/src/app/features/passenger/passenger.component.ts:94`, `repo/frontend/src/app/features/passenger/passenger.component.ts:111`, `repo/backend/src/main/java/com/busapp/service/NotificationService.java:59`, `repo/backend/src/main/java/com/busapp/service/NotificationService.java:71`, `repo/backend/src/main/java/com/busapp/service/AuthService.java:37`, `repo/backend/src/main/java/com/busapp/infra/config/DataInitializer.java:64`

### 2. Delivery Completeness
#### 2.1 Coverage of core prompt requirements
- Conclusion: Partial Pass
- Rationale: The repository includes implementations for all three UI areas and most backend domains, but several explicit prompt requirements are incomplete or only nominally covered: no verifiable dispatcher account path, no real passenger results page, notification templates are disconnected from message generation, and dictionary maintenance does not drive cleaning behavior.
- Evidence: `repo/frontend/src/app/app.routes.ts:12`, `repo/frontend/src/app/app.routes.ts:13`, `repo/frontend/src/app/app.routes.ts:14`, `repo/frontend/src/app/features/passenger/passenger.component.html:25`, `repo/backend/src/main/java/com/busapp/service/NotificationService.java:57`, `repo/backend/src/main/java/com/busapp/service/AdminConfigService.java:45`, `repo/backend/src/main/java/com/busapp/service/DataCleaningService.java:151`

#### 2.2 Basic end-to-end deliverable vs partial/demo
- Conclusion: Partial Pass
- Rationale: This is a structured full-stack repository, not a single-file demo, but some behavior is demo-seeded and not fully productized. Search/workflow data are seeded examples, frontend specs are creation-only, and the documented verification flow depends on Docker and external execution.
- Evidence: `repo/backend/src/main/java/com/busapp/infra/config/DataInitializer.java:76`, `repo/backend/src/main/java/com/busapp/infra/config/DataInitializer.java:118`, `repo/frontend/src/app/features/passenger/passenger.component.spec.ts:21`, `repo/frontend/src/app/features/dispatcher/dispatcher.component.spec.ts:21`, `README.md:24`

### 3. Engineering and Architecture Quality
#### 3.1 Engineering structure and module decomposition
- Conclusion: Pass
- Rationale: The project is separated into backend/frontend, then controllers/services/repositories/models on the backend and role-based feature areas on the frontend. Responsibilities are mostly understandable.
- Evidence: `README.md:31`, `repo/backend/src/main/java/com/busapp/controller/PassengerSearchController.java:13`, `repo/backend/src/main/java/com/busapp/service/SearchService.java:11`, `repo/backend/src/main/java/com/busapp/repository/SearchRepository.java:10`, `repo/frontend/src/app/app.routes.ts:9`

#### 3.2 Maintainability and extensibility
- Conclusion: Partial Pass
- Rationale: The structure is extensible, but some admin/config surfaces are effectively dead configuration, key workflow semantics are simplified, and documentation/tests are drifting from code, which reduces maintainability.
- Evidence: `repo/backend/src/main/java/com/busapp/service/AdminConfigService.java:63`, `repo/backend/src/main/java/com/busapp/service/DataCleaningService.java:151`, `repo/backend/src/main/java/com/busapp/service/WorkflowService.java:36`, `repo/backend/src/test/java/com/busapp/SearchModuleTest.java:60`

### 4. Engineering Details and Professionalism
#### 4.1 Error handling, logging, validation, API design
- Conclusion: Partial Pass
- Rationale: There is centralized exception handling and basic input validation, but observability is incomplete for key workflows, some logs are ad hoc, and API documentation is inconsistent with implementation.
- Evidence: `repo/backend/src/main/java/com/busapp/controller/ApiExceptionHandler.java:12`, `repo/backend/src/main/java/com/busapp/service/AuthService.java:59`, `repo/backend/src/main/java/com/busapp/service/DataCleaningService.java:104`, `repo/backend/src/main/java/com/busapp/infra/TraceIdFilter.java:22`, `repo/backend/src/main/java/com/busapp/service/WorkflowService.java:1`, `repo/backend/src/main/java/com/busapp/service/SearchService.java:1`
- Manual verification note: Runtime log shape and MDC propagation beyond request boundaries require manual verification.

#### 4.2 Product/service maturity vs teaching sample
- Conclusion: Partial Pass
- Rationale: The repository resembles a real product skeleton, but the delivery still has sample traits: seeded records, placeholder frontend tests, a debug simulation endpoint, and a shell script that claims full audit coverage without current static consistency.
- Evidence: `repo/backend/src/main/java/com/busapp/infra/config/DataInitializer.java:81`, `repo/backend/src/main/java/com/busapp/controller/AdminMaintenanceController.java:68`, `repo/frontend/src/app/features/admin/admin.component.spec.ts:21`, `run_tests.sh:161`

### 5. Prompt Understanding and Requirement Fit
#### 5.1 Business goal, semantics, and constraints
- Conclusion: Fail
- Rationale: The delivered code broadly recognizes the prompt but misses important semantics: three-role operation is not statically verifiable because dispatcher provisioning is missing; passenger search UX is autocomplete-first rather than a true results workflow; notification template maintenance does not affect generated notifications; and missing values are stored as literal `"NULL"` strings instead of null values.
- Evidence: `repo/backend/src/main/java/com/busapp/service/AuthService.java:34`, `repo/backend/src/main/java/com/busapp/infra/config/DataInitializer.java:64`, `repo/frontend/src/app/features/passenger/passenger.component.ts:72`, `repo/frontend/src/app/features/passenger/passenger.component.ts:114`, `repo/backend/src/main/java/com/busapp/service/NotificationService.java:59`, `repo/backend/src/main/java/com/busapp/service/DataCleaningService.java:96`, `repo/backend/src/test/java/com/busapp/DataIntegrationModuleTest.java:55`

### 6. Aesthetics (frontend-only / full-stack)
#### 6.1 Visual and interaction design
- Conclusion: Partial Pass
- Rationale: Static CSS shows role-distinct visual treatments, card separation, responsive breakpoints, and basic interaction affordances. However, this audit did not run the UI, so rendering correctness and interactive polish remain manual-verification items.
- Evidence: `repo/frontend/src/app/features/passenger/passenger.component.css:1`, `repo/frontend/src/app/features/dispatcher/dispatcher.component.css:1`, `repo/frontend/src/app/features/admin/admin.component.css:1`, `repo/frontend/src/app/features/passenger/passenger.component.html:15`, `repo/frontend/src/app/features/dispatcher/dispatcher.component.html:24`, `repo/frontend/src/app/features/admin/dictionary/dictionary.component.html:72`
- Manual verification note: Rendering, spacing, typography, and interaction feedback need browser verification.

## 5. Issues / Suggestions (Severity-Rated)

### Blocker / High
- Severity: Blocker
- Title: Delivery instructions are not statically executable as documented
- Conclusion: Fail
- Evidence: `README.md:24`, `README.md:27`, `run_tests.sh:1`, `repo/docker-compose.yml:7`, `repo/docker-compose.yml:29`, `repo/backend/src/main/java/com/busapp/infra/config/DataInitializer.java:65`
- Impact: A reviewer cannot follow the documented verification path without first fixing missing files and guessing required secrets such as `POSTGRES_PASSWORD` and `ADMIN_INITIAL_PASSWORD`.
- Minimum actionable fix: Add accurate non-Docker startup/test instructions, document required env vars with an example file, and align the documented test command with a real script.

- Severity: High
- Title: Dispatcher role is implemented in routes but not provisioned or documented
- Conclusion: Fail
- Evidence: `repo/frontend/src/app/app.routes.ts:13`, `repo/backend/src/main/java/com/busapp/service/AuthService.java:34`, `repo/backend/src/main/java/com/busapp/service/AuthService.java:37`, `repo/backend/src/main/java/com/busapp/infra/config/DataInitializer.java:64`
- Impact: One of the three primary business roles cannot be statically verified end-to-end without manual database intervention or undocumented setup.
- Minimum actionable fix: Provide a documented dispatcher provisioning path, such as seeded dispatcher credentials, admin user management, or controlled role assignment.

- Severity: High
- Title: Passenger search UI does not implement a real sorted results workflow
- Conclusion: Fail
- Evidence: `repo/frontend/src/app/features/passenger/passenger.component.ts:86`, `repo/frontend/src/app/features/passenger/passenger.component.ts:94`, `repo/frontend/src/app/features/passenger/passenger.component.ts:111`, `repo/backend/src/main/java/com/busapp/controller/PassengerSearchController.java:27`
- Impact: The prompt requires a search results page with sorted and deduplicated results, but the frontend only loads autocomplete suggestions and collapses selection to a single row instead of consuming the dedicated `/search/results` endpoint.
- Minimum actionable fix: Use `/api/v1/search/results` for an explicit results view, preserve the full ordered result list, and keep autocomplete separate from results rendering.

- Severity: High
- Title: Notification templates are maintained but never applied to generated messages
- Conclusion: Fail
- Evidence: `repo/backend/src/main/java/com/busapp/controller/AdminMaintenanceController.java:53`, `repo/frontend/src/app/features/admin/console/console.component.ts:42`, `repo/backend/src/main/java/com/busapp/service/NotificationService.java:59`, `repo/backend/src/main/java/com/busapp/service/NotificationService.java:71`, `repo/backend/src/main/java/com/busapp/service/NotificationService.java:91`
- Impact: Administrator updates to templates do not affect reservation, reminder, or missed-check-in content, so a core admin maintenance feature is functionally dead.
- Minimum actionable fix: Resolve template bodies by `templateKey` inside notification generation and render placeholders from saved template records instead of hard-coded strings.

- Severity: High
- Title: Documentation and API spec drift from implemented admin endpoints
- Conclusion: Fail
- Evidence: `docs/api-spec.md:49`, `docs/api-spec.md:51`, `docs/api-spec.md:52`, `docs/api-spec.md:53`, `repo/backend/src/main/java/com/busapp/controller/DataIntegrationController.java:74`, `repo/backend/src/main/java/com/busapp/controller/DataIntegrationController.java:79`, `repo/backend/src/main/java/com/busapp/controller/AdminMaintenanceController.java:34`, `repo/backend/src/main/java/com/busapp/controller/AdminMaintenanceController.java:43`
- Impact: Static verification is undermined because a reviewer following the API specification will hit non-existent paths for audit logs, dictionaries, and ranking weights.
- Minimum actionable fix: Regenerate the API spec from code or align controller mappings and docs to one canonical contract.

- Severity: High
- Title: Test suite is stale and cannot be trusted as coverage evidence
- Conclusion: Fail
- Evidence: `repo/backend/src/test/java/com/busapp/SearchModuleTest.java:60`, `repo/backend/src/main/java/com/busapp/repository/SearchRepository.java:30`, `repo/backend/src/test/java/com/busapp/SearchModuleTest.java:63`, `repo/backend/src/main/java/com/busapp/service/ConfigService.java:14`
- Impact: Static coverage claims are weakened because at least one backend test file is out of sync with current production signatures and assertions, so severe defects could remain undetected even if some tests pass elsewhere.
- Minimum actionable fix: Repair tests against current interfaces, add compile-valid coverage for the prompt’s core paths, and document which tests are expected to validate each module.

### Medium
- Severity: Medium
- Title: Missing-value handling stores literal `"NULL"` strings instead of null values
- Conclusion: Fail
- Evidence: `repo/backend/src/main/java/com/busapp/service/DataCleaningService.java:96`, `repo/backend/src/main/java/com/busapp/service/DataCleaningService.java:97`, `repo/backend/src/main/java/com/busapp/service/DataCleaningService.java:143`, `repo/backend/src/test/java/com/busapp/DataIntegrationModuleTest.java:55`
- Impact: This diverges from the prompt’s null-handling requirement and can pollute downstream filtering, analytics, and UI behavior by treating missing data as a real string.
- Minimum actionable fix: Persist actual nulls for missing optional fields and record missingness in audit logs rather than encoding `"NULL"` into business fields.

- Severity: Medium
- Title: Field standard dictionaries are maintained but not consumed by the cleaning pipeline
- Conclusion: Partial Fail
- Evidence: `repo/backend/src/main/java/com/busapp/service/AdminConfigService.java:45`, `repo/backend/src/main/java/com/busapp/service/AdminConfigService.java:63`, `repo/backend/src/main/java/com/busapp/service/DataCleaningService.java:23`, `repo/backend/src/main/java/com/busapp/service/DataCleaningService.java:151`
- Impact: Administrator-maintained dictionary values appear ornamental; changing them does not influence normalization behavior, which weakens configurability.
- Minimum actionable fix: Read dictionary values from `FieldDictionaryRepository` during normalization or remove the unused dictionary concept from the documented scope.

- Severity: Medium
- Title: Workflow return semantics are simplified back to `PENDING`
- Conclusion: Partial Fail
- Evidence: `repo/backend/src/main/java/com/busapp/service/WorkflowService.java:36`, `repo/backend/src/main/java/com/busapp/service/WorkflowService.java:38`
- Impact: Returned tasks are not distinguishable from untouched pending tasks, weakening visual progress semantics and resubmission auditability.
- Minimum actionable fix: Introduce an explicit returned/resubmission status and surface it in the dashboard.

- Severity: Medium
- Title: Key workflow observability is only partially implemented
- Conclusion: Partial Fail
- Evidence: `repo/backend/src/main/java/com/busapp/infra/TraceIdFilter.java:22`, `repo/backend/src/main/java/com/busapp/service/SearchService.java:1`, `repo/backend/src/main/java/com/busapp/service/WorkflowService.java:1`, `repo/backend/src/main/java/com/busapp/service/NotificationScheduler.java:39`, `repo/backend/src/main/java/com/busapp/service/DataCleaningService.java:185`
- Impact: The prompt calls for trace-ID tracking across search, parsing, workflows, and queue consumption, but only request-level trace injection and partial audit persistence are present; search and workflow services have no trace-aware logging at all, and queue logging uses a literal `traceId=batch`.
- Minimum actionable fix: Add structured logs with trace IDs in search, workflow, parsing, and queue handlers, and use MDC/task trace IDs consistently instead of placeholder text.

## 6. Security Review Summary
- Authentication entry points: Partial Pass. Local username/password auth exists with BCrypt hashing and 8-character minimum checks (`repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:22`, `repo/backend/src/main/java/com/busapp/service/AuthService.java:44`), but admin bootstrap depends on undocumented env input and dispatcher provisioning is absent (`repo/backend/src/main/java/com/busapp/infra/config/DataInitializer.java:65`, `repo/backend/src/main/java/com/busapp/service/AuthService.java:37`).
- Route-level authorization: Pass. Centralized route protection is defined in `SecurityConfig` for passenger, dispatcher, admin, and actuator routes (`repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:37`).
- Object-level authorization: Partial Pass. Notification preferences and messages are derived from the authenticated username rather than caller-supplied IDs (`repo/backend/src/main/java/com/busapp/controller/PassengerNotificationController.java:27`, `repo/backend/src/main/java/com/busapp/service/NotificationService.java:106`), but there is no dedicated object-authorization test coverage.
- Function-level authorization: Partial Pass. Authorization is enforced at route pattern level instead of method annotations (`repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:37`); this is acceptable but coarse-grained.
- Tenant / user isolation: Partial Pass. User-scoped notification retrieval is implemented by looking up the current user and filtering by `userId` (`repo/backend/src/main/java/com/busapp/service/NotificationService.java:107`), but the project has no explicit tenant model and no isolation tests beyond route access.
- Admin / internal / debug protection: Pass. Admin maintenance, data import, actuator, and the shipped debug simulation endpoint sit behind admin-only route rules (`repo/backend/src/main/java/com/busapp/security/SecurityConfig.java:41`, `repo/backend/src/main/java/com/busapp/controller/AdminMaintenanceController.java:68`).

## 7. Tests and Logging Review
- Unit tests: Fail. Backend unit tests exist, but at least `SearchModuleTest` is statically out of sync with current code, and frontend specs only assert component creation (`repo/backend/src/test/java/com/busapp/SearchModuleTest.java:60`, `repo/frontend/src/app/features/passenger/passenger.component.spec.ts:21`).
- API / integration tests: Partial Pass. `SecurityModuleTest` uses `MockMvc` for some route checks (`repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:30`), but the documented end-to-end script is Docker-dependent and mismatched to the repository (`README.md:27`, `run_tests.sh:8`).
- Logging categories / observability: Partial Pass. There is request trace injection, DB startup logging, queue/backlog logging, and P95 monitoring (`repo/backend/src/main/java/com/busapp/infra/TraceIdFilter.java:22`, `repo/backend/src/main/java/com/busapp/infra/StartupDbVerifier.java:22`, `repo/backend/src/main/java/com/busapp/infra/monitoring/PerformanceMonitor.java:14`), but key business services lack consistent structured trace logging.
- Sensitive-data leakage risk in logs / responses: Partial Pass. Passwords are hashed before persistence (`repo/backend/src/main/java/com/busapp/service/AuthService.java:36`), notification responses expose masked final content rather than raw content (`repo/backend/src/main/java/com/busapp/model/MessageResponse.java:11`, `repo/backend/src/main/java/com/busapp/service/MaskingUtils.java:12`), and no password logging was found. Manual verification is still required for runtime exception/log paths.

## 8. Test Coverage Assessment (Static Audit)
### 8.1 Test Overview
- Backend tests exist under `repo/backend/src/test/java/com/busapp` using JUnit 5, Mockito, and some Spring `MockMvc` (`repo/backend/pom.xml:54`, `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:30`).
- Frontend specs exist under Angular feature folders and use TestBed (`repo/frontend/package.json:9`, `repo/frontend/src/app/features/passenger/passenger.component.spec.ts:1`).
- Test entry points are documented inaccurately in the README; it references `run_tests.ps1`, but the repository contains `run_tests.sh` (`README.md:27`, `run_tests.sh:1`).

### 8.2 Coverage Mapping Table
| Requirement / Risk Point | Mapped Test Case(s) | Key Assertion / Fixture / Mock | Coverage Assessment | Gap | Minimum Test Addition |
|---|---|---|---|---|---|
| Password minimum and hashing | `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:51`, `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:71` | Rejects short login password; asserts encoded password saved (`repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:57`, `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:84`) | Basically covered | No coverage for admin bootstrap secret or session/logout behavior | Add tests for successful login, logout, and seeded admin bootstrap preconditions |
| Passenger/admin route authorization | `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:117`, `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:125` | Expects 403 for passenger on admin route and 401 for unauthenticated passenger route (`repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:120`, `repo/backend/src/test/java/com/busapp/SecurityModuleTest.java:126`) | Basically covered | No dispatcher-path authorization coverage | Add 401/403 tests for dispatcher routes and admin access to actuator |
| Search ranking, autocomplete threshold, deduplication | `repo/backend/src/test/java/com/busapp/SearchModuleTest.java:24`, `repo/backend/src/test/java/com/busapp/SearchModuleTest.java:31`, `repo/backend/src/test/java/com/busapp/SearchModuleTest.java:38` | Score formula and short-query assertion exist (`repo/backend/src/test/java/com/busapp/SearchModuleTest.java:27`, `repo/backend/src/test/java/com/busapp/SearchModuleTest.java:35`) | Insufficient | Deduplication test is stale against current repository signatures (`repo/backend/src/test/java/com/busapp/SearchModuleTest.java:60`, `repo/backend/src/main/java/com/busapp/repository/SearchRepository.java:30`) | Replace with compile-valid service tests covering autocomplete and `/search/results` ordering |
| Data cleaning conversion and versioning | `repo/backend/src/test/java/com/busapp/DataIntegrationModuleTest.java:35`, `repo/backend/src/test/java/com/busapp/DataIntegrationModuleTest.java:58` | Sqft conversion and version increment assertions (`repo/backend/src/test/java/com/busapp/DataIntegrationModuleTest.java:45`, `repo/backend/src/test/java/com/busapp/DataIntegrationModuleTest.java:75`) | Basically covered | Missing structured HTML parsing, dictionary usage, and null-vs-string-null semantics | Add template parsing tests and assert actual null persistence |
| DND and missed check-in logic | `repo/backend/src/test/java/com/busapp/NotificationModuleTest.java:28`, `repo/backend/src/test/java/com/busapp/NotificationModuleTest.java:39` | Asserts DND suppression and 5-minute missed-check-in threshold (`repo/backend/src/test/java/com/busapp/NotificationModuleTest.java:36`, `repo/backend/src/test/java/com/busapp/NotificationModuleTest.java:44`) | Basically covered | No tests that scheduler masks content, persists delivered status, or uses saved templates | Add end-to-end service tests for queued reservation/reminder generation and masking |
| Workflow branching and batch approval | `repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java:30`, `repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java:45` | Asserts risky branch on return and routine batch approval (`repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java:41`, `repo/backend/src/test/java/com/busapp/WorkflowModuleTest.java:65`) | Insufficient | No test for joint approval by two roles, return-state semantics, or 24h escalation | Add tests for risky task requiring both dispatcher and admin approvals and for timeout escalation |
| Observability / P95 monitor | `repo/backend/src/test/java/com/busapp/ObservabilityModuleTest.java:11` | Only checks `calculateP95` lower bound (`repo/backend/src/test/java/com/busapp/ObservabilityModuleTest.java:14`) | Insufficient | No trace-ID propagation or queue backlog alert coverage | Add tests for response header trace IDs and diagnostic-trigger behavior |
| Frontend role flows | `repo/frontend/src/app/features/passenger/passenger.component.spec.ts:21`, `repo/frontend/src/app/features/dispatcher/dispatcher.component.spec.ts:21`, `repo/frontend/src/app/features/admin/admin.component.spec.ts:21` | Component creation only | Missing | No assertions for forms, HTTP calls, routing, or role guards | Add Angular tests for login/register, passenger search/preferences/messages, dispatcher actions, and admin maintenance forms |

### 8.3 Security Coverage Audit
- Authentication: Basically covered for minimum password and hashing, but not for successful session lifecycle, seeded admin credentials, or dispatcher provisioning.
- Route authorization: Basically covered for one admin and one unauthenticated path; dispatcher and object-level authorization remain weakly tested.
- Object-level authorization: Missing meaningful tests. No test proves one user cannot read or mutate another user’s message/preference objects.
- Tenant / data isolation: Missing. There is no test coverage for user-to-user isolation in notifications or for any tenant boundary.
- Admin / internal protection: Partially covered. One admin-route 403 case and actuator 401 case exist, but no tests cover the admin debug endpoint or data import/config endpoints specifically.

### 8.4 Final Coverage Judgment
- Fail
- Major risks covered: some password validation, some route authorization, basic cleaning math, some DND logic, and a small slice of workflow behavior.
- Major uncovered risks: dispatcher/account provisioning, actual passenger results workflow, template-driven notifications, object-level isolation, admin maintenance correctness, traceability/observability completeness, and frontend behavioral flows. Severe defects could remain while many current tests still pass.

## 9. Final Notes
- This audit is static-only. No runtime-success claim is made for startup, Docker, tests, schedulers, backups, or UI rendering.
- The strongest negative conclusions above are based on direct code/doc mismatches, not on speculation about runtime failures.
