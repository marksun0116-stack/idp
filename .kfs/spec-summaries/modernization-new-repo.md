# Modernization new repo (agent summary)

**Full spec:** `.kfs/specs/modernization_new_repo_spec.md` · **Runbook:** [bootstrap-modernization-repo.md](../agent/runbooks/bootstrap-modernization-repo.md)

| Repo | Role |
| --- | --- |
| **Legacy** | As-is baseline ([reverse-engineer-baseline.md](../agent/runbooks/reverse-engineer-baseline.md)) |
| **New** | PRD delta, target architecture, target **`knowledge/`**, new **`src/`** |

**Runtime:** new app **never** depends on legacy (dev/test/prod). **No strangler.** **Lineage** = docs only.

**Cutover:** blue/green; rollback = traffic switch.

**PRD delta:** Retained · Removed · Added · Changed. **`docs/modernization/lineage.yml`** pins legacy **ref**.

Then: merge-knowledge → plan → implement (new repo only).
