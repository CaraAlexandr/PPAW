-- Flyway Migration V2: Create service_plans table
-- Echivalent Add-Migration din Entity Framework
-- Creează tabela service_plans

CREATE TABLE IF NOT EXISTS vault_schema.service_plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    price DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

COMMENT ON TABLE vault_schema.service_plans IS 'Tabela pentru planurile de servicii (Free, Usual, Premium)';
COMMENT ON COLUMN vault_schema.service_plans.id IS 'ID unic al planului';
COMMENT ON COLUMN vault_schema.service_plans.name IS 'Numele planului (Free, Usual, Premium)';
COMMENT ON COLUMN vault_schema.service_plans.price IS 'Prețul planului în moneda specificată';
COMMENT ON COLUMN vault_schema.service_plans.currency IS 'Codul monedei (USD, EUR, etc.)';

