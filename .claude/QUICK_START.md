# Claude Code Quick Start — IDP Project

## KFS-First Development Workflow

This project uses the **Knowledge-First System (KFS)** v1.1 with **RULE-014** (implementation plan management).

Before writing code, follow this workflow:

### 1. Understand the Knowledge Base

```bash
# Check the knowledge catalog
cat knowledge/catalog.yml

# Load the relevant FEAT specification
cat knowledge/specs/FEAT-investor-development-platform-001.yml

# Check linked constraints, invariants, and contracts
cat knowledge/constraints/CON-private-research-default-001.yml
cat knowledge/invariants/INV-decision-record-integrity-001.yml
cat knowledge/contracts/CONR-decision-record-api-001.yml
```

### 2. For Multi-Story Work: Create an Implementation Plan

If your work spans multiple stories or phases:

```bash
# Reference the spec
cat .knowledge-first-system/specs/implementation_plan_spec.md

# Create a plan file
vim docs/implementation/your_feature_implementation_plan.md
```

Include:
- Phase overview with goals and dependencies
- User-story-sized slices (not too big)
- Requirements-review gates at phase start
- Trace links to FEAT/CON/INV/CONR IDs
- Progress updates (dated entries)

### 3. Implement with Traceability

- Map your implementation to FEAT specifications
- Respect CON (constraints) and INV (invariants)
- Test against CONR (contracts)
- Update tests to reference INV/CON IDs where practical

### 4. Validate Before Committing

```bash
# Validate the knowledge graph
python3 .knowledge-first-system/scripts/validate_knowledge.py

# Expected: PASS with no errors
```

## Key Files

| Path | Purpose |
| --- | --- |
| `AGENTS.md` | Root workflow instructions |
| `knowledge/catalog.yml` | Authoritative primitives index |
| `knowledge/specs/` | FEAT specifications |
| `knowledge/constraints/` | CON constraint definitions |
| `knowledge/invariants/` | INV invariant definitions |
| `knowledge/contracts/` | CONR API contract definitions |
| `docs/implementation/` | Implementation plans (when needed) |
| `.knowledge-first-system/rules/` | KFS rules (read-only reference) |
| `.knowledge-first-system/skills/` | Workflow skills (read-only reference) |
| `.knowledge-first-system/specs/` | KFS specifications (read-only reference) |

## Stop Conditions

Stop and resolve knowledge/docs **before** code if:

- There's no suitable FEAT for net-new behavior
- Implementation would violate a CON/INV/CONR
- A contract/API shape changes without a matching CONR update
- User-facing features change without matching PRD/design updates
- Multi-story implementation proceeds without a `docs/implementation/` plan

## Quick Commands

```bash
# Run the app
./scripts/dev-up.sh

# Follow backend logs
./scripts/dev-logs.sh

# Stop and reset
./scripts/dev-down.sh
./scripts/dev-reset-db.sh

# Validate KFS
python3 .knowledge-first-system/scripts/validate_knowledge.py
```

## Tips

- **Start from AGENTS.md** — It's the root workflow guide for this project
- **Keep catalog.yml updated** — When you add/rename primitives, update the index
- **Use implementation plans for multi-story work** — It saves coordination pain later
- **Trace tests to primitives** — Comment tests with INV/CON IDs; it helps future readers

## Questions?

- Read `AGENTS.md` (root KFS workflow)
- Check `.knowledge-first-system/README.md` (KFS overview)
- See `.knowledge-first-system/specs/implementation_plan_spec.md` (plan details)
- Review `KFS_SETUP_SUMMARY.md` (setup details)
