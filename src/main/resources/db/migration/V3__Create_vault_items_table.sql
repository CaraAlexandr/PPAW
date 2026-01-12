-- Create vault_items table
CREATE TABLE vault_items (
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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vault_items_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index
CREATE INDEX idx_vault_items_user_id ON vault_items(user_id);
CREATE INDEX idx_vault_items_folder ON vault_items(folder);
CREATE INDEX idx_vault_items_created_at ON vault_items(created_at);

