# Knowledge-first system — high-level design (HLD) specification

This document defines **how to write high-level design** in a knowledge-first repository: what belongs in documents under the conventional path **`docs/design/`**, how they relate to the **PRD**, **architecture intent**, **`knowledge/`** primitives (especially **`FEAT-*`** and **`CONR-*`**), and **tests**, and what must **not** live only in ungrounded prose.

**Distinction:** The **knowledge model** (artifact layers, primitives, graph, validation, trace flow) is specified in **`knowledge_model_spec.md`**. **Architecture intent** (principles, topology, NFRs at intent level) is specified in **`architecture_intent_spec.md`**. This file is only about the **human-readable HLD** layer—concrete components, interfaces, and flows for a **specific** design snapshot.

**See also:** [guides index](../README.md) · `architecture_intent_spec.md` · `prd_spec.md` · `knowledge_model_spec.md` · `infrastructure_spec.md` (where systems run; complements §4.1 item 6) · `test_strategy_spec.md` · [`../use_guide.md`](../use_guide.md)

---

## 1. Role of high-level design (HLD)

| Artifact | Role |
| --- | --- |
| **HLD** (`docs/design/…`) | **Concrete** structure for the current or target system: components, responsibilities, **interfaces**, main **data and control flows**, and how **constraints** and **invariants** appear in the design. |
| **Architecture intent** (`docs/architecture/…`) | Durable **direction**: principles, boundaries, NFRs—relatively stable across releases. |
| **PRD** (`docs/prd/…`) | **Product** intent: users, scope, journeys, acceptance. |
| **`knowledge/`** | **Enforceable** rules: **`FEAT-*`** (scoped work and bindings), **`CONR-*`** (published shapes at boundaries), **`DEC-*`**, **`CON-*`**, **`INV-*`**, indexed in **`catalog.yml`**. |

**Flow (compressed):** PRD and architecture intent **inform** **decisions** and **constraints**; **HLD** **implements** the design view for engineers; implementation and tests **trace** to **`FEAT-*`**, **`CONR-*`**, **`INV-*`**, and **`CON-*`**. See **`knowledge_model_spec.md`** §8 for the full trace model.

**Boundary (RULE-012):** A new “must” or “must not” for **runtime behavior** belongs in a **primitive**, not only in HLD prose. The HLD **references** those IDs. Published API or event **payload shapes** belong in **`CONR-*`**; HLD may summarize and **link** `CONR-*`, not replace it as the authoritative contract. If a review adds a hard rule, update **`knowledge/`** (Knowledge PR) and then link it from the HLD.

---

## 2. File locations

| File | Purpose |
| --- | --- |
| `.kfs/specs/design_spec.md` | This file — **how** to write HLD documents. |
| `docs/design/<area>_<name>_hld.md` (or team naming, e.g. `_design.md`) | **Individual** high-level design documents (conventional path). |

**This knowledge-first-system package** may ship **without** a `docs/` tree. When you add HLD in-repo, **create `docs/design/`** first (see **`.kfs/INDEX.md`**). Filenames are not fixed; keep them **stable and searchable** (avoid frequent renames so trace links from reviews stay valid).

---

## 3. Required metadata

Start every HLD document with traceability metadata. Use **YAML frontmatter** (below) or a **Metadata** table if your stack does not support frontmatter.

**YAML example**

```yaml
---
hld_id: HLD-<product-or-system>-<nnn>
title: "<short title>"
status: draft | review | approved | superseded
owner: "<team or name>"
engineering_lead: "<name>"
last_updated: YYYY-MM-DD
version: "<semver or integer>"
driving_features: []   # FEAT-* ids
linked_contracts: [] # CONR-* ids
linked_decisions: [] # DEC-* ids
linked_constraints: [] # CON-* ids
linked_invariants: [] # INV-* ids
related_prds: []
related_architecture_intents: []
---
```

**Table example**

| Field | Value |
| --- | --- |
| HLD ID | HLD-… |
| Status | draft |
| Owner |  |
| Last updated | YYYY-MM-DD |
| Version |  |
| Driving FEAT-* | *(ids)* |
| Linked CONR-* / DEC-* / CON-* / INV-* | *(ids)* |
| Related PRDs / architecture intents | *(ids or paths)* |

### Change log or version history (required for every HLD)

