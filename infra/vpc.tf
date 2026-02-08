# =============================================================================
# VPC Network - finders-vpc
# =============================================================================

resource "google_compute_network" "main" {
  name                    = "finders-vpc"
  project                 = var.project_id
  auto_create_subnetworks = false
  routing_mode            = "REGIONAL"
  mtu                     = 1460

  lifecycle {
    prevent_destroy = true
  }
}

# =============================================================================
# Subnets
# =============================================================================

resource "google_compute_subnetwork" "public" {
  name                     = "public-subnet"
  project                  = var.project_id
  region                   = var.region
  network                  = google_compute_network.main.id
  ip_cidr_range            = "10.0.1.0/24"
  private_ip_google_access = true
  stack_type               = "IPV4_ONLY"
  purpose                  = "PRIVATE"

  lifecycle {
    prevent_destroy = true
  }
}

resource "google_compute_subnetwork" "private_app" {
  name                     = "private-app-subnet"
  description              = "Spring Boot 서버들이 들어가는 서브넷. 외부 통신 가능."
  project                  = var.project_id
  region                   = var.region
  network                  = google_compute_network.main.id
  ip_cidr_range            = "10.0.2.0/24"
  private_ip_google_access = true
  stack_type               = "IPV4_ONLY"
  purpose                  = "PRIVATE"

  lifecycle {
    prevent_destroy = true
  }
}

resource "google_compute_subnetwork" "private_db" {
  name                     = "private-db-subnet"
  description              = "DB가 들어가는 서브넷. 외부 통신 불가."
  project                  = var.project_id
  region                   = var.region
  network                  = google_compute_network.main.id
  ip_cidr_range            = "10.0.3.0/24"
  private_ip_google_access = true
  stack_type               = "IPV4_ONLY"
  purpose                  = "PRIVATE"

  lifecycle {
    prevent_destroy = true
  }
}

# =============================================================================
# Firewall Rules (6 rules on finders-vpc)
# =============================================================================

# Firewall: Allow API traffic (8080 Prod, 8081 Dev)
resource "google_compute_firewall" "allow_api_traffic" {
  name        = "allow-api-traffic"
  description = "도커로 띄운 8080(Prod)와 8081(Dev) 서버에 접속하기 위한 규칙"
  project     = var.project_id
  network     = google_compute_network.main.name
  direction   = "INGRESS"
  priority    = 1000

  allow {
    protocol = "tcp"
    ports    = ["8080", "8081"]
  }

  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["api-server"]
}

# Firewall: Allow DB access from app subnet only
resource "google_compute_firewall" "allow_db_from_app" {
  name        = "allow-db-from-app"
  description = "API 서버 구역에서만 DB에 접속할 수 있게 제한하는 핵심 규칙"
  project     = var.project_id
  network     = google_compute_network.main.name
  direction   = "INGRESS"
  priority    = 1000

  allow {
    protocol = "tcp"
    ports    = ["3306"]
  }

  source_ranges = ["10.0.2.0/24"]
}

# Firewall: Allow internal VPC traffic between subnets
resource "google_compute_firewall" "allow_internal_vpc" {
  name        = "allow-internal-vpc"
  description = "VPC 내의 서로 다른 서브넷끼리 자유롭게 데이터를 주고받는 규칙"
  project     = var.project_id
  network     = google_compute_network.main.name
  direction   = "INGRESS"
  priority    = 1000

  allow {
    protocol = "all"
  }

  source_ranges = ["10.0.0.0/16"]
}

# Firewall: Allow SSH from IAP (Identity-Aware Proxy)
resource "google_compute_firewall" "allow_ssh_from_iap" {
  name        = "allow-ssh-from-iap"
  description = "외부 IP 없이 SSH 접속하기 위한 필수 규칙"
  project     = var.project_id
  network     = google_compute_network.main.name
  direction   = "INGRESS"
  priority    = 1000

  allow {
    protocol = "tcp"
    ports    = ["22"]
  }

  source_ranges = ["35.235.240.0/20"]
  target_tags   = ["api-server"]
}

# Firewall: Allow HTTP (port 80)
resource "google_compute_firewall" "allow_http" {
  name      = "finders-vpc-allow-http"
  project   = var.project_id
  network   = google_compute_network.main.name
  direction = "INGRESS"
  priority  = 1000

  allow {
    protocol = "tcp"
    ports    = ["80"]
  }

  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["http-server"]
}

# Firewall: Allow HTTPS (port 443)
resource "google_compute_firewall" "allow_https" {
  name      = "finders-vpc-allow-https"
  project   = var.project_id
  network   = google_compute_network.main.name
  direction = "INGRESS"
  priority  = 1000

  allow {
    protocol = "tcp"
    ports    = ["443"]
  }

  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["https-server"]
}
