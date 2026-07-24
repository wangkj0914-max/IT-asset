#!/bin/bash
# IT Asset System - Database Backup Script
# Usage: ./backup.sh [retain_days]
# Crontab: 0 2 * * * /opt/it-asset/backup.sh 7

BACKUP_DIR="./backups"
DB_NAME="it_asset_manage"
DB_USER="root"
DB_PASS="CHNX#000"
RETENTION_DAYS=${1:-7}
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
FILE="${BACKUP_DIR}/${DB_NAME}_${TIMESTAMP}.sql.gz"

mkdir -p "$BACKUP_DIR"

echo "[$(date)] Starting backup..."
mysqldump -u"$DB_USER" -p"$DB_PASS" --single-transaction --routines --triggers "$DB_NAME" | gzip > "$FILE"

if [ $? -eq 0 ]; then
    SIZE=$(du -h "$FILE" | cut -f1)
    echo "[$(date)] Backup OK: $FILE ($SIZE)"
else
    echo "[$(date)] Backup FAILED!" >&2
    exit 1
fi

# Clean old backups
DELETED=$(find "$BACKUP_DIR" -name "${DB_NAME}_*.sql.gz" -mtime +$RETENTION_DAYS -delete -print | wc -l)
echo "[$(date)] Cleaned $DELETED old backups (older than $RETENTION_DAYS days)"
