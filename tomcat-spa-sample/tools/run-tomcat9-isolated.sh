#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Prefer user-provided CATALINA_HOME, else Homebrew tomcat@9.
CATALINA_HOME="${CATALINA_HOME:-$(brew --prefix tomcat@9)/libexec}"

BASE="${CATALINA_BASE:-/tmp/tomcat9-base-tomcat-spa-sample}"
HTTP_PORT="${TOMCAT_HTTP_PORT:-9080}"
SHUTDOWN_PORT="${TOMCAT_SHUTDOWN_PORT:-9005}"
AJP_PORT="${TOMCAT_AJP_PORT:-9009}"

port_in_use() {
  local port="$1"
  lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1
}

pid_listening_on_port() {
  local port="$1"
  lsof -nP -iTCP:"$port" -sTCP:LISTEN -t 2>/dev/null | head -n 1
}

is_isolated_tomcat_pid() {
  local pid="$1"
  local cmd
  cmd="$(ps -p "$pid" -o command= 2>/dev/null || true)"
  [[ -n "$cmd" ]] && [[ "$cmd" == *"catalina.base=$BASE"* || "$cmd" == *"tomcat9-base-tomcat-spa-sample"* ]]
}

stop_existing_isolated_tomcat() {
  # Try graceful stop first.
  export CATALINA_HOME
  export CATALINA_BASE="$BASE"

  if [[ -x "$CATALINA_HOME/bin/catalina.sh" ]]; then
    "$CATALINA_HOME/bin/catalina.sh" stop >/dev/null 2>&1 || true
  fi

  # If still listening, force-kill only if it's our isolated Tomcat.
  local pid
  pid="$(pid_listening_on_port "$HTTP_PORT" || true)"
  if [[ -n "${pid:-}" ]] && is_isolated_tomcat_pid "$pid"; then
    kill "$pid" >/dev/null 2>&1 || true
    sleep 1
  fi

  pid="$(pid_listening_on_port "$HTTP_PORT" || true)"
  if [[ -n "${pid:-}" ]] && is_isolated_tomcat_pid "$pid"; then
    kill -9 "$pid" >/dev/null 2>&1 || true
    sleep 1
  fi
}

choose_free_port() {
  local port="$1"
  while port_in_use "$port"; do
    port=$((port + 1))
  done
  echo "$port"
}

if port_in_use "$SHUTDOWN_PORT"; then
  original="$SHUTDOWN_PORT"
  SHUTDOWN_PORT="$(choose_free_port "$SHUTDOWN_PORT")"
  echo "NOTE: TOMCAT_SHUTDOWN_PORT $original is in use; using $SHUTDOWN_PORT instead." >&2
fi

if port_in_use "$HTTP_PORT"; then
  echo "NOTE: TOMCAT_HTTP_PORT $HTTP_PORT is in use; stopping existing isolated Tomcat on $HTTP_PORT." >&2
  stop_existing_isolated_tomcat
fi

if port_in_use "$HTTP_PORT"; then
  pid="$(pid_listening_on_port "$HTTP_PORT" || true)"
  echo "ERROR: Port $HTTP_PORT is still in use (pid=${pid:-unknown}). Refusing to start on a different port." >&2
  echo "Stop the process using port $HTTP_PORT, then retry." >&2
  exit 1
fi

WAR="${WAR_PATH:-$ROOT_DIR/target/tomcat-spa-sample.war}"
CONTEXT_PATH="/$(basename "$WAR" .war)"

if [[ ! -f "$WAR" ]]; then
  echo "ERROR: WAR not found: $WAR" >&2
  echo "Build it first:" >&2
  echo "  mvn clean package" >&2
  exit 1
fi

rm -rf "$BASE"
mkdir -p "$BASE"/{conf,logs,temp,webapps,work}
cp -R "$CATALINA_HOME/conf"/* "$BASE/conf/"

# Update ports in server.xml to avoid conflicts with other Tomcat instances.
perl -pi -e "s/port=\"8005\"/port=\"$SHUTDOWN_PORT\"/g; s/port=\"8080\"/port=\"$HTTP_PORT\"/g; s/port=\"8009\"/port=\"$AJP_PORT\"/g" "$BASE/conf/server.xml"

cp -f "$WAR" "$BASE/webapps/"

export CATALINA_HOME
export CATALINA_BASE="$BASE"

"$CATALINA_HOME/bin/catalina.sh" start

echo "Started isolated Tomcat 9" >&2
echo "  CATALINA_HOME=$CATALINA_HOME" >&2
echo "  CATALINA_BASE=$CATALINA_BASE" >&2
echo "  URL: http://localhost:$HTTP_PORT$CONTEXT_PATH/app/" >&2
echo "  Logs: $CATALINA_BASE/logs/catalina.out" >&2
echo "To stop:" >&2
echo "  $ROOT_DIR/tools/stop-tomcat9-isolated.sh" >&2
echo "  (or)" >&2
echo "  CATALINA_HOME=\"$CATALINA_HOME\" CATALINA_BASE=\"$CATALINA_BASE\" \"$CATALINA_HOME/bin/catalina.sh\" stop" >&2
