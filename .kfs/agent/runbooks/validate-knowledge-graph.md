# Validate the knowledge graph

**Model:** `.kfs/spec-summaries/knowledge-model.md` · **Depth:** `.kfs/spec-summaries/primitive-depth.md`

## When

PRs touching **`knowledge/`**; before Knowledge PR merge; after **`catalog.yml`** conflicts. RULE-013: first FEAT needs **`docs/prd/`** + **`docs/architecture/`**.

## Run

```bash
.kfs/validate/validate-knowledge-graph.sh
```

Fix **blocking** errors first (catalog drift, broken bindings, shape). Fix **depth warnings** before **`implementation_readiness: ready`**.

## Fix order

1. **`knowledge/`** + catalog
2. Docs (align to merged primitives)
3. Code (after knowledge is correct)
