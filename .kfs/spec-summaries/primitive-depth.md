# Primitive depth (agent summary)

**Full spec:** `.kfs/specs/knowledge_model_spec.md` §5.5–5.6

| Level | Implement? | Minimum |
| --- | --- | --- |
| **L0** Intent | No | Umbrella FEAT and/or PRD |
| **L1** Capability | Risky | Child FEAT + **≥3** observable **`success_criteria`** |
| **L2** Governed | Yes (bounded) | L1 + **bindings** (CON/INV; CONR at seams) |
| **L3** Design-ready | Yes | L2 + HLD + **TC-*** mapped |

**FEAT** = product **what**; stack/migration → **DEC** / HLD.

**`implementation_readiness`:** **`draft`** (no implement) | **`ready`** (L2+ met).

Before implementation PR: falsifiable criteria; must/must-not in **CON/INV**; run validator — fix depth **warnings** before **`ready`**.

```bash
.kfs/validate/validate-knowledge-graph.sh
```
