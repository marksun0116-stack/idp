#!/usr/bin/env bash
# Validate implementation plans — see .kfs/validate/validate-implementation-plan.py
set -euo pipefail
KFS_VALIDATE="$(cd "$(dirname "$0")" && pwd)"
exec python3 "$KFS_VALIDATE/validate-implementation-plan.py" "$@"
