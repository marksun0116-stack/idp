# Knowledge-first PRD — specification and template

This document defines **how to write a product requirements document (PRD)** in a knowledge-first repository: required content, **traceability to `knowledge/` primitives**, and what must **not** live only in prose.

**See also:** [`../use_guide.md`](../use_guide.md) (workflows, layout) · [`../executive_overview.md`](../executive_overview.md) (strategy) · `architecture_intent_spec.md` (architecture intent; conventional **`docs/architecture/`**) · `design_spec.md` (HLD; conventional **`docs/design/`**) · `knowledge_model_spec.md` (knowledge layer) · [guides index](../README.md)

---

## 1. Role of the PRD

| Artifact | Role |
| --- | --- |
| **PRD** (`docs/prd/…`) | Product intent: problems, users, scope, journeys, and acceptance in **plain language**. |
| **`knowledge/`** | Enforceable design: **`FEAT-*`**, **`CON-*`**, **`INV-*`**, **`DEC-*`**, **`CONR-*`**, indexed in **`catalog.yml`**. |

**Boundary (RULE-012):** A new “must” or “must not” belongs in a **primitive** (`CON-*`, `INV-*`, `DEC-*`, or `CONR-*`), not only in PRD text. The PRD **references** those IDs. If a review adds a hard rule, update **`knowledge/`** (Knowledge PR) and then link it from the PRD.

**Feature specs in PRD metadata:** **`linked_feature_specs`** may list an **umbrella** **`FEAT-*`** and **child** capability specs. User stories (**`US-*`**) and functional requirements (**`FR-*`**) should **`Trace:`** the **one driving child `FEAT-*`** (or umbrella when no child exists yet). When a **`FR-*`** is enforceable, ensure a matching **`CON-*`** or **`INV-*`** exists and appears in **`related_constraints`** / **`related_invariants`** — see **`knowledge_model_spec.md`** §5.5.

---

## 2. File locations

| File | Purpose |
| --- | --- |
| `.kfs/specs/prd_spec.md` | This file — **how** to write PRDs. |
| `docs/prd/<area>_<name>_prd.md` | **Individual** PRD documents (conventional path). |

**This knowledge-first-system package** may ship **without** a `docs/` tree. When you add PRDs in-repo, **create `docs/prd/`** first (see **`.kfs/INDEX.md`**).

---

## 3. Required metadata

Start every PRD with traceability metadata. Use **YAML frontmatter** (below) or a **Metadata** table if your stack does not support frontmatter.

**YAML example**

```yaml
---
prd_id: PRD-<product>-<feature>-<nnn>
title: "<short title>"
status: draft | review | approved | superseded
owner: "<team or name>"
product_manager: "<name>"
engineering_lead: "<name>"
last_updated: YYYY-MM-DD
version: "<semver or integer>"
linked_feature_specs:
  - FEAT-XXX
  # Optional: umbrella + child capability specs
  # - FEAT-XXX-PH1-001
  # - FEAT-XXX-PH1-003
related_constraints: []
related_invariants: []
related_decisions: []
related_contracts: []
slices: []
---
```

**Table example**

| Field | Value |
| --- | --- |
| PRD ID | PRD-… |
| Status | draft |
| Owner |  |
| Last updated | YYYY-MM-DD |
| Version |  |
| Linked FEAT | FEAT-… |
| CON / INV / DEC / CONR | *(ids)* |

### Change log or version history (required for every PRD)

End every PRD markdown file with **`## Change log`** or **`## Version history`** as the **last** heading (after all body sections and optional appendices). Include a table with at least **revision** (e.g. document version or semver), **date** (`last_updated`), **status** (draft | review | approved | superseded), **supersedes** (`—` if none), and **what changed**. Keep the latest row **aligned** with YAML frontmatter or the top **Metadata** table when both exist. Do not append a separate duplicate version block after that section.

---

## 4. Standard sections (in order)

1. **Summary** — Problem, proposed solution, why now.
2. **Goals and non-goals** — Table is fine.
3. **Users and stakeholders** — Roles, personas, approvers.
4. **User journeys and stories** — Story IDs (`US-…`); each major item: `Trace: FEAT-…` (or TBD with commitment to add `FEAT-*` before build).
5. **Functional requirements** — Numbered `FR-…`; point to `CON-*` / `INV-*` for hard rules once they exist.
6. **Non-functional requirements** — Point to primitives when NFRs are binding.
7. **Data and privacy** — Summarize; link privacy or security `CON-*` as applicable.
8. **Integrations and dependencies** — Link `CONR-*` for defined APIs, events, or file formats.
9. **Edge cases, errors, failure behavior** — Align invariants with `INV-*`.
10. **Success metrics and definition of done** — Include “knowledge graph updated” if that is your bar.
11. **Rollout and migration** — Product-facing phasing; deep technical details may also live in HLD or `docs/migration/`.
12. **Open questions and decisions** — Decisions with long form in **`DEC-*`**, not duplicated in full here.
13. **Risks and mitigations** — Table: risk, impact, mitigation, owner.
14. **Change log or version history** — Terminal section per **§3**; must be the **last** heading in the file.

---

## 5. Gate before implementation

1. A driving **`FEAT-*`** exists (or is added with the same change set that updates this PRD).
2. New binding rules are reflected in **`CON-*` / `INV-*` / `DEC-*` / `CONR-*`**.
3. **`knowledge/catalog.yml`** lists every new or touched primitive.
4. Metadata on this PRD lists the same IDs.

Workflows: `.cursor/skills/merge-knowledge-first/SKILL.md` · `.cursor/skills/validate-knowledge-graph/SKILL.md`

---

## 6. Blank PRD (copy and save as `docs/prd/<area>_<name>_prd.md`)

The block below is a **starter body**. Add frontmatter as in section 3, or a metadata table, at the top.

```markdown
# <Title>

## 1. Summary

### Problem

### Proposed solution

### Why now

## 2. Goals and non-goals

| Goals | Non-goals |
| --- | --- |
| | |

## 3. Users and stakeholders

## 4. User journeys and stories

- US-001: … (Trace: FEAT-…)

## 5. Functional requirements

- FR-001: …

## 6. Non-functional requirements

## 7. Data and privacy

## 8. Integrations and dependencies

## 9. Edge cases, errors, and failure behavior

## 10. Success metrics and definition of done

## 11. Rollout and migration

## 12. Open questions and decisions

## 13. Risks and mitigations

## 14. Change log or version history

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| *(initial)* | YYYY-MM-DD | draft | — | First publication. |
```

## 7. Change log (required)

Every PRD under **`docs/prd/`** must end with **`## Change log`** or **`## Version history`** as specified in **§3** above.

### This spec

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 1.0 |  | approved | — | Initial spec under `.kfs/specs/` |
| 1.1 | 2026-05-06 | approved | — | Required separate Document version footer for PRDs (superseded by 1.2). |
| 1.2 | 2026-05-11 | approved | — | Required terminal change log for PRDs and this spec; removed duplicate Document version footer; blank PRD template includes **§14** change log. |
| 1.3 | 2026-05-12 | approved | — | Conventional **`docs/prd/`**; guides-only template may omit **`docs/`** until product work. |
| 1.4 | 2026-05-25 | approved | — | Umbrella/child **`FEAT-*`** in metadata; **`FR-*`** trace to primitives (**§1**, **`knowledge_model_spec.md`** §5.5). |
