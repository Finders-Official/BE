# Decisions

Architectural and technical decisions made during migration.


## [2026-02-09] VPC Strategy Decision

**Audit Result**: Custom `finders-vpc` EXISTS and is actively used by both GCE and Cloud SQL.

**Decision**: Phase 2 will IMPORT `finders-vpc` and its 3 subnets into Terraform state.

**Details**:
- `finders-vpc` (CUSTOM mode) with 3 subnets in asia-northeast3
- All production resources (GCE, Cloud SQL) are already on `finders-vpc`
- `default` VPC also exists but is not used by any production resources
- No VPC migration needed — no downtime risk

**Implications for Phase 2-4**:
- Phase 2: Import `finders-vpc`, 3 subnets, 5 firewall rules
- Phase 3: Import Cloud SQL (already on finders-vpc private network)
- Phase 4: Import GCE (already on private-app-subnet)
- No network migration required → significantly reduces risk

## [2026-02-09] Cloudflare Tunnel Deployment Strategy

**Audit Result**: Cloudflare Tunnel runs as systemd service, NOT Docker container.

**Decision**: Phase 5 will manage tunnel configuration via Terraform `cloudflare_tunnel` + `cloudflare_tunnel_config` resources.

**Details**:
- Tunnel runs via systemd at `/etc/systemd/system/cloudflared.service`
- `docker-compose.prod.yml` has cloudflared defined but is NOT how it's actually deployed
- Actual domain: `finders-api.log8.kr` (docs mention `api.finders.it.kr` — stale)
- Credentials file on server: `/home/finders_official_kr/.cloudflared/37fc77f7-c7ba-473f-8fc6-4ceec783034a.json`

**Implications**: 
- Terraform can manage tunnel config (routes, DNS) but systemd service management stays on GCE
- Need Cloudflare API token with tunnel permissions for Phase 5

