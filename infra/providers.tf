provider "google" {
  project = var.project_id
  region  = var.region
}

# Cloudflare provider configuration
# API token required (set via TF_VAR_cloudflare_api_token or terraform.tfvars)
provider "cloudflare" {
  api_token = var.cloudflare_api_token
}
