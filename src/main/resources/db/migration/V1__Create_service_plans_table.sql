-- Create service_plans table
CREATE TABLE service_plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    price DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create plan_limits table
CREATE TABLE plan_limits (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    max_vault_items INTEGER NOT NULL DEFAULT 20,
    max_password_length INTEGER NOT NULL DEFAULT 16,
    can_export BOOLEAN NOT NULL DEFAULT false,
    can_import BOOLEAN NOT NULL DEFAULT false,
    can_share BOOLEAN NOT NULL DEFAULT false,
    max_history_versions INTEGER NOT NULL DEFAULT 0,
    can_attachments BOOLEAN NOT NULL DEFAULT false,
    max_devices INTEGER NOT NULL DEFAULT 1,
    exclude_ambiguous BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan_limits_plan FOREIGN KEY (plan_id) REFERENCES service_plans(id) ON DELETE CASCADE,
    CONSTRAINT uq_plan_limits_plan UNIQUE (plan_id)
);

-- Create indexes
CREATE INDEX idx_plan_limits_plan_id ON plan_limits(plan_id);
CREATE INDEX idx_service_plans_active ON service_plans(is_active);

