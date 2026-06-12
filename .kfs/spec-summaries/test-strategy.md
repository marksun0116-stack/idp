# Test strategy (agent summary)

**Full spec:** `.kfs/specs/test_strategy_spec.md`  
**Path:** `docs/test/*` plans; strategy in handbook spec

## Principles

- Tests trace to **INV / CON / CONR / FEAT** — not orphan rules
- Contract tests at **CONR-*** boundaries
- Invariant tests for **INV-***
- Slice map optional: capability → FEAT → TC groups

## TDD

Red → green → refactor for new behavior; characterization before legacy refactors.

Runbook: `.kfs/agent/runbooks/implement-with-primitives.md`
