# Implement with primitives

**Testing:** `.kfs/spec-summaries/test-strategy.md` · **Depth:** `.kfs/spec-summaries/primitive-depth.md` · **Plan:** `.kfs/spec-summaries/implementation-plan.md` · **Constraints:** `.kfs/constraints/knowledge-first.md`

Applies when writing or reviewing code under **`src/`** (RULE-001, RULE-010). **TDD default:** tests first for behavior changes; characterization tests before legacy refactors.

## 0. Knowledge-first (RULE-001)

Before changing behavior under **`src/`**:

- **RULE-013:** Net-new product without **`docs/prd/`** + **`docs/architecture/`** → stop; create or draft those first
- Identify driving **FEAT-***; load linked CON, INV, DEC, CONR via **`catalog.yml`**
- Driving child **FEAT** must be **L2+** (`.kfs/spec-summaries/primitive-depth.md`) unless human explicitly accepts risk
- Active **`ST-*`** from **`knowledge/plans/`** with **`depends_on`** satisfied; reference story id in PR
- **PR base branch:** active **`feature/*`** (cut from **`dev`**) — never **`main`** for developer implementation work (**`.kfs/specs/cicd_spec.md`** §2.1)
- Do not implement requirements that exist only in chat without **`knowledge/`** update

## 1. Constrained generation

- Constraints and contracts are **hard** requirements
- Preserve invariants; violations → Knowledge PR, not silent workarounds
- **Test first:** new code (red → green → refactor); existing code (characterization then change)

## 2. Static validation

- Linters, **CONR-*** schema checks, **CON-*** policy scans where applicable
- API drift → fix code **or** Knowledge PR for contract first

## 3. Dynamic validation (RULE-010)

- **INV-*** → integration/focused tests
- **CON-*** → unit or static enforcement
- **CONR-*** → contract tests at boundaries
- No new product rules in tests without CON/INV/FEAT trace

## Release slice alignment

On ship: **FEAT** status, **CONR** versions, related **`docs/`** metadata — same PR or immediate follow-up.

## Escalation

Would violate a primitive → amend knowledge (human approval) or stop and report conflict.
