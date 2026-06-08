# Knowledge-first system — knowledge model specification

This document is the **single official** description of **artifact layers** (where things live), **primitive shapes** in `knowledge/`, the **knowledge graph** (nodes and edges), **validation** expectations, and **official trace flow** from product and technical intent to code and tests.

It is **not** the specification for *what to write* in product **architecture intent** documents (conventional path **`docs/architecture/`**)—that is **`architecture_intent_spec.md`**. Nor is it the specification for **high-level design** (conventional **`docs/design/`**)—that is **`design_spec.md`**.

**Agents, humans, and CI** should use this file for the **knowledge layer**; do not maintain a parallel informal schema in chat or untracked markdown.

**See also:** [guides index](../README.md) · `architecture_intent_spec.md` · `design_spec.md` · [`../use_guide.md`](../use_guide.md) · `cicd_spec.md`

---

## 1. Overview

This specification defines:

- how **documentation** and **`knowledge/`** primitives fit together
- **official flows** from intent to code to tests
- a **knowledge graph** view (nodes and edges) for traceability
- **validation** expectations for primitives and the catalog

---

## 2. Design principles

- **Knowledge is authoritative** — enforceable intent lives in **`knowledge/`** (indexed by **`catalog.yml`**), not only in prose.
- **Graph is derived** — trace links are explicit; a computed graph may be built from primitives and docs over time.
- **Relationships are typed** — use a consistent vocabulary for edges (see §7).
- The system must support human readability, AI-assisted editing, enforcement in review and CI, and end-to-end traceability.

---

## 3. Artifact layers (authoritative vs derived)

**`docs/...` paths:** The table below uses the **recommended convention** for product and program narrative in application repositories. The **knowledge-first-system** template may ship **without** an empty **`docs/`** tree; create **`docs/prd/`**, **`docs/architecture/`**, and siblings when you capture that prose in-repo (**`.knowledge-first-system/README.md`**).

### Authoritative (files you maintain)

| Location | Concept | Role |
| --- | --- | --- |
| `docs/prd/` | Product narrative | PRD, scope, journeys; links to **`FEAT-*`** |
| `docs/architecture/` | Architecture intent (narrative) | Principles, topology, NFRs in prose; links **`DEC-*`**, **`CON-*`** — **see `architecture_intent_spec.md`** for how to write these documents |
| `docs/design/` | High-level design | Components, flows, boundaries; aligns with **`CONR-*`** and HLD narrative — **see `design_spec.md`** for how to write these documents |
| `docs/implementation/` | Implementation plans | Lightweight phase/story execution plans; order, dependencies, parallelization, status, progress updates; links **`FEAT-*`**, **`CON-*`**, **`INV-*`**, **`CONR-*`** — **see `implementation_plan_spec.md`** |
| `docs/test/` | Test plans | Release or product test plans; links **`INV-*`**, **`CONR-*`** |
| `.knowledge-first-system/` | Shared guide specs | **This file** (knowledge model), **architecture intent**, **HLD**, PRD / test / infra / CI templates (`*_spec.md` under `specs/`) |
| `knowledge/constraints/` | **`CON-*`** | Must / must-not rules |
| `knowledge/invariants/` | **`INV-*`** | Properties that must always hold |
| `knowledge/decisions/` | **`DEC-*`** | Architecture and product decisions |
| `knowledge/contracts/` | **`CONR-*`** | APIs, events, schemas |
| `knowledge/specs/` | **`FEAT-*`** | Feature specs with bindings to the above |
| `knowledge/catalog.yml` | Index | ID → file path for all primitives |

### Multi-repo workspaces

When a workspace contains multiple repositories, KFS artifacts are organized by ownership:

| Ownership level | Recommended location | Role |
| --- | --- | --- |
| Workspace/program knowledge | Program or governance repo `knowledge/` and `docs/` | Cross-repo decisions, shared constraints, shared contracts, workspace implementation plans |
| Shared-service knowledge | Shared-service repo `knowledge/` and `docs/`, or program/governance repo when the service repo does not yet carry KFS | Service-owned behavior, data ownership, service-local tests and runtime docs |
| Consumer knowledge | Consumer repo `knowledge/` and `docs/` | Consumer adapter behavior, local user-facing behavior, local tests |
| Mirrored contracts | Consumer repo `knowledge/contracts/` when needed | References the owning contract/version; does not redefine incompatible shape |

One repo must be named as owner for every shared `CONR-*`, `DEC-*`, and cross-repo `CON-*`. Consumers may mirror or reference the owning primitive, but must not silently fork it. Workspace-level implementation plans live in the program/governance repo.

### Derived (optional tooling)

- **Knowledge graph** — nodes and edges extracted from YAML and doc links for trace queries and CI.

---

## 4. Base fields (conceptual; match sibling YAML in this repo)

Every primitive file should include at least **`id`**, **`kind`**, **`title`**, **`status`**. Other fields follow **`knowledge-primitives.mdc`** and existing files in each directory.

---

## 5. Primitive kinds (conceptual schema)

Concrete YAML shapes are defined by existing files under `knowledge/` and by **`knowledge-primitives.mdc`**. Conceptually:

### 5.1 Constraint (`CON-*`)

- Enforceable rules: scope, enforcement mechanism, validation expectations.

### 5.2 Invariant (`INV-*`)

- Statement of what must always hold; evidence (tests, audits); severity if violated.

### 5.3 Decision (`DEC-*`)

- Context, chosen option, alternatives, consequences.

### 5.4 Contract (`CONR-*`)

- Interface type (REST, event, etc.), schema or shape, versioning when published.

