locals {
  compute_sa_member = "serviceAccount:${var.compute_sa_email}"
}

# =============================================================================
# Admin IAM bindings (팀장급 2명 — 프로젝트 전반 관리 권한)
#
# roles/editor를 의도적으로 사용합니다.
# - 대상: 팀장 2명 (sachi009955, wldy4627)
# - 사유: Owner 계정(finders.official.kr) 대신 프로젝트 운영을 위임받은 관리자
# - roles/editor는 logging.viewer, monitoring.viewer, serviceAccountTokenCreator를
#   포함하지 않으므로 아래에서 별도 부여합니다.
# - 검토일: 2026-02-09
# =============================================================================

resource "google_project_iam_member" "admin_editor" {
  for_each = toset(var.admin_member_emails)

  project = var.project_id
  role    = "roles/editor"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "admin_logging_viewer" {
  for_each = toset(var.admin_member_emails)

  project = var.project_id
  role    = "roles/logging.viewer"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "admin_monitoring_viewer" {
  for_each = toset(var.admin_member_emails)

  project = var.project_id
  role    = "roles/monitoring.viewer"
  member  = "user:${each.value}"
}

resource "google_service_account_iam_member" "admin_sa_token_creator" {
  for_each = toset(var.admin_member_emails)

  service_account_id = "projects/${var.project_id}/serviceAccounts/${var.compute_sa_email}"
  role               = "roles/iam.serviceAccountTokenCreator"
  member             = "user:${each.value}"
}

# roles/iam.securityReviewer — IAM 정책 읽기 전용 (terraform plan 실행에 필요)
resource "google_project_iam_member" "admin_iam_security_reviewer" {
  for_each = toset(var.admin_member_emails)

  project = var.project_id
  role    = "roles/iam.securityReviewer"
  member  = "user:${each.value}"
}

# =============================================================================
# Team member IAM bindings (logging + monitoring + IAP + compute + SA impersonation)
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

resource "google_service_account_iam_member" "team_sa_token_creator" {
  for_each = toset(var.team_member_emails)

  service_account_id = "projects/${var.project_id}/serviceAccounts/${var.compute_sa_email}"
  role               = "roles/iam.serviceAccountTokenCreator"
  member             = "user:${each.value}"
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

# =============================================================================
# Service account self-impersonation (GCE VM Presigned URL generation)
# =============================================================================

resource "google_service_account_iam_member" "sa_self_token_creator" {
  service_account_id = "projects/${var.project_id}/serviceAccounts/${var.compute_sa_email}"
  role               = "roles/iam.serviceAccountTokenCreator"
  member             = local.compute_sa_member
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
