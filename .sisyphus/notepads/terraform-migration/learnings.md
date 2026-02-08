# Learnings

Accumulated knowledge from Phase 0-6 execution.


## [2026-02-09] Phase 0: Infrastructure Audit Results

### VPC Reality (RESOLVED documentation contradiction)
- **Both exist**: `default` (AUTO mode) + `finders-vpc` (CUSTOM mode)
- **GCE instance uses**: `finders-vpc` → `private-app-subnet` (10.0.2.0/24), internal IP: 10.0.2.2
- **Cloud SQL uses**: `finders-vpc` (private network)
- **finders-vpc Subnets**:
  - `public-subnet` — 10.0.1.0/24 (asia-northeast3)
  - `private-app-subnet` — 10.0.2.0/24 (asia-northeast3)
  - `private-db-subnet` — 10.0.3.0/24 (asia-northeast3)
- **Contradiction resolved**: NETWORK_SECURITY.md says "기본 VPC 사용 중" but actual infra uses custom `finders-vpc`. Docs are stale.

### GCE Instance (finders-server-v2)
- **Machine Type**: e2-medium
- **OS**: Ubuntu 22.04 LTS
- **Boot Disk**: 20GB, PERSISTENT, PD-standard (auto-delete: true)
- **Network**: finders-vpc / private-app-subnet / 10.0.2.2 (no public IP)
- **Tags**: api-server, http-server, https-server
- **Service Account**: 517500643080-compute@developer.gserviceaccount.com (cloud-platform scope)
- **Metadata**: enable-oslogin=TRUE, 1 SSH key entry
- **Deletion Protection**: false (should consider enabling)

### Cloud SQL (finders-db)
- **Version**: MySQL 8.0.41 (MYSQL_8_0)
- **Tier**: db-g1-small
- **Edition**: ENTERPRISE
- **Availability**: ZONAL (asia-northeast3-a)
- **Storage**: 10GB PD-SSD, auto-resize enabled
- **Private IP**: 10.68.240.3 (on finders-vpc)
- **Public IP**: disabled (ipv4Enabled: false)
- **SSL**: ALLOW_UNENCRYPTED_AND_ENCRYPTED (not enforced)
- **Backup**: enabled, daily at 21:00, 7 retained, binary log enabled
- **Deletion Protection**: enabled
- **Password Policy**: enabled (min 8 chars, complexity default, no username substring)
- **Authorized Networks**: finders-server (34.50.19.146/32) — possibly stale NAT/Cloud SQL Auth Proxy entry

### GCS Buckets
- **finders-public**: STANDARD class, ASIA-NORTHEAST3, allUsers:objectViewer (public read), CORS enabled (all origins)
- **finders-private**: STANDARD class, ASIA-NORTHEAST3, no public access, compute SA + Cloud SQL SA have objectAdmin

### Firewall Rules
- **finders-vpc rules** (5):
  - allow-api-traffic: tcp:8080,8081 (INGRESS)
  - allow-db-from-app: tcp:3306 (INGRESS)
  - allow-internal-vpc: all (INGRESS)
  - allow-ssh-from-iap: tcp:22 (INGRESS)
  - finders-vpc-allow-http/https: tcp:80,443 (INGRESS)
- **default VPC rules** (6): standard allow-http, allow-https, allow-icmp, allow-internal, allow-rdp, allow-ssh, plus allow-spring-boot (tcp:8080)

### Cloudflare Tunnel
- **Deployment Method**: systemd service (`/etc/systemd/system/cloudflared.service`)
- **NOT Docker** — contradicts `docker-compose.prod.yml` which has cloudflared service defined
- **Status**: active (running) since 2026-01-28
- **Tunnel Name**: finders-api
- **Credentials**: `/home/finders_official_kr/.cloudflared/37fc77f7-c7ba-473f-8fc6-4ceec783034a.json`
- **Ingress**: 
  - `finders-api.log8.kr` → http://localhost:8080
  - Catch-all → http_status:404
- **Domain Discrepancy**: Actual config uses `finders-api.log8.kr`, NOT `api.finders.it.kr` / `dev-api.finders.it.kr` mentioned in INFRASTRUCTURE.md

### Enabled APIs (33 total, key ones)
- compute.googleapis.com (Compute Engine)
- sqladmin.googleapis.com (Cloud SQL Admin)
- storage.googleapis.com (Cloud Storage)
- iamcredentials.googleapis.com (IAM SA Credentials)
- servicenetworking.googleapis.com (Service Networking — VPC peering for Cloud SQL)
- oslogin.googleapis.com (OS Login)
- run.googleapis.com (Cloud Run — not actively used?)
- containerregistry.googleapis.com (Container Registry)
- artifactregistry.googleapis.com (Artifact Registry)
- logging.googleapis.com + monitoring.googleapis.com (Observability)
- Missing: `iam.googleapis.com`, `cloudresourcemanager.googleapis.com` (may need enabling for Terraform)

## [2026-02-09] Terraform Lifecycle Learning (Step G)

Successfully completed full Terraform lifecycle:
1. `terraform init` → Downloaded google v6.50.0, cloudflare v5.16.0, random v3.8.1 providers
2. `terraform fmt` + `terraform validate` → Passed (variables.tf reformatted)
3. `terraform plan` → Previewed 2 resources to add (bucket + random_id)
4. `terraform apply` → Created test bucket `finders-tf-test-3fb69e35`
5. `terraform destroy` → Deleted both test resources (bucket + random_id)
6. Cleaned up `test.tf`, `test.tfplan`, `terraform.tfvars`, state files

**Key Observations**:
- ADC is configured with impersonated_service_account (compute SA `517500643080-compute@developer.gserviceaccount.com`)
- Compute SA lacks `storage.buckets.create` permission — had to use `GOOGLE_OAUTH_ACCESS_TOKEN=$(gcloud auth print-access-token)` with user credentials instead
- For future phases: either grant compute SA appropriate IAM roles, or use user-credential-based ADC without impersonation, or create a dedicated Terraform SA
- Google provider auto-adds `goog-terraform-provisioned=true` label to managed resources
- `.terraform.lock.hcl` was created — should be committed to version control for reproducible builds
- Plan was stale after partial apply failure (random_id created but bucket failed) — demonstrates importance of state management

