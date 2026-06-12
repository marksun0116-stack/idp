# Technical debt discovery — specification

This guide defines **how to discover, score, and record technical debt** in a knowledge-first repository using **reverse engineering** of the **as-is system**. It complements **[adopt_existing_repo_spec.md](adopt_existing_repo_spec.md)** / **reverse-engineer-baseline** (baseline docs and **`knowledge/`**) and **supply-chain gates** in `cicd_spec.md`.

**See also:** [specs README](README.md) · [kfs-handbook/use_guide.md](../../kfs-handbook/use_guide.md) · [`knowledge_model_spec.md`](knowledge_model_spec.md) · [`cicd_spec.md`](cicd_spec.md) · [`test_strategy_spec.md`](test_strategy_spec.md) · [`infrastructure_spec.md`](infrastructure_spec.md)

---

## 1. Purpose and scope

### 1.1 What this spec is for

- A **repeatable process** for humans and agents to produce a **technical debt register** from code, config, tests, and deployment artifacts.
- **Current-state baseline** documentation that supports onboarding, risk review, slice prioritization, and (optionally) migration planning.
- **Evidence discipline** so debt items are actionable, not opinion lists.

### 1.2 What this spec is not

- Not a substitute for **`knowledge/`** primitives (`CON-*`, `INV-*`, `DEC-*`, `CONR-*`, `FEAT-*`). Debt findings describe **cost and risk in the present**; normative rules still live in primitives after human review.
- Not a mandate to compare every item to a **target architecture** (that is a separate mode — see **§3**).
- Not a license to record “should rewrite as X” without describing **observed present cost**.

### 1.3 Fit with knowledge-first

```text
Observe (reverse engineer)  →  Record (`docs/assessment/`)  →  Prioritize  →  Govern change (FEAT / DEC / CON / tests)
```

Technical debt discovery **feeds** slice inventory, current-state docs, and modernization decisions. When a finding implies a **new rule** or **migration choice**, promote it through a **Knowledge PR** (`DEC-*`, `CON-*`, `INV-*`) — do not leave enforceable intent only in a debt spreadsheet.

---

## 2. What belongs here vs in other artifacts

| Topic | Technical debt register (`docs/assessment/`) | Architecture intent (`docs/architecture/`) | `knowledge/` primitives | `cicd_spec.md` |
| --- | --- | --- | --- | --- |
| “ExportServiceImpl is 2,500+ LOC” | **Yes** — debt item with evidence | Optional one-line risk | No | No |
| “CSRF disabled on UI WAR” | **Yes** — security debt + exposure | **Yes** — as-is compromise | **`CON-*`** if policy requires CSRF | SAST/DAST gate |
| “Spring web 6.2.1 vs 6.2.11 skew” | **Yes** — supply-chain debt | Optional | **`DEC-*`** if upgrade is a formal decision | Dependency scan gate |
| “Operators must not delete referenced attributes” | No (product rule) | Link to invariants | **`INV-*`** | Contract/invariant tests |
| “We will move to API-only Boot” | No (target intent) | Target architecture doc | **`DEC-*`** | Deploy pipeline |

### Conventional paths (KFS) — `docs/assessment/`

**Purpose:** Derived **as-is analysis** from code and operations — not product PRD/architecture and not a current/target split. Houses the **slice map** (where the system is bounded) and the **tech debt register** (what hurts today), plus optional per-slice notes.

| Artifact | Path |
| --- | --- |
| **Slice inventory** | `docs/assessment/slice_inventory.md` |
| **Tech debt register** | `docs/assessment/tech_debt_register.md` |
| **Per-slice flow notes** | `docs/assessment/slices/{slice}_notes.md` (optional) |
| **Index** | `docs/assessment/README.md` |

Baseline product truth stays in **`docs/prd/`**, **`docs/architecture/`**, and **`knowledge/`**.

**Legacy ingest path:** Some tools (e.g. EKCC **`CONR-EKCC-INGEST-002`**) still expect `docs/current/tech_debts_current.md`. Until those contracts update, symlink or copy the register to the legacy path — do **not** use `docs/current/` for baseline PRD, architecture, or HLD.

---

## 3. Discovery modes

Choose one mode explicitly at the start of a pass.

