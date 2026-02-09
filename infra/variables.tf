# =============================================================================
# GCP Core
# =============================================================================

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

# =============================================================================
# Cloudflare
# =============================================================================

variable "enable_cloudflare" {
  description = "Enable Cloudflare module (requires valid API token)"
  type        = bool
  default     = false
}

variable "cloudflare_api_token" {
  description = "Cloudflare API Token"
  type        = string
  sensitive   = true
  default     = ""
}

variable "cloudflare_account_id" {
  description = "Cloudflare Account ID"
  type        = string
  sensitive   = true
  default     = ""
}

variable "cloudflare_tunnel_hostname" {
  description = "Hostname for the Cloudflare tunnel (e.g. finders-api.log8.kr)"
  type        = string
  default     = ""
}

variable "cloudflare_tunnel_service" {
  description = "Local service URL for the Cloudflare tunnel"
  type        = string
  default     = "http://localhost:8080"
}

# =============================================================================
# IAM
# =============================================================================

variable "admin_member_emails" {
  description = "Admin email addresses (logging + monitoring + SA impersonation + resource management)"
  type        = list(string)
  default     = []
}

variable "team_member_emails" {
  description = "Team member email addresses (logging + monitoring + IAP + compute + SA impersonation)"
  type        = list(string)
  default     = []
}

# =============================================================================
# Compute
# =============================================================================

variable "compute_sa_email" {
  description = "Compute Engine default service account email"
  type        = string
}

# =============================================================================
# GCS
# =============================================================================

variable "cors_allowed_origins" {
  description = "Allowed CORS origins for GCS buckets"
  type        = list(string)
  default     = ["*"]
}
