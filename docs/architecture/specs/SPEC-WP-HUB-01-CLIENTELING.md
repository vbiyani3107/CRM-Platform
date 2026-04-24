# SPEC-WP-HUB-01 — Clienteling Hub

> Technical specification for Hyper-Profiles, Influence Graphing, and Ambient Engagement Memory (WP-HUB-01).

---

## 1. Hyper-Profiles Management

### 1.1 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/clienteling/profiles` | List hyper-profiles (paginated, filterable by tier/influence) |
| GET | `/api/v1/clienteling/profiles/{id}` | Get hyper-profile by ID |
| POST | `/api/v1/clienteling/profiles` | Create hyper-profile |
| PATCH | `/api/v1/clienteling/profiles/{id}` | Update hyper-profile |
| GET | `/api/v1/clienteling/profiles/{id}/engagements` | List client engagements |
| POST | `/api/v1/clienteling/profiles/{id}/engagements` | Log new client engagement (triggers ambient parsing) |
| GET | `/api/v1/clienteling/profiles/{id}/network` | Get Tastemaker Influence Graph (edges) |

### 1.2 Discretion & Field-Level Security (FLS)

**Rule:** `influence_score` and `communication_protocols` are highly sensitive.
- If the associate's JWT lacks the `ROLE_VIP_DIRECTOR` or `ROLE_PHANTOM_CLEARANCE`, the service layer MUST strip these keys from the response JSON payload before serialization.

### 1.3 Database Schema

```sql
-- schema: luxury_brand

CREATE TABLE hyper_profiles (
    id                  UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    full_name           VARCHAR(255) NOT NULL,
    vip_tier            VARCHAR(50) DEFAULT 'Standard',
    lifestyle_persona   TEXT,     -- AI-maintained summary of tastes and habits
    influence_score     NUMERIC(5,2), -- Calculated metric of the client's tastemaker status
    communication_protocols JSONB, -- Strict rules (e.g., "WhatsApp only, no calls")
    significant_dates   JSONB,    -- Key milestones, anniversaries, family birthdays
    tastemaker_network  JSONB,   -- Graph edges to other profiles they influence
    taste_vector        vector(1536),  -- Continuously shifting math representation of aesthetic preference
    custom_attributes   JSONB DEFAULT '{}', -- Dynamically extended by the Foundation service
    is_deleted          BOOLEAN DEFAULT FALSE NOT NULL,
    created_at          TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at          TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    created_by          UUID NOT NULL,
    updated_by          UUID NOT NULL,
    version             BIGINT DEFAULT 0 NOT NULL
);

CREATE INDEX idx_profiles_tier ON hyper_profiles(vip_tier) WHERE is_deleted = FALSE;
CREATE INDEX idx_profiles_created ON hyper_profiles(created_at DESC);
```

---

## 2. Client Engagements (Ambient Memory)

### 2.1 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/clienteling/engagements/{id}` | Get engagement |
| PATCH | `/api/v1/clienteling/engagements/{id}` | Update engagement |

### 2.2 Database Schema

```sql
CREATE TABLE client_engagements (
    id                  UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    profile_id          UUID NOT NULL REFERENCES hyper_profiles(id),
    channel             VARCHAR(50), -- Showroom, WhatsApp, Private Dinner
    interaction_notes   TEXT,
    sentiment           VARCHAR(50), -- AI-derived (e.g., Delighted, Frustrated)
    associate_id        UUID NOT NULL,
    custom_attributes   JSONB DEFAULT '{}',
    is_deleted          BOOLEAN DEFAULT FALSE NOT NULL,
    created_at          TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at          TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    created_by          UUID NOT NULL,
    updated_by          UUID NOT NULL,
    version             BIGINT DEFAULT 0 NOT NULL
);

CREATE INDEX idx_engagements_profile ON client_engagements(profile_id);
```

---

## 3. Ambient Intelligence (LangChain4j) Workflow

```
Event: EngagementCreatedEvent
Trigger: POST /api/v1/clienteling/profiles/{id}/engagements

1. [Controller] Returns HTTP 201 Created immediately (async handoff).
2. [Service - Async] ApplicationEventPublisher fires EngagementCreatedEvent.
3. [LangChain4j Listener] Reads `interaction_notes`.
4. [LangChain4j LLM Call] Prompt: "Extract sizing, sentiment, and aesthetic preferences."
5. [LangChain4j Embedding] Vectorizes the aesthetic preference string into `vector(1536)`.
6. [Repository] Updates `hyper_profiles.taste_vector`, `hyper_profiles.lifestyle_persona`, and `client_engagements.sentiment`.
```
