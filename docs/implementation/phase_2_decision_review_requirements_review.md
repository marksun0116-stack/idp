---
review_id: RR-002
title: "Phase 2 Decision Review Requirements Review"
status: approved
owner: "Investor Development Platform Team"
last_updated: 2026-06-05
version: "0.1"
linked_plan: PLAN-investor-development-platform-001
linked_story:
  - US-003
  - US-004
linked_primitives:
  - CONR-decision-review-api-001
  - CON-decision-review-cadence-001
  - INV-review-schedule-integrity-001
---

# Phase 2 Decision Review Requirements Review

## Purpose

Phase 2 adds the scheduled review loop that turns decision records into learning artifacts. The platform must create durable review tasks for active decisions, keep overdue reviews visible, and record outcome/lesson data without mutating the original decision context.

## Scope

- Generate review tasks for a decision when it first transitions to `active`.
- Use standard review milestones: `30d`, `90d`, `180d`, and `1y`.
- List pending, completed, dismissed, or rescheduled reviews owned by the authenticated user.
- Complete a review with outcome notes, accuracy ratings, lessons learned, and optional next action.
- Preserve owner isolation. A user must not see or complete another user's reviews.

## Requirements Decisions

| Topic | Decision | Rationale |
| --- | --- | --- |
| Scheduling event | Generate milestones when a decision first becomes `active`. | This matches the learning loop after a decision becomes actionable and gives a durable lifecycle event for due-date calculation. |
| Due-date basis | Due dates are derived from the `activatedAt` timestamp using UTC dates. | Stable across mutable labels and avoids local timezone drift in backend scheduling. |
| Milestones | `30d`, `90d`, `180d`, `1y`. | Matches `CON-decision-review-cadence-001` and MVP scope. |
| Idempotency | A decision receives one review per milestone; repeated active transitions do not duplicate tasks. | Protects review schedule integrity. |
| Overdue behavior | No special persisted overdue state in Phase 2; pending reviews with due dates before today are considered overdue by clients. | Keeps the data model compact while satisfying visibility requirements. |
| Completion ratings | `thesisAccuracy` and `riskAssessmentAccuracy` are integer ratings from 1 to 10. | Aligns with the Phase 1 confidence scale and keeps scoring inputs simple for Phase 3. |
| Closed decisions | Closing a decision does not delete pending reviews. | Missed or pending reviews must remain visible until completed, dismissed, or rescheduled. |
| Dismiss/reschedule | Contract names the statuses, but Phase 2 implementation prioritizes schedule generation and completion. | Dismiss/reschedule can be added after completion behavior is stable. |
| Notifications | Out of scope for the backend slice. | Reviews are visible through listing; notification delivery can be a later integration. |

## Acceptance Criteria

- Activating a private decision creates four pending review tasks with due dates at 30, 90, 180 days, and 1 year from activation.
- Repeating an `active` transition does not create duplicate review tasks.
- Review listing returns only reviews owned by the authenticated user.
- Review completion records completion time, reviewer owner, outcome summary, thesis accuracy, risk accuracy, lessons learned, and optional next action.
- Completed reviews remain linked to their source decision.
- Users receive `404` when attempting to access or complete another user's review.
- Invalid completion ratings return `400`.

## Test Coverage

- `DecisionReviewScheduleTest`: active transition creates expected review milestones and preserves owner isolation.
- `DecisionReviewCompletionTest`: completion captures learning fields, prevents cross-owner completion, and validates ratings.

## Parallelization

- `US-003` must complete before `US-004` because completion depends on scheduled review records.
- Review UI can proceed after `US-003` stabilizes the list response and review status semantics.
- Phase 3 scoring design can proceed in parallel after `US-004` defines completion fields.

## Open Follow-Ups

- Add dismiss and reschedule endpoints after the core completion flow is stable.
- Decide whether overdue should become a materialized status or remain a computed UI/API field.
- Decide whether future review schedules should be configurable per user.

## Revision History

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 0.1 | 2026-06-05 | approved | — | Initial Phase 2 requirements review for scheduled decision reviews and completion. |