| Mode | Question answered | Target docs used? |
| --- | --- | --- |
| **A — As-is only** | What is costly or risky **today**? | **No** |
| **B — Migration gap** | What blocks or complicates a **known target**? | **Yes** — gap matrix vs target PRD/HLD |
| **C — Slice paydown** | What debt matters for **one governed slice**? | Optional |

**Default for legacy adoption:** start with **Mode A** until current-state slices and a register exist. Add **Mode B** only when target architecture is approved (`DEC-*`).

---

## 4. Principles

1. **Describe reality, not aspiration** — Every item states what is true and why it hurts **now**.
2. **Slice-first** — Partition by boundaries that change together (routes, jobs, modules, integrations). See slice inventory in `use_guide.md` Scenario 2.
3. **Evidence labels** — Tag each fact:
   - **(observed)** — seen in code, config, tests, CI, or runtime artifacts
   - **(inferred)** — reasonable conclusion from multiple observations
   - **(open question)** — needs SME, staging probe, or production data
4. **Classify legacy behavior** (when behavior is in scope): **preserve**, **fix**, **clarify**, **drop**, or **unknown** — not “refactor because old.”
5. **Separate code debt from library/infra debt** — Same register, distinct categories (§7–8).
6. **Fail loudly on contradictions** — If docs and code disagree, record both and mark **open question**; do not pick a silent winner.

---

## 5. Process overview

```text
Prepare → Boundaries → Flows → Dependencies → Code signals → Tests → Supply chain → Score → Publish → (Optional) Promote to knowledge
```

### Phase 0 — Prepare

| Step | Action |
| --- | --- |
| 0.1 | Confirm **mode** (§3) and **scope** (whole system vs one slice). |
| 0.2 | Locate existing **slice inventory**, current PRD/architecture intent, and `knowledge/catalog.yml`. |
| 0.3 | Identify **production deploy path** (CI, Docker, Helm, legacy install scripts) — not only source tree. |

### Phase 1 — Identify boundaries (slice map)

Sources: HTTP routes, controllers/handlers, UI pages, schedulers, message consumers, DB schemas, external APIs, git churn.

**Output:** slice table — name, entry points, core modules, data stores, external systems, inferred priority, open questions.

**Artifact:** `docs/assessment/slice_inventory.md` (or extend existing).

### Phase 2 — Map entry points and flows

Per slice (or top N by churn/risk):

- Catalog sync/async surfaces, auth model, input/output formats, error behavior.
- Trace **2–4 representative flows** (happy path + one failure path): validation → logic → persistence → side effects.

**Output:** flow notes or sequence sketches in **`docs/assessment/slices/{slice}_notes.md`** (optional).

### Phase 3 — Inventory dependencies

| Layer | Examples |
| --- | --- |
| Runtime | DB, cache, search, queues, third-party HTTP, filesystem |
| Build | Language version, frameworks, pinned vs floating deps |
| Deploy | Container base image, orchestrator, secrets, env-specific config |
| Frontend (if applicable) | Bundled JS/CSS libraries not in package lockfiles |

Note **version skew** (client vs server vs install scripts vs sibling repos).

### Phase 4 — Code and operability signals

Scan for present cost (automated + manual):

| Signal | Typical debt type |
| --- | --- |
| File/class size, nesting depth | Complexity |
| Duplicated validation or mapping | Duplication |
| God services, cross-slice imports | Coupling |
| Broad `catch (Exception)`, empty catches | Reliability |
| TODO/FIXME on critical paths | Incomplete behavior |
| Singletons outside DI | Testability |
| Missing timeouts/retries on external calls | Reliability |
| Dual deploy/install paths | Operational |

### Phase 5 — Test and verification gap

Compare `src/` (or equivalent) to test directories:

- Coverage by **slice** and **boundary** (HTTP, contract, job), not only line coverage.
- Presence of **characterization** and **contract** tests at migration boundaries.
- Flaky or manual-only verification paths.

Link gaps to **`test_strategy_spec.md`** and future **`INV-*` / `CONR-*`** tests — debt register records the gap; primitives encode the rule once agreed.

### Phase 6 — Supply chain and infrastructure (§8)

Run dependency and platform inventory (§8.3). Record CVE-backed findings with scan date and limits.

### Phase 7 — Score and prioritize (§9)

Assign severity, effort, and qualitative priority per item. Produce Critical / High / Med summary.

### Phase 8 — Publish

Write or update **`docs/assessment/tech_debt_register.md`** with:

