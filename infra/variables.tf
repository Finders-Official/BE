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
  description = "Admin: roles/editor + projectIamAdmin + securityReviewer + SSH (near-owner level)"
  type        = list(string)
  default     = []
}

variable "lead_member_emails" {
  description = "Lead: roles/editor + projectIamAdmin + SSH (team lead level)"
  type        = list(string)
  default     = []
}

variable "team_member_emails" {
  description = "Member: logging/monitoring viewer + SSH (read-only access)"
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

variable "db_root_password" {
  description = "Root password for Cloud SQL instance (min 8 chars, mixed complexity)"
  type        = string
  sensitive   = true
}

# =============================================================================
# GCS
# =============================================================================

variable "cors_allowed_origins" {
  description = "GCS 버킷 CORS 허용 origin 목록 (브라우저→버킷 직접 접근 시, API CORS와 별도)"
  type        = list(string)
  default = [
    "https://finders.it.kr",
    "https://dev-api.finders.it.kr",
    "https://api.finders.it.kr",
    "http://localhost:5173",
    "http://localhost:8080",
  ]
}

# =============================================================================
# GitHub
# =============================================================================

variable "github_repository" {
  description = "GitHub repository for WIF (e.g. Finders-Official/BE)"
  type        = string
  default     = "Finders-Official/BE"
}
