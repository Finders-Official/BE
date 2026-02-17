resource "google_sql_database_instance" "main" {
  name             = "${var.name_prefix}-db"
  project          = var.project_id
  region           = var.region
  database_version = "MYSQL_8_0"

  settings {
    tier                        = "db-g1-small"
    edition                     = "ENTERPRISE"
    availability_type           = "ZONAL"
    disk_type                   = "PD_SSD"
    disk_size                   = 10
    disk_autoresize             = true
    activation_policy           = "ALWAYS"
    pricing_plan                = "PER_USE"
    deletion_protection_enabled = true
    retain_backups_on_delete    = true

    ip_configuration {
      ipv4_enabled    = false
      private_network = var.network_id

      ssl_mode                                      = "ALLOW_UNENCRYPTED_AND_ENCRYPTED"
      enable_private_path_for_google_cloud_services = false
    }

    backup_configuration {
      enabled                        = true
      start_time                     = "21:00"
      binary_log_enabled             = true
      location                       = "asia"
      transaction_log_retention_days = 7

      backup_retention_settings {
        retained_backups = 7
        retention_unit   = "COUNT"
      }
    }

    maintenance_window {
      day          = 7
      hour         = 0
      update_track = "canary"
    }

    password_validation_policy {
      enable_password_policy      = true
      min_length                  = 8
      complexity                  = "COMPLEXITY_DEFAULT"
      disallow_username_substring = true
    }

    location_preference {
      zone = var.zone
    }

    insights_config {
      query_insights_enabled  = false
      query_plans_per_minute  = 0
      record_application_tags = false
      record_client_address   = false
    }
  }

  deletion_protection = true

  lifecycle {
    prevent_destroy = true
  }
}

resource "google_sql_database" "prod" {
  name      = "${var.name_prefix}_prod"
  instance  = google_sql_database_instance.main.name
  project   = var.project_id
  charset   = "utf8mb4"
  collation = "utf8mb4_unicode_ci"
}

resource "google_sql_database" "dev" {
  name      = "${var.name_prefix}_dev"
  instance  = google_sql_database_instance.main.name
  project   = var.project_id
  charset   = "utf8mb4"
  collation = "utf8mb4_0900_ai_ci"
}

# Reserved IP range for Cloud SQL private networking
resource "google_compute_global_address" "private_ip_address" {
  name          = "${var.name_prefix}-vpc-ip-range-1769092915737"
  project       = var.project_id
  purpose       = "VPC_PEERING"
  address_type  = "INTERNAL"
  address       = "10.68.240.0"
  prefix_length = 20
  network       = var.network_id
}

# Service networking connection (VPC peering with Google services)
resource "google_service_networking_connection" "private_vpc_connection" {
  network                 = var.network_id
  service                 = "servicenetworking.googleapis.com"
  reserved_peering_ranges = [google_compute_global_address.private_ip_address.name]
}
