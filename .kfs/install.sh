#!/usr/bin/env bash
# Install KFS tool adapters into a repo (.kfs/adapters/* → tool paths).
set -euo pipefail

KFS_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$KFS_ROOT/.." && pwd)"
INSTALLER_VERSION="1.0.0"
LEDGER="$KFS_ROOT/install.manifest"

TOOL="cursor"
MODE="copy"
DRY_RUN=0
CHECK_ONLY=0
INIT_PRODUCT=0
UNINSTALL=""
TOOLS=()

usage() {
  cat <<EOF
Usage: $0 [OPTIONS]

Install KFS tool adapters from .kfs/adapters/ into this repository.

Options:
  --tool TOOL       cursor (default), claude, codex, copilot, antigravity, all
  --link            Symlink instead of copy (dev/template repos)
  --dry-run         Print actions only
  --check           Verify install.manifest vs disk; exit 1 on drift
  --init-product    Create empty knowledge/docs skeletons (not PRD content)
  --uninstall TOOL  Remove files recorded for TOOL in install.manifest
  -h, --help        Show this help

Examples:
  ./.kfs/install.sh
  ./.kfs/install.sh --tool all
  ./.kfs/install.sh --check
EOF
}

log() { echo "$*"; }
run() {
  if [[ "$DRY_RUN" == 1 ]]; then
    echo "[dry-run] $*"
  else
    "$@"
  fi
}

parse_args() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --tool) TOOL="$2"; shift 2 ;;
      --link) MODE="symlink"; shift ;;
      --dry-run) DRY_RUN=1; shift ;;
      --check) CHECK_ONLY=1; shift ;;
      --init-product) INIT_PRODUCT=1; shift ;;
      --uninstall) UNINSTALL="$2"; shift 2 ;;
      -h|--help) usage; exit 0 ;;
      *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
    esac
  done
  if [[ "$TOOL" == "all" ]]; then
    TOOLS=(cursor claude codex copilot antigravity)
  else
    TOOLS=("$TOOL")
  fi
}

install_file() {
  local src="$1" dest="$2" tool="$3"
  local dest_dir rel_dest
  dest_dir="$(dirname "$dest")"
  rel_dest="${dest#"$REPO_ROOT"/}"
  if [[ "$DRY_RUN" == 0 ]]; then
    mkdir -p "$dest_dir"
  else
    run mkdir -p "$dest_dir"
  fi
  if [[ "$MODE" == "symlink" ]]; then
    run ln -sf "$src" "$dest"
  else
    run cp -f "$src" "$dest"
  fi
  INSTALLED_FILES+=("$rel_dest")
  INSTALLED_BY_TOOL["$tool"]+="$rel_dest"$'\n'
}

install_tree() {
  local src_dir="$1" dest_dir="$2" tool="$3"
  if [[ ! -d "$src_dir" ]]; then
    echo "WARN: missing adapter dir: $src_dir" >&2
    return 0
  fi
  while IFS= read -r -d '' f; do
    local rel="${f#"$src_dir"/}"
    install_file "$f" "$dest_dir/$rel" "$tool"
  done < <(find "$src_dir" -type f -print0)
}

demote_legacy_always_apply() {
  local rule="$REPO_ROOT/.cursor/rules/ai-native-knowledge-core.mdc"
  if [[ -f "$rule" ]] && grep -q 'alwaysApply: true' "$rule" 2>/dev/null; then
    log "Note: set ai-native-knowledge-core.mdc alwaysApply: false manually or rely on kfs-bootstrap only"
    if [[ "$DRY_RUN" == 0 ]] && [[ -w "$rule" ]]; then
      sed -i 's/alwaysApply: true/alwaysApply: false/' "$rule" || true
      log "  → set ai-native-knowledge-core.mdc alwaysApply: false"
    fi
  fi
}

install_cursor() {
  log "Installing Cursor adapter..."
  install_tree "$KFS_ROOT/adapters/cursor/rules" "$REPO_ROOT/.cursor/rules" "cursor"
  install_tree "$KFS_ROOT/adapters/cursor/skills" "$REPO_ROOT/.cursor/skills" "cursor"
  if [[ -f "$KFS_ROOT/adapters/cursor/README.md" ]]; then
    install_file "$KFS_ROOT/adapters/cursor/README.md" "$REPO_ROOT/.cursor/README.md" "cursor"
  fi
  demote_legacy_always_apply
  merge_cursorignore
}

install_claude() {
  log "Installing Claude Code adapter..."
  install_file "$KFS_ROOT/adapters/CLAUDE.md" "$REPO_ROOT/CLAUDE.md" "claude"
}

install_codex() {
  log "Installing Codex / AGENTS adapter..."
  install_file "$KFS_ROOT/adapters/AGENTS.md" "$REPO_ROOT/AGENTS.md" "codex"
}

install_copilot() {
  log "Installing GitHub Copilot adapter..."
  install_file "$KFS_ROOT/adapters/copilot/copilot-instructions.md" \
    "$REPO_ROOT/.github/copilot-instructions.md" "copilot"
}

install_antigravity() {
  log "Installing Antigravity adapter..."
  install_file "$KFS_ROOT/adapters/antigravity/project-rules.md" \
    "$REPO_ROOT/.antigravity/project-rules.md" "antigravity"
}

