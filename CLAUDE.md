# Claude Code Instructions: Knowledge-First System

This repository uses the Knowledge-First System (KFS). Claude Code must follow the same KFS process described in `AGENTS.md`; agent-specific tooling can differ, but the order of knowledge, docs, implementation, tests, and validation must not diverge.

## Required Workflow

Before changing implementation code, inspect and align the relevant knowledge and docs.

1. Identify the driving knowledge primitive:
   - Start from `knowledge/catalog.yml`.
   - Load the relevant `FEAT-*` file under `knowledge/specs/`.
   - Load its linked `CON-*`, `INV-*`, `DEC-*`, and `CONR-*` primitives.
2. If the requested change introduces or changes product behavior, architecture, contracts, constraints, invariants, or user-visible scope, update knowledge first:
   - Add or edit primitives under `knowledge/`.
   - Update `knowledge/catalog.yml` for new or renamed primitives.
   - Keep `FEAT-*` bindings pointed at existing catalog IDs.
3. Update product docs when the change affects documented intent or shipped behavior:
   - PRDs: `docs/prd/`
   - Architecture intent: `docs/architecture/`
   - Design/HLD: `docs/design/`
   - Implementation plans: `docs/implementation/`
   - Test plans, when present: `docs/test/`
   - Keep the final heading as `## Change log` or `## Version history` and refresh the revision table when editing governed docs.
4. For multi-phase, multi-story, or multi-repo work, create or update the implementation plan before broad implementation:
   - Use `docs/implementation/`.
   - Track phases, user stories, order, owning repo(s), dependencies, parallelization, status, validation, and progress notes.
   - Update the plan when a story starts, finishes, is blocked, is deferred, is split, is merged, changes order, or changes repo ownership.
   - Keep a validation log for material checkpoints.
5. Only then change implementation code in `src/` or platform-specific implementation folders.
6. Add or update tests so behavior remains traceable to `FEAT-*`, `CON-*`, `INV-*`, or `CONR-*` IDs. Prefer explicit test names or short comments with the relevant primitive ID.
7. Validate before finishing:
   - Run `python3 .knowledge-first-system/scripts/validate_knowledge.py`.
   - Run the relevant backend, frontend, or mobile tests for the touched area.
   - If a validation or test command cannot be run, report that clearly.

## Implementation Plans

Implementation plans are the lightweight project-management artifact for KFS. Use them whenever work spans multiple stories, phases, or repositories.

- Location: `docs/implementation/<product-or-epic>_implementation_plan.md`.
- Required shape: `.knowledge-first-system/specs/implementation_plan_spec.md`.
- Required rule: `.knowledge-first-system/rules/implementation-plan-management.mdc`.
- Status vocabulary: `planned`, `ready`, `in_progress`, `blocked`, `done`, `deferred`.
- Mark a story `done` only after implementation, required knowledge/docs, and relevant validation are complete or an accepted exception is recorded.
- Add progress updates rather than rewriting history.

## Multi-Repo Workspaces

This workspace may include multiple repositories. Follow `.knowledge-first-system/rules/multi-repo-workspace-management.mdc`.

- Identify repo roles before changing code: program repo, service/application repo, shared-service repo, consumer repo.
- Cross-repo decisions, shared constraints, shared contracts, and workspace implementation plans belong in the program repo unless a separate governance repo is explicitly named.
- Repo-local behavior belongs in that repo's own `knowledge/` and `docs/`.
- Shared service contracts must have one owner. Consumers may reference or mirror the owning contract/version, but must not silently fork it.
- Workspace implementation plans must list affected repos, owning repo(s) per story, cross-repo dependencies, and per-repo validation.
- If a participating repo lacks KFS files, record that in the plan and keep workspace-level knowledge in the program repo until the repo is brought under KFS.

## Stop Conditions

Stop and resolve knowledge/docs before code when:

- There is no suitable `FEAT-*` for net-new behavior.
- A requested implementation would violate an active `CON-*`, `INV-*`, or `CONR-*`.
- A contract/API shape changes without a matching `CONR-*` update.
- A user-facing feature changes without matching PRD/design/architecture updates where those docs cover the area.
- Multi-story implementation proceeds without a current `docs/implementation/` plan.
- Multi-repo work proceeds without a named contract owner, repo ownership, and cross-repo validation expectations in the implementation plan.
- Traceability would be broken by deleting or renaming a primitive without updating references.

## KFS Source Files

Use these files as the authoritative process references:

- `.knowledge-first-system/rules/implementation-constrain-validate-test.mdc`
- `.knowledge-first-system/rules/knowledge-primitives.mdc`
- `.knowledge-first-system/rules/traceability-enforcement.mdc`
- `.knowledge-first-system/rules/architecture-compliance.mdc`
- `.knowledge-first-system/rules/new-project-intent-gate.mdc`
- `.knowledge-first-system/rules/implementation-plan-management.mdc`
- `.knowledge-first-system/rules/multi-repo-workspace-management.mdc`
- `.knowledge-first-system/skills/implement-with-primitives/SKILL.md`
- `.knowledge-first-system/skills/manage-implementation-plan/SKILL.md`
- `.knowledge-first-system/skills/validate-knowledge-graph/SKILL.md`
- `.knowledge-first-system/skills/merge-knowledge-first/SKILL.md`
- `.knowledge-first-system/specs/knowledge_model_spec.md`
- `.knowledge-first-system/specs/implementation_plan_spec.md`

If KFS instructions conflict with generic implementation convenience, follow KFS unless the user explicitly overrides it for that task.

## Agent Parity

`AGENTS.md` and `CLAUDE.md` must describe the same KFS process. If one is updated for process, update the other or make it point to the updated canonical KFS rule/spec.
