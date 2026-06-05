---
name: validate-knowledge-graph
description: >-
  Validates knowledge primitives and catalog for this repo: required fields,
  catalog drift, binding IDs, and CI-style blocking checks. Use when editing
  knowledge/, catalog.yml, FEAT bindings, before merging a Knowledge PR, or
  when the user asks to validate the catalog, primitive links, or knowledge CI.
---

# Validate the knowledge graph

**Human checklist:** **`.knowledge-first-system/use_guide.md`** (Validation checklist). **CI alignment:** **`.knowledge-first-system/specs/cicd_spec.md`**. **Formal categories:** **`.knowledge-first-system/specs/knowledge_model_spec.md`** §9 (primitive completeness, binding cross-checks, graph integrity). Local checks and CI should converge toward that model as scripts mature.

## When to run

- Any PR that touches **`knowledge/`**.
- Before declaring a Knowledge PR merge-ready.
- After resolving conflicts in **`knowledge/catalog.yml`** or primitive files.
- **Greenfield bootstrap (RULE-013):** When validating the **first** product **`FEAT-*`** or first batch of primitives for a **new initiative**, confirm conventional **`docs/prd/`** and **`docs/architecture/`** each exist and contain at least one **`*.md`** file. If not, **report a documentation gate** and ask the user to **create the folders and documents** before treating the graph as merge-ready for net-new scope (**`.knowledge-first-system/rules/new-project-intent-gate.mdc`**). **Guides-only** repos may omit **`docs/`** until product work begins.

## Goals (align with project CI when implemented)

- **Fail** when `catalog.yml` references missing files, duplicate IDs, or broken **`related_primitives`** / binding IDs.
- Optionally validate YAML **shape** per `kind` as scripts mature.
- Prefer **small, fast** checks locally; mirror them in CI.

## Blocking conditions (when implemented)

- Missing required primitive fields or unknown IDs referenced from specs.
- Catalog / file mismatch.
- Where mandated: missing or untraceable tests for critical **INV-*** coverage.

## Local workflow

1. If **`.knowledge-first-system/scripts/`** contains validation entrypoints (e.g. catalog or schema checks), run them from the repo root and fix reported issues in **`knowledge/`** — not by weakening checks.
2. Manually spot-check: every **FEAT-*** trace section references IDs that exist on disk and in **`knowledge/catalog.yml`**.
3. Grep for removed IDs in **`related_primitives`** across primitives.

## Fix order when something breaks

1. **YAML and catalog** — correct IDs, paths, and bindings.
2. **Docs** — align narrative with merged primitives if prose drifted.
3. **Code** — only after knowledge is correct; do not “fix” broken bindings only in **`docs/`** prose.

See **`.knowledge-first-system/rules/knowledge-validation-scripts.mdc`** for the standing policy on scripts and CI scope.

---

## Change log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 1.1 | 2026-05-06 | approved | — | Prior release (Document version footer). |
| 1.2 | 2026-05-12 | approved | — | RULE-013 note for optional **`docs/`**; terminal change log. |
