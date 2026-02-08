resource "google_compute_instance" "app_server" {
  name                       = "${var.name_prefix}-server-v2"
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
  }

  boot_disk {
    auto_delete  = true
    device_name  = "${var.name_prefix}-server-v2"
    force_attach = false
    mode         = "READ_WRITE"

    initialize_params {
      size = 20
      type = "pd-balanced"
    }
  }

  network_interface {
    network    = var.network_id
    subnetwork = var.subnet_id
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
    email  = var.service_account_email
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