- Methodology summary (what was run)
- System snapshot metrics
- Debt register tables by slice and category
- Open questions
- Suggested next steps (documentation and measurement — not mandatory redesign)
- **Change log** (§10)

### Phase 9 — Promote to knowledge (optional)

| Finding type | Promote to |
| --- | --- |
| Migration strategy choice | `DEC-*` |
| New security/tenancy must | `CON-*` |
| Property tests must always hold | `INV-*` |
| Boundary schema for strangler | `CONR-*` |
| Slice scope for paydown | `FEAT-*` |

Use **`merge-knowledge-first`** skill before implementation PRs.

---

## 6. Evaluation dimensions (code and design debt)

Score each finding **1 (low) – 5 (high)** on relevant dimensions. Not every dimension applies to every item.

| Dimension | Question |
| --- | --- |
| **Comprehensibility** | Can a new developer understand and change this area safely? |
| **Complexity** | Is logic unnecessarily hard (size, nesting, state machines)? |
| **Coupling / cohesion** | Are slices entangled? Shared mutable state? |
| **Duplication / inconsistency** | Same rule implemented in multiple places or formats? |
| **Data integrity** | Clear source of truth? Transaction boundaries? Schema drift? |
| **Reliability** | Error handling, idempotency, resource cleanup, degradation? |
| **Security** | Authn/authz gaps, input validation, secrets handling? |
| **Test gap** | Regressions likely undetected? |
| **Operability** | Logs, metrics, health checks, runbooks, deploy complexity? |

**Prioritization heuristic (qualitative):**

```text
Priority ≈ (Severity × blast radius × change frequency) / effort
```

Adjust with **incident history** and **roadmap pressure** when data exists.

---

## 7. Debt item schema (register row)

Use stable IDs: **`TD-{scope}-{nnn}`** (example: `TD-160` for IMPEX slice) or **`TD-{nnn}`** globally — pick one convention per repo and stay consistent.

| Field | Required | Description |
| --- | ---: | --- |
| **ID** | Yes | Stable identifier |
| **Slice** | Yes | Boundary name or `system` |
| **Category** | Yes | See §7.1 |
| **Title** | Yes | Short description |
| **Observation** | Yes | What was seen (paths, modules, config keys) |
| **Impact** | Yes | Why it hurts **today** (velocity, reliability, security, cost) |
| **Evidence** | Yes | observed \| inferred \| open question |
| **Severity** | Yes | 1–5 |
| **Effort** | Yes | 1–5 (remediation in present architecture) |
| **Priority** | Yes | Critical \| High \| Med \| Low |
| **Behavior class** | No | preserve \| fix \| clarify \| drop \| unknown |
| **Links** | No | Related `FEAT-*`, incidents, PRs |

### 7.1 Categories

| Category | Examples |
| --- | --- |
| Comprehensibility | Naming drift, missing docs, stale comments |
| Complexity | Large classes, deep nesting |
| Coupling | Cross-slice imports, monolith blast radius |
| Duplication | Repeated validation, parallel code paths |
| Data | Dual stores, unclear authority, query risk |
| Security | Auth gaps, CSRF, exposed batch endpoints |
| Reliability | Swallowed errors, missing mutex on jobs |
| Tests | Missing characterization/contract tests |
| Ops | Dual runbooks, weak observability |
| Supply chain | EOL libraries, CVEs, version skew |
| Infrastructure | Platform EOL, multi-container complexity |

---

## 8. Supply chain and infrastructure debt

### 8.1 When libraries/infra count as debt

Include when **any** of the following is true **today**:

- **EOL or unmaintained** — no safe patch path
- **Known CVE** — confirmed by scanner or vendor advisory
- **Upgrade blocker** — prevents runtime, framework, or platform bumps
- **Version skew** — client, server, install script, or sibling repo disagree
- **Operational burden** — opaque JARs, manual install paths, extra containers
- **Compliance gap** — audit requires supported versions or SBOM

**Do not** score “not latest version” alone without present cost.

### 8.2 Support status labels

| Label | Meaning |
| --- | --- |
| **Maintained** | Active security patches |
| **Legacy** | Old but still patched (limited) |
| **EOL / unmaintained** | No safe patch path |
| **Skew** | Mismatch between components or repos |
| **Unknown** | Needs scan or vendor confirmation |

### 8.3 Discovery steps

