---
name: merge-knowledge-first
description: >-
  Opens and structures Knowledge PRs before implementation PRs for this repo.
  Covers merge order, PR bodies listing primitive IDs, doc alignment after
  knowledge changes, and RULE-013 gate (PRD + architecture intent) for
  greenfield before first driving FEAT. Use when adding FEAT/CON/INV/DEC/CONR,
  changing catalog, splitting knowledge vs code work, release trains and
  FEAT/CONR/doc metadata alignment, or when the user mentions Knowledge PR,
  merge knowledge first, primitive IDs in PR descriptions, or new project
  documentation gate.
---

# Merge knowledge first

**Preferred execution:** Have a **human direct a Cursor agent** with this skill (and project rules) so **`knowledge/`**, **`catalog.yml`**, and related **product `docs/`** (conventional paths) or **`.knowledge-first-system/`** pointers move in **one reviewable PR**—captures intent, reduces drift, and keeps artifacts in sync better than fragmented manual edits or chat-only instructions.

**Human docs:** **`.knowledge-first-system/use_guide.md`** (workflows, PR order, layout); index **`.knowledge-first-system/README.md`** (includes conventional **`docs/`** layout). **Knowledge model:** **`.knowledge-first-system/specs/knowledge_model_spec.md`** — artifact layers (§3), graph flow (§8), validation (§9). **Architecture intent (conventional `docs/architecture/`):** **`.knowledge-first-system/specs/architecture_intent_spec.md`**. **HLD (conventional `docs/design/`):** **`.knowledge-first-system/specs/design_spec.md`**. Folder mapping and enforcement philosophy: **`.knowledge-first-system/rules/ai-native-knowledge-core.mdc`**.

## New project gate (RULE-013) — before the first driving FEAT

For a **greenfield** or **new product** initiative, confirm **before** opening the first Knowledge PR that encodes net-new scope:

- [ ] **`docs/prd/`** exists and contains at least one **`*.md`** (or team-agreed) PRD — see **`.knowledge-first-system/specs/prd_spec.md`**. If the folder is missing or empty for **net-new product** work, **ask the user to create** it (or draft from goals) before a driving **`FEAT-*`**.
- [ ] **`docs/architecture/`** exists and contains at least one **`*.md`** architecture intent document — see **`.knowledge-first-system/specs/architecture_intent_spec.md`**. If missing or empty for **net-new product** work, **ask the user to create** it first.

If either check fails for **greenfield product** scope, **do not** proceed with a driving **`FEAT-*`** for that product based only on chat. **Guides-only** repos may omit **`docs/`** until product work begins (**`.knowledge-first-system/README.md`**). **Slice-first legacy** work follows **`.knowledge-first-system/use_guide.md`** Scenarios 2–3 instead (**`.knowledge-first-system/rules/new-project-intent-gate.mdc`** exceptions).

## When to use

- Net-new behavior or scope that needs **FEAT-*** and linked primitives.
- Any change that adds or renames files under **`knowledge/`** or edits **`knowledge/catalog.yml`**.
- You are about to open an **implementation PR** but primitives are missing or stale.

## Merge order

1. **Knowledge PR** — merge first when behavior or enforceable design changes.
2. **Implementation PR** — code and tests after the knowledge PR is merged, **or** a single combined PR when scope is small and **traceability is explicit** (per **`.knowledge-first-system/use_guide.md`**). Do not mix broad behavior change with missing primitives.

## Knowledge PR checklist

- [ ] **RULE-013 (greenfield):** If this PR introduces the **first driving `FEAT-*`** for a **new product**, confirm conventional **`docs/prd/`** and **`docs/architecture/`** each exist with at least one **`*.md`** file; otherwise **stop** and ask the user to create them first (**`.knowledge-first-system/rules/new-project-intent-gate.mdc`**).
- [ ] Touch only **`knowledge/`** (and doc pointers if needed); list **new/changed primitive IDs** in the PR description.
- [ ] Update **`knowledge/catalog.yml`** for every added, renamed, or removed primitive file.
- [ ] Resolve **FEAT** bindings to existing **CON / INV / DEC / CONR** IDs (no dangling references).
- [ ] Review expectations: PM → feature specs; architect → constraints, decisions, contracts.
- [ ] **Change log / version history:** Any **product `docs/`** path edited in this PR (**`docs/prd/`**, **`docs/architecture/`**, **`docs/design/`**, **`docs/test/`**) or **`.knowledge-first-system/**/*.md`** file edited ends with **`## Change log`** or **`## Version history`** as the **last** heading (table: **revision**, **date** (`last_updated`), **status**, **supersedes**, **what changed**) per **`.knowledge-first-system/use_guide.md`** (*Releases and documentation lifecycle*).

## Implementation PR checklist

- [ ] Driving **FEAT-*** (and linked primitives) already merged or explicitly in scope.
- [ ] Review: correctness, performance, security in code — not re-litigating merged intent unless a defect upgrades knowledge.

## Docs after a Knowledge PR

When **PRD / HLD / design docs** and merged YAML disagree:

1. Treat merged **`knowledge/`** as authoritative for enforceable intent.
2. Align conventional **`docs/prd/`**, **`docs/architecture/`**, **`docs/design/`** (when those trees exist) in the same PR or a **tightly coupled follow-up**; do not leave prose drift.

## Releases and documentation lifecycle

Multi-release products should **anchor each train** in **`FEAT-*`** (and update **`CONR-*`** **version** fields when published APIs or events ship). When merging work for a release slice:

- [ ] **`FEAT-*`** **`status`** and bindings reflect what is merging (not stale scope).
- [ ] **PRD / HLD / `docs/test/`** plans touched for the slice carry current **metadata** (`version`, `last_updated`, links to driving **`FEAT-*`**); superseded design docs point to their **successor** (see **`.knowledge-first-system/use_guide.md`** → **Releases and documentation lifecycle**).

## Traceability reminder

Major **PRD / HLD** sections should remain **consistent** with indexed primitives. If architecture or scope changes, update **DEC / CON / FEAT** (and catalog) first, then align narrative docs — not the reverse.

---

## Change log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 1.1 | 2026-05-06 | approved | — | Prior release (`## Document version` footer). |
| 1.3 | 2026-05-12 | approved | — | **`docs/`** optional in guides-only repos; RULE-013 wording aligned with **README** / **use_guide**. |
