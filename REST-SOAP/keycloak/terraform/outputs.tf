output "realm_name" {
  value       = keycloak_realm.gameapi.realm
  description = "The Keycloak realm name"
}

output "client_id" {
  value       = keycloak_openid_client.gameapi_client.client_id
  description = "The client ID to use when requesting tokens"
}

output "issuer_uri" {
  value       = "${var.keycloak_url}/realms/${keycloak_realm.gameapi.realm}"
  description = "Set this as SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI in docker-compose.yml"
}

output "token_endpoint" {
  value       = "${var.keycloak_url}/realms/${keycloak_realm.gameapi.realm}/protocol/openid-connect/token"
  description = "Endpoint to request tokens (password grant or code exchange)"
}

output "test_token_curl" {
  value       = "curl -s -X POST ${var.keycloak_url}/realms/${keycloak_realm.gameapi.realm}/protocol/openid-connect/token -d 'client_id=${keycloak_openid_client.gameapi_client.client_id}&grant_type=password&username=${var.test_user_username}&password=${var.test_user_password}' | jq -r .access_token"
  description = "Ready-to-run curl command to get a test token"
}
