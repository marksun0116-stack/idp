# Discover technical debt (as-is)

**Spec:** `.kfs/specs/tech_debt_discovery_spec.md` · **Card:** `.kfs/spec-summaries/tech-debt-register.md`

Refresh **`docs/assessment/tech_debt_register.md`**. Default **Mode A** — as-is only, no target comparison.

## Scope

Whole system or slice; refresh if register exists. Load slice inventory, baseline docs, **`catalog.yml`** (trace only — do not invent primitives).

## Steps

1. **Snapshot** — size, test ratio, frameworks, optional git churn
2. **Slice map** — update **`docs/assessment/slice_inventory.md`**
3. **Per-slice signals** — entry points, code smells, test gaps → **TD-{nnn}** rows per spec
4. **Supply chain** — deps, deploy path, vuln scan if available
5. **Write register** — **`docs/assessment/tech_debt_register.md`** with Change log; link from **`docs/assessment/README.md`**

**EKCC ingest:** legacy path `docs/current/tech_debts_current.md` — symlink/copy until ingest contract updates.

## Promote to knowledge

Only when asked → [merge-knowledge-first.md](merge-knowledge-first.md).

## Before PR

Mode stated; Critical/High have evidence; no normative rules only in debt doc (RULE-012).
