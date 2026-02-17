# Backend configuration for GCS
# State stored in finders-487717-tf-state bucket (created manually before terraform init)
# DO NOT manage this bucket with Terraform — circular dependency

terraform {
  backend "gcs" {
    bucket = "finders-487717-tf-state"
    prefix = "terraform/state"
  }
}
