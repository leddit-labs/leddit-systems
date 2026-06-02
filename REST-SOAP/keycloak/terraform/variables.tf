variable "keycloak_url" {
  description = "Keycloak server URL"
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
  default     = "admin"
}

variable "github_client_id" {
  description = "GitHub OAuth App Client ID"
  type        = string
  default     = ""
}

variable "github_client_secret" {
  description = "GitHub OAuth App Client Secret"
  type        = string
  sensitive   = true
  default     = ""
}

variable "valid_redirect_uris" {
  description = "Valid redirect URIs for the client"
  type        = list(string)
  default     = ["http://localhost:*", "http://localhost:8080/*", "http://localhost:3000/*"]
}

variable "web_origins" {
  description = "Allowed web origins for CORS"
  type        = list(string)
  default     = ["http://localhost:8080", "http://localhost:3000"]
}

variable "test_user_username" {
  description = "Test user username"
  type        = string
  default     = "testuser"
}

variable "test_user_email" {
  description = "Test user email"
  type        = string
  default     = "testuser@example.com"
}

variable "test_user_password" {
  description = "Test user password"
  type        = string
  sensitive   = true
  default     = "testpass123"
}