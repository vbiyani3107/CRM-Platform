# SPEC-WP-HUB-04 — Concierge Hub

> Technical specification for Experiential Requests, Lifestyle Management, and Proactive Gifting (WP-HUB-04).

---

## 1. Experiential Requests

### 1.1 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/concierge/requests` | List experiential requests |
| GET | `/api/v1/concierge/requests/{id}` | Get request details |
| POST | `/api/v1/concierge/requests` | Create experiential request |
| PATCH | `/api/v1/concierge/requests/{id}` | Update request |
| POST | `/api/v1/concierge/requests/{id}/fulfill` | Mark as fulfilled |

### 1.2 Discretion Enclaves
**Rule:** Requests marked with `discretion_level = 'Phantom'` must have their `concierge_notes` and `custom_attributes` encrypted at the application level before being saved to PostgreSQL. Only associates with `ROLE_PHANTOM_CLEARANCE` can decrypt and read them.

### 1.3 Database Schema

```sql
-- schema: luxury_brand

CREATE TABLE experiential_requests (
    id                  UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    number              VARCHAR(20) NOT NULL UNIQUE, -- EXP0000001
    profile_id          UUID NOT NULL REFERENCES hyper_profiles(id),
    request_type        VARCHAR(100), -- e.g., 'Yacht Charter', 'Private Viewing', 'Event Seating', 'Spa/Repair'
    discretion_level    VARCHAR(50) DEFAULT 'High', -- High, Maximum, Phantom
    state               VARCHAR(50) DEFAULT 'Open', -- Open, In_Progress, Fulfilled, Cancelled
    concierge_notes     TEXT,
    target_date         DATE,
    custom_attributes   JSONB DEFAULT '{}',
    is_deleted          BOOLEAN DEFAULT FALSE NOT NULL,
    created_at          TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at          TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    created_by          UUID NOT NULL,
    updated_by          UUID NOT NULL,
    version             BIGINT DEFAULT 0 NOT NULL
);

CREATE INDEX idx_experiential_profile ON experiential_requests(profile_id);
```

---

## 2. Proactive Gifting Alerts

### 2.1 Concept
This is a headless Spring Batch job that runs daily. It scans `hyper_profiles` for upcoming `significant_dates` (e.g., Anniversary in 21 days). When a date is found, it queries The Vault using the client's `taste_vector` and generates a dashboard alert for the assigned Sales Associate.

### 2.2 Execution Flow
1. **Spring Batch** queries: `SELECT id FROM hyper_profiles WHERE ... significant date is in 21 days`.
2. For each profile, fetch `taste_vector`.
3. Call **Semantic Search Service** (`curated_vault` repo): `SELECT * FROM curated_vault ORDER BY aesthetic_embedding <-> :taste_vector LIMIT 3`.
4. Generate a `concierge_alert` record.

### 2.3 Database Schema

```sql
CREATE TABLE concierge_alerts (
    id                  UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    profile_id          UUID NOT NULL REFERENCES hyper_profiles(id),
    associate_id        UUID NOT NULL,
    alert_type          VARCHAR(50), -- Upcoming_Anniversary, Taste_Match_Arrival
    message             TEXT,
    suggested_items     JSONB, -- Array of Vault Item UUIDs
    is_read             BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE INDEX idx_alerts_associate ON concierge_alerts(associate_id) WHERE is_read = FALSE;
```
