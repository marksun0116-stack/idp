# Implementation Plan Specification

This document defines the lightweight project-management artifact used by KFS when work needs ordered phases, user stories, dependencies, and progress tracking.

## Purpose

An implementation plan translates approved product intent and knowledge primitives into executable slices. It is not a replacement for PRD, architecture, HLD, or `knowledge/`; it is the coordination layer that answers:

- What should be built first?
- Which stories depend on other stories?
- Which stories can proceed in parallel?
- What is the current progress?
- Which FEAT / CON / INV / CONR IDs constrain the work?
- What phase-specific requirements must be reviewed before implementation starts?
- Which repository owns each slice when work spans multiple repositories?
- What validation has passed or failed for each material slice?

## Location

Use the conventional path:

```text
docs/implementation/<product-or-epic>_implementation_plan.md
```

## Required Metadata

Start each plan with YAML front matter:

```yaml
---
plan_id: PLAN-example-001
title: "Example Implementation Plan"
status: draft
owner: "Team or person"
last_updated: YYYY-MM-DD
version: "0.1"
linked_prds:
  - PRD-example-001
linked_features:
  - FEAT-example-001
workspace_repos:
  - name: example-service
    path: /path/to/example-service
    role: shared-service
---
```

## Required Sections

Use these headings in order:

1. `## 1. Scope`
2. `## 2. Planning Principles`
3. `## 3. Workspace Repositories`
4. `## 4. Phase Overview`
5. `## 5. User Story Plan`
6. `## 6. Parallelization Notes`
7. `## 7. Progress Updates`
8. `## 8. Validation Log`
9. `## 9. Open Questions`
10. `## Change log`

`## Change log` must be the final heading.

For single-repo plans, `## 3. Workspace Repositories` may state "Single repo" and name the repo. For multi-repo plans, it is required and must list every affected repo.

## Workspace Repositories

Represent repository ownership as a table:

| Repo | Path | Role | Owns | Notes |
| --- | --- | --- | --- | --- |
| product-platform | `/path/to/product-platform` | program repo, consumer repo | Workspace plan, shared contracts, local adapter | Coordinates cross-repo KFS |

Common roles:

- `program repo`
- `governance repo`
- `service/application repo`
- `shared-service repo`
- `consumer repo`

## Phase Overview

Represent phases as a table:

| Phase | Goal | Status | Owning repo(s) | Depends on | Parallelizable with | Trace |
| --- | --- | --- | --- | --- | --- | --- |
| Phase 1 | Short goal | planned | app-repo | None | Phase 2 discovery | FEAT-example-001 |

Status vocabulary:

- `planned`
- `ready`
- `in_progress`
- `blocked`
- `done`
- `deferred`

## User Story Plan

Represent stories as a table:

| Story ID | Phase | Repo(s) | User Story | Status | Depends on | Can run in parallel? | Trace | Validation | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| US-001 | Phase 1 | app-repo | As a user, I want... | planned | None | Yes, with US-002 | FEAT-example-001, CON-example-001 | Not run | Short note |

Each implementation phase should begin with a requirements-review row before implementation stories. Use `RR-###` IDs for review gates. A review gate should confirm phase-level scope, detailed acceptance criteria, trace IDs, dependencies, open questions, and parallelization boundaries.

Example:

| Story ID | Phase | Repo(s) | User Story | Status | Depends on | Can run in parallel? | Trace | Validation | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| RR-001 | Phase 1 | app-repo | Review Phase 1 requirements before implementation starts. | planned | Phase 0 | No | FEAT-example-001 | N/A | Drill into acceptance criteria, contracts, tests, and blockers. |

Keep implementation stories implementation-sized. Split any story that is too broad to test or review coherently.

## Progress Updates

Add a dated entry whenever a phase or story starts, completes, is blocked, is deferred, or changes dependency/order:

```markdown
- YYYY-MM-DD: Set US-001 to `in_progress`; blocked US-003 on contract finalization.
```

Do not rewrite history except to correct obvious mistakes; add a new update instead.

## Validation Log

Add a dated validation entry for material checkpoints:

```markdown
- YYYY-MM-DD: `python3 .knowledge-first-system/scripts/validate_knowledge.py` in `program-repo` failed: malformed YAML in CON-example-001.
- YYYY-MM-DD: `mvn test -q` in `shared-service` passed.
```

When validation cannot be run, state why and which repo is affected. Do not mark a story `done` if required validation is missing unless the plan explicitly records an accepted exception.

## Metadata Updates

When editing a plan, update metadata and append a change log row:

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 0.1 | YYYY-MM-DD | draft | — | Initial plan. |

## Change log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 1.0 | 2026-06-05 | approved | — | Initial implementation plan artifact specification. |
| 1.1 | 2026-06-05 | approved | 1.0 | Added phase-start requirements-review gates. |
| 1.2 | 2026-06-07 | approved | 1.1 | Added generic multi-repo ownership fields, workspace repository section, and validation log requirements. |
