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
