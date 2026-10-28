#!/bin/sh
# Runs once, the first time the postgres volume is created.
# One role + one database per service (each service owns its own data).
# To add a service: add a create_db line, and pass its password env var to the postgres container in docker-compose.yml.
set -e

create_db() {
  svc_user="$1"
  svc_pass="$2"
  svc_db="$3"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<EOSQL
CREATE ROLE ${svc_user} LOGIN PASSWORD '${svc_pass}';
CREATE DATABASE ${svc_db} OWNER ${svc_user};
EOSQL
}

create_db bin_service "$BIN_DB_PASSWORD" bin_db
