#!/bin/bash
# Cria (ou atualiza a senha de) os usuários separados da Caderneta. Idempotente.
#   caderneta_app       -> só DML (SELECT/INSERT/UPDATE/DELETE): usado pela API
#   caderneta_migracao  -> DDL + DML: usado apenas pela tarefa "migracao" (Flyway)
#
# Uso:
#  - Automático no compose: montado em /docker-entrypoint-initdb.d (só roda em volume NOVO).
#  - Manual (banco já existente ou outro servidor), com as variáveis exportadas:
#      MYSQL_HOST=... MYSQL_ROOT_USER=root MYSQL_ROOT_PASSWORD=... \
#      CADERNETA_DB_PASSWORD=... CADERNETA_MIGRATION_PASSWORD=... ./criar-usuarios-mysql.sh
# Nunca grave as senhas em arquivo versionado.
set -euo pipefail

: "${MYSQL_ROOT_PASSWORD:?defina MYSQL_ROOT_PASSWORD}"
: "${CADERNETA_DB_PASSWORD:?defina CADERNETA_DB_PASSWORD}"
: "${CADERNETA_MIGRATION_PASSWORD:?defina CADERNETA_MIGRATION_PASSWORD}"
DB="${MYSQL_DATABASE:-api_caderneta_database}"
APP="${CADERNETA_DB_USER:-caderneta_app}"
MIG="${CADERNETA_MIGRATION_USER:-caderneta_migracao}"
ROOT_USER="${MYSQL_ROOT_USER:-root}"
# Sem MYSQL_HOST usa o socket local (no initdb do container o servidor temporário não abre TCP).
HOST_ARGS=(--protocol=SOCKET)
[ -n "${MYSQL_HOST:-}" ] && HOST_ARGS=(--protocol=TCP -h "$MYSQL_HOST" -P "${MYSQL_PORT:-3306}")

if [ "$APP" = "$MIG" ] || [ "$CADERNETA_DB_PASSWORD" = "$CADERNETA_MIGRATION_PASSWORD" ]; then
  echo "Usuário e senha da aplicação devem ser diferentes dos da migração." >&2
  exit 1
fi

# Escapa para literal SQL entre aspas simples.
esc() { printf '%s' "$1" | sed -e 's/\\/\\\\/g' -e "s/'/''/g"; }

# A senha do root vai por variável de ambiente (não aparece na lista de processos).
MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql "${HOST_ARGS[@]}" -u"$ROOT_USER" <<SQL
CREATE DATABASE IF NOT EXISTS \`$DB\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS '$APP'@'%' IDENTIFIED BY '$(esc "$CADERNETA_DB_PASSWORD")';
ALTER USER '$APP'@'%' IDENTIFIED BY '$(esc "$CADERNETA_DB_PASSWORD")';
GRANT SELECT, INSERT, UPDATE, DELETE ON \`$DB\`.* TO '$APP'@'%';
CREATE USER IF NOT EXISTS '$MIG'@'%' IDENTIFIED BY '$(esc "$CADERNETA_MIGRATION_PASSWORD")';
ALTER USER '$MIG'@'%' IDENTIFIED BY '$(esc "$CADERNETA_MIGRATION_PASSWORD")';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON \`$DB\`.* TO '$MIG'@'%';
SQL
echo "Usuários '$APP' (DML) e '$MIG' (DDL) prontos em '$DB'."
