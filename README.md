# Investor Development Platform

Local prototype for the Investor Development Platform. The current app includes a React frontend, a Spring Boot API, and PostgreSQL.

## Run Locally With Docker Compose In WSL

Prerequisites:

- WSL2
- Docker Desktop with WSL integration enabled, or Docker Engine installed inside WSL
- Run commands from the repository root

Start the app:

```bash
./scripts/dev-up.sh
```

The first run downloads the Postgres image, Maven builder image, JRE runtime image, and Maven dependencies, so it can take a few minutes.

The compose stack starts:

- `postgres` on `localhost:5433`
- `backend` on `http://localhost:8081`
- `frontend` on `http://localhost:3000`

Open the UI:

```text
http://localhost:3000
```

The frontend proxies `/api` requests to the backend inside Docker Compose.

Follow backend logs:

```bash
./scripts/dev-logs.sh
```

Stop the stack:

```bash
./scripts/dev-down.sh
```

Delete the local database volume and stop the stack:

```bash
./scripts/dev-reset-db.sh
```

Rebuild the backend image from scratch:

```bash
./scripts/dev-rebuild.sh
```

The local database uses these default credentials:

```text
database: idp
username: idp
password: idp
```

If you want to expose Postgres on a different host port:

```bash
IDP_POSTGRES_PORT=5434 ./scripts/dev-up.sh
```

If you want to expose the backend on a different host port:

```bash
IDP_BACKEND_PORT=8082 ./scripts/dev-up.sh
```

If you want to expose the frontend on a different host port:

```bash
IDP_FRONTEND_PORT=3001 ./scripts/dev-up.sh
```

Spring Boot uses `ddl-auto: update`, so tables are created automatically in the local Postgres volume.

## Quick API Smoke Test

Private API endpoints use the development bearer-subject auth filter. Any bearer token becomes the local user id.

```bash
curl -i http://localhost:8081/api/decisions \
  -H "Authorization: Bearer alice"
```

Create a decision:

```bash
curl -i -X POST http://localhost:8081/api/decisions \
  -H "Authorization: Bearer alice" \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "NVDA",
    "decisionType": "watch",
    "title": "NVDA infrastructure thesis",
    "thesis": "AI infrastructure demand may support durable revenue growth.",
    "evidence": ["Data center demand", "GPU supply constraints easing"],
    "riskFactors": ["Valuation compression"],
    "confidence": 8,
    "timeHorizon": "6 months",
    "exitCriteria": ["Thesis invalidated"],
    "visibility": "private"
  }'
```

Public strategy endpoints are unauthenticated under `/api/public/**`.

## Useful Docker Compose Commands

```bash
docker compose ps
docker compose logs -f frontend
docker compose logs -f backend
docker compose logs -f postgres
docker compose down -v
```

Use `docker compose down -v` only when you want to delete the local Postgres data volume.
