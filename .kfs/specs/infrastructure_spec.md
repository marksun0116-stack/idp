# Knowledge-first infrastructure and deployment — specification

This guide documents **where the system runs**: cloud accounts, networks, environments, deployment topology, secrets handling, and operational runbooks *at a documentation level* (not live credentials). It complements **architecture intent** (conventional **`docs/architecture/`** when present), which should state **decisions and constraints** and **link** here for depth.

**See also:** [guides index](../README.md) · `cicd_spec.md` (how software reaches these environments) · `test_strategy_spec.md` (test environments)

---

## 1. What belongs here vs in architecture intent and HLD

| Topic | Architecture intent (conventional **`docs/architecture/`**) | `docs/design/` (HLD) | This spec (infrastructure) |
| --- | --- | --- | --- |
| “We are multi-tenant, single region for MVP” | **Yes** — and link `DEC-*` / `CON-*` | **Yes** — component and data flow | **Yes** — which accounts, VPCs, clusters (diagrams, CIDRs at doc level). |
| Service A talks to service B | Summary + constraints | **Yes** — interfaces | **Yes** — how traffic routes (LB, service mesh, DNS) if not purely app-level. |
| **Secrets** and key management | High-level (link `CON-*`) |  | **Yes** — which vault, rotation expectations (no real secrets in git). |
| **DR and backups** | RPO/RTO as decisions |  | **Yes** — which regions, backup targets. |

**Rule of thumb:** If an engineer asks “**where** do I run this and **how** do I get access?”, the answer belongs in this guide (or a linked runbook) — not in a 40-page HLD.

---

## 2. Required sections (fill for your program)

### 2.1 Environments

- **Names** (e.g. dev, staging, prod) and **purpose** of each.
- **Data** policy per environment (real PII, synthetic, anonymized).

### 2.2 Cloud and accounts

- Cloud provider(s), account or subscription model, org boundaries.
- **Tagging** and **cost** ownership (optional but useful).

### 2.3 Network and security

- VPCs, subnets, public vs private, peering, firewall policies.
- Link **security** constraints as **`CON-*`**.

### 2.4 Compute and runtime

- Kubernetes, serverless, VMs — what hosts `src/` artifacts.
- Autoscaling and **capacity** notes.

### 2.5 Data stores

- Databases, caches, object storage — by environment; link to HLD for *logical* schema, here for *physical* or managed service names as needed.

### 2.6 Deployment and rollback (runtime)

- How a **release** is applied to each environment (high level). **Pipeline stages** and PR gates are in **`cicd_spec.md`**.

### 2.7 Observability (runtime)

- Logging, metrics, tracing stack; where dashboards live. Link **`CON-*`** for PII in logs if applicable.

### 2.8 Disaster recovery and backups

- RPO/RTO references, backup scope, runbook pointers.

### 2.9 Access and on-call

- Who can deploy, break-glass, escalation paths (no sensitive tokens in docs).

---

## 3. Traceability

- Tenancy, isolation, or regulatory boundaries → **`CON-*`** / **`DEC-*`**
- SLOs that must hold in production → often **`INV-*`** with tests per **test strategy spec**

---

## 4. Change log (required)

Program infrastructure documentation that follows this specification must end with **`## Change log`** or **`## Version history`** as the **last** heading. Include a table with at least **revision**, **date** (`last_updated`), **status** (draft | review | approved | superseded), **supersedes** (`—` when none), and **what changed**. Keep the latest row aligned with any document metadata at the top. Do not append a second version block after that section.

### This spec

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 1.0 |  | approved | — | Template under `.kfs/specs/` |
| 1.1 | 2026-05-06 | approved | — | Added separate document version footer (legacy pattern). |
| 1.2 | 2026-05-11 | approved | — | Single terminal change log; removed duplicate Document version footer. |
| 1.3 | 2026-05-12 | approved | — | Conventional **`docs/`** wording in intro and comparison table. |
