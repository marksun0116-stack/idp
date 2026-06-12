# Modernization via new repository — specification

**Major rework** in a **new repository**; **legacy** keeps as-is baseline from [reverse-engineer-baseline](../agent/runbooks/reverse-engineer-baseline.md). Link with **lineage**; do not host current + target truth in one repo.

**Runbook:** [bootstrap-modernization-repo.md](../agent/runbooks/bootstrap-modernization-repo.md)

---

## 1. Two-repo model

| Repo | Role |
| --- | --- |
| **Legacy** | Production today; baseline **`docs/`** + **`knowledge/`**; incremental fixes |
| **New** | Target PRD delta, architecture intent, **`knowledge/`**, new **`src/`** |

## 2. Prerequisites (legacy)

Reviewed baseline on a **pinned ref** (tag/commit). Human approval that work is major rework — not in-place enhancement. Legacy does **not** get target architecture or rewrite **FEAT**.

## 3. Lineage (`docs/modernization/lineage.yml`)

Record legacy **repository**, **ref**, paths to baseline **prd/architecture**, **`knowledge/catalog.yml`**, **`MOD-*`** program id. Optional read-only **`references/legacy/`** snapshot — trace only; legacy repo remains authority when online.

## 4. New-repo documents

**PRD:** **Retained / Removed / Added / Changed** — explicit removals; **`linked_feature_specs`** → target **FEAT** (product what).

**Architecture intent:** target topology; **standalone** deployable; **blue/green** cutover (**DEC**); **non-goals** (no strangler).

**HLD / test plans:** under **`docs/design/`**, **`docs/test/`** in new repo only.

## 5. Knowledge + runtime rules

| Kind | New repo |
| --- | --- |
| **DEC** | Stack, repo split, **blue/green**, **no legacy runtime dependency**, migration policy |
| **FEAT** | Target capabilities — re-specify; link lineage |
| **CON/INV** | Target rules; legacy trace in metadata when replacing |
| **CONR** | **New** boundaries only — not legacy shims |

**Runtime independence (non-negotiable):** no legacy API calls, shared runtime libs, or strangler routing in dev, test, or steady-state prod. **Lineage** = documentation only.

**Cutover:** deploy new app in parallel → validate → switch traffic → retire legacy. Rollback = traffic switch. One-time migration jobs OK if documented in **DEC** — no ongoing prod dependency.

## 6. Legacy during program

Allowed: incremental fixes; as-is **`knowledge/`** updates when behavior changes; new tags for lineage.

Not allowed: target architecture in legacy; rewrite **FEAT** on legacy **`src/`**; dual current/target trees; runtime integration for new app.

Re-pin **`lineage.yml`** when baseline moves materially.

## 7. When to use

| New repo | Legacy repo only |
| --- | --- |
| Major stack change, new deployable, long rewrite | Bug fixes, small features, in-place refactors |

## 8. Validation

RULE-013 + **`lineage.yml`**; **DEC** states no legacy runtime + blue/green; **`.kfs/validate/validate-knowledge-graph.sh`**; PR review: **Removed** items approved; no proxy **CONR**.

---

## 9. Change log

| Revision | Date | Status | Notes |
| --- | --- | --- | --- |
| 1.0 | 2026-05-22 | approved | Initial |
| 1.1 | 2026-05-22 | approved | Runtime independence, blue/green |
| 1.2 | 2026-05-22 | approved | Compacted |
