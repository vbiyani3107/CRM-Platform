-- ═══════════════════════════════════════════════════════════════════════════════
-- V3__dynamic_api_seed.sql
-- Bespoke Luxury Platform — Dynamic API Field Definitions
--
-- Seeds sys_dictionary with non-sensitive field definitions for core entities.
-- These entries drive the DynamicPayloadValidator — the API layer validates
-- incoming JSON payloads against these definitions at runtime.
--
-- Note: Sensitive fields (influence_score, net_worth_band, etc.) are already
-- seeded by V2__security_seed.sql. This migration only adds standard fields.
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── hyper_profiles: Standard Fields ─────────────────────────────────────────
INSERT INTO sys_dictionary (entity_name, attribute_name, data_type, is_required, max_length, description)
VALUES
    ('hyper_profiles', 'first_name',      'string',    TRUE,  100, 'Client first name.'),
    ('hyper_profiles', 'last_name',       'string',    TRUE,  100, 'Client last name.'),
    ('hyper_profiles', 'email',           'string',    FALSE, 255, 'Client email address.'),
    ('hyper_profiles', 'phone',           'string',    FALSE, 50,  'Client phone number.'),
    ('hyper_profiles', 'preferred_title', 'string',    FALSE, 20,  'Preferred form of address (Mr., Ms., Dr., etc.).'),
    ('hyper_profiles', 'date_of_birth',   'date',      FALSE, NULL, 'Client date of birth for milestone gifting.'),
    ('hyper_profiles', 'taste_vectors',   'jsonb',     FALSE, NULL, 'Aesthetic taste vectors for AI matching.'),
    ('hyper_profiles', 'notes',           'string',    FALSE, 2000, 'General notes about the client.')
ON CONFLICT (entity_name, attribute_name) DO NOTHING;

-- ─── curated_vault: Standard Fields ──────────────────────────────────────────
INSERT INTO sys_dictionary (entity_name, attribute_name, data_type, is_required, max_length, description)
VALUES
    ('curated_vault', 'title',           'string',    TRUE,  255,  'Vault item title / display name.'),
    ('curated_vault', 'category',        'string',    FALSE, 100,  'Category (Watches, Jewellery, Art, etc.).'),
    ('curated_vault', 'brand',           'string',    FALSE, 100,  'Brand or maison name.'),
    ('curated_vault', 'description',     'string',    FALSE, 4000, 'Full description of the vault item.'),
    ('curated_vault', 'sku',             'string',    FALSE, 50,   'Stock-keeping unit identifier.'),
    ('curated_vault', 'availability',    'string',    FALSE, 20,   'Availability status (available, reserved, sold).'),
    ('curated_vault', 'aesthetic_embed', 'vector',    FALSE, NULL, '1536-dim aesthetic embedding for semantic matching.')
ON CONFLICT (entity_name, attribute_name) DO NOTHING;

-- ─── experiential_requests: Standard Fields ──────────────────────────────────
INSERT INTO sys_dictionary (entity_name, attribute_name, data_type, is_required, max_length, description)
VALUES
    ('experiential_requests', 'title',          'string',    TRUE,  255,  'Request title / summary.'),
    ('experiential_requests', 'description',    'string',    FALSE, 4000, 'Detailed description of the experience request.'),
    ('experiential_requests', 'status',         'string',    FALSE, 30,   'Request status (draft, submitted, in_progress, fulfilled, cancelled).'),
    ('experiential_requests', 'priority',       'string',    FALSE, 20,   'Priority level (low, medium, high, urgent).'),
    ('experiential_requests', 'requested_date', 'date',      FALSE, NULL, 'Target date for the experience.'),
    ('experiential_requests', 'client_id',      'reference', FALSE, NULL, 'Reference to the hyper_profile of the requesting client.')
ON CONFLICT (entity_name, attribute_name) DO NOTHING;
