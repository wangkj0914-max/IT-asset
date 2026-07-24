#!/bin/bash
# IT Asset System - Database Restore Script
# Usage: ./restore.sh </path/to/backup.sql.gz>

if [ -z "$1" ]; then
    echo "Usage: $0 <backup_file.sql.gz>"
    exit 1
fi

DB_NAME="it_asset_manage"
DB_USER="root"
DB_PASS="CHNX#000"
FILE="$1"

if [ ! -f "$FILE" ]; then
    echo "Error: $FILE not found"
    exit 1
fi

echo "WARNING: This will overwrite database '$DB_NAME'!"
read -p "Type 'yes' to confirm: " CONFIRM
if [ "$CONFIRM" != "yes" ]; then
    echo "Aborted."
    exit 0
fi

echo "[$(date)] Restoring from $FILE..."
gunzip < "$FILE" | mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME"

if [ $? -eq 0 ]; then
    echo "[$(date)] Restore completed successfully."
else
    echo "[$(date)] Restore FAILED!" >&2
    exit 1
fi
