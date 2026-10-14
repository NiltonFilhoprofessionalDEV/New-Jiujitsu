#!/bin/sh
# Dump diário do Postgres em formato custom (pg_restore), com retenção em dias.
# Conexão via PGHOST/PGUSER/PGPASSWORD/PGDATABASE. Copie /backups para fora do servidor
# (ex.: rclone para um bucket): backup no mesmo disco não protege contra perda do servidor.
#
# Restaurar (com a API parada):
#   docker compose -f docker-compose.prod.yml exec backup \
#     pg_restore --clean --if-exists --no-owner -d "$PGDATABASE" /backups/<arquivo>.dump
set -eu

: "${BACKUP_KEEP_DAYS:=14}"
: "${BACKUP_INTERVAL_SECONDS:=86400}"
BACKUP_DIR=/backups

while true; do
  stamp=$(date -u +%Y%m%d-%H%M%S)
  target="$BACKUP_DIR/academia-$stamp.dump"
  # Grava num temporário e renomeia: um dump interrompido nunca parece válido
  if pg_dump --format=custom --no-owner --file="$target.partial"; then
    mv "$target.partial" "$target"
    echo "backup ok: $(basename "$target")"
  else
    rm -f "$target.partial"
    echo "backup FALHOU em $stamp" >&2
  fi
  find "$BACKUP_DIR" -name 'academia-*.dump' -type f -mtime "+$BACKUP_KEEP_DAYS" -delete
  sleep "$BACKUP_INTERVAL_SECONDS"
done
