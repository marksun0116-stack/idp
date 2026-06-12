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

**`docs/...` paths:** The table below uses the **recommended convention** for product and program narrative in application repositories. The **knowledge-first-system** template may ship **without** an empty **`docs/`** tree; create **`docs/prd/`**, **`docs/architecture/`**, and siblings when you capture that prose in-repo (see **`.kfs/INDEX.md`**).

### Authoritative (files you maintain)

| Location | Concept | Role |
| --- | --- | --- |
| `docs/prd/` | Product narrative | PRD, scope, journeys; links to **`FEAT-*`** |
| `docs/architecture/` | Architecture intent (narrative) | Principles, topology, NFRs in prose; links **`DEC-*`**, **`CON-*`** — **see `architecture_intent_spec.md`** for how to write these documents |
| `docs/design/` | High-level design | Components, flows, boundaries; aligns with **`CONR-*`** and HLD narrative — **see `design_spec.md`** for how to write these documents |
| `docs/test/` | Test plans | Release or product test plans; links **`INV-*`**, **`CONR-*`** |
| `.kfs/specs/` | KFS template specs | **This file** (knowledge model), **architecture intent**, **HLD**, PRD / test / infra / CI templates (`*_spec.md`) |
| `knowledge/constraints/` | **`CON-*`** | Must / must-not rules |
| `knowledge/invariants/` | **`INV-*`** | Properties that must always hold |
| `knowledge/decisions/` | **`DEC-*`** | Architecture and product decisions |
| `knowledge/contracts/` | **`CONR-*`** | APIs, events, schemas |
| `knowledge/specs/` | **`FEAT-*`** | Feature specs with bindings to the above |
| `knowledge/plans/` | **`PLAN-*`** | Implementation plan: phases, stories, dependencies, progress (see **`implementation_plan_spec.md`**) |
| `knowledge/catalog.yml` | Index | ID → file path for all primitives |

### Derived (optional tooling)

- **Knowledge graph** — nodes and edges extracted from YAML and doc links for trace queries and CI.

---

## 4. Base fields (conceptual; match sibling YAML in this repo)

Every primitive file should include at least **`id`**, **`kind`**, **`title`**, **`status`**. Other fields follow **`knowledge-primitives.mdc`** and existing files in each directory.

### 4.1 Lifecycle `status` (authoritative in YAML)

The **`status`** field is **governance and delivery lifecycle** in `knowledge/` — not a hand-maintained “built in code” flag.

| Kind | Typical values | Meaning |
| --- | --- | --- |
| **`FEAT-*`** | `proposed`, `accepted`, `deprecated` | Delivery scope: planned → shipped (or retired). Update when closing a release slice. |
| **`CONR-*`** | `draft`, `accepted`, `deprecated` | Contract publication lifecycle; pair with **`version`** when published. |
| **`CON-*` / `INV-*` / `DEC-*`** | `proposed`, `accepted`, `deprecated` | Rule or decision is approved and in force (or retired). |

Normalize legacy values such as `present` → **`accepted`** in read tooling only; prefer **`accepted`** in new YAML.

### 4.2 Derived `trace_coverage` (read model — not edited in YAML)

Graph and Observe tooling may attach **`trace_coverage`** per primitive at ingest/read time:

| Value | Meaning |
| --- | --- |
| **`verified`** | Primitive id or linked **`TC-*`** appears in test sources and/or test plan with trace. |
| **`partial`** | YAML cites tests (`enforcement`, `evidence`, `success_criteria`) or primitive is bound to a **`FEAT-*`** with test criteria, but full verification signal is incomplete. |
| **`specified`** | Lifecycle status is captured in knowledge; no automated trace signal yet. |
| **`unknown`** | Missing or unrecognized lifecycle/trace inputs. |

**Do not** duplicate implementation truth as manual **`status: implemented`** on **`CON-*`** / **`INV-*`**. Tests and CI assert rules; **`implements`** / **`verifies`** edges and derived coverage overlays show code alignment.

### 4.3 Lifecycle during an implementation cycle

Primitive **`status`** tracks **governance** (approved, in force, retired). **Day-to-day delivery progress** lives in the **implementation plan** story **`status`** — see **`implementation_plan_spec.md`**.

