-- Flyway Migration V12: Create employees table
-- Tabela pentru Laboratorul 6 - MVC Complex
-- Are FK către companies

CREATE TABLE IF NOT EXISTS vault_schema.employees (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    position VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT true,
    company_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_employees_company FOREIGN KEY (company_id) 
        REFERENCES vault_schema.companies(id) ON DELETE RESTRICT
);

-- Indexuri
CREATE INDEX IF NOT EXISTS idx_employees_company_id ON vault_schema.employees(company_id);
CREATE INDEX IF NOT EXISTS idx_employees_email ON vault_schema.employees(email);
CREATE INDEX IF NOT EXISTS idx_employees_is_active ON vault_schema.employees(is_active);

COMMENT ON TABLE vault_schema.employees IS 'Tabela pentru angajați - Laboratorul 6 MVC Complex';
COMMENT ON COLUMN vault_schema.employees.company_id IS 'Cheie străină către companies.id';
COMMENT ON COLUMN vault_schema.employees.is_active IS 'Status activ/inactiv (checkbox)';