merge_cursorignore() {
  local ignore="$REPO_ROOT/.cursorignore"
  local marker="# kfs-agent-exclude"
  local block="$marker
references/
"
  if [[ -f "$ignore" ]] && grep -qF "$marker" "$ignore" 2>/dev/null; then
    return 0
  fi
  log "Appending KFS entries to .cursorignore"
  if [[ "$DRY_RUN" == 1 ]]; then
    echo "[dry-run] append .cursorignore"
  else
    { echo ""; echo "$block"; } >> "$ignore"
  fi
  INSTALLED_FILES+=(".cursorignore")
}

init_product_skeleton() {
  log "Creating optional product skeleton (--init-product)..."
  run mkdir -p "$REPO_ROOT/docs/prd" "$REPO_ROOT/docs/architecture" \
    "$REPO_ROOT/docs/design" "$REPO_ROOT/docs/test" "$REPO_ROOT/knowledge/specs"
  if [[ ! -f "$REPO_ROOT/knowledge/catalog.yml" ]]; then
    run cp -f "$KFS_ROOT/templates/catalog.yml.stub" "$REPO_ROOT/knowledge/catalog.yml" 2>/dev/null || \
      run bash -c "cat > '$REPO_ROOT/knowledge/catalog.yml' <<'YAML'
version: \"1\"
entries: []
YAML"
  fi
  for d in prd architecture design test; do
    local keep="$REPO_ROOT/docs/$d/.gitkeep"
    [[ -f "$keep" ]] || run touch "$keep"
  done
}

write_ledger() {
  [[ "$DRY_RUN" == 1 ]] && return 0
  local pkg_ver
  pkg_ver="$(grep -E '^kfs_package_version:' "$KFS_ROOT/kfs.manifest.yml" | awk '{print $2}' | tr -d '\"' || echo unknown)"
  {
    echo "installer_version: \"$INSTALLER_VERSION\""
    echo "kfs_package_version: \"$pkg_ver\""
    echo "installed_at: \"$(date -u +%Y-%m-%dT%H:%M:%SZ)\""
    echo "mode: $MODE"
    echo "adapters:"
    for t in "${TOOLS[@]}"; do
      echo "  $t:"
      echo "    files:"
      local files="${INSTALLED_BY_TOOL[$t]:-}"
      while IFS= read -r f; do
        [[ -n "$f" ]] && echo "      - $f"
      done <<< "$files"
    done
  } > "$LEDGER"
  log "Wrote $LEDGER"
}

check_install() {
  if [[ ! -f "$LEDGER" ]]; then
    echo "ERROR: no install.manifest — run ./.kfs/install.sh first" >&2
    exit 1
  fi
  local missing=0
  while IFS= read -r line; do
    [[ "$line" =~ ^[[:space:]]+-[[:space:]]+(.*)$ ]] || continue
    local f="${BASH_REMATCH[1]}"
    f="${f#./}"
    if [[ ! -e "$REPO_ROOT/$f" ]]; then
      echo "MISSING: $f"
      missing=1
    fi
  done < "$LEDGER"
  if [[ "$missing" == 1 ]]; then
    echo "Install drift detected. Re-run: ./.kfs/install.sh" >&2
    exit 1
  fi
  local count
  count="$(grep -E '^\s+- ' "$LEDGER" | wc -l | tr -d ' ')"
  log "Install check OK ($count managed paths)"
}

do_uninstall() {
  if [[ ! -f "$LEDGER" ]]; then
    echo "Nothing to uninstall (no install.manifest)" >&2
    exit 0
  fi
  log "Uninstall not fully automated yet for: $UNINSTALL — remove files listed under adapters.$UNINSTALL in $LEDGER"
  exit 0
}

declare -a INSTALLED_FILES=()
declare -A INSTALLED_BY_TOOL=()

main() {
  parse_args "$@"
  cd "$REPO_ROOT"

  if [[ -n "$UNINSTALL" ]]; then
    do_uninstall
  fi

  if [[ "$CHECK_ONLY" == 1 ]]; then
    check_install
    exit 0
  fi

  if [[ ! -f "$KFS_ROOT/kfs.manifest.yml" ]]; then
    echo "ERROR: $KFS_ROOT/kfs.manifest.yml missing" >&2
    exit 1
  fi

  chmod +x "$KFS_ROOT/validate/"*.sh "$KFS_ROOT/validate/"*.py 2>/dev/null || true

  for t in "${TOOLS[@]}"; do
    case "$t" in
      cursor) install_cursor ;;
      claude) install_claude ;;
      codex) install_codex ;;
      copilot) install_copilot ;;
      antigravity) install_antigravity ;;
      *) echo "Unknown tool: $t" >&2; exit 1 ;;
    esac
  done

  # AGENTS.md for cursor+all as well (codex installs it; ensure for cursor-only)
  if [[ "$TOOL" == "cursor" ]] || [[ "$TOOL" == "all" ]]; then
    if [[ ! -f "$REPO_ROOT/AGENTS.md" ]]; then
      install_codex
    fi
  fi

  [[ "$INIT_PRODUCT" == 1 ]] && init_product_skeleton

  write_ledger

  log ""
  log "KFS adapters installed: ${TOOLS[*]}"
  log "  Agent index: .kfs/INDEX.md"
  log "  Verify:      ./.kfs/install.sh --check"
}

main "$@"
