-- Flyway Migration V10: Modificări entități
-- Echivalent Add-Migration în Entity Framework pentru modificări modele
-- 
-- Modificări efectuate:
-- 1. Adăugare 2 proprietăți noi la tabela users: last_login_at, login_count
-- 2. Modificare tip date: currency din VARCHAR(3) în VARCHAR(10) în service_plans
-- 3. Adăugare model nou: audit_logs

-- 1. Adăugare coloane noi la tabela users
ALTER TABLE vault_schema.users 
ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS login_count INTEGER NOT NULL DEFAULT 0;

COMMENT ON COLUMN vault_schema.users.last_login_at IS 'Data și ora ultimei autentificări a utilizatorului';
COMMENT ON COLUMN vault_schema.users.login_count IS 'Numărul total de autentificări ale utilizatorului';

-- 2. Modificare tip date pentru currency în service_plans
-- PostgreSQL permite modificarea tipului direct
ALTER TABLE vault_schema.service_plans 
ALTER COLUMN currency TYPE VARCHAR(10);

COMMENT ON COLUMN vault_schema.service_plans.currency IS 'Codul monedei (extins de la 3 la 10 caractere pentru suport mai bun)';

-- 3. Creare tabel nou: audit_logs
CREATE TABLE IF NOT EXISTS vault_schema.audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    description TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) 
        REFERENCES vault_schema.users(id) ON DELETE CASCADE
);

-- Indexuri pentru audit_logs
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON vault_schema.audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON vault_schema.audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON vault_schema.audit_logs(created_at);

COMMENT ON TABLE vault_schema.audit_logs IS 'Tabela pentru înregistrarea acțiunilor utilizatorilor (audit trail)';
COMMENT ON COLUMN vault_schema.audit_logs.user_id IS 'Cheie străină către users.id';
COMMENT ON COLUMN vault_schema.audit_logs.action IS 'Tipul acțiunii (LOGIN, LOGOUT, CREATE_VAULT_ITEM, etc.)';
COMMENT ON COLUMN vault_schema.audit_logs.ip_address IS 'Adresa IP a utilizatorului';

-- Actualizare login_count pentru utilizatorii existenți (dacă există)
-- Setăm default 0 pentru utilizatorii care nu au această valoare setată
UPDATE vault_schema.users 
SET login_count = 0 
WHERE login_count IS NULL;

