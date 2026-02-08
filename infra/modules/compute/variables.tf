variable "project_id" {
  description = "GCP Project ID"
  type        = string
  sensitive   = true
}

variable "zone" {
  description = "GCP Zone"
  type        = string
}

variable "name_prefix" {
  description = "Prefix for resource names (e.g. 'finders')"
  type        = string
}

variable "network_id" {
  description = "VPC network ID"
  type        = string
}

variable "subnet_id" {
  description = "Subnet ID for the instance"
  type        = string
}

variable "service_account_email" {
  description = "Service account email for the instance"
  type        = string
}
