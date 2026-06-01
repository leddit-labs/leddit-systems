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
  login_theme  = "keycloak"
}

resource "keycloak_openid_client" "gameapi_client" {
  realm_id  = keycloak_realm.gameapi.id
  client_id = "gameapi-client"
  name      = "GameAPI Client"
  enabled   = true

  access_type                 = "PUBLIC"
  standard_flow_enabled       = true
  direct_access_grants_enabled = true

  valid_redirect_uris = var.valid_redirect_uris
  web_origins         = var.web_origins
}

resource "keycloak_role" "user_role" {
  realm_id    = keycloak_realm.gameapi.id
  name        = "user"
  description = "Standard user role"
}

resource "keycloak_user" "test_user" {
  realm_id = keycloak_realm.gameapi.id
  username = var.test_user_username
  enabled  = true

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

resource "keycloak_oidc_identity_provider" "github" {
  realm                         = keycloak_realm.gameapi.id
  alias                         = "github"
  display_name                  = "GitHub"
  provider_id                   = "github"
  enabled                       = true
  trust_email                   = true
  first_broker_login_flow_alias = "first broker login"
  sync_mode                     = "IMPORT"

  client_id     = var.github_client_id
  client_secret = var.github_client_secret
  authorization_url = "https://github.com/login/oauth/authorize"
  token_url         = "https://github.com/login/oauth/access_token"

  default_scopes = "user:email"
}

resource "keycloak_hardcoded_role_identity_provider_mapper" "github_user_role" {
  realm                   = keycloak_realm.gameapi.id
  name                    = "github-user-role-mapper"
  identity_provider_alias = keycloak_oidc_identity_provider.github.alias
  role                    = keycloak_role.user_role.name
}