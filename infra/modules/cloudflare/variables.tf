variable "cloudflare_account_id" {
  description = "Cloudflare Account ID"
  type        = string
  sensitive   = true
}

variable "name_prefix" {
  description = "Prefix for resource names (e.g. 'finders')"
  type        = string
}

variable "tunnel_hostname" {
  description = "Hostname for the Cloudflare tunnel"
  type        = string
}

variable "tunnel_service" {
  description = "Local service URL for the Cloudflare tunnel"
  type        = string
  default     = "http://localhost:8080"
}
