# =============================================================================
# Cloudflare Tunnel — finders-api
# =============================================================================
# Tunnel runs as systemd service on GCE instance (NOT Docker).
# Auth: token-based (--token flag in systemd unit), config managed by Cloudflare.
# Domain: finders-api.log8.kr → http://localhost:8080

# -----------------------------------------------------------------------------
# Import blocks (remove after successful terraform apply)
# -----------------------------------------------------------------------------

import {
  to = cloudflare_zero_trust_tunnel_cloudflared.main
  id = "${var.cloudflare_account_id}/1459c65d-7ff6-476e-9fa7-d3f47125803f"
}

import {
  to = cloudflare_zero_trust_tunnel_cloudflared_config.main
  id = "${var.cloudflare_account_id}/1459c65d-7ff6-476e-9fa7-d3f47125803f"
}

# -----------------------------------------------------------------------------
# Tunnel
# -----------------------------------------------------------------------------

resource "cloudflare_zero_trust_tunnel_cloudflared" "main" {
  account_id = var.cloudflare_account_id
  name       = "finders-api"
  config_src = "cloudflare"
}

# -----------------------------------------------------------------------------
# Tunnel Configuration
# -----------------------------------------------------------------------------

resource "cloudflare_zero_trust_tunnel_cloudflared_config" "main" {
  account_id = var.cloudflare_account_id
  tunnel_id  = cloudflare_zero_trust_tunnel_cloudflared.main.id

  config = {
    ingress = [
      {
        hostname = "finders-api.log8.kr"
        service  = "http://localhost:8080"
      },
      {
        service = "http_status:404"
      }
    ]
  }
}
