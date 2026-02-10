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

resource "google_service_account_iam_member" "team_sa_user" {
  for_each = toset(var.team_member_emails)

  service_account_id = "projects/${var.project_id}/serviceAccounts/${var.compute_sa_email}"
  role               = "roles/iam.serviceAccountUser"
  member             = "user:${each.value}"
}

# =============================================================================
# Compute SA — CI/CD + Runtime (최소 권한 원칙)
#
# - Artifact Registry: Docker push (CI/CD)
# - Secret Manager: 시크릿 목록 + 값 읽기 (CI/CD + startup script)
# - Cloud SQL: client only (접속만, admin 아님)
# - GCS: 버킷 레벨 objectAdmin (아래 별도 정의)
# - Logging: 쓰기만 (뷰어 불필요)
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

# =============================================================================
# Terraform CI/CD Service Account (GitHub Actions 전용)
#
# Compute SA와 분리하여 blast radius를 줄이고 audit 추적을 명확히 함.
# WIF를 통해 GitHub Actions에서 인증하며, Terraform plan/apply + CD 배포에 사용.
# =============================================================================

resource "google_service_account" "terraform_ci" {
  account_id   = "terraform-ci"
  display_name = "Terraform CI/CD"
  description  = "GitHub Actions에서 Terraform plan/apply 및 CD 배포에 사용하는 전용 서비스 계정"
  project      = var.project_id
}

locals {
  terraform_ci_member = "serviceAccount:${google_service_account.terraform_ci.email}"

  # Terraform plan/apply + CD 배포에 필요한 역할
  terraform_ci_roles = [
    "roles/compute.networkAdmin",            # VPC, Firewall 관리
    "roles/compute.instanceAdmin.v1",        # GCE 인스턴스 관리
    "roles/cloudsql.admin",                  # Cloud SQL 관리
    "roles/storage.admin",                   # GCS 버킷 관리
    "roles/iam.serviceAccountAdmin",         # SA 관리 + getIamPolicy
    "roles/resourcemanager.projectIamAdmin", # 프로젝트 IAM 바인딩 관리
    "roles/artifactregistry.admin",          # Docker 이미지 push + 메타데이터 관리 (CD)
    "roles/iam.serviceAccountUser",          # SA impersonation (CD)
    "roles/iap.tunnelResourceAccessor",      # SSH via IAP (CD)
    "roles/logging.logWriter",               # CI 로깅
    "roles/secretmanager.admin",             # 시크릿 생성/관리 (CI)
    "roles/secretmanager.viewer",            # 시크릿 목록 조회
    "roles/iam.workloadIdentityPoolAdmin",   # WIF pool/provider 관리
    "roles/monitoring.editor",               # 모니터링 대시보드 관리 (CI)
    "roles/run.admin",                       # Cloud Run 서비스 관리
  ]
}

resource "google_project_iam_member" "terraform_ci" {
  for_each = toset(local.terraform_ci_roles)

  project = var.project_id
  role    = each.value
  member  = local.terraform_ci_member
}

# Self token creator (GCS presigned URL 생성 등)
resource "google_service_account_iam_member" "terraform_ci_self_token_creator" {
  service_account_id = google_service_account.terraform_ci.name
  role               = "roles/iam.serviceAccountTokenCreator"
  member             = local.terraform_ci_member
}

# WIF binding (GitHub Actions → terraform-ci SA)
resource "google_service_account_iam_member" "terraform_ci_wif" {
  service_account_id = google_service_account.terraform_ci.name
  role               = "roles/iam.workloadIdentityUser"
  member             = "principalSet://iam.googleapis.com/projects/${data.google_project.current.number}/locations/global/workloadIdentityPools/finders-pool/attribute.repository/Finders-Official/BE"
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

# =============================================================================
# Image Resizer SA — Cloud Run 전용 (최소 권한)
#
# img-resizer는 public 버킷의 이미지만 리사이징하므로
# storage.objectAdmin(public 버킷) + logging.logWriter만 부여.
# =============================================================================

resource "google_service_account" "img_resizer" {
  account_id   = "img-resizer"
  display_name = "Image Resizer Service Account"
  description  = "Cloud Run img-resizer 전용 서비스 계정 (public 버킷 접근 + 로깅)"
  project      = var.project_id
}

resource "google_storage_bucket_iam_member" "public_img_resizer_admin" {
  bucket = module.storage.public_bucket_name
  role   = "roles/storage.objectAdmin"
  member = "serviceAccount:${google_service_account.img_resizer.email}"
}

resource "google_project_iam_member" "img_resizer_logging_writer" {
  project = var.project_id
  role    = "roles/logging.logWriter"
  member  = "serviceAccount:${google_service_account.img_resizer.email}"
}

resource "google_storage_bucket_iam_member" "public_all_users_viewer" {
  bucket = module.storage.public_bucket_name
  role   = "roles/storage.objectViewer"
  member = "allUsers"
}
