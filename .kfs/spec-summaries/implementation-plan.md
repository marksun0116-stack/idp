# Implementation plan (agent summary)

**Full spec:** `.kfs/specs/implementation_plan_spec.md` · **Runbook:** [manage-implementation-plan.md](../agent/runbooks/manage-implementation-plan.md)

**When:** FEAT **L2+**, knowledge wave merged, docs aligned.

**Where:** **`knowledge/plans/PLAN-{slice}-001.yml`** (filename = stable **`PLAN-*` ID**)

**Optional summary:** **`docs/plans/{yyyymmdd}_{topic}_implementation_plan.md`** — date prefix for browsing; links authoritative YAML.

```text
PLAN-* → PH-* → ST-*  (DAG depends_on; sequential | parallel_ok + touch_paths)
```

| Progress | Lives in |
| --- | --- |
| CON/INV/DEC/CONR | Primitive **`status`** (Knowledge PR) |
| FEAT driver | **`proposed`** → **`accepted`** when plan **closed** |
| Day-to-day | Story **`status`** |

**Closed phase/plan** = frozen; new scope → new **PH/ST** or **PLAN-***.

```bash
.kfs/validate/validate-implementation-plan.sh
```
