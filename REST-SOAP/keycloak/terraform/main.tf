terraform {
  required_providers {
    keycloak = {
      source  = "mrparkers/keycloak"
      version = "~> 4.0"
    }
  }
}

provider "keycloak" {
  client_id = "admin-cli"
  username  = var.keycloak_admin_user
  password  = var.keycloak_admin_password
  url       = var.keycloak_url
}

resource "keycloak_realm" "gameapi" {
  realm        = "gameapi"
  enabled      = true
  display_name = "GameAPI"

  access_token_lifespan = "300s"
  login_theme = "keycloak"
}

# Client
resource "keycloak_openid_client" "gameapi_client" {
  realm_id  = keycloak_realm.gameapi.id
  client_id = "gameapi-client"
  name      = "GameAPI Client"
  enabled   = true

  access_type   = "CONFIDENTIAL"
  client_secret = "dev-secret-2024"

  standard_flow_enabled        = true
  direct_access_grants_enabled = true

valid_redirect_uris = [
    "http://localhost:8080/*",
    "http://localhost:3000/*",
    "http://localhost:5500",
    "http://localhost:5500/*",
    "http://127.0.0.1:5500",
    "http://127.0.0.1:5500/*",
    "http://localhost:8080",
    "http://localhost:3000"
]
web_origins = [
    "http://localhost:8080",
    "http://localhost:3000",
    "http://localhost:5500",
    "http://127.0.0.1:5500",
    "http://localhost:8180"
]
}

# Role
resource "keycloak_role" "user_role" {
  realm_id    = keycloak_realm.gameapi.id
  name        = "user"
  description = "Standard user role"
}

# Test user
resource "keycloak_user" "test_user" {
  realm_id   = keycloak_realm.gameapi.id
  username   = var.test_user_username
  enabled    = true
  email      = var.test_user_email
  first_name = "Test"
  last_name  = "User"

  initial_password {
    value     = var.test_user_password
    temporary = false
  }
}

resource "keycloak_user_roles" "test_user_roles" {
  realm_id = keycloak_realm.gameapi.id
  user_id  = keycloak_user.test_user.id
  role_ids = [keycloak_role.user_role.id]
}

# GitHub Identity Provider
resource "keycloak_oidc_identity_provider" "github" {
  realm                         = keycloak_realm.gameapi.id
  alias                         = "github"
  display_name                  = "GitHub"
  provider_id                   = "github"
  enabled                       = true
  store_token                   = false
  trust_email                   = true
  first_broker_login_flow_alias = "first broker login"
  sync_mode                     = "IMPORT"

  client_id     = var.github_client_id
  client_secret = var.github_client_secret
  default_scopes = "user:email"

  # GitHub specific endpoints
  authorization_url = "https://github.com/login/oauth/authorize"
  token_url         = "https://github.com/login/oauth/access_token"
  user_info_url     = "https://api.github.com/user"
}

# Assign user role to GitHub users
resource "keycloak_hardcoded_role_identity_provider_mapper" "github_user_role" {
  realm                   = keycloak_realm.gameapi.id
  name                    = "github-user-role-mapper"
  identity_provider_alias = keycloak_oidc_identity_provider.github.alias
  role                    = keycloak_role.user_role.name
}

# JWT roles mapper
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