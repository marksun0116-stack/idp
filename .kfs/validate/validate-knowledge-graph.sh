#!/usr/bin/env bash
# Validate knowledge/ trees — see .kfs/validate/validate-knowledge.py
set -euo pipefail
KFS_VALIDATE="$(cd "$(dirname "$0")" && pwd)"
exec python3 "$KFS_VALIDATE/validate-knowledge.py" "$@"
