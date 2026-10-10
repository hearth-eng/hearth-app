#!/bin/sh
# Writes cert/key material (injected by ECS from Secrets Manager as env vars) to the
# fixed, container-relative paths server.xml already expects (WORKDIR /app, see
# <keystore-config>/<truststore-config> in server.xml):
#   store/hearth.pkcs      <- SHARED_PKCS12_B64 (JWT signing/verification keypair -
#                              byte-identical file to hearth-ui's store/hearth.pkcs)
#   store/hearth-app.pem   <- MTLS_APP_BUNDLE_PEM (this service's own mTLS server
#                              key+cert, combined - server.xml's <bundle-path>)
#   store/ca_javalabs.crt  <- SHARED_CA_PEM (CA trust anchor - byte-identical file
#                              to hearth-ui's store/ca_javalabs.crt)
# server.xml is parsed once at startup by a third-party framework with no env var
# support, so these paths cannot be overridden - they must match exactly.
#
# Also renders a fresh db.config JSON file at /app/app-runtime.json (distinct from
# the app.json baked into the jar) from DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD,
# and points the JVM at it via -Dapp.config so AppContainer.java picks it up.
set -eu
umask 077

write_pem() { # content path
  if [ -n "$1" ] && [ -n "$2" ]; then
    mkdir -p "$(dirname "$2")"
    printf '%s\n' "$1" > "$2"
  fi
}

write_pem "${MTLS_APP_BUNDLE_PEM:-}" "store/hearth-app.pem"
write_pem "${SHARED_CA_PEM:-}"       "store/ca_javalabs.crt"
unset MTLS_APP_BUNDLE_PEM SHARED_CA_PEM

if [ -n "${SHARED_PKCS12_B64:-}" ]; then
  mkdir -p store
  echo "${SHARED_PKCS12_B64}" | base64 -d > store/hearth.pkcs
  chmod 600 store/hearth.pkcs
fi
unset SHARED_PKCS12_B64

# Escape a string for safe embedding inside a JSON double-quoted value:
# backslash first, then double-quote (order matters).
json_escape() {
  printf '%s' "$1" | sed -e 's/\\/\\\\/g' -e 's/"/\\"/g'
}

DB_HOST="${DB_HOST:-}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-}"
DB_USER_ESC=$(json_escape "${DB_USER:-}")
DB_PASSWORD_ESC=$(json_escape "${DB_PASSWORD:-}")

DB_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?sslmode=require&sslrootcert=/certs/rds-global-bundle.pem"
DB_URL_ESC=$(json_escape "$DB_URL")

# DistributedCache.init() (Redis pub/sub cache-invalidation subscriber) reads
# redis.config.url from this same runtime config - REDIS_URL is already a full
# rediss://:<auth-token>@host:port URL (same secret value hearth-ui uses), so
# it's passed straight through, just JSON-escaped.
REDIS_URL_ESC=$(json_escape "${REDIS_URL:-}")

cat > /app/app-runtime.json <<EOF
{
    "db.config": {
        "url": "${DB_URL_ESC}",
        "user": "${DB_USER_ESC}",
        "password": "${DB_PASSWORD_ESC}"
    },
    "redis.config": {
        "url": "${REDIS_URL_ESC}"
    }
}
EOF
chmod 600 /app/app-runtime.json
unset DB_USER DB_PASSWORD REDIS_URL

export JAVA_OPTS="${JAVA_OPTS:-} -Dapp.config=/app/app-runtime.json"

exec "$@"
