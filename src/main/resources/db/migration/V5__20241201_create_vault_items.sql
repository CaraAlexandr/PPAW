-- Flyway Migration V5: Create vault_items table
-- Creează tabela vault_items cu cheie străină către users

CREATE TABLE IF NOT EXISTS vault_schema.vault_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    username VARCHAR(255),
    encrypted_password TEXT NOT NULL,
    password_iv VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,
    url VARCHAR(500),
    notes TEXT,
    folder VARCHAR(100),
    tags VARCHAR(255),
    is_favorite BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_vault_items_user FOREIGN KEY (user_id) 
        REFERENCES vault_schema.users(id) ON DELETE CASCADE
);

COMMENT ON TABLE vault_schema.vault_items IS 'Tabela pentru item-urile din vault-ul utilizatorilor';
COMMENT ON COLUMN vault_schema.vault_items.user_id IS 'Cheie străină către users.id';

