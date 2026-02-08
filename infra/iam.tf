locals {
  compute_sa_member = "serviceAccount:${var.compute_sa_email}"
}

# =============================================================================
# Admin IAM bindings (roles/editor + logging + monitoring)
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
# Photo team additional access
# =============================================================================

resource "google_project_iam_member" "photo_team_storage_viewer" {
  for_each = toset(var.photo_team_member_emails)

  project = var.project_id
  role    = "roles/storage.objectViewer"
  member  = "user:${each.value}"
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
  bucket = google_storage_bucket.public.name
  role   = "roles/storage.objectAdmin"
  member = local.compute_sa_member
}

resource "google_storage_bucket_iam_member" "private_compute_admin" {
  bucket = google_storage_bucket.private.name
  role   = "roles/storage.objectAdmin"
  member = local.compute_sa_member
}

resource "google_storage_bucket_iam_member" "public_all_users_viewer" {
  bucket = google_storage_bucket.public.name
  role   = "roles/storage.objectViewer"
  member = "allUsers"
}
