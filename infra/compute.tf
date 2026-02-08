# =============================================================================
# GCE Instance - finders-server-v2 (e2-medium, Ubuntu 22.04)
# CRITICAL: Many attributes are ForceNew — changes trigger instance recreation!
# =============================================================================

resource "google_compute_instance" "app_server" {
  name                       = "finders-server-v2"
  project                    = var.project_id
  zone                       = var.zone
  machine_type               = "e2-medium"
  can_ip_forward             = false
  deletion_protection        = false
  enable_display             = false
  key_revocation_action_type = "NONE"
  labels                     = {}
  tags                       = ["api-server", "http-server", "https-server"]

  metadata = {
    enable-oslogin = "TRUE"
    ssh-keys       = "finders_official_kr:ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBE9hTILabEVfIHV75/2hhcdeFzJh1psht8Zn7hZcKELfXlOb5iLv+iF4XuGroYVG3l/fu8/uYbq80SzwaXbN0zc= google-ssh {\"userName\":\"finders.official.kr@gmail.com\",\"expireOn\":\"2026-01-24T16:40:25+0000\"}"
  }

  boot_disk {
    auto_delete  = true
    device_name  = "finders-server-v2"
    force_attach = false
    mode         = "READ_WRITE"

    initialize_params {
      size = 20
      type = "pd-balanced"
    }
  }

  network_interface {
    network    = google_compute_network.main.id
    subnetwork = google_compute_subnetwork.private_app.id
    network_ip = "10.0.2.2"
    stack_type = "IPV4_ONLY"
  }

  scheduling {
    automatic_restart   = true
    on_host_maintenance = "MIGRATE"
    preemptible         = false
    provisioning_model  = "STANDARD"
  }

  service_account {
    email  = "517500643080-compute@developer.gserviceaccount.com"
    scopes = ["https://www.googleapis.com/auth/cloud-platform"]
  }

  shielded_instance_config {
    enable_integrity_monitoring = true
    enable_secure_boot          = false
    enable_vtpm                 = true
  }

  lifecycle {
    prevent_destroy = true

    ignore_changes = [
      metadata["ssh-keys"],
    ]
  }
}
