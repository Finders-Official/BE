output "network_id" {
  description = "VPC network ID"
  value       = google_compute_network.main.id
}

output "network_name" {
  description = "VPC network name"
  value       = google_compute_network.main.name
}

output "public_subnet_id" {
  description = "Public subnet ID"
  value       = google_compute_subnetwork.public.id
}

output "private_app_subnet_id" {
  description = "Private app subnet ID"
  value       = google_compute_subnetwork.private_app.id
}

output "private_db_subnet_id" {
  description = "Private DB subnet ID"
  value       = google_compute_subnetwork.private_db.id
}
