#!/usr/bin/env bash
set -euo pipefail

# Creates test employees via the existing servlet JSON API.
#
# Usage:
#   ./tools/seed-employees.sh            # seeds 50 employees to default URL
#   ./tools/seed-employees.sh 50         # seeds 50
#   API_URL=http://localhost:9080/tomcat-spa-sample/api/employees ./tools/seed-employees.sh 50

COUNT="${1:-50}"
API_URL="${API_URL:-http://localhost:9080/tomcat-spa-sample/api/employees}"

if ! [[ "$COUNT" =~ ^[0-9]+$ ]]; then
  echo "COUNT must be a number (got: $COUNT)" >&2
  exit 1
fi

DEPARTMENTS=(Engineering HR Sales Finance Operations)

echo "Seeding $COUNT employees into: $API_URL" >&2

for i in $(seq 1 "$COUNT"); do
  dept_index=$((RANDOM % ${#DEPARTMENTS[@]}))
  dept="${DEPARTMENTS[$dept_index]}"

  # Make emails unique per run.
  email="employee${i}.$(date +%s)@example.com"
  name="Employee ${i}"
  salary=$((50000 + (RANDOM % 90000)))
  if (( i % 4 == 0 )); then
    active=false
  else
    active=true
  fi

  json=$(printf '{"name":"%s","email":"%s","department":"%s","salary":%s,"active":%s}' \
    "$name" "$email" "$dept" "$salary" "$active")

  curl -fsS \
    -H 'Content-Type: application/json' \
    -X POST \
    -d "$json" \
    "$API_URL" \
    >/dev/null

done

echo "Done." >&2
