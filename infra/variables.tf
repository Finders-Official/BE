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
  description = "Cloudflare API Token"
  type        = string
  sensitive   = true
}

variable "cloudflare_account_id" {
  description = "Cloudflare Account ID"
  type        = string
  sensitive   = true
}

variable "admin_member_emails" {
  description = "Admin email addresses (roles/editor + logging + monitoring + SA impersonation)"
  type        = list(string)
  default     = []
}

variable "team_member_emails" {
  description = "Team member email addresses (logging + monitoring + IAP + compute + SA impersonation)"
  type        = list(string)
  default     = []
}

variable "photo_team_member_emails" {
  description = "Photo team member emails (additional GCS viewer access)"
  type        = list(string)
  default     = []
}

variable "compute_sa_email" {
  description = "Compute Engine default service account email"
  type        = string
  default     = ""
}
