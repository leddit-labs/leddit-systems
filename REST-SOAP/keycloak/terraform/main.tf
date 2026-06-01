terraform {
  required_providers {
    keycloak = {
      source  = "mrparkers/keycloak"
      version = "~> 4.0"
    }
  }
}

# ---------------------------------------------------------------------------
# Provider — points at your local Keycloak container
# Credentials come from terraform.tfvars or environment variables
# ---------------------------------------------------------------------------
provider "keycloak" {
  client_id = "admin-cli"
  username  = var.keycloak_admin_user
  password  = var.keycloak_admin_password
  url       = var.keycloak_url
}

# ---------------------------------------------------------------------------
# Realm
# ---------------------------------------------------------------------------
resource "keycloak_realm" "gameapi" {
  realm        = "gameapi"
  enabled      = true
  display_name = "GameAPI"

  # Token lifetimes
  access_token_lifespan              = "15m"
  access_token_lifespan_for_implicit = "15m"
  sso_session_idle_timeout           = "30m"
  sso_session_max_lifespan           = "10h"

  # Refresh token
  refresh_token_max_reuse = 0

  login_theme = "keycloak"
}

# ---------------------------------------------------------------------------
# Client — the Spring resource server / frontend will use this
# ---------------------------------------------------------------------------
resource "keycloak_openid_client" "gameapi_client" {
  realm_id  = keycloak_realm.gameapi.id
  client_id = "gameapi-client"
  name      = "GameAPI Client"
  enabled   = true

  # Public client (no client secret) — suitable for SPAs / mobile
  # Change to "confidential" if you have a backend client that can keep a secret
  access_type = "PUBLIC"

  # Both flows as requested
  standard_flow_enabled        = true  # Authorization Code (browser)
  direct_access_grants_enabled = true  # Password grant (curl / testing)

  # Where Keycloak is allowed to redirect after login
  valid_redirect_uris = var.valid_redirect_uris

  # Allowed origins for CORS
  web_origins = var.web_origins

  login_theme = "keycloak"
}

# ---------------------------------------------------------------------------
# Role
# ---------------------------------------------------------------------------
resource "keycloak_role" "user_role" {
  realm_id    = keycloak_realm.gameapi.id
  name        = "user"
  description = "Standard user role"
}

# ---------------------------------------------------------------------------
# Test user
# ---------------------------------------------------------------------------
resource "keycloak_user" "test_user" {
  realm_id = keycloak_realm.gameapi.id
  username = var.test_user_username
  enabled  = true

  email      = var.test_user_email
  first_name = "Test"
  last_name  = "User"

  # Sets an initial password — user will NOT be forced to reset on first login
  initial_password {
    value     = var.test_user_password
    temporary = false
  }
}

resource "keycloak_user_roles" "test_user_roles" {
  realm_id = keycloak_realm.gameapi.id
  user_id  = keycloak_user.test_user.id

  role_ids = [
    keycloak_role.user_role.id,
  ]
}

# ---------------------------------------------------------------------------
# GitHub Identity Provider
# ---------------------------------------------------------------------------
resource "keycloak_oidc_identity_provider" "github" {
  realm             = keycloak_realm.gameapi.id
  alias             = "github"
  display_name      = "GitHub"
  provider_id       = "github"
  enabled           = true
  store_token       = false
  trust_email       = true   # Trust the email GitHub provides — set false if you want verification
  first_broker_login_flow_alias = "first broker login"

  client_id     = var.github_client_id
  client_secret = var.github_client_secret

  extra_config = {
    # GitHub's OIDC-compatible endpoints
    "authorizationUrl" = "https://github.com/login/oauth/authorize"
    "tokenUrl"         = "https://github.com/login/oauth/access_token"
    "userInfoUrl"      = "https://api.github.com/user"
    "defaultScope"     = "user:email"
    "syncMode"         = "IMPORT"
  }
}

# Map GitHub's "login" attribute to Keycloak username on first login
resource "keycloak_attribute_importer_identity_provider_mapper" "github_username" {
  realm                   = keycloak_realm.gameapi.id
  name                    = "github-username-mapper"
  identity_provider_alias = keycloak_oidc_identity_provider.github.alias
  attribute_name          = "login"
  user_attribute          = "username"
  extra_config = {
    syncMode = "INHERIT"
  }
}

# Map GitHub's email to Keycloak email
resource "keycloak_attribute_importer_identity_provider_mapper" "github_email" {
  realm                   = keycloak_realm.gameapi.id
  name                    = "github-email-mapper"
  identity_provider_alias = keycloak_oidc_identity_provider.github.alias
  attribute_name          = "email"
  user_attribute          = "email"
  extra_config = {
    syncMode = "INHERIT"
  }
}

# Automatically assign the "user" role to anyone who logs in via GitHub
resource "keycloak_hardcoded_role_identity_provider_mapper" "github_user_role" {
  realm                   = keycloak_realm.gameapi.id
  name                    = "github-user-role-mapper"
  identity_provider_alias = keycloak_oidc_identity_provider.github.alias
  role                    = keycloak_role.user_role.name
  extra_config = {
    syncMode = "INHERIT"
  }
}

# ---------------------------------------------------------------------------
# Include roles in the JWT so Spring can use them
# ---------------------------------------------------------------------------
resource "keycloak_openid_user_realm_role_protocol_mapper" "roles_mapper" {
  realm_id  = keycloak_realm.gameapi.id
  client_id = keycloak_openid_client.gameapi_client.id
  name      = "realm-roles"

  claim_name          = "roles"
  multivalued         = true
  add_to_id_token     = true
  add_to_access_token = true
  add_to_userinfo     = true
}
