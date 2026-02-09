variable "project_id" {
  description = "GCP Project ID"
  type        = string
  sensitive   = true
}

variable "region" {
  description = "GCP Region"
  type        = string
}

variable "name_prefix" {
  description = "Prefix for resource names (e.g. 'finders')"
  type        = string
}

variable "cors_allowed_origins" {
  description = "Allowed CORS origins for GCS buckets"
  type        = list(string)
  default     = ["*"]
}
