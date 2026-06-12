# Manage implementation plan

**Spec:** `.kfs/spec-summaries/implementation-plan.md` · **Depth:** `.kfs/spec-summaries/primitive-depth.md`

PM layer for delivery: **`PLAN-*`** → phases **`PH-*`** → stories **`ST-*`**. Coordinates work; does **not** replace **`knowledge/`** rules (RULE-012).

## When

Knowledge + docs ready for code/tests; need parallel lanes or phase closure with immutability.

## Readiness (before `status: active`)

- Driving child **FEAT** at **L2+** (`implementation_readiness: ready`)
- Shared **CON/INV/CONR** merged (Knowledge PR)
- **`docs/prd/`**, **`docs/architecture/`** aligned; HLD when **L3**
- `.kfs/validate/validate-knowledge-graph.sh` — no blocking errors

## Workflow

1. Copy **`.kfs/templates/implementation-plan.yml.stub`** → **`knowledge/plans/PLAN-{slice}-001.yml`** (filename = **`PLAN-*` id** — no date prefix)
2. Set **`cycle_feat`**, phases, stories; optional **`docs/plans/{yyyymmdd}_{topic}_implementation_plan.md`** summary linking **`PLAN-*`** (see **`implementation_plan_spec.md`** §3)
3. **Phases:** DAG via **`depends_on`**; **`closed`** = frozen — append new **`PH-*`** for more scope
4. **Stories:** **`depends_on`** **`ST-*`/`PH-*`**; **`execution`**: `sequential` | `parallel_ok`
5. **Parallel:** disjoint **`isolation.touch_paths`**; integration story after parallel wave
6. Validate → **`status: active`** → implement per **[implement-with-primitives.md](implement-with-primitives.md)**; PR cites **`ST-*`**
7. Close phase when all stories **`done`/`dropped`**; close plan when cycle ships → driving **FEAT** **`accepted`**

## Rules

| Topic | Rule |
| --- | --- |
| New must/must-not | **CON/INV** Knowledge PR — not plan prose |
| Progress | Story **`status`** — not primitive `implemented` |
| Parallel implement | Knowledge wave merged; non-overlapping **`touch_paths`** |
| Closed phase | **Forbidden** to edit — new **`PH-*`/`ST-*`** only |

```bash
.kfs/validate/validate-implementation-plan.sh
.kfs/validate/validate-knowledge-graph.sh
```
