-- Flyway Migration V9: Seed data pentru service_plans și plan_limits
-- Echivalent Configuration.Seed() din Entity Framework
-- Inserează date de test în 2 tabele: service_plans și plan_limits
-- IMPORTANT: Datele se inserează în ordine - mai întâi service_plans (părinte), apoi plan_limits (copil)

-- Seed service_plans (tabela părinte - fără FK)
INSERT INTO vault_schema.service_plans (name, price, currency, is_active, created_at, updated_at)
VALUES 
    ('Free', 0.00, 'USD', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Usual', 4.99, 'USD', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Premium', 9.99, 'USD', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Seed plan_limits (tabela copil - cu FK către service_plans)
-- Datele se inserează DOAR după ce service_plans sunt inserate
INSERT INTO vault_schema.plan_limits (
    plan_id, max_vault_items, max_password_length, can_export, can_import, 
    can_share, max_history_versions, can_attachments, max_devices, 
    exclude_ambiguous, created_at, updated_at
)
SELECT 
    sp.id,
    CASE sp.name 
        WHEN 'Free' THEN 20
        WHEN 'Usual' THEN 200
        WHEN 'Premium' THEN 2000
    END as max_vault_items,
    CASE sp.name 
        WHEN 'Free' THEN 16
        WHEN 'Usual' THEN 32
        WHEN 'Premium' THEN 64
    END as max_password_length,
    CASE sp.name 
        WHEN 'Free' THEN false
        WHEN 'Usual' THEN true
        WHEN 'Premium' THEN true
    END as can_export,
    CASE sp.name 
        WHEN 'Free' THEN false
        WHEN 'Usual' THEN false
        WHEN 'Premium' THEN true
    END as can_import,
    CASE sp.name 
        WHEN 'Free' THEN false
        WHEN 'Usual' THEN false
        WHEN 'Premium' THEN true
    END as can_share,
    CASE sp.name 
        WHEN 'Free' THEN 0
        WHEN 'Usual' THEN 3
        WHEN 'Premium' THEN 10
    END as max_history_versions,
    CASE sp.name 
        WHEN 'Free' THEN false
        WHEN 'Usual' THEN false
        WHEN 'Premium' THEN true
    END as can_attachments,
    CASE sp.name 
        WHEN 'Free' THEN 1
        WHEN 'Usual' THEN 3
        WHEN 'Premium' THEN 10
    END as max_devices,
    CASE sp.name 
        WHEN 'Free' THEN false
        WHEN 'Usual' THEN true
        WHEN 'Premium' THEN true
    END as exclude_ambiguous,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM vault_schema.service_plans sp
WHERE NOT EXISTS (
    SELECT 1 FROM vault_schema.plan_limits pl WHERE pl.plan_id = sp.id
);

COMMENT ON TABLE vault_schema.service_plans IS 'Date seed: Free (0$), Usual (4.99$), Premium (9.99$)';
COMMENT ON TABLE vault_schema.plan_limits IS 'Date seed: Limitări pentru fiecare plan';

