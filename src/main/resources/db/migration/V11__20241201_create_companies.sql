-- Flyway Migration V11: Create companies table
-- Tabela pentru Laboratorul 5 - MVC

CREATE TABLE IF NOT EXISTS vault_schema.companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    country VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    email VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Indexuri
CREATE INDEX IF NOT EXISTS idx_companies_country ON vault_schema.companies(country);
CREATE INDEX IF NOT EXISTS idx_companies_is_active ON vault_schema.companies(is_active);

COMMENT ON TABLE vault_schema.companies IS 'Tabela pentru companii - Laboratorul 5 MVC';
COMMENT ON COLUMN vault_schema.companies.country IS 'Țara companiei (câmp select)';
COMMENT ON COLUMN vault_schema.companies.is_active IS 'Status activ/inactiv (checkbox)';



