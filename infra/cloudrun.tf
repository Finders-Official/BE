resource "google_cloud_run_v2_service" "img_resizer" {
  project  = var.project_id
  location = var.region
  name     = "img-resizer"
  ingress  = "INGRESS_TRAFFIC_ALL"

  template {
    scaling {
      max_instance_count = 3
    }

    containers {
      image = "asia-northeast3-docker.pkg.dev/${var.project_id}/finders-image/img-resizer:3"
      name  = "hello-1"

      ports {
        container_port = 8080
        name           = "http1"
      }

      resources {
        limits = {
          cpu    = "1000m"
          memory = "512Mi"
        }
      }

      startup_probe {
        tcp_socket {
          port = 8080
        }
        failure_threshold = 1
        period_seconds    = 240
        timeout_seconds   = 240
      }
    }

    max_instance_request_concurrency = 80
    timeout                          = "300s"
    service_account                  = var.compute_sa_email

    annotations = {
      "run.googleapis.com/startup-cpu-boost" = "true"
    }
  }

  traffic {
    type    = "TRAFFIC_TARGET_ALLOCATION_TYPE_LATEST"
    percent = 100
  }

  lifecycle {
    prevent_destroy = true
    ignore_changes = [
      template[0].containers[0].image,
      client,
      client_version,
    ]
  }
}


