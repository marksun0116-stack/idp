#!/usr/bin/env python3
"""
Validate implementation plan YAML (implementation_plan_spec.md §7).

Usage:
  python3 .kfs/validate/validate-implementation-plan.py [PLAN.yml ...]
  .kfs/validate/validate-implementation-plan.sh

When no paths given, discovers knowledge/plans/PLAN-*.yml under the repo.
Exit 0 when valid (warnings allowed); non-zero on blocking errors.
"""

from __future__ import annotations

import fnmatch
import sys
from collections import defaultdict
from pathlib import Path

try:
    import yaml
except ImportError:
    print("PyYAML required: pip install pyyaml", file=sys.stderr)
    sys.exit(2)

TERMINAL_STORY = frozenset({"done", "dropped"})
ACTIVE_STORY = frozenset({"in_progress", "blocked"})
CLOSED_PHASE = "closed"
PARALLEL_OK = "parallel_ok"


def repo_root() -> Path:
    """Parent of .kfs/ (this file lives in .kfs/validate/)."""
    return Path(__file__).resolve().parent.parent.parent


def load_yaml(path: Path) -> dict:
    data = yaml.safe_load(path.read_text(encoding="utf-8"))
    return data if isinstance(data, dict) else {}


def discover_plans(explicit: list[str]) -> list[Path]:
    if explicit:
        return [Path(p).resolve() for p in explicit]
    return sorted(repo_root().rglob("knowledge/plans/PLAN-*.yml"))


def glob_overlap(a: str, b: str) -> bool:
    a, b = a.strip(), b.strip()
    if not a or not b:
        return False
    if fnmatch.fnmatch(a, b) or fnmatch.fnmatch(b, a):
        return True
    a_dir = a.rstrip("/") + "/" if not a.endswith("*") else a
    b_dir = b.rstrip("/") + "/" if not b.endswith("*") else b
    return a.startswith(b_dir.rstrip("*")) or b.startswith(a_dir.rstrip("*"))


def detect_cycle(nodes: set[str], edges: dict[str, list[str]]) -> list[str] | None:
    visited: set[str] = set()
    stack: set[str] = set()

    def dfs(n: str) -> bool:
        visited.add(n)
        stack.add(n)
        for dep in edges.get(n, []):
            if dep not in nodes:
                continue
            if dep not in visited:
                if dfs(dep):
                    return True
            elif dep in stack:
                return True
        stack.remove(n)
        return False

    for node in nodes:
        if node not in visited and dfs(node):
            return ["cycle detected among: " + ", ".join(sorted(stack))]
    return None


