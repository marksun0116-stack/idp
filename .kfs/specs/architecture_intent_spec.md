# Knowledge-first system — architecture intent specification

This document defines **how to write architecture intent** in a knowledge-first repository: what belongs in documents under the conventional path **`docs/architecture/`**, how they relate to the **PRD**, **`knowledge/`** primitives, and **high-level design** (conventional **`docs/design/`**), and what must **not** live only in ungrounded prose.

**Distinction:** The **knowledge model** (artifact layers, primitives, graph, validation, trace flow) is specified in **`knowledge_model_spec.md`**. This file is only about the **human-readable architecture intent** layer.

**See also:** [guides index](../README.md) · `prd_spec.md` · `design_spec.md` · `knowledge_model_spec.md` · [`../use_guide.md`](../use_guide.md)

---

## 1. Role of architecture intent

| Artifact | Role |
| --- | --- |
| **Architecture intent** (`docs/architecture/…`) | Durable **technical and structural direction**: context, principles, major boundaries, NFRs, and **forbidden** patterns—**in plain language** with links to primitives. |
| **PRD** (`docs/prd/…`) | **Product** intent: users, problems, scope, journeys, acceptance. |
| **HLD** (`docs/design/…`) | **Design** of components, interfaces, and flows for the current or target system. |
| **`knowledge/`** | **Enforceable** rules: **`DEC-*`**, **`CON-*`**, plus **`INV-*`**, **`CONR-*`**, **`FEAT-*`**, indexed in **`catalog.yml`**. |

**Flow (compressed):** PRD and architecture intent **inform** **decisions** and **constraints**; HLD **implements** the design view; features and code **trace** to **`FEAT-*`** and linked primitives. See **`knowledge_model_spec.md`** §8 for the full trace model.

**Boundary (RULE-012):** A new “must” or “must not” for the system belongs in a **primitive** (`CON-*`, `INV-*`, `DEC-*`, or `CONR-*` as appropriate), not only in architecture intent text. The architecture document **references** those IDs. If a review adds a hard rule, update **`knowledge/`** (Knowledge PR) and then link it from the doc.

---

## 2. File locations

| File | Purpose |
| --- | --- |
| `.kfs/specs/architecture_intent_spec.md` | This file — **how** to write architecture intent. |
| `docs/architecture/<area>_<name>_architecture_intent.md` (or team naming) | **Individual** architecture intent documents (conventional path). |

**This knowledge-first-system package** may ship **without** a `docs/` tree. When you add architecture intent in-repo, **create `docs/architecture/`** first (see **`.kfs/INDEX.md`**). Filenames are not fixed; keep them **stable and searchable** (avoid frequent renames so trace links from reviews stay valid).

---

## 3. Required metadata

Start every architecture intent document with traceability metadata. Use **YAML frontmatter** (below) or a **Metadata** table if your stack does not support frontmatter.

**YAML example**

```yaml
---
arch_intent_id: ARCH-<product-or-system>-<nnn>
title: "<short title>"
status: draft | review | approved | superseded
owner: "<team or name>"
architect: "<name>"
last_updated: YYYY-MM-DD
version: "<semver or integer>"
linked_decisions: []
related_constraints: []
related_invariants: []
related_prds: []
slices: []
---
```

**Table example**

| Field | Value |
| --- | --- |
| Architecture intent ID | ARCH-… |
| Status | draft |
| Owner |  |
| Last updated | YYYY-MM-DD |
| Version |  |
| Linked DEC / CON / INV | *(ids)* |
| Related PRDs | PRD-… |

### Change log or version history (required for every architecture intent doc)

End every architecture intent markdown file with **`## Change log`** or **`## Version history`** as the **last** heading (after all body sections). Include a table with at least **revision** (e.g. document version or semver), **date** (`last_updated`), **status** (draft | review | approved | superseded), **supersedes** (`—` if none), and **what changed**. Keep the latest row aligned with YAML frontmatter or the top **Metadata** table. Do not append a separate duplicate version block after that section.

