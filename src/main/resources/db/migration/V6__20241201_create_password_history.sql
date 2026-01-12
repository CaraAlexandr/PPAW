-- Flyway Migration V6: Create password_history table
-- Creează tabela password_history cu cheie străină către vault_items

CREATE TABLE IF NOT EXISTS vault_schema.password_history (
    id BIGSERIAL PRIMARY KEY,
    vault_item_id BIGINT NOT NULL,
    encrypted_password TEXT NOT NULL,
    password_iv VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_password_history_vault_item FOREIGN KEY (vault_item_id) 
        REFERENCES vault_schema.vault_items(id) ON DELETE CASCADE
);

COMMENT ON TABLE vault_schema.password_history IS 'Tabela pentru istoricul parolelor (versiuni anterioare)';
COMMENT ON COLUMN vault_schema.password_history.vault_item_id IS 'Cheie străină către vault_items.id';

