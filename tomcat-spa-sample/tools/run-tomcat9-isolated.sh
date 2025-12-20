#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Prefer user-provided CATALINA_HOME, else Homebrew tomcat@9.
CATALINA_HOME="${CATALINA_HOME:-$(brew --prefix tomcat@9)/libexec}"

BASE="${CATALINA_BASE:-/tmp/tomcat9-base-tomcat-spa-sample}"
HTTP_PORT="${TOMCAT_HTTP_PORT:-9080}"
SHUTDOWN_PORT="${TOMCAT_SHUTDOWN_PORT:-9005}"
AJP_PORT="${TOMCAT_AJP_PORT:-9009}"

WAR="$ROOT_DIR/target/tomcat-spa-sample.war"

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
echo "  URL: http://localhost:$HTTP_PORT/tomcat-spa-sample/app/" >&2
echo "  Logs: $CATALINA_BASE/logs/catalina.out" >&2
echo "To stop:" >&2
echo "  CATALINA_HOME=\"$CATALINA_HOME\" CATALINA_BASE=\"$CATALINA_BASE\" \"$CATALINA_HOME/bin/catalina.sh\" stop" >&2
