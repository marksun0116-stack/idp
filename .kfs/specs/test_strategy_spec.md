# Knowledge-first test strategy — specification

This guide defines **where and how** to document testing: frameworks, automation, regression, performance, and (optionally) a **change-centric quality engineering** operating model (**§3**). It complements **architecture intent** (conventional **`docs/architecture/`** when present), which should only state **principles** and **links** to `knowledge/` and to this file.

**See also:** [guides index](../README.md) · [`../use_guide.md`](../use_guide.md) · `cicd_spec.md` (what runs in CI)

---

## 1. What belongs here vs in architecture intent

| Topic | `docs/architecture/` (intent) | This spec (`test_strategy_spec`) |
| --- | --- | --- |
| “We require contract tests for `CONR-*` at service boundaries” | Yes — short, link to `DEC-*` or `CON-*` if needed. | Expand: tools, folder layout, examples. |
| Test stack (xUnit, pytest, Playwright, k6, …) | Optional one-liner. | **Yes** — versions, ownership, when to add new kinds. |
| Regression scope for a release | Link to `docs/test/` or program process. | **Yes** — how regression suites are built and run (local vs CI). |
| Load / performance SLOs and how they are proven | NFR + link to `INV-*` if applicable. | **Yes** — environments, scenarios, thresholds, gating. |

**Product- or release-specific** test plans stay in **`docs/test/`** and must reference **`INV-*`**, **`CON-*`**, and **`CONR-*`** by ID. Each test-plan markdown file must **end** with **`## Change log`** or **`## Version history`** as the **last** heading, per the terminal change-log requirement in **§5** of this spec (see **`.kfs/agent/runbooks/merge-knowledge-first.md`** — *Releases and documentation lifecycle*).

---

## 2. Required sections (fill for your program)

### 2.1 Test philosophy

- How **unit**, **integration**, **contract**, **e2e**, **security**, and **characterization** tests relate to your **pyramid** or strategy.
- How every test that encodes a product rule **traces** to a primitive (RULE-010).
- **Spec-driven development (SDD) and knowledge-first:** Treat **`FEAT-*`**, **`DEC-*`**, **`CONR-*`**, and linked **`CON-*` / `INV-*`** as the **normative spec surface** for governed work—so design and implementation PRs stay answerable to the same IDs that appear in reviews and release notes, not to a parallel “shadow spec” in chat or tickets alone.
- **Test-driven development (TDD):** Prefer **red → green → refactor** for new behavior: express the expected outcome as a **failing test** (or contract check) **before** implementation, grounded in **FEAT / INV / CONR** where applicable. For **existing** codebases, when requirements or behavior change, **add or extend tests first** so the change is specified and visible in CI; use **characterization** tests when locking behavior before refactors (see **reverse-engineer-baseline** runbook). Agents and code generators should follow the same order: **tests that encode the rule, then `src/`**, before merge.
- **Strengthening TDD with primitives:** When failures and merges are discussed in terms of **which primitive** was violated or extended, TDD stops being “tests first” in isolation and becomes **executable traceability** for the same rules SDD commits to in **`knowledge/`**.

**Release test plans (`docs/test/`):**

- List **driving `FEAT-*`** in frontmatter (umbrella and/or child capability specs).
- Optional **slice map** table after the introduction: capability name → driving **`FEAT-*`** → primary **`TC-*`** groups (see **`merge-knowledge-first`** skill).
- In **`TC-*`** rows, **Traces to** should include the **driving `FEAT-*`** for scope **and** the **`CON-*` / `INV-*` / `CONR-*`** the test asserts (not **`FEAT-*`** alone when a rule is under test).

### 2.2 Frameworks and layout

- Languages and test runners.
- **Directory layout** under `tests/` (or per-module conventions).
- Shared fixtures, factories, and test data (including PII policies — link `CON-*`).

### 2.3 Contract and API testing

- How **`CONR-*`** are verified (schema tests, consumer-driven tests, etc.).
- Mocking rules for external systems.

### 2.4 Automation

- What runs on **every commit**, **every PR to `feature/*`**, **feature → `main` merge**, and **`release/*` maintenance** (align with `cicd_spec.md` §2.1).
- Flake and quarantine policy.

### 2.5 Regression

