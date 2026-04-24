# Threat Model & Security Architecture

## The Phantom Enclave Principle
- Data marked as `is_sensitive=true` in `sys_dictionary` must NEVER leave the backend engine in plain text.
- Field-level security must strip sensitive fields from API payloads before serialization.

## Identity & Access
- Entra ID (OIDC) is the sole source of truth for identity.
- Strict Role-Based Access Control (RBAC) enforces "Showroom" vs "Backstage" access.

## Data at Rest & In Transit
- All databases must use disk encryption (e.g., AWS KMS or Azure CMK).
- TLS 1.3 mandated for all API endpoints.
