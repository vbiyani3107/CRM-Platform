-- ═══════════════════════════════════════════════════════════════════════════════
-- V1__init_foundation.sql
-- Bespoke Luxury Platform — Foundation Schema
--
-- Creates:
--   1. pgvector extension
--   2. sys_db_object  — Entity Registry
--   3. sys_dictionary  — Attribute Registry
-- ═══════════════════════════════════════════════════════════════════════════════

-- Enable vector support for aesthetic embeddings (1536-dim OpenAI)
CREATE EXTENSION IF NOT EXISTS vector;

-- ─── Entity Registry ─────────────────────────────────────────────────────────
-- Defines core modules (hyper_profiles, curated_vault) and dynamically
-- created bespoke tables.
CREATE TABLE IF NOT EXISTS sys_db_object (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100)  NOT NULL UNIQUE,
    label           VARCHAR(255),
    description     TEXT,
    is_extensible   BOOLEAN       DEFAULT TRUE,
    custom_attributes JSONB       DEFAULT '{}',

    -- Audit fields (BaseEntity)
    created_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    version         BIGINT        DEFAULT 0,
    is_deleted      BOOLEAN       DEFAULT FALSE
);

-- GIN index for fast JSONB queries on custom_attributes
CREATE INDEX IF NOT EXISTS idx_sys_db_object_custom_attrs
    ON sys_db_object USING GIN (custom_attributes);

-- ─── Attribute Registry ──────────────────────────────────────────────────────
-- Defines column types per entity. Enforces validation before writes.
-- data_type ENUM: 'string', 'boolean', 'jsonb', 'vector', 'reference',
--                 'integer', 'decimal', 'date', 'timestamp'
CREATE TABLE IF NOT EXISTS sys_dictionary (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_name     VARCHAR(100)  NOT NULL,
    attribute_name  VARCHAR(100)  NOT NULL,
    data_type       VARCHAR(20)   NOT NULL,
    is_custom       BOOLEAN       DEFAULT FALSE,
    is_sensitive    BOOLEAN       DEFAULT FALSE,
    is_required     BOOLEAN       DEFAULT FALSE,
    default_value   VARCHAR(500),
    max_length      INTEGER,
    description     TEXT,
    validation_rules JSONB        DEFAULT '{}',

    -- Audit fields (BaseEntity)
    created_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    version         BIGINT        DEFAULT 0,
    is_deleted      BOOLEAN       DEFAULT FALSE,

    -- Unique constraint: one definition per entity + attribute pair
    CONSTRAINT uq_dictionary_entity_attr UNIQUE (entity_name, attribute_name)
);

-- GIN index for JSONB containment queries on validation_rules
CREATE INDEX IF NOT EXISTS idx_sys_dictionary_validation_rules
    ON sys_dictionary USING GIN (validation_rules);

-- Composite index for fast entity-level lookups
CREATE INDEX IF NOT EXISTS idx_sys_dictionary_entity_name
    ON sys_dictionary (entity_name);

-- ─── Seed: Register the foundation tables themselves ─────────────────────────
INSERT INTO sys_db_object (name, label, description)
VALUES
    ('sys_db_object',  'Entity Registry',    'Metadata table registering all luxury domain entities.'),
    ('sys_dictionary', 'Attribute Registry',  'Metadata table defining attributes and validation rules per entity.')
ON CONFLICT (name) DO NOTHING;
