# Knowledge-first CI/CD — specification

This guide documents **pipelines, automation, and quality gates**: how code and `knowledge/` are built, tested, and promoted. It complements **infrastructure** (where things run) in `infrastructure_spec.md` and **test strategy** in `test_strategy_spec.md` (what tests mean).

**See also:** [guides index](../README.md) · `infrastructure_spec.md` (runtime SLOs, platforms) · `.cursor/rules/knowledge-validation-scripts.mdc` · `.cursor/skills/validate-knowledge-graph/SKILL.md`

---

## 1. What belongs here vs in architecture intent

| Topic | Architecture intent (conventional **`docs/architecture/`**) | This spec (CI/CD) |
| --- | --- | --- |
| “Releases require passing tests and knowledge validation” | **Yes** — one paragraph + `DEC-*` if it is a formal gate. | **Yes** — exact stages, branch rules, and tooling. |
| “We use GitHub Actions / GitLab / Jenkins / …” | Optional. | **Yes** — pipeline layout, job names, secrets *names* (not values). |
| PR must validate `knowledge/catalog.yml` | **Intent** | **Job definition**, failure modes, and who fixes. |

---

## 2. Required sections (fill for your program)

### 2.1 Branches and promotion

**Default KFS branch model** (adapt branch names in your program’s **`docs/architecture/`** or CI config, but keep the **roles**):

```text
dev                  integration line — cut feature branches from here
  ↓
feature/*            scoped delivery — default PR target for Knowledge and Implementation work
  ↓ merge when feature tests pass (not per-developer PR)
main                 integration / release-ready line — receives tested feature branches
  ↓ tag when release is ready
release/*            cut from tagged main — post-release maintenance and hotfixes

plan/*, impl/*       optional story branches from PLAN-* — PR into active feature/* (or via feature/*)
```

| Rule | Detail |
| --- | --- |
| **Cut feature branches from** | **`dev`** |
| **Developer PR base (default)** | Active **`feature/*`** for the slice — **never `main`** |
| **Knowledge PR** | Merge **`knowledge/`** into **`feature/*`** (same base as implementation) |
| **Implementation PR** | Merge **`src/`** / **`tests/`** into **`feature/*`** after knowledge is there |
| **Sync (optional)** | **`dev` → `feature/*`** PR to refresh a long-lived feature branch with integration line |
| **Feature → `main`** | After **feature-level** tests pass — typically feature owner or release integrator, not every developer PR |
| **Release** | When **`main`** is release-ready: **tag** on **`main`**, then **cut `release/*`** from that tag |
| **CI by event** | **PR → `feature/*`:** knowledge validate, unit/integration, contract checks. **Merge `feature/*` → `main`:** feature/regression gate. **Tag on `main` + cut `release/*`:** release regression and deploy (see §2.5, §2.8). |

Do **not** document or configure PR templates that default the base branch to **`main`** for day-to-day knowledge or code work.

### 2.2 Build

- How application artifacts and **test** images are built.
- Caching, reproducibility, and **SBOM** or image signing if used.

### 2.3 Knowledge and docs gates (recommended for knowledge-first repos)

- **Validate** `knowledge/` and `catalog.yml` (`.kfs/validate/validate-knowledge-graph.sh` and `.kfs/validate/validate-implementation-plan.sh`).
- Optional: lint for **docs** with broken primitive ID references.

### 2.4 Test stages

- Map to **`test_strategy_spec.md`**: unit, integration, contract, e2e, security scans.
- **Gating:** which failures block merge vs warn.

### 2.5 Artifacts and deployment

- What gets **deployed** to which **environment** (link infrastructure spec for targets).
- **Rollback** and **blue/green** or canary if used.

### 2.6 Security and compliance in the pipeline

- SAST, dependency scanning, secret scanning — and link to **`CON-*`**.

### 2.7 Observability of the pipeline

- Where to see build history, flaky test tracking, and release audit trail.

