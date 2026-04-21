# Ambiguity Resolution Log

Date: 2026-04-21
Scope: City Bus Operation and Service Coordination Platform
Status: Adopted for Phase 1 and onward unless superseded

## Adopted Decisions

1. Stop popularity source
- Decision: Compute popularity from in-platform signals only.
- Formula inputs: search result clicks + arrival reminder activations.

2. Reservation event source
- Decision: Reservation records are imported from offline data integration (not created by this platform in Phase 1).
- Notification trigger: emit successful reservation messages when imported reservation state is "success".

3. Workflow branching conditions
- Decision: Use field-risk based routing.
- Routine branch: stop name/address updates.
- Risky branch: GPS coordinate and price changes requiring joint approval.

4. Integration input mechanism
- Decision: Use a local watched directory for HTML/JSON source drops by administrators.
- Parser runs on file arrival and records source logs.

5. Pinyin/initial matching scope
- Decision: Pinyin matching applies to Chinese text fields (stop name/residential area fields).
- Route number search uses alphanumeric prefix matching.

6. 24-hour escalation target
- Decision: Escalations go to all users with administrator role.
- UI impact: overdue badge visible on dispatcher dashboard.

7. Desensitization levels
- Decision: 3 levels are enforced.
- Level 1 (admin): no masking.
- Level 2 (dispatcher): partial personal-name masking.
- Level 3 (passenger): mask precise addresses and price details.

## Notes
- These decisions are aligned with offline LAN constraints and existing design contract.
- Any change requires updating this file and API/service contracts in the same change set.
