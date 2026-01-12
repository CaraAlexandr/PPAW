-- Flyway Migration V4: Create users table
-- Creează tabela users cu cheie străină către service_plans

CREATE TABLE IF NOT EXISTS vault_schema.users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    service_plan_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_users_service_plan FOREIGN KEY (service_plan_id) 
        REFERENCES vault_schema.service_plans(id) ON DELETE RESTRICT
);

COMMENT ON TABLE vault_schema.users IS 'Tabela pentru utilizatorii aplicației';
COMMENT ON COLUMN vault_schema.users.service_plan_id IS 'Cheie străină către service_plans.id';

