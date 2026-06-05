---
name: modernize-legacy-with-knowledge
description: >-
  Reverse-engineers code-only legacy repositories into slice-based current-state
  PRD, architecture intent, HLD, primitives, and regression tests, then guides
  modernization to a target architecture. Use when the user mentions legacy
  modernization, reverse engineering docs from code, strangler migration,
  regression-first modernization, or applying knowledge-first to legacy systems.
---

# Modernize legacy with knowledge

Use this workflow when a repository has working legacy code but weak or missing documentation, or when the goal is to migrate to a different architecture or implementation.

**Human procedure and examples:** **`.knowledge-first-system/use_guide.md`** (Scenario 3). **Adoption context:** **`.knowledge-first-system/executive_overview.md`** (legacy adoption and slice-first governance).

## 1. Inventory slices

Build a shallow map before deep documentation:

- Identify slices from routes/controllers, UI pages, services, jobs, database tables, integrations, tests, and recent git churn.
- Rank slices by change frequency, roadmap value, defect history, operational risk, coupling, and testability.
- Select one high-change slice first; avoid whole-system archaeology.

Record each slice with entry points, core modules, data, external systems, owners if known, risk, and open questions.

## 2. Reverse-engineer current state

For the selected slice, derive docs from code and tests:

- **Current PRD** — actors, workflows, observable behavior, permissions, inputs/outputs, error cases, limitations.
- **Current architecture intent** — runtime shape, data ownership, integration boundaries, consistency assumptions, security/tenancy boundaries, known legacy compromises.
- **Current HLD** — entry points, components, data model, request/job/event flows, state transitions, error handling, test coverage, gaps.

Mark facts as **observed**, **inferred**, or **open question**. Do not turn guesses into authoritative primitives.

## 3. Define target state

For modernization, write the target separately:

- Target PRD: intended user/system behavior and intentional behavior changes.
- Target architecture intent: new boundaries, deployment shape, data ownership, integration model, scale/security goals.
- Target HLD: target components, contracts, migration boundary, data flow, rollout and retirement plan.

Capture migration choices as **DEC-***, hard requirements as **CON-***, always-true rules as **INV-***, and boundary schemas as **CONR-***.

## 4. Add regression-first safety

Before replacing code in the slice (TDD-style: **tests first**, then change implementation):

- Add characterization tests for behavior that must be preserved or understood.
- Add contract tests for APIs, events, data formats, and permissions at the migration boundary.
- Add invariant tests for rules that must hold in both legacy and target implementations.
- Classify legacy behaviors as **preserve**, **fix**, **clarify**, or **drop**; ask humans for unresolved product decisions.

Tests that encode product rules should link to **INV / CON / CONR / FEAT** where practical.

## 5. Modernize by boundary

Prefer strangler-style migration unless a human explicitly chooses a big-bang rewrite:

1. Place or identify a boundary around the legacy slice.
2. Define **CONR-*** contracts for that boundary.
3. Implement the target slice behind the same contract.
4. Compare old and new behavior using regression and contract tests.
5. Route reads, writes, jobs, or traffic incrementally when feasible.
6. Retire old paths and update product narrative (conventional **`docs/`**) and primitives when the slice is migrated.

## 6. Validate and repeat

- Validate `knowledge/catalog.yml` and primitive links after each knowledge change.
- Keep current-state docs, target-state docs, and primitives consistent; conflicts must be resolved in `knowledge/` first.
- Expand enforcement to the next slice only after the current slice has useful docs, contracts, and regression coverage.

Standing rule: `.knowledge-first-system/rules/legacy-modernization-slices.mdc`.

---

## Change log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 1.1 | 2026-05-06 | approved | — | Prior release (Document version footer). |
| 1.2 | 2026-05-12 | approved | — | Terminal change log added. |
