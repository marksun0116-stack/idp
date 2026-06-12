# Adopt existing repo (agent summary)

**Full spec:** `.kfs/specs/adopt_existing_repo_spec.md` · **Runbook:** [reverse-engineer-baseline.md](../agent/runbooks/reverse-engineer-baseline.md)

Support existing app + incremental features — **no** current/target split.

```text
src/ + tests/ [+ references/] → docs/ + knowledge/ → validate → greenfield flow
```

| Source | Role |
| --- | --- |
| **`src/`**, **`tests/`** | Primary evidence |
| **`references/`** | Supplemental; code wins on conflict |

Outputs: **`docs/prd|architecture|design`**, product-shaped **FEAT**, evidenced **CON/INV/CONR**. Label **(observed)|(inferred)|(open question)**.

**Major rework:** legacy baseline here → [modernization-new-repo.md](modernization-new-repo.md) in **new repo**.
