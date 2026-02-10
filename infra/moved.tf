# Networking
moved {
  from = google_compute_network.main
  to   = module.networking.google_compute_network.main
}

moved {
  from = google_compute_subnetwork.public
  to   = module.networking.google_compute_subnetwork.public
}

moved {
  from = google_compute_subnetwork.private_app
  to   = module.networking.google_compute_subnetwork.private_app
}

moved {
  from = google_compute_subnetwork.private_db
  to   = module.networking.google_compute_subnetwork.private_db
}

moved {
  from = google_compute_firewall.allow_api_traffic
  to   = module.networking.google_compute_firewall.allow_api_traffic
}

moved {
  from = google_compute_firewall.allow_db_from_app
  to   = module.networking.google_compute_firewall.allow_db_from_app
}

moved {
  from = google_compute_firewall.allow_internal_vpc
  to   = module.networking.google_compute_firewall.allow_internal_vpc
}

moved {
  from = google_compute_firewall.allow_ssh_from_iap
  to   = module.networking.google_compute_firewall.allow_ssh_from_iap
}

moved {
  from = google_compute_firewall.allow_http
  to   = module.networking.google_compute_firewall.allow_http
}

moved {
  from = google_compute_firewall.allow_https
  to   = module.networking.google_compute_firewall.allow_https
}

# Database
moved {
  from = google_sql_database_instance.main
  to   = module.database.google_sql_database_instance.main
}

moved {
  from = google_sql_database.prod
  to   = module.database.google_sql_database.prod
}

moved {
  from = google_sql_database.dev
  to   = module.database.google_sql_database.dev
}

moved {
  from = google_compute_global_address.private_ip_address
  to   = module.database.google_compute_global_address.private_ip_address
}

moved {
  from = google_service_networking_connection.private_vpc_connection
  to   = module.database.google_service_networking_connection.private_vpc_connection
}

# Compute
moved {
  from = google_compute_instance.app_server
  to   = module.compute.google_compute_instance.app_server
}

# Storage
moved {
  from = google_storage_bucket.public
  to   = module.storage.google_storage_bucket.public
}

moved {
  from = google_storage_bucket.private
  to   = module.storage.google_storage_bucket.private
}

# ============================================================
# IAM 3-tier restructure: admin → owner
# ============================================================

moved {
  from = google_project_iam_member.admin_editor
  to   = google_project_iam_member.owner_editor
}

moved {
  from = google_project_iam_member.admin_logging_viewer
  to   = google_project_iam_member.owner_logging_viewer
}

moved {
  from = google_project_iam_member.admin_monitoring_viewer
  to   = google_project_iam_member.owner_monitoring_viewer
}

moved {
  from = google_project_iam_member.admin_iam_security_reviewer
  to   = google_project_iam_member.owner_iam_security_reviewer
}

moved {
  from = google_service_account_iam_member.admin_sa_token_creator
  to   = google_service_account_iam_member.owner_sa_token_creator
}
