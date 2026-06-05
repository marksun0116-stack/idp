---
review_id: RR-001
title: "Phase 1 Decision Journal Requirements Review"
status: approved
owner: "Investor Development Platform Team"
last_updated: 2026-06-05
version: "0.2"
linked_plan: PLAN-investor-development-platform-001
linked_prds:
  - PRD-investor-development-platform-001
linked_features:
  - FEAT-investor-development-platform-001
linked_contracts:
  - CONR-decision-record-api-001
linked_constraints:
  - CON-private-research-default-001
linked_invariants:
  - INV-decision-record-integrity-001
  - INV-private-data-visibility-001
---

# Phase 1 Decision Journal Requirements Review

## 1. Scope

Phase 1 creates the private decision journal foundation. Users must be able to create, list, view, update, close, and archive investment decision records while preserving original decision context for future reviews.

In scope:

- Private decision records.
- Decision lifecycle states: `draft`, `active`, `closed`, `archived`.
- Owner-scoped APIs and UI behavior.
- Revision or preservation behavior for original thesis, evidence, risks, confidence, horizon, and exit criteria.
- Tests for ownership, required fields, lifecycle transitions, and integrity.

Out of scope:

- Scheduled decision reviews. That starts in Phase 2.
- DQS and behavioral analytics. Those start in Phase 3.
- Public strategy portfolio publishing. That starts in Phase 4.
- Public decision sharing beyond the private visibility default.

## 2. Acceptance Criteria

- A user can create a decision record with ticker, decision type, title, thesis, evidence, risk factors, confidence, time horizon, and exit criteria.
- New decision records default to private visibility.
- A user can list their own decisions and filter by status or ticker.
- A user can view one decision record they own.
- A user can update mutable decision fields while the system preserves enough prior context for later review comparison.
- A user can transition a decision through valid lifecycle states.
- A user cannot view, update, close, or archive another user's decision.
- Invalid lifecycle transitions return a conflict response.
- Missing required create fields return validation errors.

## 3. Contract Details

Driving contract: `CONR-decision-record-api-001` version `0.2`.

Required endpoints:

- `GET /api/decisions`
- `POST /api/decisions`
- `GET /api/decisions/{id}`
- `PUT /api/decisions/{id}`
- `POST /api/decisions/{id}/transition`

The implementation may use separate close/archive endpoints internally, but the published contract uses a transition endpoint for one lifecycle boundary.

## 4. Data Model Notes

Minimum decision record fields:

- `id`
- `user_id`
- `ticker`
- `decision_type`
- `title`
- `thesis`
- `evidence`
- `risk_factors`
- `confidence`
- `time_horizon`
- `exit_criteria`
- `visibility`
- `status`
- `created_at`
- `updated_at`

The implementation must preserve original context for future review.

Phase 1 decision:

- Use revision history for decision context changes.
- Store one revision on create and one new revision each time thesis, evidence, risk factors, confidence, time horizon, or exit criteria changes.
- Later Phase 2 review records should reference the decision revision they are evaluating.
- Confidence is an integer from `1` to `10` inclusive.
- `watch` and `avoid` decisions may become `active`; for those types, active means actively tracking/researching rather than holding a position.

## 5. Testing Requirements

Required focused tests:

- `DecisionRecordCreateTest`: required fields, defaults, and response shape.
- `DecisionRecordOwnershipTest`: user B cannot access or mutate user A's decisions.
- `DecisionRecordLifecycleTest`: valid and invalid status transitions.
- `DecisionRecordIntegrityTest`: updates preserve original context or revision history.
- `DecisionRecordListFilterTest`: status and ticker filters return only owner-scoped records.

## 6. Parallelization

Backend and UI can proceed in parallel after `CONR-decision-record-api-001` version `0.2` is accepted.

Recommended split:

- Backend: data model, owner-scoped API, lifecycle tests.
- Frontend: create form, list/detail shell, mocked API client against the contract.

Do not start Phase 2 review scheduling until `US-001` and `US-002` are implemented and validated.

## 7. Decisions Resolved

- Decision context preservation: revision history.
- Confidence scale: integer `1-10`.
- `watch` and `avoid` lifecycle: allowed to become `active` as research/tracking decisions.
- Public sharing: deferred; Phase 1 supports private decision records only.

## Change log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 0.1 | 2026-06-05 | approved | — | Initial Phase 1 requirements review for decision journal implementation. |
| 0.2 | 2026-06-05 | approved | 0.1 | Resolved Phase 1 implementation choices for revision history, confidence scale, and watch/avoid lifecycle. |
