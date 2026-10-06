#!/bin/sh
# 1. Writes mTLS PEMs (injected by ECS from Secrets Manager as env vars) to the
#    fixed, container-relative paths that server.xml already expects (WORKDIR /app):
#      ca/ca_javalabs.crt        <- MTLS_CA_PEM    (truststore)
#      server_cert/hearth-app.crt <- MTLS_CERT_PEM (server cert)
#      server_cert/hearth-app.key <- MTLS_KEY_PEM  (server key)
#    server.xml is parsed once at startup by a third-party framework with no
#    env var support, so these paths cannot be overridden - they must match exactly.
# 2. Renders a fresh db.config JSON file at /app/app-runtime.json (distinct from
#    the app.json baked into the jar) from DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD,
#    and points the JVM at it via -Dapp.config so AppContainer.java picks it up.
# 3. Writes the shared RS256 JWT keystore to /app/hearth.pkcs. server.xml's
#    <store-name>hearth.pkcs</store-name> is a bare relative path with no dir prefix
#    and no env var hook, same constraint as above - the file must land exactly at
#    /app/hearth.pkcs. Vert.x's FileResolver checks the real filesystem before the
#    classpath, so this loose file shadows the (stale) copy baked into the jar at
#    build time - no code change needed, confirmed via Vert.x's own FileSystemOptions
#    docs. The store password stays the fixed value server.xml's <store-password>
#    already has ("secret") - only the key material itself is rotated/externalized.
set -eu
umask 077

write_pem() { # content path
  if [ -n "$1" ] && [ -n "$2" ]; then
    mkdir -p "$(dirname "$2")"
    printf '%s\n' "$1" > "$2"
  fi
}

write_pem "${MTLS_CA_PEM:-}"   "ca/ca_javalabs.crt"
write_pem "${MTLS_CERT_PEM:-}" "server_cert/hearth-app.crt"
write_pem "${MTLS_KEY_PEM:-}"  "server_cert/hearth-app.key"
unset MTLS_CA_PEM MTLS_CERT_PEM MTLS_KEY_PEM

if [ -n "${JWT_KEYSTORE_B64:-}" ]; then
  echo "${JWT_KEYSTORE_B64}" | base64 -d > /app/hearth.pkcs
  chmod 600 /app/hearth.pkcs
fi
unset JWT_KEYSTORE_B64 JWT_STOREPASS

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

cat > /app/app-runtime.json <<EOF
{
    "db.config": {
        "url": "${DB_URL_ESC}",
        "user": "${DB_USER_ESC}",
        "password": "${DB_PASSWORD_ESC}"
    }
}
EOF
chmod 600 /app/app-runtime.json
unset DB_USER DB_PASSWORD

export JAVA_OPTS="${JAVA_OPTS:-} -Dapp.config=/app/app-runtime.json"

exec "$@"
