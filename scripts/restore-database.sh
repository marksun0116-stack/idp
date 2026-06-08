#!/bin/bash

# Database Restore Script
# Restores a database from backup
# Usage: ./restore-database.sh <database_name> <backup_file>
# Example: ./restore-database.sh idp ./backups/idp_20260608_101530.sql

set -e

BACKUP_DIR="$(dirname "$0")/../backups"

if [ $# -lt 2 ]; then
    echo "Usage: $0 <database_name> <backup_file>"
    echo ""
    echo "Example: $0 idp ../backups/idp_20260608_101530.sql"
    echo ""
    echo "Available databases: idp, finance_data, stock_monitor"
    echo ""
    echo "Recent backups:"
    ls -lh "$BACKUP_DIR" 2>/dev/null | tail -5 || echo "No backups found in $BACKUP_DIR"
    exit 1
fi

DB_NAME=$1
BACKUP_FILE=$2

# Verify backup file exists
if [ ! -f "$BACKUP_FILE" ]; then
    echo "Error: Backup file not found: $BACKUP_FILE"
    exit 1
fi

# Handle gzipped files
if [[ "$BACKUP_FILE" == *.gz ]]; then
    echo "Decompressing gzipped backup..."
    TEMP_FILE=$(mktemp)
    gunzip -c "$BACKUP_FILE" > "$TEMP_FILE"
    BACKUP_FILE=$TEMP_FILE
    CLEANUP_TEMP=true
else
    CLEANUP_TEMP=false
fi

echo "=========================================="
echo "Database Restore"
echo "=========================================="
echo "Database: $DB_NAME"
echo "Backup file: $BACKUP_FILE"
echo "Timestamp: $(date)"
echo ""

# Confirmation
read -p "⚠️  This will overwrite the '$DB_NAME' database. Continue? (yes/no) " -r
echo ""
if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
    echo "Restore cancelled."
    [ "$CLEANUP_TEMP" = true ] && rm -f "$BACKUP_FILE"
    exit 0
fi

echo "Restoring database: $DB_NAME"

# Drop and recreate database
echo "  - Dropping existing database..."
docker exec postgres-shared psql -U postgres -c "DROP DATABASE IF EXISTS $DB_NAME;" || true

echo "  - Creating new database..."
docker exec postgres-shared psql -U postgres -c "CREATE DATABASE $DB_NAME;"

# Restore from backup
echo "  - Restoring from backup..."
docker exec -i postgres-shared psql -U postgres "$DB_NAME" < "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Restore successful!"
    echo "Database '$DB_NAME' has been restored."
else
    echo ""
    echo "✗ Restore failed!"
    exit 1
fi

# Cleanup temp file if decompressed
if [ "$CLEANUP_TEMP" = true ]; then
    rm -f "$BACKUP_FILE"
fi

echo ""
echo "Restore completed at $(date)"
