# Database Backup & Restore Guide

This directory contains scripts to backup and restore your PostgreSQL databases for the IDP, Stock Monitor, and Finance Data Service applications.

## Quick Start

### Backup All Databases

**Simple backup:**
```bash
./backup-database.sh
```

**Compressed backup (recommended for storage):**
```bash
./backup-database.sh --compress
```

**Backup with custom retention (keep 60 days instead of 30):**
```bash
./backup-database.sh --compress --retain-days 60
```

### Restore a Database

**List available backups:**
```bash
ls -lh backups/
```

**Restore a specific database:**
```bash
./restore-database.sh idp ./backups/idp_20260608_101530.sql
./restore-database.sh finance_data ./backups/finance_data_20260608_101530.sql
./restore-database.sh stock_monitor ./backups/stock_monitor_20260608_101530.sql
```

**Restore from compressed backup:**
```bash
./restore-database.sh idp ./backups/idp_20260608_101530.sql.gz
```

## Backup Files

Backups are stored in the `backups/` directory with timestamp naming:
```
backups/
├── idp_20260608_101530.sql
├── finance_data_20260608_101530.sql
└── stock_monitor_20260608_101530.sql
```

Backup sizes (uncompressed): ~1-5 MB per database
Backup sizes (compressed): ~100-500 KB per database

## Automated Backup with Cron

To run backups automatically every day at 2 AM:

```bash
# Edit crontab
crontab -e

# Add this line (daily backup at 2 AM, compressed, keep 30 days):
0 2 * * * cd /home/msun/projects/idp && ./backup-database.sh --compress --retain-days 30 >> backups/cron.log 2>&1
```

View backup logs:
```bash
tail -f backups/cron.log
```

## Recovery Scenarios

### Scenario 1: Restore Latest Backup
```bash
# Get the most recent backup
LATEST=$(ls -t backups/idp_*.sql | head -1)
./restore-database.sh idp "$LATEST"
```

### Scenario 2: Restore Specific Date
```bash
# Restore from a specific backup file
./restore-database.sh idp ./backups/idp_20260605_020000.sql
```

### Scenario 3: Backup Before Making Changes
```bash
# Backup before a major operation
./backup-database.sh --compress

# Do your thing...

# If something breaks, restore
./restore-database.sh idp ./backups/idp_20260608_120000.sql
```

## Important Notes

⚠️ **Prerequisites:**
- Docker containers must be running (`docker-compose up`)
- `postgres-shared` container must be accessible
- Have read/write permissions in the `backups/` directory

⚠️ **Before Restoring:**
- Restore will **drop and recreate** the database
- All data in the target database will be **permanently deleted**
- You will be prompted to confirm before proceeding
- Consider backing up again before restoring if uncertain

✅ **Best Practices:**
- Run backups daily (use cron)
- Compress backups to save space
- Keep at least 2 weeks of backups
- Test restores periodically to ensure they work
- Store off-site backups for disaster recovery

## Troubleshooting

### "postgres-shared container not found"
```bash
# Start docker containers first
docker-compose up -d

# Then run backup
./backup-database.sh
```

### "Permission denied" error
```bash
# Make scripts executable
chmod +x backup-database.sh restore-database.sh
```

### Very large backup files
```bash
# Use compression to reduce size
./backup-database.sh --compress

# Files will be saved as .sql.gz instead of .sql
```

### Need to backup specific database only
```bash
# Manually backup single database
docker exec postgres-shared pg_dump -U postgres idp > backups/idp_manual.sql

# Or restore single database
docker exec -i postgres-shared psql -U postgres idp < backups/idp_manual.sql
```

## Backup Schedule Recommendation

For production:
```bash
# Daily backup at 2 AM (compressed, 30-day retention)
0 2 * * * cd /home/msun/projects/idp && ./backup-database.sh --compress --retain-days 30

# Weekly full backup at 3 AM Sunday (compressed, 90-day retention)
0 3 * * 0 cd /home/msun/projects/idp && ./backup-database.sh --compress --retain-days 90
```

## Manual Backup/Restore Commands

If you need to do it manually without scripts:

```bash
# Backup
docker exec postgres-shared pg_dump -U postgres idp > backup.sql

# Restore
docker exec -i postgres-shared psql -U postgres idp < backup.sql

# Backup all databases at once
docker exec postgres-shared pg_dumpall -U postgres > full_backup.sql

# Restore all databases
docker exec -i postgres-shared psql -U postgres < full_backup.sql
```
