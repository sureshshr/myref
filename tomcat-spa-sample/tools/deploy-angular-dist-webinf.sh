#!/usr/bin/env bash
set -euo pipefail

# Copies the Angular build output into WEB-INF/app so it is not directly browsable.
# A servlet mapping (/app/*) is used to serve it.

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC_DIR_DEFAULT="$ROOT_DIR/angular-employee-crud/dist/angular-employee-crud/browser"
DST_DIR="$ROOT_DIR/src/main/webapp/WEB-INF/app"

SRC_DIR="${1:-$SRC_DIR_DEFAULT}"

if [[ ! -d "$SRC_DIR" ]]; then
  echo "ERROR: Angular dist folder not found: $SRC_DIR" >&2
  echo "Build it first:" >&2
  echo "  cd angular-employee-crud && npm install && npx ng build --configuration production --base-href /tomcat-spa-sample/app/" >&2
  exit 1
fi

mkdir -p "$DST_DIR"

echo "Deploying Angular dist into WEB-INF" >&2
echo "  from: $SRC_DIR" >&2
echo "  to:   $DST_DIR" >&2

if command -v rsync >/dev/null 2>&1; then
  rsync -a --delete "$SRC_DIR/" "$DST_DIR/"
  echo "Done (rsync)." >&2
  exit 0
fi

shopt -s nullglob
rm -rf "$DST_DIR"/*
cp -R "$SRC_DIR/"* "$DST_DIR/"
echo "Done (cp -R)." >&2
