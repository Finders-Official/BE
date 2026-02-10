resource "google_secret_manager_secret" "prod_config" {
  project   = var.project_id
  secret_id = "finders-prod-config"

  labels = {
    env        = "prod"
    managed-by = "terraform"
  }

  replication {
    auto {}
  }
}

resource "google_secret_manager_secret" "dev_config" {
  project   = var.project_id
  secret_id = "finders-dev-config"

  labels = {
    env        = "dev"
    managed-by = "terraform"
  }

  replication {
    auto {}
  }
}
