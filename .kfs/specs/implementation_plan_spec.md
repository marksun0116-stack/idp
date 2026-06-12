# Implementation plan — specification

Phases (**`PH-*`**) and implementation stories (**`ST-*`**) sequence code/test work. Plans coordinate delivery; enforceable rules stay in **`knowledge/`** (RULE-012).

**Runbook:** [manage-implementation-plan.md](../agent/runbooks/manage-implementation-plan.md) · **Model:** [knowledge_model_spec.md](knowledge_model_spec.md) §4.3, §5.5–5.6

---

## 1. Role

| Artifact | Role |
| --- | --- |
| **`knowledge/`** | What must be true (CON, INV, DEC, CONR, FEAT) |
| **`docs/`** | PRD, architecture, HLD, test plans |
| **Plan** | How work is sequenced, staffed, tracked |

## 2. When to create

| Gate | Requirement |
| --- | --- |
| **FEAT** | Child at **L2+** ([primitive-depth.md](../spec-summaries/primitive-depth.md)) |
| **Docs** | **`docs/prd/`**, **`docs/architecture/`** aligned (RULE-013) |
| **Knowledge** | Shared CON/INV/CONR merged before parallel implement stories |

## 3. Location and naming

| Path | Purpose |
| --- | --- |
| **`knowledge/plans/PLAN-{slice}-001.yml`** | Authoritative plan (stable **`PLAN-*`** ID) |
| **`docs/plans/{yyyymmdd}_{topic}_implementation_plan.md`** | Optional human summary; link **`PLAN-*`** |
| **`.kfs/templates/implementation-plan.yml.stub`** | Starter |

Register in **`knowledge/catalog.yml`**; **`cycle_feat`** must resolve to a **FEAT**.

### 3.1 Authoritative YAML (`knowledge/plans/`)

- **Filename mirrors ID:** `PLAN-EKCC-KFS-100-001.yml` for **`id: PLAN-EKCC-KFS-100-001`** — do **not** embed calendar dates in the YAML filename.
- **Temporal metadata** lives in **`last_updated`** (ISO date), plan **`status`**, and the plan **change log** when you maintain one in the optional summary.
- **Supersession:** when a cycle closes, set plan **`status: closed`**. New delivery scope → increment sequence (**`PLAN-{slice}-002`**) or append **`PH-*`/`ST-*`** to an **`active`** plan — not a date-prefixed rename.
- **Closed plans are frozen** — do not edit story rows in place; append a new phase/story or open a new **`PLAN-*`**.

### 3.2 Optional human summary (`docs/plans/`)

- **Recommended pattern:** **`{yyyymmdd}_{topic}_implementation_plan.md`** where **`yyyymmdd`** is the **creation or last substantive revision** date (e.g. `20260611_ekcc_kfs_1_0_0_beta_upgrade_implementation_plan.md`).
- The summary **links** the authoritative **`PLAN-*`** YAML; PRs and **`catalog.yml`** cite the **`PLAN-*` ID**, not the markdown filename.
- **Avoid renaming** an in-flight summary unless you update **`FEAT-*` `references.plan_summary`** (and any doc backlinks) in the same PR.
- Multiple summaries for the same slice over time are allowed (each with its own date prefix); only one authoritative **`PLAN-*`** should be **`active`** for a given **`cycle_feat`** unless explicitly superseding via Knowledge PR.

## 4. YAML shape

### 4.1 Root

| Field | Required | Description |
| --- | --- | --- |
| **`id`** | yes | `PLAN-*` |
| **`kind`** | yes | `implementation_plan` |
| **`title`**, **`status`** | yes | `draft` \| `active` \| `closed` |
| **`cycle_feat`** | yes | Driving **FEAT** |
| **`plan_format_version`** | yes | `"1"` |
| **`last_updated`** | yes | ISO date |
| **`phases`**, **`stories`** | yes | See below |
| **`readiness`**, **`owner`**, **`concurrency`** | optional | Gates and caps |

### 4.2 Phases (`PH-*`)

| Field | Description |
| --- | --- |
| **`depends_on`** | **`PH-*`** DAG; start when deps **`closed`** |
| **`default_execution`** | `sequential` \| `parallel_ok` |
| **`status`** | `open` \| `in_progress` \| `closed` |

**`closed`** phase = frozen. New scope → append **PH/ST** only.

### 4.3 Stories (`ST-*`)

| Field | Description |
| --- | --- |
| **`phase_id`**, **`title`**, **`status`** | `not_started` … `done` \| `dropped` |
| **`depends_on`** | **`ST-*`** and/or **`PH-*`** (DAG, no cycles) |
| **`execution`** | `sequential` \| `parallel_ok` |
| **`isolation.touch_paths`** | Required for **`parallel_ok`**; must not overlap among **`in_progress`** stories |
| **`isolation.conr_boundary`** | Optional **CONR** seam |
| **`links`** | **`feat`**, **`primitives[]`**, **`tests[]`**, etc. |

Start **`in_progress`** only when all **`depends_on`** satisfied. After parallel wave: sequential integration story.

## 5. Lifecycle (with primitives)

| Event | Primitive | Plan |
| --- | --- | --- |
| Knowledge PR merges | CON/INV/DEC/CONR **`accepted`** | Knowledge stories **`done`** |
| Cycle active | Driving FEAT **`proposed`** | Plan **`active`** |
| Story completes | Change primitives only via Knowledge PR | Story **`done`** |
| Cycle closes | Driving FEAT **`accepted`** | Plan **`closed`** |

No manual **`status: implemented`** on CON/INV — tests show alignment.

## 6. Validation

```bash
.kfs/validate/validate-implementation-plan.sh [PLAN_PATH ...]
```

**Errors:** bad refs, cycles, **`parallel_ok`** without **`touch_paths`**, **`closed`** phase with open stories.

---

## 7. Change log

| Revision | Date | Status | Notes |
| --- | --- | --- | --- |
| 1.0 | 2026-05-22 | approved | Initial spec |
| 1.1 | 2026-05-22 | approved | Compacted; workflow in runbook |
| 1.2 | 2026-06-11 | approved | §3 naming — stable **`PLAN-*`** YAML vs optional **`yyyymmdd_`** human summary |
