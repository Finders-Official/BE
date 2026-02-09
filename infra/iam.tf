locals {
  compute_sa_member = "serviceAccount:${var.compute_sa_email}"
}

# =============================================================================
# Owner IAM bindings (프로젝트 소유자 — 전체 관리 권한)
#
# roles/editor를 의도적으로 사용합니다.
# - 대상: 프로젝트 소유자
# - 사유: 프로젝트 전반 관리 및 보안 리뷰 권한 필요
# - roles/editor는 logging.viewer, monitoring.viewer, serviceAccountTokenCreator를
#   포함하지 않으므로 아래에서 별도 부여합니다.
# =============================================================================

resource "google_project_iam_member" "owner_editor" {
  for_each = toset(var.owner_member_emails)

  project = var.project_id
  role    = "roles/editor"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "owner_logging_viewer" {
  for_each = toset(var.owner_member_emails)

  project = var.project_id
  role    = "roles/logging.viewer"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "owner_monitoring_viewer" {
  for_each = toset(var.owner_member_emails)

  project = var.project_id
  role    = "roles/monitoring.viewer"
  member  = "user:${each.value}"
}

resource "google_service_account_iam_member" "owner_sa_token_creator" {
  for_each = toset(var.owner_member_emails)

  service_account_id = "projects/${var.project_id}/serviceAccounts/${var.compute_sa_email}"
  role               = "roles/iam.serviceAccountTokenCreator"
  member             = "user:${each.value}"
}

resource "google_project_iam_member" "owner_iam_security_reviewer" {
  for_each = toset(var.owner_member_emails)

  project = var.project_id
  role    = "roles/iam.securityReviewer"
  member  = "user:${each.value}"
}

# =============================================================================
# Editor IAM bindings (에디터 — 운영 권한)
#
# roles/editor + IAP SSH 접근 + 모니터링 권한
# =============================================================================

resource "google_project_iam_member" "editor_editor" {
  for_each = toset(var.editor_member_emails)

  project = var.project_id
  role    = "roles/editor"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "editor_logging_viewer" {
  for_each = toset(var.editor_member_emails)

  project = var.project_id
  role    = "roles/logging.viewer"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "editor_monitoring_viewer" {
  for_each = toset(var.editor_member_emails)

  project = var.project_id
  role    = "roles/monitoring.viewer"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "editor_iap_tunnel" {
  for_each = toset(var.editor_member_emails)

  project = var.project_id
  role    = "roles/iap.tunnelResourceAccessor"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "editor_compute_viewer" {
  for_each = toset(var.editor_member_emails)

  project = var.project_id
  role    = "roles/compute.viewer"
  member  = "user:${each.value}"
}

resource "google_project_iam_member" "editor_compute_os_login" {
  for_each = toset(var.editor_member_emails)

  project = var.project_id
  role    = "roles/compute.osLogin"
  member  = "user:${each.value}"
}

resource "google_service_account_iam_member" "editor_sa_token_creator" {
  for_each = toset(var.editor_member_emails)

  service_account_id = "projects/${var.project_id}/serviceAccounts/${var.compute_sa_email}"
  role               = "roles/iam.serviceAccountTokenCreator"
  member             = "user:${each.value}"
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
# CI/CD service account permissions
#
# GitHub Actions deploy workflow uses Workload Identity Federation (WIF) to
# authenticate as `var.compute_sa_email`.
# - Artifact Registry push requires `roles/artifactregistry.writer`
# - Secret Manager env refresh uses `gcloud secrets list`, which requires
#   `secretmanager.secrets.list` (covered by `roles/secretmanager.viewer`)
# =============================================================================

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
