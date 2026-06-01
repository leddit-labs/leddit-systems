#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
TERRAFORM_DIR="$SCRIPT_DIR"

KEYCLOAK_URL="http://localhost:8180"
KEYCLOAK_HEALTH_URL="$KEYCLOAK_URL/health/ready"
KEYCLOAK_TIMEOUT=120

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------
log() { echo "[$(date '+%H:%M:%S')] $*"; }
err() { echo "[$(date '+%H:%M:%S')] ERROR: $*" >&2; }
die() {
  err "$*"
  exit 1
}

check_dependency() {
  command -v "$1" &>/dev/null || die "'$1' is not installed or not in PATH"
}

wait_for_keycloak() {
  log "Waiting for Keycloak to be ready at $KEYCLOAK_HEALTH_URL ..."
  local elapsed=0
  until curl -sf "$KEYCLOAK_HEALTH_URL" | grep -q '"status":"UP"' 2>/dev/null; do
    if ((elapsed >= KEYCLOAK_TIMEOUT)); then
      die "Keycloak did not become healthy within ${KEYCLOAK_TIMEOUT}s"
    fi
    sleep 3
    ((elapsed += 3))
    log "  still waiting... (${elapsed}s elapsed)"
  done
  log "Keycloak is healthy."
}

# ---------------------------------------------------------------------------
# Pre-flight checks
# ---------------------------------------------------------------------------
check_dependency docker
check_dependency terraform
check_dependency curl

[[ -f "$TERRAFORM_DIR/terraform.tfvars" ]] ||
  die "terraform.tfvars not found in $TERRAFORM_DIR — copy terraform.tfvars.example and fill in your values"

# ---------------------------------------------------------------------------
# Docker Compose
# ---------------------------------------------------------------------------
log "Starting Docker Compose from $COMPOSE_DIR ..."
docker compose -f "$COMPOSE_DIR/docker-compose.yml" up -d --build

# ---------------------------------------------------------------------------
# Wait for Keycloak
# ---------------------------------------------------------------------------
wait_for_keycloak

# ---------------------------------------------------------------------------
# Terraform
# ---------------------------------------------------------------------------
log "Running Terraform in $TERRAFORM_DIR ..."
cd "$TERRAFORM_DIR"
terraform init -input=false
terraform apply -input=false -auto-approve

log "Done. Useful outputs:"
terraform output
