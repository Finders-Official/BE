# Backend configuration for GCS
# State bucket is created manually before terraform init — NOT managed by Terraform (circular dependency)
# Bucket name is injected at init time via -backend-config flag:
#   terraform init -backend-config="bucket=<PROJECT_ID>-tf-state"
# CI/CD uses TF_STATE_BUCKET GitHub Secret for the bucket name.

terraform {
  backend "gcs" {
    prefix = "terraform/state"
  }
}