### 5.5 Feature spec (`FEAT-*`)

- Bindings to **`CON-*`**, **`INV-*`**, **`DEC-*`**, **`CONR-*`**; scope and success criteria.

---

## 6. Supporting narrative artifacts (relationship to this spec)

- **Architecture intent** (conventional **`docs/architecture/`**) — *Human* architecture direction for the product or program: principles, context, NFRs, boundaries. **How to structure** those documents: **`architecture_intent_spec.md`**. This knowledge-model spec only places that folder in the **layer table** (§3) and in the **graph** (§7–8).
- **High-level design** (conventional **`docs/design/`**) — Services or modules, responsibilities, main data flows, how **constraints** and **invariants** show up in the design. **How to structure** those documents: **`design_spec.md`**.
- **Implementation plan** (conventional **`docs/implementation/`**) — How approved scope will be sequenced into phases and user stories; maps to **`FEAT-*`**, **`CON-*`**, **`INV-*`**, **`CONR-*`**, and **`implementation_plan_spec.md`**.
- **Test plan** (conventional **`docs/test/`**) — What you will verify for a release or epic; maps to **`INV-*`**, **`CONR-*`**, and **`test_strategy_spec.md`**.

---

## 7. Knowledge graph — node and edge vocabulary

### Node types (conceptual)

Architecture intent (narrative in conventional **`docs/architecture/`** when present), **Constraint**, **Invariant**, **Decision**, **Contract**, **Feature spec** (`FEAT-*`), high-level design narrative, implementation plan, test plan narrative.

### Edge types (use in reviews and tooling)

| Edge | Meaning |
| --- | --- |
| guides | Intent guides downstream artifacts |
| uses | Artifact uses another |
| derived_from | Derived from source artifact |
| enforces | Constraint or policy enforcement |
| verifies | Test or process verifies an invariant or contract |
| implements | Code implements spec or contract |
| constrains | Constraint limits scope |
| references | Doc or spec references a primitive ID |

---

## 8. Official graph flow

```text
Architecture intent → Decision → HLD
Constraint / Invariant → Feature spec → HLD → Implementation plan → Code → Tests
```

Alternate shorthand used in reviews:

```text
Constraint / Invariant → Spec → HLD → plan → code → tests
```

**Meaning:** Product and architecture direction inform **decisions** and **design**. **Primitives** bind **features**; implementation plans sequence work into ordered and parallelizable slices; implementation and tests must remain traceable to **`FEAT-*`**, **`INV-*`**, **`CON-*`**, and **`CONR-*`**.

For multi-repo workspaces, the same flow applies across repo boundaries:

```text
Workspace DEC / CON / CONR -> shared-service plan + implementation -> consumer adapter plan + implementation -> per-repo tests
```

Cross-repo work must name the contract owner and the consumer repos in the implementation plan. Validation is complete only when the owning knowledge validates and the relevant per-repo tests have been run or explicitly recorded as not run.

---

## 9. Validation (targets for local scripts and CI)

### Primitive validation

- Required fields present for each `kind`.
- No dangling **`FEAT-*`** bindings (every referenced ID exists in **`knowledge/`** and **`catalog.yml`**).

### Cross-validation

- Feature spec bindings resolve to real primitives.
- HLD and PRD narratives do not contradict merged primitives (resolve in **`knowledge/`** first).

### Graph integrity (when tooling exists)

- No orphan primitives that should be linked.
- No duplicate IDs across primitives.

---

## 10. Implementation phases (evolution of tooling)

1. **Schema and catalog checks** — validate YAML shape and `catalog.yml`.
2. **Graph extraction** — optional: build edges from `related_primitives` and doc links.
3. **Impact and trace queries** — optional: “what uses this `CON-*`?” for change analysis.

---

## 11. Summary

- **`knowledge/`** is the **source of truth** for enforceable rules and feature bindings.
- **Documentation** in **product `docs/`** (when present) and **`.knowledge-first-system/`** explains, motivates, and links IDs.
- **This specification** is the **single place** agents should cite for **layers, primitive kinds, graph vocabulary, validation, and end-to-end trace flow** in the knowledge-first system.
- **Architecture intent** *documents* (conventional **`docs/architecture/`**) are specified in **`architecture_intent_spec.md`**.
- **High-level design** *documents* (conventional **`docs/design/`**) are specified in **`design_spec.md`**.

---

## 12. One-line summary

> Define knowledge precisely in **`knowledge/`**, connect it to docs and code, and validate links so the system stays traceable and enforceable.

---

## 13. Change log (required)

Specifications under **`.knowledge-first-system/specs/`** (including this file) end with **`## Change log`** or **`## Version history`** as the **last** heading, with a table of revisions (see sibling `*_spec.md` files for column guidance). Product narrative under conventional **`docs/`** (when present) follows the change-log rules in **`prd_spec.md`**, **`architecture_intent_spec.md`**, **`design_spec.md`**, and **`test_strategy_spec.md`** as applicable.

### This spec

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 1.1 | 2026-05-06 | approved | — | Prior release (separate Document version footer). |
| 1.2 | 2026-05-11 | approved | — | Terminal change log; removed duplicate Document version footer. |
| 1.3 | 2026-05-12 | approved | — | **`docs/`** paths documented as conventional; guides-only template may omit empty tree (**README**). |
| 1.4 | 2026-06-05 | approved | 1.3 | Added **`docs/implementation/`** as the lightweight phase/story implementation planning layer. |
| 1.5 | 2026-06-07 | approved | 1.4 | Added generic multi-repo workspace ownership, contract-owner, and cross-repo validation guidance. |
