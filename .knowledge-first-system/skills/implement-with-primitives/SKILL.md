---
name: implement-with-primitives
description: >-
  Implements or reviews code under src/ using knowledge-first steps: load
  FEAT and linked CON/INV/DEC/CONR, constrain implementation, static checks,
  and invariant-led tests. On greenfield work, applies RULE-013: confirm PRD
  and architecture intent exist under conventional docs/ before net-new src. Use when
  writing application code, mapping tests to INV/CON, TDD, closing a release
  slice (FEAT status, CONR versions, doc metadata), or when the user mentions
  RULE-001, RULE-010, RULE-013, knowledge-first implementation, or
  constrain-validate-test.
---

# Implement with primitives

**Preferred execution:** Have a **human direct a Cursor agent** with this skill so **`src/`**, **`tests/`**, and any follow-on **`knowledge/`** / **product `docs/`** edits land as **one coherent, reviewable change set**—easier to trace to **FEAT / INV / CONR** than siloed hand edits.

Applies when writing or reviewing code under **`src/`** (create these paths when implementation lands). Maps **RULE-001** (no implementation without spec + primitives) and **RULE-010** (tests derived from knowledge).

**Operations guide:** **`.knowledge-first-system/use_guide.md`**. **Testing detail:** **`.knowledge-first-system/specs/test_strategy_spec.md`**. **Trace model:** **`.knowledge-first-system/specs/knowledge_model_spec.md`** §8 — **Constraint / Invariant → Spec → HLD → code → tests**; keep implementation and tests on that trace, not side-channel rules in chat only.

**Implementation planning:** For multi-phase or multi-story work, use **`.knowledge-first-system/skills/manage-implementation-plan/SKILL.md`** before broad implementation and keep the relevant **`docs/implementation/*_implementation_plan.md`** status current as stories start, finish, block, defer, or change order.

**TDD default:** For behavior changes, add or update **tests first** (failing or extended), then **`src/`**; for legacy areas, add **characterization** tests before refactors when the use guide's modernization flow applies.

## 0. Knowledge-first (RULE-001)

Before changing behavior under `src/`:

- **RULE-013 (new project):** If the change is **net-new product behavior** (not slice-only legacy per **`.knowledge-first-system/use_guide.md`** Scenarios 2–3) and conventional **`docs/prd/`** or **`docs/architecture/`** is **missing or has no** `*.md` files yet, **stop** and **ask the user** to **create those folders** and add at least one PRD and one architecture intent document (see **`.knowledge-first-system/specs/prd_spec.md`** and **`.knowledge-first-system/specs/architecture_intent_spec.md`**), or offer to draft them from stated goals. See **`.knowledge-first-system/rules/new-project-intent-gate.mdc`**.
- Identify the driving **FEAT-*** (or add one in a Knowledge PR) and load linked **CON**, **INV**, **DEC**, **CONR** via **`knowledge/catalog.yml`** and the spec's trace sections.
- Do not implement net-new requirements that exist only in free-form chat or unlinked docs without updating **`knowledge/`** first.
- For multi-story work, create or update the relevant **`docs/implementation/`** plan with phase/story order, dependencies, parallelization notes, and the active story status before broad **`src/`** edits.

## 1. Constrained generation

- Treat constraints and contracts as **hard** requirements in implementation and review — not optional context.
- Preserve invariants; if code cannot satisfy them, stop and resolve via a Knowledge PR (human approval), not silent workarounds.
- **Test first when changing code:** (1) **New** code — derive tests from **FEAT / INV / CONR**; write failing tests, then implement (**red → green → refactor**). (2) **Existing** code — before changing production behavior, add a **failing** test for the new requirement or a **characterization** test for current behavior (then change code and update expectations if the contract intentionally shifts). (3) **Generated code** — same order: test artifact that encodes the rule, then production change, unless the user is explicitly spiking (still land tests before merge).

## 2. Static validation

- After changes, favor checks that **do not require full e2e** where possible: linters, schema validation against **CONR-*** , policy scans for **CON-*** (logging, PII, secrets).
- API handlers and clients must match published contracts; drift = fix code **or** update the contract in a **Knowledge PR** first.

## 3. Dynamic validation — knowledge-derived tests (RULE-010)

- **Invariants (INV-*)** → integration or focused tests; each invariant should be traceable to one or more tests or documented evidence.
- **Constraints (CON-*)** → unit tests where cheap; security/compliance constraints → dedicated tests or static rules where possible.
- **Contracts (CONR-*)** → contract/schema tests at boundaries.

Avoid tests that encode new product rules with **no** CON/INV/FEAT trace.

## Release slice alignment

When closing work for a **release or epic**, align the **graph** with what shipped: driving **`FEAT-*`** **`status`**, **`CONR-*`** **version** bumps for published contracts, and related **product `docs/`** metadata when used (see **`.knowledge-first-system/use_guide.md`** → **Releases and documentation lifecycle**). Prefer the same PR or an immediate follow-up so **code, knowledge, and narrative** do not drift across the cut.

## Escalation

If implementation would violate a primitive, **do not ship a silent workaround**. Either amend knowledge via a Knowledge PR (with human approval) or stop and report the conflict.

Standing constraints also live in **`.knowledge-first-system/rules/implementation-constrain-validate-test.mdc`**.

---

## Change log

| Revision | Date | Status | Supersedes | Notes |
| --- | --- | --- | --- | --- |
| 1.1 | 2026-05-06 | approved | — | Prior release (Document version footer). |
| 1.2 | 2026-05-12 | approved | — | RULE-013 / conventional **`docs/`** wording; terminal change log. |
| 1.3 | 2026-06-05 | approved | 1.2 | Added RULE-014 implementation-plan coordination before multi-story implementation. |
