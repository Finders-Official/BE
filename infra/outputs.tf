output "gce_instance_name" {
  description = "GCE VM instance name (used by CD workflows)"
  value       = module.compute.instance_name
}

output "gce_zone" {
  description = "GCE VM zone (used by CD workflows)"
  value       = var.zone
}