- What counts as the **regression** suite for your product.
- How **existing** or **slice** work adds **characterization** tests before refactors (see **reverse-engineer-baseline** runbook).

### 2.6 Performance and load

- Tools, environments (staging, perf lab), and **pass/fail** criteria.
- Links to NFRs and **invariants** (e.g. latency ceilings as `INV-*` or `CON-*`).

### 2.7 Test data and environments

- Anonymized data, reset strategy, who owns test accounts and secrets (cross-link **`infrastructure_spec.md`** as needed).

---

## 3. Quality engineering operating model (optional program pattern)

This section describes a **change-centric**, **reuse-first** way to run quality engineering (QE) on top of knowledge-first. It **does not** replace **`knowledge/`** primitives or the standard workflow in **`use_guide.md`**; it adds discipline for **test assets** (scenarios, data, automation) and for promoting learning into **official** docs and primitives after runs.

**Fit with knowledge-first**

- **Official** rules and scope stay in **`knowledge/`** (`CON-*`, `INV-*`, `DEC-*`, `CONR-*`, `FEAT-*`) and in reviewed narrative under **`docs/`** (see **[README.md](../README.md)** in the knowledge-first-system doc folder — terminology for **official** vs **draft** vs **derived**).
- **Operational** QE artifacts (impact notes, delta plans, execution plans, triage summaries) can live under **`docs/test/`**, a team `docs/qe/` folder, or change-pack markdown your program agrees on—they must **point to** primitive IDs and **`FEAT-*`** when they encode decisions, not invent shadow requirements.

### 3.1 Principles

- **Change-aware lifecycle** — Default is to **maintain** scenarios, data, helpers, and suites as the product changes, not only to generate net-new artifacts.
- **Reuse first** — Prefer extending existing cases, rows, helpers, and scripts before adding duplicates.
- **Official knowledge** — What the team treats as true after review is **official**; promote updates from triage and pilots through the same merge and **Knowledge PR** discipline as code.
- **Targeted execution** — Select **must-run** and **recommended** suites from **risk and impact**, not “run everything” by default (details in your **§2.4** / **§2.5** program fill-in).

### 3.2 Change-centric stages

Use the **same lightweight stages** for net-new behavior and for behavior-changing fixes. At each stage, decide explicitly whether to **reuse**, **create**, **update**, **merge**, or **retire** the relevant assets.

| Stage | Purpose | Typical outputs (examples) |
| --- | --- | --- |
| **1. Change intake** | Scope, linked stories or **`FEAT-*`**, assumptions, open questions | Intake note or ticket appendix |
| **2. Impact analysis** | Map affected workflows, rules, tests, data, automation | Impact map (links to **`INV-*` / `CONR-*`** where applicable) |
| **3. Delta planning** | Decide create / update / merge / retire per asset type | Scenario delta, data delta, automation delta (markdown or tables) |
| **4. Asset update** | Revise cases, data, helpers, scripts, tags | PRs against `tests/` and related code |
| **5. Execution selection** | Choose suites, data prerequisites, environments | Execution plan; align with **§2.4** automation tiers |
| **6. Failure triage** | Classify failures (product vs test vs data vs environment vs flake); dedupe | Triage summary; defect text |
| **7. Knowledge refresh** | Promote reviewed learning into **official** docs and, when rules change, **`knowledge/`** | Doc updates, **Knowledge PR** for new or changed **`INV-*` / `CON-*` / `CONR-*`** |

Stages **1–4** align with **merge-knowledge-first** and **implement-with-primitives** (intent and primitives before or with code). Stages **5–7** close the loop so runs produce **signal** and incidents feed the graph (**§4** traceability).

### 3.3 Asset decisions (create / update / merge / retire)

For each change, the team should record at least one decision per **asset class** below (even if the decision is **no change**), so automation and data do not silently sprawl.

| Asset class | Examples | Reuse-first prompts |
| --- | --- | --- |
| **Scenarios / cases** | Manual or automated case specs, BDD scenarios | Extend or **merge** overlapping cases before adding new ones |
| **Test data** | Rows, fixtures, factories, reference data | Extend rows; **merge** duplicates; **retire** stale combinations |
| **Automation** | Page objects, API clients, helpers, jobs | Reuse helpers; **merge** duplicate flows; **retire** dead code |
| **Shared QE knowledge** | Catalogs, “how we test X”, change packs | Consolidate notes into **official** indexed docs; link **`FEAT-*`** |