---

## 4. Standard sections (recommended order)

1. **Summary** — What system or scope this document covers; current vs target state in one paragraph.
2. **Context and drivers** — Business and technical drivers; **assumptions** and **non-goals** (technical).
3. **Stakeholders and consumers** — Who uses this: teams, other systems, operations; who approves changes.
4. **Current state (if modernizing)** — Short **as-is** view: runtime shape, major pain points, **known violations** (no sugar-coating). Mark **observed** vs **inferred** where needed.
5. **Principles and constraints** — Numbered `AP-…` (architecture principles) or similar; each binding principle that is **enforceable** should point to **`CON-*`**, **`DEC-*`**, or **`INV-*`** once they exist.
6. **Context and boundaries** — Trust boundaries, tenancy, major integrations; link **`CONR-*`** for defined interfaces; link **`CON-*`** for hard boundaries (e.g. no cross-tenant data access).
7. **Runtime and deployment (conceptual)** — How the system is deployed and operated at a **high** level: regions, environments, key dependencies. Deeper **infrastructure** detail lives in **`infrastructure_spec.md`** and product-specific docs—do not duplicate long runbooks here.
8. **Data and platform** — Data ownership, retention, and platform choices at intent level; link privacy or security **`CON-*`** as applicable.
9. **Non-functional requirements (architectural)** — Latency, availability, RTO/RPO, scalability—**link** to **`CON-*`**, **`INV-*`**, or **`DEC-*`** when the NFR is a **binding** commitment.
10. **Risks and open decisions** — Risks, **open questions**, and **pending** `DEC-*` work. Do not treat unresolved choices as if they were decided.
11. **Traceability** — Table of **PRD** IDs, **`FEAT-*`**, **`DEC-*`**, **`CON-*`**, **`CONR-*`**, and **HLD** doc links this intent **guides** or **constrains**.

---

## 5. Relationship to high-level design

- **Architecture intent** sets **direction and rules**; it is relatively **stable** across releases when principles do not change.
- **HLD** (`docs/design/`) describes **concrete** modules, APIs, and flows for a **specific** design snapshot. Structure and trace expectations for HLD: **`design_spec.md`**. HLD must **not** contradict active **`CON-*`**, **`DEC-*`**, or this intent; if the design must break a rule, add a new **`DEC-*`** (or retire/replace a constraint) in a **Knowledge PR** first, then update documents.

---

## 6. Reviews and change control

- **Architect** (or owner role) approves **architecture intent** for merge when it is **consistent** with merged **`DEC-*`**, **`CON-*`**, and linked **PRD** / **HLD** references.
- **Version** the document when **binding** content changes; call out what changed in the review or change log.

---

## 7. What not to do

- Do not use architecture intent as a **second PRD**—product scope and user stories stay in **`docs/prd/`**.
- Do not hide **must / must not** only in narrative—**promote** to **`knowledge/`** and link by ID.
- Do not **paste** full API schemas here—use **`CONR-*`** and link from this doc and from HLD.

---

## 8. One-line summary

> Architecture intent in **`docs/architecture/`** states **technical direction and boundaries** in clear prose, **grounded in `knowledge/`** primitives and **traceable** to PRD, HLD, and features.

---

## 9. Change log (required)

Every architecture intent document under **`docs/architecture/`** must end with **`## Change log`** or **`## Version history`** as specified in **§3** above. Specifications under **`.kfs/specs/`** use the same terminal pattern for their own revision history.

### This spec

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 1.1 | 2026-05-06 | approved | — | Prior release (document version footer pattern). |
| 1.2 | 2026-05-11 | approved | — | Required terminal change log for architecture intent docs and this spec; removed duplicate Document version footer. |
| 1.3 | 2026-05-12 | approved | — | Conventional **`docs/`** paths; guides-only template may omit tree until product work. |
