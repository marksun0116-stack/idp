# Knowledge-first (RULE-001)

> Knowledge defines, rules enforce, AI executes, humans approve.

- **Truth:** `knowledge/` + `catalog.yml`; product narrative under `docs/` when present
- **Order:** Knowledge PR before implementation PR
- **Branches:** Cut **`feature/*` from `dev`**; PRs target **`feature/*`** — **never `main`** for developer knowledge or code work; **`feature/*` → `main`** after feature tests; release **tag on `main`**, then **`release/*`** (**`.kfs/specs/cicd_spec.md`** §2.1)
- **Agents:** Follow `.kfs/INDEX.md` and runbooks only — do not bulk-load human onboarding prose outside `.kfs/` unless asked
- **Conflicts:** Stop; resolve in `knowledge/` — not only in code or chat
- **Defects:** Upgrade CON/INV/specs when the rule was wrong or missing

Runbooks: merge-knowledge-first · implement-with-primitives · validate-knowledge-graph
