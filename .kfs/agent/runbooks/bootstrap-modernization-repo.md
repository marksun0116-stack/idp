# Bootstrap modernization repository

**Spec:** `.kfs/spec-summaries/modernization-new-repo.md` · **Legacy:** [reverse-engineer-baseline.md](reverse-engineer-baseline.md)

New repo for **major rework**; legacy keeps as-is baseline only.

## When

New stack/deployable; not in-place incremental enhancement. Legacy **`docs/`** + **`knowledge/`** exists or is scheduled.

## Steps

1. **`./.kfs/install.sh`**; create **`docs/`** tree
2. **`docs/modernization/lineage.yml`** — legacy repo URL, pinned **ref**, paths to baseline docs/knowledge, **`MOD-*`** program id
3. **PRD delta** — **Retained / Removed / Added / Changed**; target **FEAT** = product **what**; Change log
4. **Target architecture intent** — standalone deployable; **blue/green** cutover; **no legacy runtime dependency** (**DEC-***)
5. **Knowledge PR** — target **FEAT/CON/INV/CONR**; no legacy compatibility shims; validate
6. **HLD, PLAN, src/tests** — standard runbooks in **new repo only**
7. **Legacy during program** — as-is baseline updates only; re-pin **ref** when baseline moves materially

## Non-negotiable

- **No strangler** — no runtime calls, proxies, or shared production runtime with legacy
- **Lineage / `references/legacy/`** = trace only
- **Cutover:** blue/green; rollback = traffic switch

## Escalation

Missing legacy baseline → **reverse-engineer-baseline** on legacy first.
