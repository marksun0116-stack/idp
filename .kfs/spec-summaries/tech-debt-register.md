# Tech debt register (agent summary)

**Full spec:** `.kfs/specs/tech_debt_discovery_spec.md`  
**Output:** `docs/assessment/tech_debt_register.md` (+ `slice_inventory.md`)

## Default

Mode **A** — as-is only; no target architecture comparison.

## Row schema

**TD-{nnn}**, evidence (observed|inferred|open question), impact, severity, slice link.

## Rules

- Do not invent **CON/INV** during discovery unless user asks promotion
- No normative rules only in debt doc (RULE-012)
- Terminal **Change log** on register
- Not a current/target split — **`docs/assessment/`** holds slice map + debt analysis

Runbook: `.kfs/agent/runbooks/discover-tech-debt.md`
