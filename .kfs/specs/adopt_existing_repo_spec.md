# Adopt an existing repository — specification

Onboard a **running application** for as-is support and incremental features. After baseline, same workflow as greenfield. **No** current/target doc split.

**Runbook:** [reverse-engineer-baseline.md](../agent/runbooks/reverse-engineer-baseline.md)

---

## 1. Pattern

| Step | Outcome |
| --- | --- |
| Reverse-engineer baseline | **`docs/`** + **`knowledge/`** for what the system does today |
| Validate | Catalog + depth checks |
| Incremental work | Standard KFS (Knowledge PR → plan → implement) |

Optional **`docs/assessment/tech_debt_register.md`** via [tech_debt_discovery_spec.md](tech_debt_discovery_spec.md) Mode A — prioritization only.

## 2. Evidence

| Source | Use |
| --- | --- |
| **`src/`**, **`tests/`** | Primary behavior and rules |
| CI / deploy config | Runtime shape |
| **`references/`** | Supplemental; cite; **code/tests win** on conflict |

Label uncertainty: **(observed)**, **(inferred)**, **(open question)**. No **`accepted`** primitives from guesswork.

## 3. Baseline outputs

**`docs/prd/`**, **`docs/architecture/`**, **`docs/design/`** — link primitives; Change log on each doc.

**`knowledge/`:** **FEAT** = capabilities (what, not migration titles); **CON/INV/CONR** from evidence; **DEC** for choices in production. **`proposed`** until review → **`accepted`** on merge.

## 4. Slices

Inventory lightly in **`docs/assessment/slice_inventory.md`** (optional); **deepen one slice first** (change frequency, defects, roadmap). Expand slice-by-slice — no full archaeology gate.

## 5. After baseline

[merge-knowledge-first.md](../agent/runbooks/merge-knowledge-first.md) → [manage-implementation-plan.md](../agent/runbooks/manage-implementation-plan.md) → [implement-with-primitives.md](../agent/runbooks/implement-with-primitives.md)

## 6. Do not

- Parallel current/target trees for routine adoption
- **FEAT** titles for stack rewrites when describing existing behavior
- Skip **`knowledge/`** because code “already works”
- Major rework in same repo → [modernization_new_repo_spec.md](modernization_new_repo_spec.md)

## 7. Major rework

| Repo | Role |
| --- | --- |
| **Legacy** | As-is baseline only |
| **New** | Target PRD delta, architecture, **`knowledge/`**, **`src/`** — [bootstrap-modernization-repo.md](../agent/runbooks/bootstrap-modernization-repo.md) |

---

## 8. Change log

| Revision | Date | Status | Notes |
| --- | --- | --- | --- |
| 1.0 | 2026-05-22 | approved | Initial |
| 1.1 | 2026-05-22 | approved | Two-repo pointer |
| 1.2 | 2026-05-22 | approved | Compacted |
