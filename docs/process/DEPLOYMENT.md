# Deployment & Infrastructure

## Docker Compose
The local setup is driven by `docker-compose.yml`.
- PostgreSQL 16 + pgvector
- Spring Boot Backend Engine
- React + Vite Frontend Shell

## Production Strategy (Phantom Enclaves)
- Highly sensitive data must reside in logically separated or encrypted regions.
- Deployments must utilize secure secret management (e.g., HashiCorp Vault or Azure Key Vault).