| When | Primitive updates | Plan updates |
| --- | --- | --- |
| Knowledge PR merges | **CON/INV/DEC/CONR** → **`accepted`** (or **`draft`** → **`accepted`** for **CONR**) | Knowledge-phase stories → **`done`** |
| Plan activated | Driving **FEAT** stays **`proposed`** until cycle ships | Plan **`active`**; eligible stories **`in_progress`** |
| Implement story completes | Change primitives only if rules/contracts changed (**Knowledge PR**) | Story **`done`** |
| Phase completes | — | Phase **`closed`** (immutable); all stories **`done`** / **`dropped`** |
| Cycle completes | Driving **FEAT** → **`accepted`** | Plan **`closed`** |

**Additional scope** after a phase is **`closed`**: append **new `PH-*` / `ST-*`** — do not rewrite closed phases or their stories.

## 5. Primitive kinds (conceptual schema)

Concrete YAML shapes are defined by existing files under `knowledge/` and by **`knowledge-primitives.mdc`**. Conceptually:

### 5.1 Constraint (`CON-*`)

- Enforceable rules: scope, enforcement mechanism, validation expectations.

### 5.2 Invariant (`INV-*`)

- Statement of what must always hold; evidence (tests, audits); severity if violated.

### 5.3 Decision (`DEC-*`)

- Context, chosen option, alternatives, consequences.

### 5.4 Contract (`CONR-*`)

**Purpose.** A **`CONR-*`** primitive is the **authoritative published boundary** for an API, event, webhook, or shared payload shape. Narrative under **`docs/design/`** (HLD “Interfaces”) and **`docs/architecture/`** may summarize and **link** `CONR-*` IDs; they must not replace the primitive as the enforceable contract (RULE-012; **`design_spec.md`**).

**Common fields** (in addition to §4 base fields):

- **`summary`** — what the boundary is for and who consumes it.
- **`version`** — semver or label when the surface is published or shared externally.
- **`owner`** — team or role accountable for the contract.
- **`references`** — paths or IDs to PRD, architecture intent, HLD (`prd`, `hld`, `architecture_intent`, etc., as used in sibling files).
- **`related_primitives`** — related **`CON-*`**, **`INV-*`**, **`DEC-*`**, **`FEAT-*`** (graph edge type `related`).

**Interface kind.** Declare how readers and tooling should interpret the file:

- Use **`interface_type`** (or an equivalent field used consistently in the repo) such as **`rest`**, **`event`**, **`webhook`**, **`payload`**, or **`other`**.
- One primitive per **published surface** (one REST API version family, one event type family, one export schema). Split breaking changes via a new `CONR-*` id or an explicit **`version`** bump policy documented in the file.

#### 5.4.1 Published REST profile (recommended)

For HTTP/REST boundaries, prefer a **list-oriented** shape so portfolio and graph tooling can summarize services and endpoints without parsing ad-hoc nested maps:

| Field | Role |
| --- | --- |
| **`service_name`** | Logical **application** or deployable (align with HLD component view and repo module names, e.g. `portfolio-api`, `tvp-reach-model-simulator`). |
| **`base_path`** | API prefix (e.g. `/api/v1`). |
| **`endpoints`** | Ordered list of operations; each entry should include **`method`**, **`path`** (relative to `base_path`), stable **`id`**, and optional **`description`**. |

A nested **`schema:`** block may still document resources, error shapes, or examples for humans and contract tests; when the contract is REST, **also** maintain **`endpoints[]`** if downstream tools should build an API catalog. Do not encode runtime “must” rules only inside `schema:` prose—lift them to **`CON-*`** / **`INV-*`** and link.

#### 5.4.2 Event, webhook, and payload profiles

For non-HTTP boundaries:

- Document the **event or payload identity** (name, topic, channel, or file role) and the **schema** or shape (JSON Schema, Avro, protobuf reference, or structured YAML).
- Use **`version`** when consumers depend on compatibility rules.
- Do **not** add HTTP **`endpoints[]`** to event-only contracts.

**Traceability.** **`FEAT-*`** bindings should list relevant **`CONR-*`** ids; tests should **`verify`** contracts per **`test_strategy_spec.md`**. Optional portfolio ingest (e.g. architecture/API views) may read REST fields and doc links; that does not change which fields are **normative** in `knowledge/`.

### 5.5 Feature spec (`FEAT-*`)