When a failure shows a **wrong or missing rule**, treat it as a **knowledge upgrade** (primitive + tests), not only a local script fix.

### 3.4 Roles and agent skills (reference mapping)

QE responsibilities can be split across people or **AI agents** (for example Cursor with project rules and skills). Map work to this repo’s skills so prompts stay scoped:

| QE-style role | Reads / uses | Produces | Typical skills / rules |
| --- | --- | --- | --- |
| **Change intelligence** | Intake, product docs, **`FEAT-*`** | Impact map | **`merge-knowledge-first`**, **`validate-knowledge-graph`** |
| **Test design** | Impact map, **`INV-*` / `CONR-*`**, catalogs | Scenario / coverage delta | **`implement-with-primitives`** (test-first), use guide |
| **Test data stewardship** | Scenario delta, data rules **`CON-*`** | Data delta, minimal new fixtures | Infra + test strategy **§2.7** |
| **Automation engineering** | Deltas, framework conventions | Automation PRs | **`implement-with-primitives`**, **`implementation-constrain-validate-test.mdc`** |
| **Execution selection** | Impact map, suite catalog, risk | Execution plan | Test strategy **§2.4–2.5**, **`cicd_spec.md`** |
| **Triage** | Results, classification rules | Triage summary, defects | Traceability to **`INV-*` / `CONR-*`** |
| **Curation / knowledge refresh** | Closure notes, **`knowledge/`** | **Official** doc + catalog updates | **`merge-knowledge-first`**, **`validate-knowledge-graph`** |

### 3.5 Guardrails

- **Ownership and triggers** — Define who updates which catalog and **when** (feature merge, release branch, defect closure).
- **Scope agent inputs** — Use indexes, “applies-to” metadata, or thin slices so agents do not load unbounded context (align with **slice-first** legacy adoption in **`executive_overview.md`**).
- **Hygiene over bureaucracy** — Prefer small, reviewable deltas and catalog validation over heavy process for its own sake.

### 3.6 Rollout

- **Pilot** on one workflow or service slice; measure duplicate reduction and reuse.
- **Augment** the existing automation stack; avoid “replace the framework” as a prerequisite.
- **Operationalize** execution-selection and triage patterns, then treat the model as the default for governed work.

---

## 4. Traceability

- Invariant tests → **`INV-*`** (and file paths in primitive **evidence** when used).
- Contract tests → **`CONR-*`**
- Security/compliance tests → **`CON-*`**
- Feature-level coverage → driving **`FEAT-*`**

---

## 5. Change log (required)

Test strategy documents and **product test plans** under **`docs/test/`** that follow this specification must end with **`## Change log`** or **`## Version history`** as the **last** heading. Include a table with at least **revision**, **date** (`last_updated`), **status** (draft | review | approved | superseded), **supersedes** (`—` when none), and **what changed**. Keep the latest row aligned with any document metadata at the top. Do not append a second version block after that section.

### This spec

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 1.0 |  | approved | — | Template under `.kfs/specs/` |
| 1.1 | 2026-05-06 | approved | — | **Official** terminology alignment; added **§3** quality engineering operating model (change lifecycle, asset decisions, role-to-skill mapping). |
| 1.2 | 2026-05-06 | approved | — | KFS specs under **`.kfs/specs/`**; spec files renamed (dropped `knowledge_first_system_` prefix). |
| 1.3 | 2026-05-06 | approved | — | **§2.1** — explicit SDD alignment and TDD “executable traceability” wording. |
| 1.4 | 2026-05-06 | approved | — | Mandatory separate Document version footer for this spec and product test plans (superseded by 1.5). |
| 1.5 | 2026-05-11 | approved | — | Single terminal change log for this spec and governed test plans; removed duplicate Document version footer. |
| 1.6 | 2026-05-12 | approved | — | Conventional **`docs/architecture/`** wording in intro. |
| 1.7 | 2026-05-25 | approved | — | **§2.1** — release test-plan slice map; **`TC-*`** traces to **`FEAT-*`** + asserting **`CON/INV/CONR`**. |
