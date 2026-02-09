output "instance_name" {
  description = "Cloud SQL instance name"
  value       = google_sql_database_instance.main.name
}

output "instance_connection_name" {
  description = "Cloud SQL instance connection name"
  value       = google_sql_database_instance.main.connection_name
}

output "private_ip_address" {
  description = "Cloud SQL private IP address"
  value       = google_sql_database_instance.main.private_ip_address
}
