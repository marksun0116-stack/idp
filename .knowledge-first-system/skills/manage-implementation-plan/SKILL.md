---
name: manage-implementation-plan
description: >-
  Creates and maintains lightweight KFS implementation plans under
  docs/implementation/ with ordered phases, phase-start requirements reviews,
  user-story-sized slices, dependencies, parallelization notes, status, progress updates, and trace links
  to FEAT/CON/INV/CONR. Use when work spans multiple stories or phases, when the
  user asks for project management, roadmap execution, implementation order,
  parallel workstreams, progress tracking, or when updating KFS process around
  implementation plans.
---

# Manage Implementation Plan

Use this skill when a request needs lightweight project management for implementation work.

Authoritative shape: **`.knowledge-first-system/specs/implementation_plan_spec.md`**. Rule: **`.knowledge-first-system/rules/implementation-plan-management.mdc`**.

## When To Create Or Update A Plan

Create or update `docs/implementation/<product-or-epic>_implementation_plan.md` when:

- Work has multiple phases, user stories, or parallel tracks.
- The user asks for implementation order, project management, or progress tracking.
- A phase/story starts, completes, is blocked, is deferred, split, merged, or reordered.
- Knowledge primitives or contracts change enough to affect execution order.

Skip only for tiny single-slice fixes unless the user explicitly wants tracking.

## Workflow

1. Load the driving PRD, FEAT, linked CON/INV/DEC/CONR, and relevant design/architecture docs.
2. If no plan exists, create one from the current scope using the spec.
3. Break work into phases and user-story-sized slices.
4. Add a requirements-review gate at the start of each phase (`RR-###`) before implementation stories.
5. For each story/review gate, record status, dependencies, whether it can run in parallel, and trace IDs.
6. For multi-repo work, record the owning repo(s) for each phase/story and the per-repo validation expected.
7. Before implementing a phase, complete or actively run that phase's requirements review.
8. Before implementation, mark the active story or phase `in_progress`.
9. After implementation or validation, update status to `done`, `blocked`, `deferred`, or keep `in_progress` with a progress note.
10. When order changes, preserve the old decision in `Progress Updates` and update the tables.
11. Keep `## Change log` as the final heading and update metadata.

## Phase Requirements Review

At the start of each phase, add or update an `RR-###` row that reviews:

- Phase scope and non-goals.
- Detailed acceptance criteria for the phase stories.
- Driving FEAT / CON / INV / CONR trace IDs.
- API contract, data model, UI state, privacy, and testing questions.
- Dependencies and what can run in parallel after the review.
- Blockers that must be resolved before implementation stories move to `in_progress`.

Mark the review `done` only when the phase is sufficiently detailed for implementation. If details are missing, keep it `blocked` or `in_progress` and name the missing decisions.

## Story Sizing Heuristics

Good stories are independently understandable and testable. Split stories that combine unrelated API, UI, data migration, and analytics behavior unless they must ship together.

Prefer dependency language over vague sequencing:

- `Depends on: US-001`
- `Can run in parallel: Yes, with US-004 after CONR-dqs-api-001 stabilizes`
- `Can run in parallel: No, requires decision record lifecycle`

## Status Rules

- `planned`: not ready to start or not selected yet.
- `ready`: knowledge/contracts are sufficient and dependencies are done.
- `in_progress`: currently being implemented or actively designed.
- `blocked`: cannot proceed without a named dependency or decision.
- `done`: implementation and relevant validation completed.
- `deferred`: intentionally moved out of the current scope.

Only mark `done` when the implementation, tests, and required KFS updates are complete for that slice.

## Final Check

Before finishing a tracked implementation turn:

- Plan status reflects the latest work.
- Progress update explains material changes.
- Active blockers are named.
- Multi-repo plans name repo ownership and per-repo validation status.
- Trace IDs still exist in `knowledge/catalog.yml`.
- KFS validation passes when knowledge changed.

## Change log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 1.0 | 2026-06-05 | approved | — | Initial lightweight project-management skill for KFS. |
| 1.1 | 2026-06-05 | approved | 1.0 | Added phase-start requirements-review gates. |
| 1.2 | 2026-06-07 | approved | 1.1 | Added multi-repo ownership and validation tracking expectations. |
