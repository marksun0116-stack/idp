# Modernize legacy with knowledge

> **Not default.** As-is + incremental → **[reverse-engineer-baseline.md](reverse-engineer-baseline.md)**. Major rework → **[bootstrap-modernization-repo.md](bootstrap-modernization-repo.md)** (new repo). Use this runbook **only when explicitly requested**.

**Constraints:** `.kfs/constraints/legacy-slices.md` · **Spec:** `.kfs/specs/tech_debt_discovery_spec.md`

Slice-first strangler / current-vs-target modernization in **one repo** — legacy path when user asks for it.

1. **Inventory slices** — rank by change, risk, roadmap; pick one slice
2. **As-is observations** — label observed vs inferred; debt register → **`docs/assessment/`** if used
3. **Target state** — **`docs/target/`** + target **`knowledge/`** when program requires dual-track docs
4. **Regression tests** — characterization before refactor; **CONR** as migration boundary
5. **Implement slice** — [implement-with-primitives.md](implement-with-primitives.md); Knowledge PR first

Do not blend legacy compromises into target architecture without **DEC**.
