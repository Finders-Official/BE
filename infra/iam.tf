locals {
  compute_sa_member = "serviceAccount:${var.compute_sa_email}"
}

# =============================================================================
# Admin tier (near-owner level)
#
# roles/editor + IAM 정책 관리 + 보안 리뷰 + SSH 접근
# roles/editor는 logging.viewer, monitoring.viewer, compute.viewer,
# secretmanager 접근 등을 이미 포함하므로 별도 부여 불필요.
# =============================================================================

resource "google_project_iam_member" "admin_editor" {
  for_each = toset(var.admin_member_emails)

  project = var.project_id
  role    = "roles/editor"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "admin_project_iam_admin" {
  for_each = toset(var.admin_member_emails)

  project = var.project_id
  role    = "roles/resourcemanager.projectIamAdmin"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "admin_security_reviewer" {
  for_each = toset(var.admin_member_emails)

  project = var.project_id
  role    = "roles/iam.securityReviewer"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "admin_iap_tunnel" {
  for_each = toset(var.admin_member_emails)

  project = var.project_id
  role    = "roles/iap.tunnelResourceAccessor"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "admin_compute_os_login" {
  for_each = toset(var.admin_member_emails)

  project = var.project_id
  role    = "roles/compute.osLogin"
  member  = "user:${each.value}"
}

resource "google_service_account_iam_member" "admin_sa_token_creator" {
  for_each = toset(var.admin_member_emails)

  service_account_id = "projects/${var.project_id}/serviceAccounts/${var.compute_sa_email}"
  role               = "roles/iam.serviceAccountTokenCreator"
  member             = "user:${each.value}"
}

# =============================================================================
# Lead tier (team lead level)
#
# roles/editor + IAM 정책 관리 + SSH 접근
# =============================================================================

resource "google_project_iam_member" "lead_editor" {
  for_each = toset(var.lead_member_emails)

  project = var.project_id
  role    = "roles/editor"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "lead_project_iam_admin" {
  for_each = toset(var.lead_member_emails)

  project = var.project_id
  role    = "roles/resourcemanager.projectIamAdmin"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "lead_iap_tunnel" {
  for_each = toset(var.lead_member_emails)

  project = var.project_id
  role    = "roles/iap.tunnelResourceAccessor"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "lead_compute_os_login" {
  for_each = toset(var.lead_member_emails)

  project = var.project_id
  role    = "roles/compute.osLogin"
  member  = "user:${each.value}"
}

resource "google_service_account_iam_member" "lead_sa_token_creator" {
  for_each = toset(var.lead_member_emails)

  service_account_id = "projects/${var.project_id}/serviceAccounts/${var.compute_sa_email}"
  role               = "roles/iam.serviceAccountTokenCreator"
  member             = "user:${each.value}"
}

# =============================================================================
# Team tier (read-only + SSH access)
#
# 로그/모니터링 뷰어 + IAP SSH 접근만 부여.
# serviceAccountTokenCreator는 의도적으로 미부여 (권한 상승 방지).
# =============================================================================

resource "google_project_iam_member" "team_logging_viewer" {
  for_each = toset(var.team_member_emails)

  project = var.project_id
  role    = "roles/logging.viewer"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "team_monitoring_viewer" {
  for_each = toset(var.team_member_emails)

  project = var.project_id
  role    = "roles/monitoring.viewer"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "team_iap_tunnel" {
  for_each = toset(var.team_member_emails)

  project = var.project_id
  role    = "roles/iap.tunnelResourceAccessor"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "team_compute_viewer" {
  for_each = toset(var.team_member_emails)

  project = var.project_id
  role    = "roles/compute.viewer"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "team_compute_os_login" {
  for_each = toset(var.team_member_emails)

  project = var.project_id
  role    = "roles/compute.osLogin"
  member  = "user:${each.value}"
}

# =============================================================================
# Compute SA — CI/CD + Runtime (최소 권한 원칙)
#
# - Artifact Registry: Docker push (CI/CD)
# - Secret Manager: 시크릿 목록 + 값 읽기 (CI/CD + startup script)
# - Cloud SQL: client only (접속만, admin 아님)
# - GCS: 버킷 레벨 objectAdmin (아래 별도 정의)
# - Logging: 쓰기만 (뷰어 불필요)
# - IAP + OS Login: CI/CD SSH 접근
# - SA Token Creator (self): GCS presigned URL 생성
# =============================================================================

resource "google_service_account_iam_member" "sa_self_token_creator" {
  service_account_id = "projects/${var.project_id}/serviceAccounts/${var.compute_sa_email}"
  role               = "roles/iam.serviceAccountTokenCreator"
  member             = local.compute_sa_member
}

resource "google_project_iam_member" "compute_sa_artifactregistry_writer" {
  project = var.project_id
  role    = "roles/artifactregistry.writer"
  member  = local.compute_sa_member
}

resource "google_project_iam_member" "compute_sa_secretmanager_viewer" {
  project = var.project_id
  role    = "roles/secretmanager.viewer"
  member  = local.compute_sa_member
}

resource "google_project_iam_member" "compute_sa_secretmanager_secret_accessor" {
  project = var.project_id
  role    = "roles/secretmanager.secretAccessor"
  member  = local.compute_sa_member
}

resource "google_project_iam_member" "compute_sa_cloudsql_client" {
  project = var.project_id
  role    = "roles/cloudsql.client"
  member  = local.compute_sa_member
}

resource "google_project_iam_member" "compute_sa_logging_writer" {
  project = var.project_id
  role    = "roles/logging.logWriter"
  member  = local.compute_sa_member
}

resource "google_project_iam_member" "compute_sa_service_account_user" {
  project = var.project_id
  role    = "roles/iam.serviceAccountUser"
  member  = local.compute_sa_member
}

resource "google_project_iam_member" "compute_sa_iap_tunnel" {
  project = var.project_id
  role    = "roles/iap.tunnelResourceAccessor"
  member  = local.compute_sa_member
}

resource "google_project_iam_member" "compute_sa_compute_os_login" {
  project = var.project_id
  role    = "roles/compute.osLogin"
  member  = local.compute_sa_member
}

# =============================================================================
# GCS bucket IAM bindings
# =============================================================================

resource "google_storage_bucket_iam_member" "public_compute_admin" {
  bucket = module.storage.public_bucket_name
  role   = "roles/storage.objectAdmin"
  member = local.compute_sa_member
}

resource "google_storage_bucket_iam_member" "private_compute_admin" {
  bucket = module.storage.private_bucket_name
  role   = "roles/storage.objectAdmin"
  member = local.compute_sa_member
}

resource "google_storage_bucket_iam_member" "public_all_users_viewer" {
  bucket = module.storage.public_bucket_name
  role   = "roles/storage.objectViewer"
  member = "allUsers"
}

# =============================================================================
# img-resizer Service Account (Cloud Run → private GCS bucket 접근용)
# =============================================================================

resource "google_service_account" "img_resizer" {
  account_id   = "img-resizer-sa"
  display_name = "img-resizer-sa"
  description  = "Cloud Run 이미지 리사이저가 private 버킷에 접근하기 위한 서비스 계정"
  project      = var.project_id
}

resource "google_project_iam_member" "img_resizer_storage_viewer" {
  project = var.project_id
  role    = "roles/storage.objectViewer"
  member  = "serviceAccount:${google_service_account.img_resizer.email}"
}
