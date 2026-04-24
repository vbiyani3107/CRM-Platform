-- ═══════════════════════════════════════════════════════════════════════════════
-- V2__security_seed.sql
-- Bespoke Luxury Platform — Security & Discretion Seed Data
--
-- Seeds sys_dictionary with sensitive field definitions used by the
-- FLS Interceptor to strip payload fields based on associate clearance.
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── Seed: Register sensitive field definitions for FLS ──────────────────────
-- These entries tell the FlsInterceptor which fields to strip when
-- the requesting associate lacks ROLE_VIP_DIRECTOR clearance.

INSERT INTO sys_dictionary (entity_name, attribute_name, data_type, is_sensitive, is_required, description)
VALUES
    ('hyper_profiles', 'influence_score',   'decimal',  TRUE, FALSE, 'Client influence score — restricted to VIP Directors only.'),
    ('hyper_profiles', 'net_worth_band',    'string',   TRUE, FALSE, 'Estimated net worth band — restricted to VIP Directors only.'),
    ('hyper_profiles', 'wealth_indicators', 'jsonb',    TRUE, FALSE, 'Composite wealth indicator data — restricted to VIP Directors only.'),
    ('hyper_profiles', 'concierge_notes',   'string',   TRUE, FALSE, 'Encrypted concierge notes — Phantom Enclave protected.'),
    ('hyper_profiles', 'family_network',    'jsonb',    TRUE, FALSE, 'Familial and corporate relationship graph — restricted.'),
    ('curated_vault',  'acquisition_price', 'decimal',  TRUE, FALSE, 'Internal acquisition cost — restricted to VIP Directors only.'),
    ('curated_vault',  'provenance_notes',  'string',   TRUE, FALSE, 'Detailed provenance chain — restricted.'),
    ('experiential_requests', 'budget_ceiling', 'decimal', TRUE, FALSE, 'Client budget ceiling for experiences — restricted.')
ON CONFLICT (entity_name, attribute_name) DO NOTHING;

-- ─── Seed: Register security domain entities in sys_db_object ────────────────
INSERT INTO sys_db_object (name, label, description)
VALUES
    ('hyper_profiles',         'Hyper Profiles',         'Core clienteling entity — ultra-high-net-worth individual profiles with taste vectors.'),
    ('curated_vault',          'Curated Vault',          'Luxury product and asset catalog with aesthetic embeddings.'),
    ('experiential_requests',  'Experiential Requests',  'Concierge and lifestyle management requests with budget and discretion tiers.')
ON CONFLICT (name) DO NOTHING;