**Purpose.** A **`FEAT-*`** is the **scoped delivery unit**: what the team is building for a release train, epic, or user-visible capability. It links **product scope** to shared enforceable rules below—not a substitute for them.

**Common fields** (in addition to §4 base fields):

- **`summary`** — capability or release slice in plain language; may reference PRD user stories (`US-*`) or functional requirements (`FR-*`) by id in prose or **`references`**.
- **`bindings`** — lists of existing **`CON-*`**, **`INV-*`**, **`DEC-*`**, **`CONR-*`** ids this slice depends on or must satisfy. Prefer **minimal** bindings (only what the capability needs), not every platform rule in the product.
- **`success_criteria`** — acceptance checklist; test plans may map **`TC-*`** cases here.
- **`references`** — paths to PRD, HLD, test plan, UX wireframes (as used in sibling files).
- **`parent_feature`** *(optional)* — id of an umbrella **`FEAT-*`** when this file is a **child capability spec**.
- **`child_features`** *(optional)* — list of child **`FEAT-*`** ids when this file is an **umbrella** (release train or phase) that delegates detail to children.

**Granularity — when to add or split `FEAT-*`**

| Slice FEAT when… | Do not slice when… |
| --- | --- |
| Distinct bindings, criteria, or test ownership | One shared **CON/INV** across many features |
| Product-shaped graph nodes | Per button, field, or endpoint |
| Umbrella too large — add children | Duplicating **CON/INV/CONR** as feature files |

**Umbrella + child:** umbrella lists **`child_features`**; children carry criteria and minimal **`bindings`**.

**Product rules:** PRD must/must-not → **CON/INV** (Knowledge PR); PRD references IDs (RULE-012). **CON/INV/CONR** are shared — multiple **FEAT** bind; do not copy per slice.

**What vs how:** **FEAT** = user-visible capability; stack/migration → **DEC** / HLD. New-repo modernization: legacy **FEAT** from observed behavior; target stack/cutover in new repo **DEC** ([modernization_new_repo_spec.md](modernization_new_repo_spec.md)).

### 5.6 Primitive depth and implementation readiness

Thin primitives are OK for **discovery**; do not drive **`src/`** until **L2+** (layered across artifacts — see agent card).

| Level | Safe to implement? | Minimum |
| --- | --- | --- |
| **L0** | No | Umbrella FEAT and/or PRD |
| **L1** | Risky | Child FEAT + **≥3** observable **`success_criteria`** |
| **L2** | Yes (bounded) | L1 + **bindings** (CON/INV; CONR at seams) |
| **L3** | Yes (predictable) | L2 + HLD + **TC-*** mapped |

Optional **`implementation_readiness`** on **FEAT:** **`draft`** | **`ready`** (L2+ met). Umbrella FEAT may stay **`draft`** until children **`ready`**.

Validator **depth warnings** (§9): shallow child FEAT, CON without **rules**, INV without **statement**. **Agent card:** [../spec-summaries/primitive-depth.md](../spec-summaries/primitive-depth.md)

---

## 6. Supporting narrative artifacts (relationship to this spec)

- **Architecture intent** (conventional **`docs/architecture/`**) — *Human* architecture direction for the product or program: principles, context, NFRs, boundaries. **How to structure** those documents: **`architecture_intent_spec.md`**. This knowledge-model spec only places that folder in the **layer table** (§3) and in the **graph** (§7–8).
- **High-level design** (conventional **`docs/design/`**) — Services or modules, responsibilities, main data flows, how **constraints** and **invariants** show up in the design. **How to structure** those documents: **`design_spec.md`**.
- **Test plan** (conventional **`docs/test/`**) — What you will verify for a release or epic; maps to **`INV-*`**, **`CONR-*`**, and **`test_strategy_spec.md`**.

---

## 7. Knowledge graph — node and edge vocabulary

### Node types (conceptual)

