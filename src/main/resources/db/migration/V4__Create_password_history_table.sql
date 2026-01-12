-- Create password_history table for tracking password changes
CREATE TABLE password_history (
    id BIGSERIAL PRIMARY KEY,
    vault_item_id BIGINT NOT NULL,
    encrypted_password TEXT NOT NULL,
    password_iv VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_history_vault_item FOREIGN KEY (vault_item_id) REFERENCES vault_items(id) ON DELETE CASCADE
);

-- Create index
CREATE INDEX idx_password_history_vault_item_id ON password_history(vault_item_id);
CREATE INDEX idx_password_history_created_at ON password_history(created_at);

