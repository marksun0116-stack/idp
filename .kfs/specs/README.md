# KFS specifications (agent core)

Normative **how to write** and **validation** specs for knowledge-first adoption. Shipped with **`.kfs/`**.

| Spec | File | Agent quick card |
| --- | --- | --- |
| Knowledge model (RULE-011) | [knowledge_model_spec.md](knowledge_model_spec.md) | [../spec-summaries/knowledge-model.md](../spec-summaries/knowledge-model.md), [../spec-summaries/primitive-depth.md](../spec-summaries/primitive-depth.md) |
| Implementation plan (PM) | [implementation_plan_spec.md](implementation_plan_spec.md) | [../spec-summaries/implementation-plan.md](../spec-summaries/implementation-plan.md) |
| Adopt existing repository | [adopt_existing_repo_spec.md](adopt_existing_repo_spec.md) | [../spec-summaries/adopt-existing-repo.md](../spec-summaries/adopt-existing-repo.md) |
| Modernization (new repo) | [modernization_new_repo_spec.md](modernization_new_repo_spec.md) | [../spec-summaries/modernization-new-repo.md](../spec-summaries/modernization-new-repo.md) |
| Architecture intent | [architecture_intent_spec.md](architecture_intent_spec.md) | [../spec-summaries/architecture-intent.md](../spec-summaries/architecture-intent.md) |
| Design (HLD) | [design_spec.md](design_spec.md) | [../spec-summaries/design-hld.md](../spec-summaries/design-hld.md) |
| PRD | [prd_spec.md](prd_spec.md) | [../spec-summaries/prd.md](../spec-summaries/prd.md) |
| Test strategy | [test_strategy_spec.md](test_strategy_spec.md) | [../spec-summaries/test-strategy.md](../spec-summaries/test-strategy.md) |
| Infrastructure | [infrastructure_spec.md](infrastructure_spec.md) | — |
| CI/CD | [cicd_spec.md](cicd_spec.md) | — |
| Tech debt discovery | [tech_debt_discovery_spec.md](tech_debt_discovery_spec.md) | [../spec-summaries/tech-debt-register.md](../spec-summaries/tech-debt-register.md) |

**Validators:** `.kfs/validate/` (knowledge catalog + implementation plans). See [../INDEX.md](../INDEX.md) §5.

**Load policy:** Prefer **spec-summaries** for routine agent tasks; open full **specs/** when writing or validating that artifact type.
