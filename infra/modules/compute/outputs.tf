output "instance_name" {
  description = "GCE instance name"
  value       = google_compute_instance.app_server.name
}

output "instance_id" {
  description = "GCE instance ID"
  value       = google_compute_instance.app_server.instance_id
}

output "internal_ip" {
  description = "Internal IP address"
  value       = google_compute_instance.app_server.network_interface[0].network_ip
}
