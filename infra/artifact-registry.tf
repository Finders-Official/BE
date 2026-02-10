resource "google_artifact_registry_repository" "api_docker" {
  project       = var.project_id
  location      = var.region
  repository_id = "finders-docker"
  format        = "DOCKER"
  description   = "Finders API Docker images"
  mode          = "STANDARD_REPOSITORY"

  lifecycle {
    prevent_destroy = true
  }
}

resource "google_artifact_registry_repository" "image_docker" {
  project       = var.project_id
  location      = var.region
  repository_id = "finders-image"
  format        = "DOCKER"
  mode          = "STANDARD_REPOSITORY"

  lifecycle {
    prevent_destroy = true
  }
}
