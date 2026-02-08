variable "project_id" {
  description = "GCP Project ID"
  type        = string
  sensitive   = true
}

variable "region" {
  description = "GCP Region"
  type        = string
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
  description = "VPC network ID for private IP"
  type        = string
}