1. **Extract direct dependencies** — lockfiles, `pom.xml`, `package.json`, `go.mod`, etc.
2. **Inventory platform/runtime** — language, app server, DB drivers, search/orchestrator versions from **deploy repos** as well as app repo.
3. **Inventory bundled frontend assets** — vendored JS not in package managers.
4. **Compare deploy paths** — CI/CD vs legacy install scripts (what production actually uses).
5. **Run vulnerability scan** — OSV, OWASP Dependency-Check, GitHub Dependabot, or org standard; record **date**, **tool**, and **limits**.
6. **Record remediation type** — patch bump \| coordinated upgrade \| compensating control \| retire path \| accept with documentation.

### 8.4 Scan limits (always document)

| Limit | Record in register |
| --- | --- |
| Direct vs transitive deps | Which were scanned |
| Private artifacts | Unresolvable internal JARs |
| Frontend | Manual/CVE-class notes if not in scanner |
| Transitive closure | “True count likely higher” if direct-only |

Align blocking gates with **`cicd_spec.md` §2.6**.

---

## 9. Scoring and summary bands

### 9.1 Severity guide

| Score | Guide |
| ---: | --- |
| 1 | Minor friction; unlikely incidents |
| 2 | Noticeable dev slowdown |
| 3 | Regular defect or ops toil |
| 4 | Significant outage or security exposure class |
| 5 | Critical: active exploit class, data loss, or change paralysis |

### 9.2 Summary bands

| Band | Typical use |
| --- | --- |
| **Critical** | Address before large refactors or major releases |
| **High** | Plan in next few sprints / slice cycles |
| **Med** | Track; fix when touching the area |
| **Low** | Accept or fix opportunistically |

Publish a **priority summary** table in the register (IDs only — details stay in register sections).

---

## 10. Document requirements for the debt register

Files under **`docs/assessment/`** that follow this spec should include:

**Frontmatter (YAML, recommended):**

```yaml
doc_id: TD-{PRODUCT}-REGISTER-001
title: "{Product} — technical debt register"
status: draft
last_updated: YYYY-MM-DD
method: reverse-engineering
scope: as-is only   # or as-is + migration-gap
```

**Body sections (minimum):**

1. Methodology (phases run, tools, date)
2. System snapshot (metrics)
3. Debt register (by slice / category)
4. Supply chain & infrastructure (if Phase 6 run)
5. Priority summary
6. Open questions
7. Suggested next steps
8. Traceability links (slice inventory, architecture intent, catalog)
9. **Change log** (terminal section)

### Change log (required)

End every debt register with **`## Change log`** or **`## Version history`**. Table columns: **revision**, **date**, **status**, **supersedes**, **notes**.

---

## 11. Automation and CI integration

Recommended pipeline additions (detail in program **`cicd_spec.md`**):

| Gate | Purpose |
| --- | --- |
| Dependency/OSV scan | Fail or warn on Critical/High **direct** deps |
| Knowledge validation | Unchanged — `catalog.yml` and primitives |
| Optional | Break build on new debt IDs without doc update when touching governed slices |

**Cadence:** full register refresh ** quarterly** or before major modernization slice; **dependency scan** every PR or nightly.

---

## 12. Agent and human workflow

| Role | Responsibility |
| --- | --- |
| **Human owner** | Approves scope, priority, and any promotion to `knowledge/` |
| **Agent** | Executes Phases 1–8, labels evidence, drafts register PR |
| **Architect** | Resolves open questions; approves Mode B gap analysis |
| **Security / platform** | Validates CVE severity and infra EOL dates |

**Skills (when present in repo):** `discover-tech-debt` (this process), `reverse-engineer-baseline` (baseline adoption), `validate-knowledge-graph` (after knowledge promotion), `merge-knowledge-first` (PR order).

**Rules:** Do not treat chat or ad-hoc markdown outside **`knowledge/`** and **`.kfs/specs/`** as authoritative schema (RULE-012). The debt register is **derived current-state** narrative — not a second source of CON/INV.

---

## 13. Validation checklist

Before merging a debt discovery PR:

- [ ] Mode (A/B/C) stated in register
- [ ] Slice map exists or updated
- [ ] Every Critical/High item has **(observed)** or **(inferred)** evidence
- [ ] Open questions listed where staging/SME needed
- [ ] Supply-chain scan date and limits recorded (if Phase 6 run)
- [ ] No normative “must” rules introduced only in debt doc (lift to `knowledge/` if needed)
- [ ] **`docs/assessment/README.md`** links to register
- [ ] Change log updated

