# Codex Instructions: Knowledge-First System

This repository uses the Knowledge-First System (KFS). Treat KFS as the default operating process for all Codex sessions in this repo.

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
4. For multi-phase or multi-story work, create or update the implementation plan before broad implementation:
   - Use `docs/implementation/`.
   - Track phases, user stories, order, dependencies, parallelization, status, and progress notes.
   - Update the plan when a story starts, finishes, is blocked, is deferred, or changes order.
5. Only then change implementation code in `src/` or platform-specific implementation folders.
6. Add or update tests so behavior remains traceable to `FEAT-*`, `CON-*`, `INV-*`, or `CONR-*` IDs. Prefer explicit test names or short comments with the relevant primitive ID.
7. Validate before finishing:
   - Run `python3 .knowledge-first-system/scripts/validate_knowledge.py`.
   - Run the relevant backend, frontend, or mobile tests for the touched area.
   - If a validation or test command cannot be run, report that clearly.

## Stop Conditions

Stop and resolve knowledge/docs before code when:

- There is no suitable `FEAT-*` for net-new behavior.
- A requested implementation would violate an active `CON-*`, `INV-*`, or `CONR-*`.
- A contract/API shape changes without a matching `CONR-*` update.
- A user-facing feature changes without matching PRD/design/architecture updates where those docs cover the area.
- Multi-story implementation proceeds without a current `docs/implementation/` plan.
- Traceability would be broken by deleting or renaming a primitive without updating references.

## KFS Source Files

Use these files as the authoritative process references:

- `.knowledge-first-system/rules/implementation-constrain-validate-test.mdc`
- `.knowledge-first-system/rules/knowledge-primitives.mdc`
- `.knowledge-first-system/rules/traceability-enforcement.mdc`
- `.knowledge-first-system/rules/architecture-compliance.mdc`
- `.knowledge-first-system/rules/new-project-intent-gate.mdc`
- `.knowledge-first-system/rules/implementation-plan-management.mdc`
- `.knowledge-first-system/skills/implement-with-primitives/SKILL.md`
- `.knowledge-first-system/skills/manage-implementation-plan/SKILL.md`
- `.knowledge-first-system/skills/validate-knowledge-graph/SKILL.md`
- `.knowledge-first-system/skills/merge-knowledge-first/SKILL.md`
- `.knowledge-first-system/specs/knowledge_model_spec.md`
- `.knowledge-first-system/specs/implementation_plan_spec.md`

If KFS instructions conflict with generic implementation convenience, follow KFS unless the user explicitly overrides it for that task.

## Area-Specific Instructions

Nested `AGENTS.md` files may add more specific instructions for subprojects. Follow both this root KFS workflow and the nested instructions, with the nested file controlling only area-specific details.