### 2.8 Site Reliability Engineering (SRE) — pipeline and release alignment

Document how **CI/CD** supports **SRE** goals: safe change velocity, measurable reliability, and fast recovery. **Runtime** SLOs/SLIs and on-call live primarily in **`infrastructure_spec.md`** and product ops docs; this subsection covers what **pipelines and promotions** must expose for SRE workflows.

| SRE theme | CI/CD details to document |
| --- | --- |
| **Release safety** | **Canary**, **blue/green**, **progressive delivery**, or **single-step** deploy; **automatic rollback** triggers (health checks, error-rate or latency thresholds, failed smoke). Who can **freeze** or **override** and how that is audited. |
| **Post-deploy verification** | Mandatory **smoke** or **synthetic** steps after promote; **readiness** / **liveness** gates; **feature-flag** or **kill-switch** checks if your stack uses them. |
| **SLO / error budget (optional)** | Whether **deploy frequency** or **change type** is gated when an error budget is depleted (link to policy in architecture intent or **`DEC-*`**). |
| **Observability correlation** | Pipeline attaches **release version**, **image digest**, and **git SHA** to deployment metadata so **traces and metrics** in production are queryable by release. |
| **Pipeline as a service** | **SLO or internal SLA** for CI itself (queue time, flaky rate, availability of the build system) and **escalation** when CI is the bottleneck during an incident. |
| **Flakes and reliability debt** | **Quarantine** or **retry policy** for tests; **SLO-style** target for flake rate; who **owns** fixing chronic flaky jobs (same bar as production defects). |
| **Incidents and break-glass** | **Emergency deploy** path (who approves, shortened checks, mandatory post-incident review). Whether CI/CD credentials are used during incidents and how access is **time-bound** and **logged**. |
| **Runbooks** | Links from failing **deploy / rollback** jobs to **runbooks** (rollback steps, known failure modes, dependency checks). |
| **Capacity and cost (optional)** | Scheduled or PR-triggered checks for **resource regressions** (e.g. image size, load-test budgets) if your SRE program requires them. |

**Handoff to runtime SRE**

- After promotion, **ownership** for health (dashboards, alerts, paging) should be explicit: pipeline **ends** at a defined boundary (e.g. “Argo synced” vs “traffic shifted 100%”).
- **Knowledge-first:** if a release changes **`CON-*` / `INV-*`** or published **`CONR-*`**, document whether **additional** post-deploy verification or **alert rule** updates are required before the change is considered complete.

---

## 3. Alignment with knowledge-first

- A **Knowledge PR** that changes behavior should pass **knowledge** validation before or as part of the same quality bar as code.
- **Implementation** changes should not skip **`FEAT-*` / primitive** trace when your policy requires it (configure checks or human review in CI).

---

## 4. Change log (required)

Program CI/CD documentation that follows this specification must end with **`## Change log`** or **`## Version history`** as the **last** heading. Include a table with at least **revision** (document version or semver), **date** (`last_updated`), **status** (draft | review | approved | superseded), **supersedes** (`—` when none), and **what changed**. Keep the latest row aligned with any document metadata at the top. Do not append a second version block after that section.

### This spec

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 1.0 |  | approved | — | Template under `.kfs/specs/` |
| 1.1 | 2026-05-06 | approved | — | Added separate document version footer (legacy pattern). |
| 1.2 | 2026-05-11 | approved | — | Single terminal change log; removed duplicate Document version footer. |
| 1.3 | 2026-05-12 | approved | — | Architecture intent column: conventional **`docs/architecture/`**. |
| 1.4 | 2026-05-15 | approved | — | **§2.8:** SRE alignment (safe releases, post-deploy checks, rollback, CI observability, flakes, incidents); **See also** links `infrastructure_spec.md`. |
| 1.5 | 2026-06-12 | approved | — | **§2.1:** Branch model **dev → feature → main → tag → release/**; developer PRs target **`feature/*`**; tested feature merges to **`main`**; release tag then cut **`release/*`**. |
