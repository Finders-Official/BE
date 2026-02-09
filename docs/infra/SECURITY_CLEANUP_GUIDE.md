# Security Cleanup Guide

> This document provides a comprehensive guide for addressing security audit findings and performing a repository-wide cleanup.

## Executive Summary
A recent security audit of the Finders API repository identified several critical issues, primarily involving sensitive data committed to the codebase. While the core git history remains clean, immediate action is required to rotate compromised credentials and update `.gitignore` patterns. A full cleanup of historical branches containing personal data is scheduled for 2 weeks after Demo Day.

**CRITICAL: This repository is PUBLIC. All committed secrets must be considered compromised.**

## Severity Assessment

| Finding | Severity | Description |
|---------|----------|-------------|
| **Committed `.env` file** | `CRITICAL` | Real secrets (JWT, Kakao, Replicate, Sendon) exposed in the codebase. |
| **Hardcoded DB Passwords** | `HIGH` | `finders123` found in `docker-compose.yml` and `application.yml`. |
| **Missing `.gitignore` patterns** | `MEDIUM` | Potential for accidental commits of `credentials.json`, `*.p12`, etc. |
| **Personal Data in History** | `LOW` | `.sisyphus/` directories in older branches contain personal emails. |
| **GCP Project Info** | `LOW` | Project number and service account emails exposed in docs. |

## Timeline

### IMMEDIATE (Do Now)
- [ ] Update `.gitignore` with missing patterns.
- [ ] Remove `.env` from the current branch (if present).
- [ ] Replace hardcoded passwords with environment variables.
- [ ] **Rotate all exposed credentials** (See Checklist).

### 2-WEEK CLEANUP (After Demo Day)
- [ ] Perform `git filter-repo` to remove `.sisyphus/` from historical branches.
- [ ] Verify repository cleanliness.
- [ ] Team-wide re-clone of the repository.

---

## Phase 1: Immediate Actions

### 1. Update `.gitignore`
Add the following patterns to `.gitignore` to prevent future leaks:
```gitignore
# Security: Missing patterns
credentials.json
service-account.json
*.p12
*.jks
application-local.properties
```

### 2. Remove Sensitive Files from Current Branch
If `.env` or other sensitive files are currently tracked:
```bash
git rm --cached .env
git commit -m "fix: remove sensitive files from tracking"
```

### 3. Externalize Hardcoded Passwords
Update `docker-compose.yml` and `application.yml` to use environment variables:
```yaml
# application.yml example
spring:
  datasource:
    password: ${DB_PASSWORD:finders123}
```

---

## Phase 2: 2-Week Cleanup (After Demo Day)

### 1. Preparation (D+1)
1. Notify all team members to push their final changes.
2. Create a backup of the current repository.
3. Ensure `git-filter-repo` is installed (`pip install git-filter-repo`).

### 2. Cleanup (D+2)
Run the following commands to remove the `.sisyphus/` directory from all branches and tags:
```bash
# WARNING: This will rewrite history. Coordinate with the team.
git filter-repo --path .sisyphus/ --invert-paths --force
```

### 3. Team Coordination (D+2)
After the cleanup is pushed to the remote:
1. **Delete local clones**: All team members must delete their local repository.
2. **Re-clone**: Perform a fresh clone.
    ```bash
    git clone https://github.com/Finders-Official/BE.git
    ```
3. **Restore `.env`**: Re-create the `.env` file using the new rotated secrets.

---

## Credential Rotation Checklist

The following credentials **MUST** be rotated immediately as they are exposed in the public repository:

- [ ] **JWT_SECRET**: Generate a new 64-byte Base64 string.
- [ ] **KAKAO_CLIENT_ID**: Rotate via Kakao Developers Console.
- [ ] **KAKAO_CLIENT_SECRET**: Rotate via Kakao Developers Console.
- [ ] **REPLICATE_API_KEY**: Rotate via Replicate Dashboard.
- [ ] **REPLICATE_WEBHOOK_SECRET**: Rotate via Replicate Dashboard.
- [ ] **SENDON_API_KEY**: Rotate via Sendon Dashboard.
- [ ] **Database Password**: Change `finders123` to a secure, unique password in production.

---

## Team Instructions

### How to handle the cleanup
1. **Do not pull** after the history rewrite. It will cause massive merge conflicts.
2. **Backup your work**: If you have uncommitted changes, save them as a patch or copy the files elsewhere.
3. **Follow the re-clone instructions** provided in Phase 2.

### Prevention
- Use `direnv` or similar tools to manage local environment variables.
- Never commit `.env` files.
- Use `application-secret.yml` (already ignored) for local secrets if needed.
- Review every commit with `git diff --cached` before pushing.

---

## Appendix: Audit Results Summary

### Audit 1: Sensitive Data in Codebase
- **Status**: FAILED (Critical)
- **Findings**: `.env` file committed with real secrets. Hardcoded DB passwords.

### Audit 2: .gitignore Coverage
- **Status**: PARTIAL
- **Missing**: `credentials.json`, `service-account.json`, `*.p12`, `*.jks`, `application-local.properties`.

### Audit 3: Git History
- **Status**: PASSED (Clean)
- **Note**: Main and develop branches do not contain sensitive files in their history. Historical feature branches (phase0-phase5) contain `.sisyphus/` metadata.

---
**Last Updated**: 2026-02-09
**Status**: Pending Cleanup
