-- Flyway Migration V7: Create shared_vault_items table
-- Creează tabela shared_vault_items cu chei străine către vault_items și users

CREATE TABLE IF NOT EXISTS vault_schema.shared_vault_items (
    id BIGSERIAL PRIMARY KEY,
    vault_item_id BIGINT NOT NULL,
    shared_by_user_id BIGINT NOT NULL,
    shared_with_user_id BIGINT NOT NULL,
    can_edit BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_shared_vault_items_vault_item FOREIGN KEY (vault_item_id) 
        REFERENCES vault_schema.vault_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_shared_vault_items_shared_by FOREIGN KEY (shared_by_user_id) 
        REFERENCES vault_schema.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_shared_vault_items_shared_with FOREIGN KEY (shared_with_user_id) 
        REFERENCES vault_schema.users(id) ON DELETE CASCADE,
    CONSTRAINT uk_shared_vault_items_unique UNIQUE (vault_item_id, shared_with_user_id)
);

COMMENT ON TABLE vault_schema.shared_vault_items IS 'Tabela pentru item-urile partajate între utilizatori';
COMMENT ON COLUMN vault_schema.shared_vault_items.vault_item_id IS 'Cheie străină către vault_items.id';
COMMENT ON COLUMN vault_schema.shared_vault_items.shared_by_user_id IS 'Cheie străină către users.id (cine partajează)';
COMMENT ON COLUMN vault_schema.shared_vault_items.shared_with_user_id IS 'Cheie străină către users.id (cu cine se partajează)';

