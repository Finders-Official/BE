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
    # -------------------------------------------------------
    mkdir -p /projects/Finders/BE

    # Recreate env file on every boot to pick up secret changes
    : > "$ENV_FILE"

    gcloud secrets list \
      --project="${var.project_id}" \
      --filter="labels.env=prod" \
      --format="value(name)" \
    | while read -r SECRET_NAME; do
        # app-prod-key-name → KEY_NAME
        VAR_NAME=$(echo "$SECRET_NAME" \
          | sed 's/^app-prod-//' \
          | tr '[:lower:]' '[:upper:]' \
          | tr '-' '_')

        SECRET_VALUE=$(gcloud secrets versions access latest \
          --project="${var.project_id}" \
          --secret="$SECRET_NAME" 2>/dev/null) || continue

        echo "$VAR_NAME=$SECRET_VALUE" >> "$ENV_FILE"
      done

    chmod 600 "$ENV_FILE"

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

  lifecycle {
    prevent_destroy = true

    ignore_changes = [
      metadata["ssh-keys"],
      metadata_startup_script, # Temporary: ignore startup script changes during directory migration
    ]
  }
}