End every HLD markdown file with **`## Change log`** or **`## Version history`** as the **last** heading (after all body sections). Include a table with at least **revision** (e.g. document version or semver), **date** (`last_updated`), **status** (draft | review | approved | superseded), **supersedes** (`—` if none), and **what changed**. Keep the latest row aligned with YAML frontmatter or the top **Metadata** table. Do not append a separate duplicate version block after that section.

---

## 4. Standard sections (recommended order)

Use one HLD per **bounded context**, **release slice**, or **migration seam** when the system is large; link related HLDs in metadata or in a short “Related designs” subsection.

### 4.1 Core (use for every governed HLD)

1. **Summary** — What subsystem or slice this HLD covers; **current vs target** in one paragraph when modernizing.
2. **Scope and goals** — In/out of scope; **link `FEAT-*`** that drive this design; **non-goals** to prevent silent scope creep.
3. **Context** — Upstream **PRD** and **architecture intent** links; assumptions; **dependencies** on other teams, shared platforms, or **critical third-party** SaaS (identity, payments, email, data warehouse)—enough to reason about **failure modes** and ownership (deep vendor runbooks stay elsewhere; **link `CON-*`** when a vendor constraint is binding).
4. **Actors, tenants, and trust boundaries** — Who uses this slice (**end users, org admins, internal operators, partners**); **tenant** or account model and **isolation** story (data and control plane); **admin vs customer** surfaces; trust zones and **blast radius**. Binding isolation or residency rules belong in **`CON-*` / `DEC-*`** with IDs here—not only prose.
5. **Component view** — Named services or modules, **responsibilities**, and **ownership**; major **read/write** responsibilities per component. Diagrams welcome; align labels with **`CONR-*`** and repo/module names used in code.
6. **Runtime topology (for this slice)** — Where these components **run** (environment tier, region or cell if relevant), **synchronous request paths** vs **workers / queues / schedulers**, and **dependencies** on shared infrastructure (cache, bus, object store). Platform and account topology depth belongs in **`infrastructure_spec.md`**—HLD stays at **allocation and dependency** level with links.

### 4.2 Large-scale SaaS (add when the slice is customer-facing, multi-tenant, or production-critical)

7. **Interfaces and published contracts** — **Public** and **internal/admin** HTTP APIs, **events**, **webhooks**, file or batch exchanges; **link `CONR-*`** per published surface. Call out **versioning**, **breaking vs additive** change expectations, and **idempotency** keys or replay behavior when they are part of the contract story (normative rules still live in **`CONR-*` / `INV-*`**).
8. **Data, state, and sensitive flows** — Primary **entities**, **ownership** and **source of truth** per aggregate; cross-service reads/writes; **PII** or regulated categories and **which components may see them**; **retention or deletion** flows at design level. Link **`DEC-*`** for authority and **`CON-*` / `INV-*`** for rules that must hold in code and tests.
9. **Asynchronous processing, reliability, and back-pressure** — Queues, outbox/inbox, **job** lifecycles, **retries**, **ordering** guarantees, **dead-letter** paths, **bulkhead** or concurrency limits. Map **idempotency**, **exactly-once illusion**, or **ordering** commitments to **`INV-*`** (and tests) where the business depends on them.
10. **Control flows** — Critical **user** and **system** workflows (onboarding, checkout, provisioning, reconciliation); **saga** or compensation boundaries if applicable; where **`INV-*`** are enforced in the path.
11. **Security, compliance, observability, and performance hooks** — **AuthN / AuthZ** enforcement points (gateway, service, resource), **service-to-service** identity; **audit** events and tamper expectations at design level; **logs, metrics, traces**, and **SLO** touchpoints for this slice; **encryption** (transit/rest) and **secret** usage patterns (**names** of secret stores, not values). **Hot paths**, **caching**, **sharding or partition keys**, and **rate limiting** placement when scale matters. Binding commitments remain **`CON-*` / `INV-*`** with links—HLD explains **where** they attach in the design.
12. **Evolution, compatibility, and migration** — **API and schema evolution**, **deprecation** policy, **feature flags** and safe rollout; for **two-repo modernization** use **blue/green** cutover per [modernization_new_repo_spec.md](modernization_new_repo_spec.md) (no runtime dependency on legacy); link **`CONR-*`** at target boundaries and **`DEC-*`** for migration choices. Include this block whenever the slice **ships** to other teams or **changes** published behavior.

