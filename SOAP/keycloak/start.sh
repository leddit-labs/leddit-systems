#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

docker compose -f "$SCRIPT_DIR/../docker-compose.yml" up -d --build
docker compose -f "$SCRIPT_DIR/docker-compose.yml" up -d --build

echo "Waiting for Keycloak..."
until curl -sf http://localhost:9000/health/ready | grep -q '"status":"UP"'; do
  sleep 3
done

cd "$SCRIPT_DIR/terraform"
terraform init -input=false
terraform apply -input=false -auto-approve
