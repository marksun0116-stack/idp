# KFS agent index

**Package:** `.kfs/` — self-contained agent core (**version `1.0.0-beta`**, declared in [`kfs.manifest.yml`](kfs.manifest.yml)). Product truth remains **`knowledge/`** and **`docs/`**.

## 1. Product truth (L0)

- **`knowledge/catalog.yml`** + **`knowledge/**`** — enforceable product intent (CON, INV, DEC, CONR, FEAT)
- **`docs/prd/`**, **`docs/architecture/`**, **`docs/design/`**, **`docs/test/`** — product narrative when present

## 2. Before you edit

| If you are… | Read first |
| --- | --- |
| Orienting / picking a workflow | [agent/START.md](agent/START.md) |
| **First KFS on existing app** (baseline from code) | [agent/runbooks/reverse-engineer-baseline.md](agent/runbooks/reverse-engineer-baseline.md) |
| **Modernization program** (new repo) | [agent/runbooks/bootstrap-modernization-repo.md](agent/runbooks/bootstrap-modernization-repo.md) |
| Editing **`knowledge/`** or catalog | [agent/runbooks/merge-knowledge-first.md](agent/runbooks/merge-knowledge-first.md) |
| Validating primitives / catalog | [agent/runbooks/validate-knowledge-graph.md](agent/runbooks/validate-knowledge-graph.md) |
| Planning implementation (phases / stories) | [agent/runbooks/manage-implementation-plan.md](agent/runbooks/manage-implementation-plan.md) |
| Editing **`src/`** or **`tests/`** | [agent/runbooks/implement-with-primitives.md](agent/runbooks/implement-with-primitives.md) |
| Technical debt / assessment (optional) | [agent/runbooks/discover-tech-debt.md](agent/runbooks/discover-tech-debt.md) |

## 3. Standing constraints

- Index: [constraints/RULE-INDEX.yml](constraints/RULE-INDEX.yml)
- Summaries: [constraints/](constraints/) (load by glob when editing matching paths)

## 4. KFS specs (agent core)

**Quick cards:** [spec-summaries/](spec-summaries/) — knowledge-model, primitive-depth, adopt-existing-repo, modernization-new-repo, implementation-plan, architecture-intent, design-hld, prd, test-strategy, tech-debt-register

**Full specs:** [specs/](specs/) — normative detail when authoring or validating artifacts

## 5. Validation (self-contained in `.kfs/validate/`)

```bash
.kfs/validate/validate-knowledge-graph.sh
.kfs/validate/validate-implementation-plan.sh
```

Requires Python 3 + PyYAML (`pip install pyyaml`).

## 6. Merge order (non-negotiable)

1. **Knowledge PR** — `knowledge/` (+ doc pointers) merges first into active **`feature/*`**
2. **Implementation PR** — `src/` / `tests/` after primitives exist on that **`feature/*`**

**Never target `main`** for developer Knowledge or Implementation PRs. **`dev` → `feature/*` → `main` → tag → `release/*`** — see **`.kfs/specs/cicd_spec.md`** §2.1.

## 7. Conflicts

Stop and resolve in **`knowledge/`** — never only in code, chat, or narrative docs.

## 8. Human onboarding prose

Do not bulk-load long onboarding or strategy documents **outside `.kfs/`** unless the user explicitly asks. Procedures in this package are sufficient for agent work.
