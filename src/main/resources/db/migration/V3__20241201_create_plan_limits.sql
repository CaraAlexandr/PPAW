-- Flyway Migration V3: Create plan_limits table
-- Creează tabela plan_limits cu cheie străină către service_plans

CREATE TABLE IF NOT EXISTS vault_schema.plan_limits (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL UNIQUE,
    max_vault_items INTEGER NOT NULL DEFAULT 20,
    max_password_length INTEGER NOT NULL DEFAULT 16,
    can_export BOOLEAN NOT NULL DEFAULT false,
    can_import BOOLEAN NOT NULL DEFAULT false,
    can_share BOOLEAN NOT NULL DEFAULT false,
    max_history_versions INTEGER NOT NULL DEFAULT 0,
    can_attachments BOOLEAN NOT NULL DEFAULT false,
    max_devices INTEGER NOT NULL DEFAULT 1,
    exclude_ambiguous BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_plan_limits_service_plan FOREIGN KEY (plan_id) 
        REFERENCES vault_schema.service_plans(id) ON DELETE CASCADE
);

COMMENT ON TABLE vault_schema.plan_limits IS 'Tabela pentru limitările fiecărui plan de servicii';
COMMENT ON COLUMN vault_schema.plan_limits.plan_id IS 'Cheie străină către service_plans.id';

