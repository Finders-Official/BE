resource "google_compute_instance" "app_server" {
  name                       = "${var.name_prefix}-server"
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
    device_name  = "${var.name_prefix}-server"
    force_attach = false
    mode         = "READ_WRITE"

    initialize_params {
      image = "ubuntu-os-cloud/ubuntu-2204-lts"
      size  = 20
      type  = "pd-balanced"
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

  metadata_startup_script = <<-EOF
    #!/bin/bash
    set -e

    MARKER="/var/run/startup-script-completed"
    ENV_FILE="/projects/Finders/BE/.env.prod"
    SERVICE_FILE="/etc/systemd/system/finders-api.service"

    # -------------------------------------------------------
    # 1. Install dependencies (idempotent)
    # -------------------------------------------------------
    if ! command -v jq &>/dev/null; then
      apt-get update -y
      apt-get install -y jq
    fi

    # gcloud is pre-installed on GCE images; verify availability
    if ! command -v gcloud &>/dev/null; then
      apt-get update -y
      apt-get install -y google-cloud-sdk
    fi

    # -------------------------------------------------------
    # 2. Fetch secrets from Secret Manager → .env.prod
    #    Single JSON secret instead of N individual secrets
    # -------------------------------------------------------
    mkdir -p /projects/Finders/BE

    gcloud secrets versions access latest \
      --project="${var.project_id}" \
      --secret="finders-prod-config" \
    | jq -r 'to_entries[] | "\(.key | ascii_upcase)=\(.value)"' \
    > "$ENV_FILE"

    chmod 600 "$ENV_FILE"

    # -------------------------------------------------------
    # 2b. Fetch secrets from Secret Manager → .env.dev
    # -------------------------------------------------------
    ENV_FILE_DEV="/projects/Finders/BE/.env.dev"

    gcloud secrets versions access latest \
      --project="${var.project_id}" \
      --secret="finders-dev-config" \
    | jq -r 'to_entries[] | "\(.key | ascii_upcase)=\(.value)"' \
    > "$ENV_FILE_DEV"

    chmod 600 "$ENV_FILE_DEV"

    # -------------------------------------------------------
    # 3. Create systemd service (idempotent)
    # -------------------------------------------------------
    cat > "$SERVICE_FILE" <<'SERVICE_EOF'
    [Unit]
    Description=Finders API Docker Compose
    Requires=docker.service
    After=docker.service

    [Service]
    Type=oneshot
    RemainAfterExit=yes
    WorkingDirectory=/projects/Finders/BE
    ExecStart=/usr/bin/docker compose --env-file .env.prod -f docker-compose.infra.yml -f docker-compose.prod.yml --profile blue up -d
    ExecStop=/usr/bin/docker compose down
    Restart=on-failure

    [Install]
    WantedBy=multi-user.target
    SERVICE_EOF

    # -------------------------------------------------------
    # 4. Enable and (re)start service
    # -------------------------------------------------------
    systemctl daemon-reload
    systemctl enable finders-api.service
    systemctl restart finders-api.service

    touch "$MARKER"
  EOF

  shielded_instance_config {
    enable_integrity_monitoring = true
    enable_secure_boot          = false
    enable_vtpm                 = true
  }

  # NOTE: ignore_changes prevents instance forced replacement
  # - metadata_startup_script: Updated for Secret Manager integration, apply manually if needed
  # - boot_disk: Image/disk attribute drift from original manual creation
  # - metadata["ssh-keys"]: Managed outside Terraform
  lifecycle {
    prevent_destroy = true
    ignore_changes = [
      metadata["ssh-keys"],
      metadata_startup_script,
      boot_disk,
    ]
  }
}
