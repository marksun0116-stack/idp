# Knowledge-first system

This package ships the **knowledge-first enforcement layer** for Cursor-based development.

## What's in here

```
.knowledge-first-system/
  rules/          ← Cursor rules (RULE-001 – RULE-013, LEGACY-001)
  skills/         ← Workflow skills (implement, merge, validate, modernize)
  README.md       ← This file
  specs/          ← Official specs for every artifact layer

knowledge/
  catalog.yml     ← Authoritative ID → path index (keep updated)
  constraints/    ← CON-* files
  invariants/     ← INV-* files
  decisions/      ← DEC-* files
  contracts/      ← CONR-* files
  specs/          ← FEAT-* files
```

## Conventional product docs layout (create when you add product scope)

```
docs/
  prd/            ← Product requirements (prd_spec.md)
  architecture/   ← Architecture intent (architecture_intent_spec.md)
  design/         ← High-level design / HLD (design_spec.md)
  test/           ← Test plans (test_strategy_spec.md)
```

These folders are **not** created here — add them when your program captures product prose in-repo.

## Primary entry points

| Purpose | File |
| --- | --- |
| Operations, layout, scenarios, checklists | `.knowledge-first-system/use_guide.md` *(add when available)* |
| Strategy, adoption, impact | `.knowledge-first-system/executive_overview.md` *(add when available)* |
| Knowledge model (layers, primitives, graph, validation) | `.knowledge-first-system/specs/knowledge_model_spec.md` |
| Architecture intent documents | `.knowledge-first-system/specs/architecture_intent_spec.md` |
| High-level design documents | `.knowledge-first-system/specs/design_spec.md` |
| PRD documents | `.knowledge-first-system/specs/prd_spec.md` |
| Test strategy | `.knowledge-first-system/specs/test_strategy_spec.md` |
| Infrastructure | `.knowledge-first-system/specs/infrastructure_spec.md` |
| CI/CD | `.knowledge-first-system/specs/cicd_spec.md` |

## Rule ID map (quick reference)

| ID | Name | Rule file |
| --- | --- | --- |
| RULE-001 | knowledge-first-development | `implementation-constrain-validate-test.mdc` |
| RULE-002–006 | primitive schemas | `knowledge-primitives.mdc` |
| RULE-007 | HLD/design consistency | `traceability-enforcement.mdc` |
| RULE-008 | traceability | `traceability-enforcement.mdc` |
| RULE-009 | architecture compliance | `architecture-compliance.mdc` |
| RULE-010 | invariant-oriented tests | `implementation-constrain-validate-test.mdc` |
| RULE-011 | official knowledge model spec | `official-knowledge-architecture.mdc` |
| RULE-012 | no authoritative rules only in docs | `docs-knowledge-boundary.mdc` |
| RULE-013 | new project PRD + architecture intent gate | `new-project-intent-gate.mdc` |
| LEGACY-001 | slice-first legacy modernization | `legacy-modernization-slices.mdc` |

## Change log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 1.0 | 2026-05-14 | approved | — | Initial transfer package. |
