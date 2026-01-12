-- Create shared_vault_items table for sharing functionality (Premium feature)
CREATE TABLE shared_vault_items (
    id BIGSERIAL PRIMARY KEY,
    vault_item_id BIGINT NOT NULL,
    shared_by_user_id BIGINT NOT NULL,
    shared_with_user_id BIGINT NOT NULL,
    can_edit BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shared_vault_item FOREIGN KEY (vault_item_id) REFERENCES vault_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_shared_by_user FOREIGN KEY (shared_by_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_shared_with_user FOREIGN KEY (shared_with_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_shared_item_user UNIQUE (vault_item_id, shared_with_user_id)
);

-- Create index
CREATE INDEX idx_shared_vault_items_shared_with ON shared_vault_items(shared_with_user_id);
CREATE INDEX idx_shared_vault_items_vault_item ON shared_vault_items(vault_item_id);

