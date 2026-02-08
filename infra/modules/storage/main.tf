resource "google_storage_bucket" "public" {
  name                        = "${var.name_prefix}-public"
  project                     = var.project_id
  location                    = upper(var.region)
  storage_class               = "STANDARD"
  public_access_prevention    = "inherited"
  uniform_bucket_level_access = true
  enable_object_retention     = false

  hierarchical_namespace {
    enabled = true
  }

  cors {
    origin          = var.cors_allowed_origins
    method          = ["GET", "POST", "PUT", "DELETE", "HEAD"]
    response_header = ["Content-Type", "Authorization", "Content-Length", "Accept"]
    max_age_seconds = 3600
  }

  lifecycle_rule {
    action {
      type = "Delete"
    }
    condition {
      age            = 30
      matches_prefix = ["temp/"]
    }
  }

  soft_delete_policy {
    retention_duration_seconds = 604800
  }

  lifecycle {
    prevent_destroy = true
  }
}

resource "google_storage_bucket" "private" {
  name                        = "${var.name_prefix}-private"
  project                     = var.project_id
  location                    = upper(var.region)
  storage_class               = "STANDARD"
  public_access_prevention    = "enforced"
  uniform_bucket_level_access = true
  enable_object_retention     = false

  hierarchical_namespace {
    enabled = true
  }

  cors {
    origin          = var.cors_allowed_origins
    method          = ["GET", "POST", "PUT", "DELETE", "HEAD"]
    response_header = ["Content-Type", "Authorization", "Content-Length", "Accept"]
    max_age_seconds = 3600
  }

  lifecycle_rule {
    action {
      type = "Delete"
    }
    condition {
      age            = 30
      matches_prefix = ["temp/"]
    }
  }

  soft_delete_policy {
    retention_duration_seconds = 604800
  }

  lifecycle {
    prevent_destroy = true
  }
}
