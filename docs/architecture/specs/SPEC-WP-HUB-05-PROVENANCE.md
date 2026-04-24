# SPEC-WP-HUB-05 — Provenance & Digital Twins

> Technical specification for Immutable Provenance and Digital Product Passports (WP-HUB-05).

---

## 1. Provenance Ledger

### 1.1 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/provenance/{vault_item_id}` | Get complete provenance history for an item |
| POST | `/api/v1/provenance/register` | Register a new DPP (Digital Product Passport) |
| POST | `/api/v1/provenance/transfer` | Record a change of ownership on the blockchain |

### 1.2 Integration Rules
- The Provenance Ledger acts as an internal caching layer for external Web3/Blockchain calls (e.g., Aura Blockchain Consortium).
- Writes to this API must dispatch an async event to the external blockchain integration service.

### 1.3 Database Schema

```sql
-- schema: luxury_brand

CREATE TABLE provenance_ledger (
    id                      UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    vault_item_id           UUID NOT NULL REFERENCES curated_vault(id),
    blockchain_hash         VARCHAR(255), -- Blockchain transaction ID
    digital_product_passport JSONB, -- DPP metadata, NFC/RFID tag info, materials origin
    artisan_genesis_record  TEXT, -- Details about who crafted it
    acquisition_date        DATE,
    current_owner_id        UUID REFERENCES hyper_profiles(id),
    custom_attributes       JSONB DEFAULT '{}',
    is_deleted              BOOLEAN DEFAULT FALSE NOT NULL,
    created_at              TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at              TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    created_by              UUID NOT NULL,
    updated_by              UUID NOT NULL,
    version                 BIGINT DEFAULT 0 NOT NULL
);

CREATE INDEX idx_provenance_vault_item ON provenance_ledger(vault_item_id);
```
