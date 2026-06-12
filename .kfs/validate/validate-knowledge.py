#!/usr/bin/env python3
"""
Validate a knowledge/ tree and catalog.yml (knowledge_model_spec.md §9).

Usage:
  python3 .kfs/validate/validate-knowledge.py [KNOWLEDGE_ROOT ...]
  .kfs/validate/validate-knowledge-graph.sh

Defaults to every directory under the repo that contains knowledge/catalog.yml.
Exit code 0 when valid (depth warnings do not fail the run); non-zero on blocking errors.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

try:
    import yaml
except ImportError:
    print("PyYAML required: pip install pyyaml", file=sys.stderr)
    sys.exit(2)

PRIMITIVE_ID = re.compile(r"^(FEAT|CON|INV|DEC|CONR)-[A-Z0-9][\w-]*$")
REQUIRED_ROOT = ("id", "kind", "title", "status")
DRAFT_STATUSES = frozenset({"draft", "proposed", "wip"})
MIN_FEAT_CRITERIA = 3
VAGUE_CRITERIA = re.compile(
    r"^(?:must be |should be )?(?:robust|scalable|user-friendly|performant|secure|reliable|modern)\b",
    re.I,
)


def repo_root() -> Path:
    """Parent of .kfs/ (this file lives in .kfs/validate/)."""
    return Path(__file__).resolve().parent.parent.parent


def discover_knowledge_roots(explicit: list[str]) -> list[Path]:
    if explicit:
        return [Path(p).resolve() for p in explicit]
    roots: list[Path] = []
    for catalog in repo_root().rglob("knowledge/catalog.yml"):
        roots.append(catalog.parent.resolve())
    return sorted(set(roots))


def load_yaml(path: Path) -> dict:
    data = yaml.safe_load(path.read_text(encoding="utf-8"))
    return data if isinstance(data, dict) else {}


def collect_binding_ids(doc: dict) -> list[str]:
    ids: list[str] = []
    bindings = doc.get("bindings")
    if isinstance(bindings, dict):
        for key in ("constraints", "invariants", "decisions", "contracts", "specs"):
            val = bindings.get(key)
            if isinstance(val, list):
                ids.extend(str(x).strip() for x in val if x)
    related = doc.get("related_primitives")
    if isinstance(related, list):
        ids.extend(str(x).strip() for x in related if x)
    return ids


def is_rest_contract(doc: dict) -> bool:
    iface = str(doc.get("interface_type", "")).strip().lower()
    if iface == "event":
        return False
    if iface == "rest":
        return True
    if isinstance(doc.get("endpoints"), list) and doc["endpoints"]:
        return True
    schema = doc.get("schema")
    if isinstance(schema, dict):
        if isinstance(schema.get("endpoints"), dict) and schema["endpoints"]:
            return True
        if isinstance(schema.get("health"), dict) and schema["health"]:
            return True
    return iface not in ("event", "webhook", "payload", "other")


def endpoint_count(doc: dict) -> int:
    eps = doc.get("endpoints")
    if isinstance(eps, list) and eps:
        return len(eps)
    schema = doc.get("schema")
    if not isinstance(schema, dict):
        return 0
    count = 0
    for section in ("endpoints", "health"):
        block = schema.get(section)
        if isinstance(block, dict):
            for key in block:
                if re.match(r"^(GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS)\s+", key, re.I):
                    count += 1
    return count


def success_criteria_list(doc: dict) -> list[str]:
    sc = doc.get("success_criteria")
    if isinstance(sc, list):
        return [str(x).strip() for x in sc if str(x).strip()]
    return []


def is_umbrella_feat(doc: dict) -> bool:
    children = doc.get("child_features")
    return isinstance(children, list) and bool(children)


def binding_lists(doc: dict) -> dict[str, list[str]]:
    bindings = doc.get("bindings")
    if not isinstance(bindings, dict):
        return {}
    out: dict[str, list[str]] = {}
    for key in ("constraints", "invariants", "decisions", "contracts", "specs"):
        val = bindings.get(key)
        if isinstance(val, list):
            out[key] = [str(x).strip() for x in val if str(x).strip()]
    return out


def has_l2_bindings(doc: dict) -> bool:
    lists = binding_lists(doc)
    has_rule = bool(lists.get("constraints") or lists.get("invariants"))
    has_contract = bool(lists.get("contracts"))
    return has_rule or has_contract


def validate_depth_warnings(pid: str, path: Path, doc: dict) -> list[str]:
    warnings: list[str] = []
    kind = str(doc.get("kind", "")).strip().lower()
    status = str(doc.get("status", "")).strip().lower()
    is_draft = status in DRAFT_STATUSES

    if kind in ("feature_spec", "spec", "feature") or pid.startswith("FEAT-"):
        if is_umbrella_feat(doc):
            if not success_criteria_list(doc) and not is_draft:
                warnings.append(
                    f"{path}: umbrella FEAT {pid} has child_features but no success_criteria "
                    f"(gate on children; knowledge_model_spec.md §5.6)"
                )
            return warnings

        criteria = success_criteria_list(doc)
        readiness = str(doc.get("implementation_readiness", "")).strip().lower()

        if not criteria:
            warnings.append(
                f"{path}: FEAT {pid} missing success_criteria (L1 minimum; §5.6)"
            )
        elif len(criteria) < MIN_FEAT_CRITERIA and not is_draft:
            warnings.append(
                f"{path}: FEAT {pid} has {len(criteria)} success_criteria "
                f"(recommend ≥{MIN_FEAT_CRITERIA} observable items; §5.6)"
            )

        vague = [c for c in criteria if VAGUE_CRITERIA.search(c)]
        if vague:
            warnings.append(
                f"{path}: FEAT {pid} success_criteria may be unmeasurable: {vague[:2]!r} (§5.6)"
            )

        if not is_draft and not has_l2_bindings(doc):
            warnings.append(
                f"{path}: FEAT {pid} lacks bindings to CON/INV/CONR (L2 before implementation; §5.6)"
            )

        if readiness == "ready":
            if len(criteria) < MIN_FEAT_CRITERIA:
                warnings.append(
                    f"{path}: FEAT {pid} implementation_readiness is ready but "
                    f"success_criteria count is {len(criteria)} (need ≥{MIN_FEAT_CRITERIA})"
                )
            if not has_l2_bindings(doc):
                warnings.append(
                    f"{path}: FEAT {pid} implementation_readiness is ready but "
                    f"bindings do not meet L2 (CON/INV and CONR when applicable)"
                )

    if (kind in ("constraint", "constraints") or pid.startswith("CON-")) and not is_draft:
        rules = doc.get("rules")
        if not isinstance(rules, list) or not any(str(r).strip() for r in rules):
            warnings.append(
                f"{path}: CON {pid} missing non-empty rules list (§5.6 depth)"
            )

    if (kind in ("invariant", "invariants") or pid.startswith("INV-")) and not is_draft:
        if not str(doc.get("statement", "")).strip():
            warnings.append(
                f"{path}: INV {pid} missing statement (§5.6 depth)"
            )

    return warnings


def validate_root(knowledge_root: Path) -> tuple[list[str], list[str]]:
    errors: list[str] = []
    warnings: list[str] = []
    catalog_path = knowledge_root / "catalog.yml"
    if not catalog_path.is_file():
        return [f"{knowledge_root}: missing catalog.yml"], []

    catalog = load_yaml(catalog_path)
    entries = catalog.get("entries")
    if not isinstance(entries, list):
        return [f"{catalog_path}: entries must be a list"], []

    known_ids: set[str] = set()
    files_by_id: dict[str, Path] = {}
    docs_by_id: dict[str, dict] = {}

    for entry in entries:
        if not isinstance(entry, dict):
            errors.append(f"{catalog_path}: entry is not a mapping")
            continue
        pid = str(entry.get("id", "")).strip()
        rel = str(entry.get("path", "")).strip()
        if not pid:
            errors.append(f"{catalog_path}: entry missing id")
            continue
        if pid in known_ids:
            errors.append(f"{catalog_path}: duplicate catalog id {pid}")
        known_ids.add(pid)
        if not rel:
            errors.append(f"{catalog_path}: {pid} missing path")
            continue
        file_path = (knowledge_root / rel).resolve()
        if not file_path.is_file():
            errors.append(f"{catalog_path}: {pid} path not found: {rel}")
            continue
        doc = load_yaml(file_path)
        files_by_id[pid] = file_path
        docs_by_id[pid] = doc
        file_id = str(doc.get("id", "")).strip()
        if file_id and file_id != pid:
            errors.append(f"{file_path}: id {file_id} does not match catalog id {pid}")
        for field in REQUIRED_ROOT:
            if not str(doc.get(field, "")).strip():
                errors.append(f"{file_path}: missing required field {field}")

    for pid, doc in docs_by_id.items():
        path = files_by_id[pid]
        kind = str(doc.get("kind", "")).strip().lower()
        status = str(doc.get("status", "")).strip().lower()
        if kind in ("contract", "contracts") or pid.startswith("CONR-"):
            iface = str(doc.get("interface_type", "")).strip().lower()
            if iface == "event":
                continue
            if status in DRAFT_STATUSES:
                continue
            if is_rest_contract(doc):
                service = str(doc.get("service_name", "")).strip()
                if not service:
                    errors.append(
                        f"{path}: REST CONR {pid} missing service_name (knowledge_model_spec.md §5.4.1)"
                    )
                if endpoint_count(doc) == 0:
                    errors.append(
                        f"{path}: REST CONR {pid} has no endpoints (add endpoints[] or schema endpoint keys)"
                    )

        for ref_id in collect_binding_ids(doc):
            if ref_id and ref_id not in known_ids:
                errors.append(f"{path}: references unknown primitive id {ref_id}")

        warnings.extend(validate_depth_warnings(pid, path, doc))

    return errors, warnings


def main() -> int:
    args = [a for a in sys.argv[1:] if not a.startswith("-")]
    roots = discover_knowledge_roots(args)
    if not roots:
        print("No knowledge/catalog.yml found. Pass KNOWLEDGE_ROOT path(s).", file=sys.stderr)
        return 2

    all_errors: list[str] = []
    all_warnings: list[str] = []
    for root in roots:
        print(f"Validating {root} ...")
        errors, warnings = validate_root(root)
        all_errors.extend(errors)
        all_warnings.extend(warnings)

    if all_warnings:
        print("\nDepth warnings (non-blocking):", file=sys.stderr)
        for warn in all_warnings:
            print(f"  - {warn}", file=sys.stderr)

    if all_errors:
        print("\nValidation failed:", file=sys.stderr)
        for err in all_errors:
            print(f"  - {err}", file=sys.stderr)
        return 1

    if all_warnings:
        print(f"Knowledge validation passed with {len(all_warnings)} depth warning(s).")
    else:
        print("Knowledge validation passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
