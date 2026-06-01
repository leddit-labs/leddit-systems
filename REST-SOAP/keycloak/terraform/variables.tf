variable "keycloak_url" {
  description = "Base URL of the Keycloak instance"
  type        = string
  default     = "http://localhost:8180"
}

variable "keycloak_admin_user" {
  description = "Keycloak admin username"
  type        = string
  default     = "admin"
}

variable "keycloak_admin_password" {
  description = "Keycloak admin password"
  type        = string
  sensitive   = true
}

variable "valid_redirect_uris" {
  description = "Allowed redirect URIs after login (use * only in dev)"
  type        = list(string)
  default     = ["http://localhost:*", "http://localhost:8080/*"]
}

variable "web_origins" {
  description = "Allowed CORS origins"
  type        = list(string)
  default     = ["http://localhost:8080", "http://localhost:3000"]
}

variable "github_client_id" {
  description = "GitHub OAuth App client ID"
  type        = string
  sensitive   = true
}

variable "github_client_secret" {
  description = "GitHub OAuth App client secret"
  type        = string
  sensitive   = true
}

variable "test_user_username" {
  description = "Username for the test user"
  type        = string
  default     = "testuser"
}

variable "test_user_email" {
  description = "Email for the test user"
  type        = string
  default     = "testuser@example.com"
}

variable "test_user_password" {
  description = "Initial password for the test user"
  type        = string
  sensitive   = true
  default     = "testpass123"
}
