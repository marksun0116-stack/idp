# Cursor agents — rules vs skills

**KFS agent core:** [`.kfs/INDEX.md`](../.kfs/INDEX.md) · **Install:** `./.kfs/install.sh`

Canonical procedures: **`.kfs/agent/runbooks/`**. Skills under **`.cursor/skills/`** are thin wrappers (source: **`.kfs/adapters/cursor/skills/`**).

Human onboarding (optional): **`kfs-handbook/`** — not default agent context.

## Rules (`.cursor/rules/*.mdc`)

Standing **constraints** — what must hold, sources of truth, globs.

## Skills (`.cursor/skills/*/SKILL.md`)

Task **runbooks** — merge order, validation, implementation workflows.

## Specs (RULE-011)

All normative specs: **`.kfs/specs/`** · agent quick cards: **`.kfs/spec-summaries/`**

| Rules | Skills |
| --- | --- |
| `ai-native-knowledge-core.mdc` | `merge-knowledge-first` |
| `knowledge-primitives.mdc` | `validate-knowledge-graph` |
| `traceability-enforcement.mdc` | `implement-with-primitives` |
| `docs-knowledge-boundary.mdc` | `discover-tech-debt` |
| `architecture-compliance.mdc` | `manage-implementation-plan` |
| `official-knowledge-architecture.mdc` | `reverse-engineer-baseline` |
| `implementation-constrain-validate-test.mdc` | `bootstrap-modernization-repo` |
| `knowledge-validation-scripts.mdc` | — |
| `new-project-intent-gate.mdc` | — |
| `legacy-modernization-slices.mdc` | `modernize-legacy-with-knowledge` |

New guidance: **rule** = standing law; **skill** = procedure for one task type.
