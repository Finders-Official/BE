output "deploy_config" {
  description = "Deployment configuration for CD workflows (JSON format)"
  value = jsonencode({
    gce_name = module.compute.instance_name
    gce_zone = var.zone
  })
}