### 4.3 Optional (include when the architecture or risk profile requires it)

13. **Multi-region, DR, and continuity** *(optional)* — **Active/active vs active/passive** roles for this slice, **failover** dependencies, **RTO/RPO** expectations **at design level** when architecture intent commits the product; link architecture intent and **`DEC-*`**. Avoid duplicating full DR runbooks—**link** infra and ops guides.
14. **Cost, quota, and abuse considerations** *(optional)* — Design-level **quota** enforcement points, **noisy neighbor** containment, or **billing-relevant** metering hooks when they drive structure; binding commercial rules still surface as **`CON-*` / `DEC-*`** where appropriate.

### 4.4 Close-out

15. **Risks and open items** — Open questions; **pending** **`DEC-*`**, **`CONR-*`**, or **HLD** decisions; do not treat unresolved interfaces as decided.
16. **Traceability** — Table of **`FEAT-*`**, **`CONR-*`**, **`DEC-*`**, **`CON-*`**, **`INV-*`**, linked **PRD** / **architecture intent**, **related HLDs**, and **test plan** (`docs/test/`) this slice **implements**, **uses**, or **verifies**.

---

## 5. Relationship to architecture intent and PRD

- **Architecture intent** sets **direction**; **HLD** describes **how** the current design realizes that direction for a **concrete** slice or release.
- **PRD** sets **product** acceptance; **HLD** shows **technical** shape that enables those outcomes. If HLD cannot meet PRD without breaking **`CON-*`** or **`DEC-*`**, resolve in **knowledge** and documents—do not “paper over” in code only.
- When architecture **changes** binding principles, update **`DEC-*` / `CON-*`** and architecture intent **before** or **with** HLD updates so the graph stays consistent.

---

## 6. Relationship to tests

- **Test plans** under **`docs/test/`** should reference **`INV-*`**, **`CONR-*`**, and driving **`FEAT-*`** aligned with this HLD. See **`test_strategy_spec.md`**.
- HLD should make **observable** where **invariant** and **contract** evidence is expected (suites, environments)—without duplicating the full test strategy document.

---

## 7. Reviews and change control

- **Engineering lead** (or owner role) approves **HLD** for merge when it is **consistent** with merged **`FEAT-*`**, **`CONR-*`**, **`DEC-*`**, **`CON-*`**, linked **PRD** / **architecture intent**, and active **code** for the same slice.
- **Version** the document when **binding** structure or interfaces change; note what changed for downstream teams and CI.

---

## 8. What not to do

- Do not use HLD as a **second PRD**—user journeys and product acceptance stay in **`docs/prd/`**.
- Do not use HLD as a **full architecture intent**—long-lived principles stay in **`docs/architecture/`** (see **`architecture_intent_spec.md`**).
- Do not define **authoritative** API or event **schemas** only in HLD—use **`CONR-*`** and link.
- Do not leave **FEAT-*** or **`CONR-*`** **orphan** when the HLD introduces or changes a boundary that ships to other teams.
- Do not omit **tenancy and trust** for **multi-tenant** SaaS—if the product is tenant-scoped, the HLD must name how isolation appears in **this** slice and link binding **`CON-*` / `DEC-*`**.
- Do not paste **long runbooks, pipeline YAML, or secret values** into HLD—**link** infra, CI, and vault **concepts** only.

---

## 9. One-line summary

> High-level design in **`docs/design/`** describes **components, interfaces, and flows**—including **tenancy, reliability, security, and evolution** when the slice needs it—**grounded in `knowledge/`** (especially **`FEAT-*`** and **`CONR-*`**) and **traceable** to PRD, architecture intent, code, and tests.

---

## 10. Change log (required)

Every HLD document under **`docs/design/`** must end with **`## Change log`** or **`## Version history`** as specified in **§3** above. Specifications under **`.kfs/specs/`** use the same terminal pattern for their own revision history.

### This spec

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 1.1 | 2026-05-06 | approved | — | Prior release (document version footer pattern for HLDs). |
| 1.2 | 2026-05-11 | approved | — | Required terminal change log for HLDs and this spec; removed duplicate Document version footer. |
| 1.3 | 2026-05-12 | approved | — | Conventional **`docs/`** paths; guides-only template may omit tree until product work. |