Architecture intent (narrative in conventional **`docs/architecture/`** when present), **Constraint**, **Invariant**, **Decision**, **Contract**, **Feature spec** (`FEAT-*`), high-level design narrative, test plan narrative.

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
Constraint / Invariant → Feature spec → HLD → Code → Tests
```

Alternate shorthand used in reviews:

```text
Constraint / Invariant → Spec → HLD → code → tests
```

**Meaning:** Product and architecture direction inform **decisions** and **design**. **Primitives** bind **features**; implementation and tests must remain traceable to **`FEAT-*`**, **`INV-*`**, **`CON-*`**, and **`CONR-*`**.

---

## 9. Validation (targets for local scripts and CI)

### Primitive validation

- Required fields present for each `kind`.
- No dangling **`FEAT-*`** bindings (every referenced ID exists in **`knowledge/`** and **`catalog.yml`**).
- Published **REST** **`CONR-*`** (when `interface_type` is `rest` or the file defines HTTP **`endpoints`**): **`service_name`** present; **`endpoints`** non-empty unless the contract is explicitly marked draft or internal-only in **`status`**.

### Cross-validation

- Feature spec bindings resolve to real primitives.
- HLD and PRD narratives do not contradict merged primitives (resolve in **`knowledge/`** first).
- HLD **component** names and architecture intent **runtime** sections should align with **`service_name`** on REST **`CONR-*`** for the same slice (resolve naming in **`knowledge/`** or docs in one PR).

### Graph integrity (when tooling exists)

- No orphan primitives that should be linked.
- No duplicate IDs across primitives.

### Depth warnings (non-blocking; `.kfs/validate/validate-knowledge.py`)

Emit **warnings** (not merge blockers) when:

- **Child** **`FEAT-*`** (no non-empty **`child_features`**) lacks **`success_criteria`**, has fewer than **three** criteria, or sets **`implementation_readiness: ready`** without meeting L2 bindings.
- **`CON-*`** with non-draft **`status`** lacks non-empty **`rules`**.
- **`INV-*`** with non-draft **`status`** lacks **`statement`**.

Teams may tighten warnings to **errors** in CI when **`implementation_readiness: ready`** is set on merged **`FEAT-*`**.

---

## 10. Implementation phases (evolution of tooling)

1. **Schema and catalog checks** — validate YAML shape and `catalog.yml`.
2. **Graph extraction** — optional: build edges from `related_primitives`, structured **`references`** paths, and primitive ID mentions in docs.
3. **Impact and trace queries** — optional: “what uses this `CON-*`?” for change analysis.

---

## 11. Summary

- **`knowledge/`** is the **source of truth** for enforceable rules and feature bindings.
- **Documentation** in **product `docs/`** (when present) explains, motivates, and links IDs.
- **This specification** is the **single place** agents should cite for **layers, primitive kinds, graph vocabulary, validation, and end-to-end trace flow** in the knowledge-first system.
- **Architecture intent** *documents* (conventional **`docs/architecture/`**) are specified in **`architecture_intent_spec.md`**.
- **High-level design** *documents* (conventional **`docs/design/`**) are specified in **`design_spec.md`**.

---

## 12. One-line summary

> Define knowledge precisely in **`knowledge/`**, connect it to docs and code, and validate links so the system stays traceable and enforceable.

---

## 13. Change log (required)

Specifications under **`.kfs/specs/`** (including this file) end with **`## Change log`** or **`## Version history`** as the **last** heading, with a table of revisions (see sibling `*_spec.md` files for column guidance). Product narrative under conventional **`docs/`** (when present) follows the change-log rules in **`prd_spec.md`**, **`architecture_intent_spec.md`**, **`design_spec.md`**, and **`test_strategy_spec.md`** as applicable.

### This spec

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 1.1 | 2026-05-06 | approved | — | Prior release (separate Document version footer). |
| 1.2 | 2026-05-11 | approved | — | Terminal change log; removed duplicate Document version footer. |
| 1.3 | 2026-05-12 | approved | — | **`docs/`** paths documented as conventional; guides-only template may omit empty tree (**README**). |
| 1.4 | 2026-05-17 | approved | — | **`CONR-*`** §5.4 expanded: REST profile (`service_name`, `base_path`, `endpoints[]`), event/payload profile, validation targets. |
| 1.5 | 2026-05-25 | approved | — | **`FEAT-*`** §5.5: umbrella/child specs, granularity, product-rule lifting; **`CON/INV/CONR`** not sliced like features. |
| 1.6 | 2026-05-22 | approved | — | §5.5 what-vs-how; §5.6 primitive depth ladder, **`implementation_readiness`**, validator depth warnings (§9). |
| 1.7 | 2026-05-22 | approved | — | §4.3 lifecycle; **PLAN-*** layer |
| 1.8 | 2026-05-22 | approved | — | §5.5–5.6 compacted; depth detail in spec-summary |
