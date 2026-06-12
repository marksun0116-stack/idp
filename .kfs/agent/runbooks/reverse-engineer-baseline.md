# Reverse-engineer baseline (adopt existing app)

**Spec:** `.kfs/spec-summaries/adopt-existing-repo.md` · **Depth:** `.kfs/spec-summaries/primitive-depth.md`

Build **`docs/`** + **`knowledge/`** from existing code so incremental work matches greenfield KFS — **one** doc tree, no current/target split.

## When

First KFS on a running app; ongoing support and small enhancements. **`references/`** = read-only input.

**Not this:** major rework → legacy baseline only; **[bootstrap-modernization-repo.md](bootstrap-modernization-repo.md)** in a **new repo**.

## Steps

1. **Confirm** as-is + incremental scope (whole system vs one slice first)
2. **Inventory** capabilities from **`src/`**, **`tests/`**, config — tag **(observed)**, **(inferred)**, **(open question)**
3. **Draft `docs/`** — **`prd/`**, **`architecture/`**, **`design/`** (first slice); cite **`references/`**; terminal Change log
4. **Knowledge PR** — **FEAT** = product **what**; **CON/INV/CONR** from evidence; **DEC** only for choices in production; **`catalog.yml`**; validate
5. **Merge baseline** — RULE-013 satisfied; list primitive IDs; document open questions

## After baseline

| Task | Runbook |
| --- | --- |
| New/changed behavior | [merge-knowledge-first.md](merge-knowledge-first.md) |
| Delivery plan | [manage-implementation-plan.md](manage-implementation-plan.md) |
| Code/tests | [implement-with-primitives.md](implement-with-primitives.md) |

## Quality bar

- No **`accepted`** primitives from guesswork; code/tests beat stale **`references/`**
- Must/must-not in **CON/INV**, not docs-only (RULE-012)
- REST **CONR** includes **endpoints[]** where applicable

## Escalation

Major modernization → **[bootstrap-modernization-repo.md](bootstrap-modernization-repo.md)**. Unknown behavior → open question + characterization test before **`accepted`**.
