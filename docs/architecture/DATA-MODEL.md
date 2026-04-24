# Bespoke Luxury Platform ERD & Data Model

This document specifies the PostgreSQL schemas and pgvector setup for the platform.

## PostgreSQL & pgvector Setup
- Ensure `pgvector` extension is enabled.
- All embedding columns should be configured as `vector(1536)` assuming OpenAI text-embedding-3-small or equivalent.

## Core Entities
- `sys_db_object` (Dynamic tables definition)
- `sys_dictionary` (Dynamic columns definition)

## Luxury Hub Entities
- `hyper_profiles`
- `client_engagements`
- `curated_vault`
- `bespoke_commissions`
- `experiential_requests`
- `provenance_ledger`
- `private_wardrobes`
