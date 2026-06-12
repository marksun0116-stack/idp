# KFS scenarios — pick a runbook

| Scenario | When | Runbook |
| --- | --- | --- |
| **New project** | Greenfield; first FEAT and src | [merge-knowledge-first.md](runbooks/merge-knowledge-first.md) → RULE-013 gate; then [implement-with-primitives.md](runbooks/implement-with-primitives.md) |
| **Adopt existing app** | Running codebase; first KFS baseline from code/tests | [reverse-engineer-baseline.md](runbooks/reverse-engineer-baseline.md) → then same as **Maintain / enhance** |
| **Major modernization** | New repo for rework; legacy keeps as-is baseline | Legacy: [reverse-engineer-baseline.md](runbooks/reverse-engineer-baseline.md) · New: [bootstrap-modernization-repo.md](runbooks/bootstrap-modernization-repo.md) |
| **Maintain / enhance** | Baseline exists; feature or defect | [merge-knowledge-first.md](runbooks/merge-knowledge-first.md) → [implement-with-primitives.md](runbooks/implement-with-primitives.md) |
| **Tech debt pass** | Optional prioritization register (as-is) | [discover-tech-debt.md](runbooks/discover-tech-debt.md) |
| **Knowledge PR only** | Adding/changing primitives or catalog | [merge-knowledge-first.md](runbooks/merge-knowledge-first.md) + [validate-knowledge-graph.md](runbooks/validate-knowledge-graph.md) |
| **Plan delivery cycle** | Knowledge/docs ready; split implement work | [manage-implementation-plan.md](runbooks/manage-implementation-plan.md) → then [implement-with-primitives.md](runbooks/implement-with-primitives.md) per story |

**Install tool adapters:** `./.kfs/install.sh` (default: Cursor).

**Human procedures:** see scenario table above and [agent/runbooks/](runbooks/).
