#!/usr/bin/env bash
set -euo pipefail

service="${1:-backend}"
docker compose logs -f "$service"
