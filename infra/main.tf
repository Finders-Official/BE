locals {
  name_prefix = "finders"
}

module "networking" {
  source = "./modules/networking"

  project_id  = var.project_id
  region      = var.region
  name_prefix = local.name_prefix
}

module "database" {
  source = "./modules/database"

  project_id  = var.project_id
  region      = var.region
  zone        = var.zone
  name_prefix = local.name_prefix
  network_id  = module.networking.network_id
}

module "compute" {
  source = "./modules/compute"

  project_id            = var.project_id
  zone                  = var.zone
  name_prefix           = local.name_prefix
  network_id            = module.networking.network_id
  subnet_id             = module.networking.private_app_subnet_id
  service_account_email = var.compute_sa_email
}

module "storage" {
  source = "./modules/storage"

  project_id           = var.project_id
  region               = var.region
  name_prefix          = local.name_prefix
  cors_allowed_origins = var.cors_allowed_origins
}

module "cloudflare" {
  source = "./modules/cloudflare"
  count  = var.enable_cloudflare ? 1 : 0

  cloudflare_account_id = var.cloudflare_account_id
  name_prefix           = local.name_prefix
  tunnel_hostname       = var.cloudflare_tunnel_hostname
  tunnel_service        = var.cloudflare_tunnel_service
}