---

## 14. Example reference (non-normative)

The **ctaas-profile-manager** program applied an earlier shape of this spec (legacy path `docs/current/tech_debts_current.md`). New repos use **`docs/assessment/tech_debt_register.md`**. Use as a **shape example**, not a mandatory template.

---

## 15. Traceability

| Artifact | Relationship |
| --- | --- |
| `docs/assessment/slice_inventory.md` | Boundaries for debt rows |
| `docs/assessment/slices/*_notes.md` | Deep flow/context per slice |
| `docs/architecture/` | System-wide architecture context (when present) |
| `knowledge/catalog.yml` | After promotion: `DEC-*`, `CON-*`, `FEAT-*` |
| `cicd_spec.md` | Scanner gates and cadence |
| `test_strategy_spec.md` | Test-gap remediation |

## 16. EKCC Observe integration

When a repository is registered in **Enterprise Knowledge Command Center (EKCC)**, the debt register feeds the Observe **tech debt** cockpit widget.

| Topic | Source repo (canonical) | Legacy ingest (EKCC) | EKCC artifact |
| --- | --- | --- | --- |
| Authoring | `docs/assessment/tech_debt_register.md` (+ optional `ekcc_widget_summary`, `debt_rows` block) | `docs/current/tech_debts_current.md` until **CONR-EKCC-INGEST-002** updates | Ingest per contract |
| Slice context | `docs/assessment/slice_inventory.md` | `docs/current/slice_inventory.md` | Slice labels in widget filters |
| Read UI | — | **`tech_debt`** widget on **`CONR-EKCC-BFF-003`** (`include_widgets=true`) |
| Driving feature | — | **`FEAT-EKCC-PH1-002`** |
| Design | — | **HLD-EKCC-TD-INGEST-001** (`Enterprise-Knowledge-Command-Center/docs/design/enterprise_knowledge_command_center_tech_debt_ingest_hld.md`) |

**Widget behavior:** Connected when register present at gold HEAD; **not_connected** with reason when absent — **no synthetic Critical/High counts** (FR-PH1-014 parity). Debt rows remain **derived current-state** narrative; promote enforceable rules to `knowledge/` via Knowledge PR only.

**Recommended register extras for EKCC:** YAML frontmatter fields `revision`, `register_format_version`; fenced block `# ekcc_widget_summary` for roll-up counts; fenced `# debt_rows` YAML array for stable parse (see ingest contract and HLD). **Do not** rely on HTML comments for widget summary — use fenced YAML with `# ekcc_widget_summary`. Use field name **`debt_id`** (not `id`). Scoped IDs (e.g. `TD-BISHOST-001`) are supported.

**Authoritative parse:** When a fenced `# debt_rows` block is present, it is the source of truth for silver rows; partial or invalid blocks prevent markdown-table fallback. Markdown tables remain a fallback for legacy registers.

**Maximize widget (Observe):** Each debt card must show **observation** (what was seen), **impact**, **severity** and **effort** (1–5), optional **behavior_class** (`preserve` | `fix` | `clarify` | `drop`), and **source_paths** (link per path). BFF exposes these on `widgets[].links[]` per **CONR-EKCC-BFF-003**.

---

## Change log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 1.0 | 2026-05-22 | approved | — | Initial spec: reverse-engineering process, evaluation dimensions, supply chain, register schema, KFS integration. |
| 1.1 | 2026-05-22 | approved | 1.0 | §12 — **`discover-tech-debt`** Cursor skill reference. |
| 1.2 | 2026-05-22 | approved | 1.1 | §16 — EKCC Observe **tech debt** widget integration (**FEAT-EKCC-PH1-002**, **CONR-EKCC-INGEST-002**). |
| 1.3 | 2026-05-22 | approved | 1.2 | Canonical paths under **`docs/assessment/`**; legacy EKCC ingest path documented. |
| 1.4 | 2026-05-22 | approved | 1.3 | Renamed from **`docs/tech-debt/`** — folder holds slice inventory + debt register. |
| 1.5 | 2026-06-11 | approved | 1.4 | §16 — debt_rows authoring, scoped `debt_id`, maximize UI field list (observation, impact, scores, behavior_class, source_paths). |
