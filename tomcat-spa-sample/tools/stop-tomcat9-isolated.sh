#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Prefer user-provided CATALINA_HOME, else Homebrew tomcat@9.
CATALINA_HOME="${CATALINA_HOME:-$(brew --prefix tomcat@9)/libexec}"

BASE="${CATALINA_BASE:-/tmp/tomcat9-base-tomcat-spa-sample}"

export CATALINA_HOME
export CATALINA_BASE="$BASE"

if [[ ! -x "$CATALINA_HOME/bin/catalina.sh" ]]; then
  echo "ERROR: catalina.sh not found/executable under CATALINA_HOME=$CATALINA_HOME" >&2
  echo "Set CATALINA_HOME to your Tomcat 9 installation (or install via Homebrew: brew install tomcat@9)." >&2
  exit 1
fi

"$CATALINA_HOME/bin/catalina.sh" stop

echo "Stopped isolated Tomcat 9" >&2
echo "  CATALINA_HOME=$CATALINA_HOME" >&2
echo "  CATALINA_BASE=$CATALINA_BASE" >&2
