# Knowledge-first (KFS)

This repository uses the **KFS agent core** under **`.kfs/`**.

## Bootstrap

1. **Router:** [`.kfs/INDEX.md`](.kfs/INDEX.md)
2. **Scenarios:** [`.kfs/agent/START.md`](.kfs/agent/START.md)
3. **Runbooks:** [`.kfs/agent/runbooks/`](.kfs/agent/runbooks/)
4. **Constraints:** [`.kfs/constraints/`](.kfs/constraints/)
5. **Specs:** [`.kfs/spec-summaries/`](.kfs/spec-summaries/) · full: [`.kfs/specs/`](.kfs/specs/)
6. **Validate:** `.kfs/validate/validate-knowledge-graph.sh`
7. **Conflicts:** stop and resolve in `knowledge/`
8. **Do not** bulk-load human onboarding prose outside `.kfs/` unless the user asks

## Merge order

Knowledge PR before implementation PR. Both target active **`feature/*`** (from **`dev`**) — never **`main`**. Product truth: `knowledge/` + `catalog.yml`. Branch model: **`.kfs/specs/cicd_spec.md`** §2.1.

## Install / upgrade adapters

```bash
./.kfs/install.sh              # Cursor (default)
./.kfs/install.sh --tool all   # all shipped adapters
./.kfs/install.sh --check      # verify install ledger
```
