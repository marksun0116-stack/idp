# Knowledge-first CI/CD — specification

This guide documents **pipelines, automation, and quality gates**: how code and `knowledge/` are built, tested, and promoted. It complements **infrastructure** (where things run) in `infrastructure_spec.md` and **test strategy** in `test_strategy_spec.md` (what tests mean).

**See also:** [guides index](../README.md) · `.knowledge-first-system/rules/knowledge-validation-scripts.mdc` · `.knowledge-first-system/skills/validate-knowledge-graph/SKILL.md`

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

- **Branch model** (trunk-based, git-flow-light, etc.).
- What runs on **pull request** vs **merge to main** vs **release** tags.

### 2.2 Build

- How application artifacts and **test** images are built.
- Caching, reproducibility, and **SBOM** or image signing if used.

### 2.3 Knowledge and docs gates (recommended for knowledge-first repos)

- **Validate** `knowledge/` and `catalog.yml` (see `validate-knowledge-graph` and `.knowledge-first-system/scripts/` when present).
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
| 1.0 |  | approved | — | Template under `.knowledge-first-system/` |
| 1.1 | 2026-05-06 | approved | — | Added separate document version footer (legacy pattern). |
| 1.2 | 2026-05-11 | approved | — | Single terminal change log; removed duplicate Document version footer. |
| 1.3 | 2026-05-12 | approved | — | Architecture intent column: conventional **`docs/architecture/`**. |
