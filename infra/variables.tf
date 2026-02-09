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

variable "owner_member_emails" {
  description = "프로젝트 소유자 이메일 (roles/editor + 보안 리뷰 + 모니터링)"
  type        = list(string)
  default     = []
}

variable "editor_member_emails" {
  description = "에디터 이메일 (roles/editor + IAP SSH 접근 + 모니터링)"
  type        = list(string)
  default     = []
}

variable "team_member_emails" {
  description = "팀 멤버 이메일 (로그/모니터링 뷰어 + IAP SSH 접근)"
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
