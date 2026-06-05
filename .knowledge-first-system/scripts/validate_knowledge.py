#!/usr/bin/env python3
"""
validate_knowledge.py — local validator for the knowledge-first system.

Aligned with:
  - .knowledge-first-system/specs/knowledge_model_spec.md  (§9 Validation)
  - .knowledge-first-system/rules/knowledge-validation-scripts.mdc
  - .knowledge-first-system/rules/knowledge-primitives.mdc                  (RULE-002–006)
  - .knowledge-first-system/rules/traceability-enforcement.mdc              (RULE-007, RULE-008)

What it checks:
  1. Every catalog.yml entry maps to a real file.
  2. Every primitive YAML parses and contains the required base fields
     (id, kind, title, status), and the `id` matches its catalog key.
  3. No duplicate IDs across primitive directories.
  4. No orphan primitive YAML files that the catalog forgot.
  5. Every CON-/INV-/DEC-/CONR-/FEAT- ID referenced by another primitive
     resolves to a primitive in the catalog (RULE-008 traceability).
  6. Every FEAT-* `bindings.{constraints,invariants,decisions,contracts}`
     entry resolves to a real catalog ID (RULE-006 spec bindings).
  7. Every governed doc listed under `catalog.docs` ends with a `## Change log`
     or `## Version history` section (RULE-007).

Usage:
  python3 .knowledge-first-system/scripts/validate_knowledge.py            # validate this repo
  python3 .knowledge-first-system/scripts/validate_knowledge.py --root ../other-repo

Exit code: 0 on PASS, 1 on FAIL. Suitable for CI.

Dependencies: PyYAML (`pip install pyyaml`).
"""
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

try:
    import yaml
except ImportError:
    sys.exit("PyYAML required: pip install pyyaml")


PRIMITIVE_SECTIONS = ("constraints", "invariants", "decisions", "contracts", "specs")
ID_PATTERN = re.compile(r"\b(?:CON|INV|DEC|CONR|FEAT)-[A-Z0-9_-]+\b")
REQUIRED_FIELDS = ("id", "kind", "title", "status")


def normalize_catalog(raw) -> dict:
    """Support both sectioned catalogs and the repo's flat [{id, path}] catalog."""
    if isinstance(raw, dict):
        return raw
    if not isinstance(raw, list):
        return {}

    catalog = {sect: {} for sect in PRIMITIVE_SECTIONS}
    for entry in raw:
        if not isinstance(entry, dict):
            continue
        pid = entry.get("id")
        ppath = entry.get("path")
        if not pid or not ppath:
            continue
        parts = Path(ppath).parts
        sect = parts[1] if len(parts) > 2 and parts[0] == "knowledge" else None
        if sect in PRIMITIVE_SECTIONS:
            catalog[sect][pid] = ppath
    return catalog


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--root", default=".", help="Repo root containing knowledge/ and docs/ (default: cwd)")
    args = parser.parse_args()

    root = Path(args.root).resolve()
    catalog_path = root / "knowledge" / "catalog.yml"
    if not catalog_path.exists():
        print(f"FAIL: {catalog_path} not found", file=sys.stderr)
        return 1

    with catalog_path.open() as f:
        catalog = normalize_catalog(yaml.safe_load(f))

    errors: list[str] = []
    all_ids: dict[str, tuple[str, str]] = {}
    seen_paths: set[str] = set()

    # 1, 2, 3 — catalog -> file -> id-match -> dup check
    for sect in PRIMITIVE_SECTIONS:
        for pid, ppath in (catalog.get(sect, {}) or {}).items():
            full = root / ppath
            if not full.exists():
                errors.append(f"[{sect}] {pid}: catalog points to missing file: {ppath}")
                continue
            seen_paths.add(str(full.resolve()))
            try:
                with full.open() as fh:
                    doc = yaml.safe_load(fh)
            except yaml.YAMLError as e:
                errors.append(f"[{sect}] {pid}: YAML parse error: {e}")
                continue
            if doc is None:
                errors.append(f"[{sect}] {pid}: file is empty")
                continue
            for field in REQUIRED_FIELDS:
                if field not in doc:
                    errors.append(f"[{sect}] {pid}: missing required field `{field}` (RULE-002–006)")
            if doc.get("id") != pid:
                errors.append(f"[{sect}] {pid}: file `id` is {doc.get('id')!r}, mismatches catalog key")
            if pid in all_ids:
                errors.append(f"DUPLICATE ID across primitive directories: {pid}")
            all_ids[pid] = (sect, ppath)

    # 4 — orphans
    for sect_dir in PRIMITIVE_SECTIONS:
        d = root / "knowledge" / sect_dir
        if not d.exists():
            continue
        for yfile in sorted(d.glob("*.yml")):
            if str(yfile.resolve()) not in seen_paths:
                errors.append(f"orphan primitive (not in catalog): {yfile.relative_to(root)}")

    # 5 — cross-reference resolution
    for pid, (sect, ppath) in all_ids.items():
        text = (root / ppath).read_text()
        refs = set(ID_PATTERN.findall(text)) - {pid}
        for r in refs:
            if r not in all_ids:
                errors.append(f"[{sect}] {pid}: references unknown primitive ID {r!r} (RULE-008)")

    # 6 — FEAT bindings
    for pid, (sect, ppath) in all_ids.items():
        if sect != "specs":
            continue
        with (root / ppath).open() as fh:
            doc = yaml.safe_load(fh)
        for cat, ids in (doc.get("bindings") or {}).items():
            for ref in (ids or []):
                if ref not in all_ids:
                    errors.append(f"[specs] {pid}: bindings.{cat} unknown {ref!r} (RULE-006)")

    # 7 — docs change log
    for label, dpath in (catalog.get("docs", {}) or {}).items():
        full = root / dpath
        if not full.exists():
            errors.append(f"docs.{label}: missing file {dpath}")
            continue
        txt = full.read_text()
        last = None
        for m in re.finditer(r"^## (.+)$", txt, re.MULTILINE):
            last = m.group(1).strip()
        ll = (last or "").lower()
        if "change log" not in ll and "version history" not in ll:
            errors.append(
                f"docs.{label} ({dpath}): last `## ` heading is {last!r}; must be `Change log` or `Version history` (RULE-007)"
            )

    # Report
    print("=" * 64)
    print(f"Repo root:            {root}")
    print(f"Catalog:              {catalog_path.relative_to(root)}")
    counts = ", ".join(f"{s}={len(catalog.get(s, {}) or {})}" for s in PRIMITIVE_SECTIONS)
    print(f"Primitives validated: {len(all_ids)}  ({counts})")
    print(f"Docs validated:       {len(catalog.get('docs', {}) or {})}")
    print(f"Errors:               {len(errors)}")
    print()
    if errors:
        print("ERRORS:")
        for e in errors:
            print("  -", e)
        print()
    print("=" * 64)
    print("PASS" if not errors else "FAIL")
    return 0 if not errors else 1


if __name__ == "__main__":
    sys.exit(main())
