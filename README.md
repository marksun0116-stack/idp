# Investor Development Platform (IDP)

Unified multi-service development platform featuring:
- **IDP** - Investment decision & strategy platform (React + Spring Boot)
- **Stock Monitor** - Stock tracking & portfolio management (React + Spring Boot)
- **Finance Data Service** - Shared market data provider (Spring Boot)
- **PostgreSQL 16** - Single unified database with 3 schemas (idp, finance_data, stock_monitor)

## Prerequisites

- WSL2 or Linux/Mac
- Docker Desktop with WSL integration, or Docker Engine
- Run commands from repository root (`/home/msun/projects/idp`)

## Quick Start

### Start All Services
```bash
./scripts/dev-up.sh
```

First run takes a few minutes to download images and build containers.

### Access Applications

| Service | URL | Port |
|---------|-----|------|
| IDP Frontend | http://localhost:3000 | 3000 |
| Stock Monitor Frontend | http://localhost:3001 | 3001 |
| IDP Backend | http://localhost:8081 | 8081 |
| Stock Monitor Backend | http://localhost:8080 | 8080 |
| Finance Data Service | http://localhost:8082 | 8082 |
| PostgreSQL | localhost:5432 | 5432 |

### View Logs

Follow logs for any service:
```bash
./scripts/dev-logs.sh stock-monitor    # Follow stock-monitor logs
./scripts/dev-logs.sh finance-data-service
./scripts/dev-logs.sh idp
```

### Stop Services
```bash
./scripts/dev-down.sh
```

### Reset Database (Delete All Data)
```bash
./scripts/dev-reset-db.sh
```

### Rebuild Services
```bash
./scripts/dev-rebuild.sh
```

## Database

### Unified PostgreSQL Setup

Single PostgreSQL 16 instance with 3 databases:

```
postgres-shared (port 5432)
├── idp (IDP application data)
├── finance_data (Market data & quotes cache)
└── stock_monitor (Watchlists, portfolios, alerts)
```

**Default credentials:**
```
username: postgres
password: postgres
```

This matches cloud deployment architecture (single RDS with multiple databases).

## Backup & Restore

### Backup All Databases
```bash
# Simple backup
./scripts/backup-database.sh

# Compressed backup (recommended - reduces size by 90%)
./scripts/backup-database.sh --compress

# Custom retention (keep 60 days instead of 30)
./scripts/backup-database.sh --compress --retain-days 60
```

Backups are stored in `backups/` with timestamps.

### Restore from Backup
```bash
# List available backups
ls -lh backups/

# Restore specific database
./scripts/restore-database.sh idp ./backups/idp_20260608_103727.sql.gz
./scripts/restore-database.sh stock_monitor ./backups/stock_monitor_20260608_103727.sql.gz
./scripts/restore-database.sh finance_data ./backups/finance_data_20260608_103727.sql.gz
```

### Automated Daily Backups

Set up cron for automatic backups at 2 AM daily:

```bash
crontab -e

# Add this line:
0 2 * * * cd /home/msun/projects/idp && ./scripts/backup-database.sh --compress --retain-days 30 >> backups/cron.log 2>&1
```

See `scripts/BACKUP_RESTORE.md` for detailed backup documentation.

## Scripts Reference

All utility scripts are in the `scripts/` directory:

| Script | Purpose |
|--------|---------|
| `dev-up.sh` | Start all services with Docker Compose |
| `dev-down.sh` | Stop all services |
| `dev-logs.sh` | View logs for a specific service |
| `dev-rebuild.sh` | Rebuild services from scratch |
| `dev-reset-db.sh` | Delete database volume and stop services |
| `backup-database.sh` | Backup all databases (compressed recommended) |
| `restore-database.sh` | Restore database from backup |

## API Testing

### Stock Monitor API
```bash
# Get watchlist for a user
curl -H "Authorization: Bearer testuser" \
  http://localhost:8080/api/watchlists

# Add stock to watchlist
curl -X POST http://localhost:8080/api/watchlists \
  -H "Authorization: Bearer testuser" \
  -H "Content-Type: application/json" \
  -d '{"name":"Tech Stocks"}'
```

### Finance Data Service API
```bash
# Get live quote
curl http://localhost:8082/api/finance/quote?symbols=AAPL

# Get market movers
curl http://localhost:8082/api/finance/movers/gainers
```

### IDP API
```bash
# Get decisions
curl -H "Authorization: Bearer testuser" \
  http://localhost:8081/api/decisions

# Create decision
curl -X POST http://localhost:8081/api/decisions \
  -H "Authorization: Bearer testuser" \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "NVDA",
    "decisionType": "watch",
    "title": "NVDA infrastructure thesis",
    "thesis": "AI infrastructure demand may support durable revenue growth.",
    "confidence": 8
  }'
```

## Useful Docker Commands

```bash
# Check service status
docker compose ps

# View service logs
docker compose logs -f stock-monitor
docker compose logs -f postgres
docker compose logs -f idp

# Connect to database directly
docker exec -it postgres-shared psql -U postgres -d idp

# Inspect container
docker inspect postgres-shared

# Stop without removing
docker compose stop

# Full reset with volume deletion
docker compose down -v
```

## Project Structure

```
idp/
├── docker-compose.yml          # Orchestrates all services
├── src/
│   ├── backend/               # IDP Spring Boot backend
│   ├── frontend/              # IDP React frontend
│   └── ...
├── scripts/                   # Utility scripts
│   ├── dev-up.sh
│   ├── dev-down.sh
│   ├── backup-database.sh
│   ├── restore-database.sh
│   └── ...
├── backups/                   # Database backups (created on first backup)
├── docker-compose.yml         # Main orchestration file
└── README.md                  # This file
```

## Knowledge-First System

This project follows the Knowledge-First System (KFS) for development. Before making implementation changes:

1. Check `knowledge/catalog.yml` for knowledge primitives
2. Update relevant knowledge files if behavior changes
3. Update architecture/design docs as needed
4. Then implement code changes
5. Run validation: `python3 .knowledge-first-system/scripts/validate_knowledge.py`

See `CLAUDE.md` for detailed KFS instructions.

## Troubleshooting

**"Address already in use" error:**
```bash
# Port is already in use, change it via environment variable
IDP_BACKEND_PORT=8082 ./scripts/dev-up.sh
```

**Database connection fails:**
```bash
# Verify postgres is running
docker compose ps

# Check postgres logs
./scripts/dev-logs.sh postgres

# Reset database
./scripts/dev-reset-db.sh
./scripts/dev-up.sh
```

**Services won't start:**
```bash
# Clean up and restart
docker compose down
docker compose up --build
```

**Need to access database directly:**
```bash
docker exec -it postgres-shared psql -U postgres

# In psql:
\l                    # List databases
\c idp                # Connect to idp database
\dt                   # List tables
```

## Development Notes

- All services run in Docker containers
- Services communicate via Docker network (not localhost)
- Database credentials are hardcoded for local development only
- Change in `docker-compose.yml` requires rebuild: `./scripts/dev-rebuild.sh`
- Database schema auto-creates via Spring Boot (ddl-auto: update)
