# SPEC-WP-HUB-02 — The Vault

> Technical specification for Curated Collections, Private Wardrobes, and Multi-Modal Semantic Search (WP-HUB-02 & WP-HUB-06).

---

## 1. Curated Vault Inventory

### 1.1 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/vault/items` | List vault items |
| GET | `/api/v1/vault/items/{id}` | Get item details |
| POST | `/api/v1/vault/items` | Create item |
| PATCH | `/api/v1/vault/items/{id}` | Update item |
| POST | `/api/v1/vault/items/{id}/vectorize` | Force re-calculation of embeddings |

### 1.2 Database Schema

```sql
-- schema: luxury_brand

CREATE TABLE curated_vault (
    id                    UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    piece_name            VARCHAR(255) NOT NULL,
    serial_number         VARCHAR(100) UNIQUE,
    collection_name       VARCHAR(100),
    material_composition  JSONB,
    aesthetic_description TEXT,
    aesthetic_embedding   vector(1536), -- Derived from text and image vision models
    current_location      VARCHAR(100),
    status                VARCHAR(50) DEFAULT 'Available', -- Available, Reserved, Sold, Vaulted
    custom_attributes     JSONB DEFAULT '{}',
    is_deleted            BOOLEAN DEFAULT FALSE NOT NULL,
    created_at            TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at            TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    created_by            UUID NOT NULL,
    updated_by            UUID NOT NULL,
    version               BIGINT DEFAULT 0 NOT NULL
);

CREATE INDEX idx_vault_serial ON curated_vault(serial_number);
-- HNSW Index for blazing fast pgvector semantic search
CREATE INDEX idx_vault_embedding ON curated_vault USING hnsw (aesthetic_embedding vector_l2_ops);
```

---

## 2. Multi-Modal Semantic Search

### 2.1 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/vault/search/text` | Search The Vault using a text string |
| POST | `/api/v1/vault/search/image` | Search The Vault using an uploaded image |
| POST | `/api/v1/vault/search/client/{profile_id}` | Recommend pieces based on client's `taste_vector` |

### 2.2 Execution Flows

**Text Search Flow:**
1. Text query -> LangChain4j EmbeddingModel -> `vector(1536)`.
2. Execute KNN query: `SELECT * FROM curated_vault ORDER BY aesthetic_embedding <-> :query_vector LIMIT :limit`.

**Client Match Flow:**
1. Fetch `taste_vector` from `hyper_profiles`.
2. Execute KNN query: `SELECT * FROM curated_vault ORDER BY aesthetic_embedding <-> :client_taste_vector LIMIT :limit`.

---

## 3. The Private Wardrobe (WP-HUB-06)

### 3.1 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/clienteling/profiles/{id}/wardrobe` | List pieces owned by the client |
| POST | `/api/v1/clienteling/profiles/{id}/wardrobe` | Add a piece to their Private Wardrobe |

### 3.2 Database Schema

```sql
CREATE TABLE private_wardrobes (
    id                      UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    profile_id              UUID NOT NULL REFERENCES hyper_profiles(id),
    brand_name              VARCHAR(100), -- E.g., "Patek Philippe" (competitor)
    piece_name              VARCHAR(255),
    estimated_market_value  NUMERIC(15,2),
    acquisition_date        DATE,
    custom_attributes       JSONB DEFAULT '{}',
    is_deleted              BOOLEAN DEFAULT FALSE NOT NULL,
    created_at              TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at              TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    created_by              UUID NOT NULL,
    updated_by              UUID NOT NULL,
    version                 BIGINT DEFAULT 0 NOT NULL
);

CREATE INDEX idx_wardrobe_profile ON private_wardrobes(profile_id);
```
