# SPEC-WP-HUB-03 — Atelier Hub

> Technical specification for Bespoke Commissions, Artisan Tracking, and Milestone Lifecycle (WP-HUB-03).

---

## 1. Bespoke Commissions

### 1.1 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/atelier/commissions` | List all bespoke commissions |
| GET | `/api/v1/atelier/commissions/{id}` | Get commission details |
| POST | `/api/v1/atelier/commissions` | Create a new commission request |
| PATCH | `/api/v1/atelier/commissions/{id}` | Update commission details |
| POST | `/api/v1/atelier/commissions/{id}/state` | Advance commission state (triggers Camunda 8) |
| GET | `/api/v1/atelier/commissions/{id}/artisan-notes` | Get internal artisan collaboration notes |
| POST | `/api/v1/atelier/commissions/{id}/artisan-notes` | Add an artisan note |

### 1.2 State Machine (Bespoke Lifecycle)

The lifecycle of a commission is heavily orchestrated via Camunda 8 BPMN workflows.

| State | Description | API Transition Action |
|---|---|---|
| `Consultation` | Gathering initial requirements | - |
| `Sketching` | Awaiting client approval of design | `POST /state { "action": "submit_sketch" }` |
| `Material_Sourcing`| Securing rare gems/exotics | `POST /state { "action": "approve_sketch" }` |
| `Artisan_Crafting` | Physical construction | `POST /state { "action": "materials_secured" }` |
| `Final_QA` | Internal brand quality check | `POST /state { "action": "crafting_complete" }` |
| `Delivery` | Ready for white-glove handover | `POST /state { "action": "qa_passed" }` |
| `Completed` | Post-delivery | `POST /state { "action": "delivered" }` |
| `Cancelled` | Terminated | `POST /state { "action": "cancel" }` |

### 1.3 Database Schema

```sql
-- schema: luxury_brand

CREATE TABLE bespoke_commissions (
    id                  UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    number              VARCHAR(20) NOT NULL UNIQUE, -- BSK0000001
    profile_id          UUID NOT NULL REFERENCES hyper_profiles(id),
    state               VARCHAR(50) NOT NULL DEFAULT 'Consultation',
    title               VARCHAR(255) NOT NULL,
    design_brief        TEXT,
    estimated_price     NUMERIC(15,2),
    target_delivery_date DATE,
    artisan_id          UUID, -- Assigned Master Artisan
    camunda_process_id  VARCHAR(255), -- Link to Camunda 8 workflow instance
    custom_attributes   JSONB DEFAULT '{}',
    is_deleted          BOOLEAN DEFAULT FALSE NOT NULL,
    created_at          TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at          TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    created_by          UUID NOT NULL,
    updated_by          UUID NOT NULL,
    version             BIGINT DEFAULT 0 NOT NULL
);

CREATE INDEX idx_commissions_profile ON bespoke_commissions(profile_id);
CREATE INDEX idx_commissions_state ON bespoke_commissions(state) WHERE is_deleted = FALSE;

CREATE TABLE artisan_notes (
    id                  UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    commission_id       UUID NOT NULL REFERENCES bespoke_commissions(id),
    note_type           VARCHAR(50) DEFAULT 'Internal', -- Internal, Client_Visible
    body                TEXT NOT NULL,
    author_id           UUID NOT NULL,
    created_at          TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE INDEX idx_artisan_notes_commission ON artisan_notes(commission_id);
```
