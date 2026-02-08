variable "project_id" {
  description = "GCP Project ID"
  type        = string
  sensitive   = true
}

variable "region" {
  description = "GCP Region (default: Seoul)"
  type        = string
  default     = "asia-northeast3"
}

variable "zone" {
  description = "GCP Zone (default: Seoul-A)"
  type        = string
  default     = "asia-northeast3-a"
}

variable "cloudflare_api_token" {
  description = "Cloudflare API Token (required for Phase 5)"
  type        = string
  sensitive   = true
  default     = "" # Empty for Phase 0-4
}

# Team member emails for IAM bindings (Phase 1)
variable "team_member_emails" {
  description = "Team member email addresses for IAM bindings"
  type        = list(string)
  sensitive   = true
  default     = []
}
