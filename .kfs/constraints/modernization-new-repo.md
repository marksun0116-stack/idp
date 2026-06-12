# Modernization new repo (MOD-001)

- **Major rework** → **new repository**; legacy repo keeps as-is baseline only
- **No strangler** — new app **never depends on legacy** at runtime (dev, test, prod)
- **Production cutover:** **blue/green** only (**`DEC-*`**)
- Legacy: [reverse-engineer-baseline.md](../agent/runbooks/reverse-engineer-baseline.md)
- New repo: [bootstrap-modernization-repo.md](../agent/runbooks/bootstrap-modernization-repo.md)
- Spec: `.kfs/specs/modernization_new_repo_spec.md`
