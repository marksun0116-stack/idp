#!/usr/bin/env bash
set -euo pipefail

docker compose build --no-cache backend
docker compose up -d
docker compose ps
