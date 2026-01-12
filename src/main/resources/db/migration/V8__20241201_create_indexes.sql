-- Flyway Migration V8: Create indexes
-- Creează indexuri pentru optimizarea interogărilor

-- Indexuri pentru service_plans
CREATE INDEX IF NOT EXISTS idx_service_plans_active ON vault_schema.service_plans(is_active);

-- Indexuri pentru plan_limits
CREATE INDEX IF NOT EXISTS idx_plan_limits_plan_id ON vault_schema.plan_limits(plan_id);

-- Indexuri pentru users
CREATE INDEX IF NOT EXISTS idx_users_plan_id ON vault_schema.users(service_plan_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON vault_schema.users(email);

-- Indexuri pentru vault_items
CREATE INDEX IF NOT EXISTS idx_vault_items_user_id ON vault_schema.vault_items(user_id);
CREATE INDEX IF NOT EXISTS idx_vault_items_folder ON vault_schema.vault_items(folder);
CREATE INDEX IF NOT EXISTS idx_vault_items_created_at ON vault_schema.vault_items(created_at);

-- Indexuri pentru password_history
CREATE INDEX IF NOT EXISTS idx_password_history_vault_item_id ON vault_schema.password_history(vault_item_id);
CREATE INDEX IF NOT EXISTS idx_password_history_created_at ON vault_schema.password_history(created_at);

-- Indexuri pentru shared_vault_items
CREATE INDEX IF NOT EXISTS idx_shared_vault_items_shared_with ON vault_schema.shared_vault_items(shared_with_user_id);
CREATE INDEX IF NOT EXISTS idx_shared_vault_items_vault_item ON vault_schema.shared_vault_items(vault_item_id);

