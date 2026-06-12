# Knowledge model (agent summary)

**Full spec:** `.kfs/specs/knowledge_model_spec.md` · **Depth:** [primitive-depth.md](primitive-depth.md)

| Kind | Prefix | Role |
| --- | --- | --- |
| Constraint | CON | Enforceable must |
| Invariant | INV | Always true → tests |
| Decision | DEC | ADR-style choice |
| Contract | CONR | API/event boundary |
| Feature | FEAT | Scope + bindings |

**Fields:** **id**, **kind**, **title**, **status** on every file. **`catalog.yml`** indexes all.

**Flow:** architecture intent → DEC → HLD; CON/INV → FEAT → HLD → code → tests

```bash
.kfs/validate/validate-knowledge-graph.sh
```

Blocking: catalog drift, dangling bindings, REST CONR shape. Depth **warnings:** [primitive-depth.md](primitive-depth.md).
