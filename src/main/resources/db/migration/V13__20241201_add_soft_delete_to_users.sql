-- Flyway Migration V13: Add soft delete support to users table
-- Adaugă coloana is_deleted pentru ștergere logică (soft delete)

ALTER TABLE vault_schema.users 
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT false;

COMMENT ON COLUMN vault_schema.users.is_deleted IS 'Marchează utilizatorul ca șters logic (soft delete). Utilizatorii șterși logic nu sunt afișați în operațiile de Get și GetAll.';

-- Create index for better query performance when filtering deleted users
CREATE INDEX IF NOT EXISTS idx_users_is_deleted ON vault_schema.users(is_deleted);

