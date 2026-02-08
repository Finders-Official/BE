# Backend configuration for GCS
# State stored in finders-terraform-state bucket (created manually in Phase 1)
# DO NOT manage this bucket with Terraform — circular dependency

terraform {
  backend "gcs" {
    bucket = "finders-terraform-state"
    prefix = "terraform/state"
  }
}
