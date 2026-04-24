-- ═══════════════════════════════════════════════════════════════════════════════
-- V9__create_experiential_requests.sql
-- WP-HUB-04 — Concierge & Experiential Management
--
-- Creates:
--   1. experiential_requests  — Client experiential request tracking
--   2. concierge_alerts       — Proactive gifting / anniversary alerts
--   3. Registers both tables in sys_db_object
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── Experiential Requests ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS experiential_requests (
    id                  UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    number              VARCHAR(20)   NOT NULL UNIQUE,  -- EXP0000001
    profile_id          UUID          NOT NULL,
    request_type        VARCHAR(100),                   -- e.g., 'Yacht Charter', 'Private Viewing'
    discretion_level    VARCHAR(50)   DEFAULT 'High',   -- High, Maximum, Phantom
    state               VARCHAR(50)   DEFAULT 'Open',   -- Open, In_Progress, Fulfilled, Cancelled
    concierge_notes     TEXT,
    target_date         DATE,
    custom_attributes   JSONB         DEFAULT '{}',
    is_deleted          BOOLEAN       DEFAULT FALSE NOT NULL,
    created_at          TIMESTAMPTZ   DEFAULT NOW() NOT NULL,
    updated_at          TIMESTAMPTZ   DEFAULT NOW() NOT NULL,
    created_by          UUID          NOT NULL,
    updated_by          UUID          NOT NULL,
    version             BIGINT        DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_experiential_profile
    ON experiential_requests(profile_id);

CREATE INDEX IF NOT EXISTS idx_experiential_state
    ON experiential_requests(state) WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_experiential_number
    ON experiential_requests(number);

-- ─── Concierge Alerts ───────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS concierge_alerts (
    id                  UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    profile_id          UUID          NOT NULL,
    associate_id        UUID          NOT NULL,
    alert_type          VARCHAR(50),                    -- Upcoming_Anniversary, Taste_Match_Arrival
    message             TEXT,
    suggested_items     JSONB,                          -- Array of Vault Item UUIDs
    is_read             BOOLEAN       DEFAULT FALSE,
    created_at          TIMESTAMPTZ   DEFAULT NOW() NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_alerts_associate
    ON concierge_alerts(associate_id) WHERE is_read = FALSE;

CREATE INDEX IF NOT EXISTS idx_alerts_profile
    ON concierge_alerts(profile_id);

-- ─── Register in Entity Registry ────────────────────────────────────────────
INSERT INTO sys_db_object (name, label, description)
VALUES
    ('experiential_requests', 'Experiential Requests',  'Client experiential request tracking for concierge operations.'),
    ('concierge_alerts',      'Concierge Alerts',       'Proactive gifting and significant date alerts for associates.')
ON CONFLICT (name) DO NOTHING;