def validate_plan(path: Path, catalog_ids: set[str] | None) -> tuple[list[str], list[str]]:
    errors: list[str] = []
    warnings: list[str] = []
    doc = load_yaml(path)

    if doc.get("kind") != "implementation_plan":
        errors.append(f"{path}: kind must be implementation_plan")
        return errors, warnings

    for field in ("id", "title", "status", "cycle_feat", "plan_format_version"):
        if not str(doc.get(field, "")).strip():
            errors.append(f"{path}: missing required field {field}")

    phases = doc.get("phases")
    stories = doc.get("stories")
    if not isinstance(phases, list) or not phases:
        errors.append(f"{path}: phases must be a non-empty list")
        return errors, warnings
    if not isinstance(stories, list):
        errors.append(f"{path}: stories must be a list")
        return errors, warnings

    phase_ids: dict[str, dict] = {}
    for ph in phases:
        if not isinstance(ph, dict):
            errors.append(f"{path}: phase entry is not a mapping")
            continue
        pid = str(ph.get("id", "")).strip()
        if not pid:
            errors.append(f"{path}: phase missing id")
            continue
        if pid in phase_ids:
            errors.append(f"{path}: duplicate phase id {pid}")
        phase_ids[pid] = ph

    story_ids: dict[str, dict] = {}
    stories_by_phase: dict[str, list[dict]] = defaultdict(list)
    for st in stories:
        if not isinstance(st, dict):
            errors.append(f"{path}: story entry is not a mapping")
            continue
        sid = str(st.get("id", "")).strip()
        if not sid:
            errors.append(f"{path}: story missing id")
            continue
        if sid in story_ids:
            errors.append(f"{path}: duplicate story id {sid}")
        story_ids[sid] = st
        ph_id = str(st.get("phase_id", "")).strip()
        if ph_id not in phase_ids:
            errors.append(f"{path}: story {sid} unknown phase_id {ph_id}")
        else:
            stories_by_phase[ph_id].append(st)

    cycle_feat = str(doc.get("cycle_feat", "")).strip()
    if catalog_ids is not None and cycle_feat and cycle_feat not in catalog_ids:
        warnings.append(f"{path}: cycle_feat {cycle_feat} not found in knowledge/catalog.yml")

    phase_edges: dict[str, list[str]] = {}
    for pid, ph in phase_ids.items():
        deps = ph.get("depends_on")
        dep_list = [str(d).strip() for d in deps] if isinstance(deps, list) else []
        phase_edges[pid] = dep_list
        for dep in dep_list:
            if dep not in phase_ids:
                errors.append(f"{path}: phase {pid} depends_on unknown phase {dep}")

    cycle = detect_cycle(set(phase_ids), phase_edges)
    if cycle:
        errors.extend(f"{path}: {c}" for c in cycle)

    story_edges: dict[str, list[str]] = {}
    for sid, st in story_ids.items():
        deps = st.get("depends_on")
        dep_list = [str(d).strip() for d in deps] if isinstance(deps, list) else []
        story_edges[sid] = [d for d in dep_list if d in story_ids]
        for dep in dep_list:
            if dep.startswith("PH-"):
                if dep not in phase_ids:
                    errors.append(f"{path}: story {sid} depends_on unknown phase {dep}")
            elif dep.startswith("ST-"):
                if dep not in story_ids:
                    errors.append(f"{path}: story {sid} depends_on unknown story {dep}")
            elif dep:
                warnings.append(f"{path}: story {sid} depends_on unrecognized id {dep}")

    cycle = detect_cycle(set(story_ids), story_edges)
    if cycle:
        errors.extend(f"{path}: {c}" for c in cycle)

    def phase_closed(ph_id: str) -> bool:
        return str(phase_ids[ph_id].get("status", "")).strip().lower() == CLOSED_PHASE

    def story_done(sid: str) -> bool:
        return str(story_ids[sid].get("status", "")).strip().lower() in TERMINAL_STORY

    for pid, ph in phase_ids.items():
        if str(ph.get("status", "")).strip().lower() != CLOSED_PHASE:
            continue
        if not str(ph.get("closed_at", "")).strip():
            warnings.append(f"{path}: closed phase {pid} should set closed_at")
        for st in stories_by_phase.get(pid, []):
            st_status = str(st.get("status", "")).strip().lower()
            sid = str(st.get("id", ""))
            if st_status not in TERMINAL_STORY:
                errors.append(
                    f"{path}: closed phase {pid} has story {sid} with status {st_status} "
                    f"(must be done or dropped)"
                )

    in_progress_parallel: list[tuple[str, list[str]]] = []
    for sid, st in story_ids.items():
        status = str(st.get("status", "")).strip().lower()
        execution = str(st.get("execution", "sequential")).strip().lower()
        deps = st.get("depends_on")
        dep_list = [str(d).strip() for d in deps] if isinstance(deps, list) else []

        isolation = st.get("isolation")
        touch: list[str] = []
        if isinstance(isolation, dict):
            tp = isolation.get("touch_paths")
            if isinstance(tp, list):
                touch = [str(p).strip() for p in tp if str(p).strip()]

        if execution == PARALLEL_OK and not touch:
            errors.append(
                f"{path}: parallel_ok story {sid} missing isolation.touch_paths (§4.5)"
            )

        if status in ACTIVE_STORY or status == "done":
            for dep in dep_list:
                if dep.startswith("ST-") and dep in story_ids and not story_done(dep):
                    if status in ACTIVE_STORY:
                        errors.append(
                            f"{path}: story {sid} is {status} but depends_on {dep} is not done"
                        )
                if dep.startswith("PH-") and dep in phase_ids and not phase_closed(dep):
                    if status in ACTIVE_STORY:
                        errors.append(
                            f"{path}: story {sid} is {status} but depends_on phase {dep} is not closed"
                        )

        if status == "in_progress" and execution == PARALLEL_OK:
            in_progress_parallel.append((sid, touch))

    for i, (sid_a, paths_a) in enumerate(in_progress_parallel):
        for sid_b, paths_b in in_progress_parallel[i + 1 :]:
            for pa in paths_a:
                for pb in paths_b:
                    if glob_overlap(pa, pb):
                        warnings.append(
                            f"{path}: in_progress parallel stories {sid_a} and {sid_b} "
                            f"overlap touch_paths ({pa!r} vs {pb!r})"
                        )

    plan_status = str(doc.get("status", "")).strip().lower()
    readiness = doc.get("readiness")
    if plan_status == "active" and isinstance(readiness, dict):
        for key, label in (
            ("knowledge_merged", "knowledge_merged"),
            ("docs_aligned", "docs_aligned"),
            ("feat_depth_l2", "feat_depth_l2"),
        ):
            if not readiness.get(key):
                warnings.append(
                    f"{path}: plan is active but readiness.{key} is not true ({label} gate)"
                )

    return errors, warnings


def load_catalog_ids() -> set[str] | None:
    for catalog in repo_root().rglob("knowledge/catalog.yml"):
        doc = load_yaml(catalog)
        entries = doc.get("entries")
        if not isinstance(entries, list):
            continue
        ids: set[str] = set()
        for entry in entries:
            if isinstance(entry, dict):
                pid = str(entry.get("id", "")).strip()
                if pid:
                    ids.add(pid)
        return ids
    return None


def main() -> int:
    args = [a for a in sys.argv[1:] if not a.startswith("-")]
    plans = discover_plans(args)
    if not plans:
        print(
            "No implementation plans found. Pass PLAN.yml path(s) or add knowledge/plans/PLAN-*.yml",
            file=sys.stderr,
        )
        return 2

    catalog_ids = load_catalog_ids()
    all_errors: list[str] = []
    all_warnings: list[str] = []

    for plan_path in plans:
        print(f"Validating {plan_path} ...")
        errors, warnings = validate_plan(plan_path, catalog_ids)
        all_errors.extend(errors)
        all_warnings.extend(warnings)

    if all_warnings:
        print("\nPlan warnings (non-blocking):", file=sys.stderr)
        for w in all_warnings:
            print(f"  - {w}", file=sys.stderr)

    if all_errors:
        print("\nPlan validation failed:", file=sys.stderr)
        for e in all_errors:
            print(f"  - {e}", file=sys.stderr)
        return 1

    if all_warnings:
        print(f"Implementation plan validation passed with {len(all_warnings)} warning(s).")
    else:
        print("Implementation plan validation passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
