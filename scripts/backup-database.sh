#!/bin/bash

# Database Backup Script
# Backs up all three databases: idp, finance_data, stock_monitor
# Usage: ./backup-database.sh [--compress] [--retain-days N]

set -e

BACKUP_DIR="$(dirname "$0")/../backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
COMPRESS=false
RETAIN_DAYS=30

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --compress)
            COMPRESS=true
            shift
            ;;
        --retain-days)
            RETAIN_DAYS=$2
            shift 2
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Create backups directory
mkdir -p "$BACKUP_DIR"

# Databases to backup
DATABASES=("idp" "finance_data" "stock_monitor")

echo "Starting database backup at $(date)"
echo "Backup directory: $BACKUP_DIR"
echo "Timestamp: $TIMESTAMP"
echo ""

# Backup each database
for db in "${DATABASES[@]}"; do
    echo "Backing up database: $db"

    BACKUP_FILE="$BACKUP_DIR/${db}_${TIMESTAMP}.sql"

    # Run pg_dump via docker
    docker exec postgres-shared pg_dump -U postgres "$db" > "$BACKUP_FILE"

    if [ $? -eq 0 ]; then
        SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
        echo "  ✓ Backup successful: $BACKUP_FILE ($SIZE)"

        # Compress if requested
        if [ "$COMPRESS" = true ]; then
            gzip "$BACKUP_FILE"
            BACKUP_FILE="${BACKUP_FILE}.gz"
            SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
            echo "  ✓ Compressed: $BACKUP_FILE ($SIZE)"
        fi
    else
        echo "  ✗ Backup failed for $db"
        exit 1
    fi
done

echo ""
echo "All backups completed successfully!"
echo ""

# Cleanup old backups
echo "Cleaning up backups older than $RETAIN_DAYS days..."
find "$BACKUP_DIR" -name "*.sql*" -type f -mtime +$RETAIN_DAYS -delete

REMAINING=$(find "$BACKUP_DIR" -name "*.sql*" -type f | wc -l)
echo "Retention cleanup complete. $REMAINING backup files remaining."
echo ""

# List recent backups
echo "Recent backups:"
ls -lh "$BACKUP_DIR" | tail -10

echo ""
echo "Backup completed at $(date)"
