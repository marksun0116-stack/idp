# Implementation plan (PM)

- Plans live in **`knowledge/plans/PLAN-*.yml`** — stable **`PLAN-*`** filename (no date prefix); optional **`docs/plans/{yyyymmdd}_*_implementation_plan.md`** summary
- Create after knowledge/docs ready for code/tests; runbook: `.kfs/agent/runbooks/manage-implementation-plan.md`
- **`closed`** phases are immutable — append new phases/stories for additional scope
- Validate: `.kfs/validate/validate-implementation-plan.sh`
- Spec: `.kfs/specs/implementation_plan_spec.md`
