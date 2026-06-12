# Merge knowledge first

**Model:** `.kfs/spec-summaries/knowledge-model.md` · **Depth:** `.kfs/spec-summaries/primitive-depth.md`

## RULE-013 gate

Net-new scope needs **`docs/prd/`** + **`docs/architecture/`**. Existing app missing them → **[reverse-engineer-baseline.md](reverse-engineer-baseline.md)** first.

## When

Changes under **`knowledge/`** / catalog; or implementation PR needs merged primitives.

## Merge order

1. **Knowledge PR** (behavior or enforceable design) → target active **`feature/*`** (never **`main`**)
2. **Implementation PR** (or one combined PR with explicit trace) → target same **`feature/*`**
3. Per **ST-*** when plan active and **`depends_on`** satisfied

**Branch model:** Cut **`feature/*` from `dev`** → developer PRs into **`feature/*`** → tested **`feature/*` merges to `main`** → **tag `main`** → cut **`release/*` from tag**. See **`.kfs/specs/cicd_spec.md`** §2.1.

## Knowledge PR

- **Base branch:** active **`feature/*`** (cut from **`dev`**) — not **`main`**
- List primitive IDs; update **`catalog.yml`**; resolve **FEAT** bindings
- **FEAT** title = user-visible **what**; stack/migration → **DEC** / HLD
- Lift PRD must/must-not → **CON/INV**; doc edits → Change log
- Child **FEAT**: **≥3** observable **`success_criteria`**; **L2+** before implement; **CONR** at API seams
- Optional: **`PLAN-*`** when starting code ([manage-implementation-plan.md](manage-implementation-plan.md))

## Implementation PR

- **Base branch:** same active **`feature/*`** — not **`main`**
- Driving child **FEAT** at **L2+** (`implementation_readiness: ready` or sign-off)
- Do not re-litigate merged intent unless defect upgrades knowledge

## Releases

**FEAT** status, **CONR** versions, doc metadata aligned on ship.

```bash
.kfs/validate/validate-knowledge-graph.sh
```
