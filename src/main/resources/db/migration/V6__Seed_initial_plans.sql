-- Insert initial service plans
INSERT INTO service_plans (name, price, currency, is_active) VALUES
    ('Free', 0.00, 'USD', true),
    ('Usual', 4.99, 'USD', true),
    ('Premium', 9.99, 'USD', true);

-- Insert plan limits for Free plan
INSERT INTO plan_limits (plan_id, max_vault_items, max_password_length, can_export, can_import, can_share, max_history_versions, can_attachments, max_devices, exclude_ambiguous)
SELECT id, 20, 16, false, false, false, 0, false, 1, false
FROM service_plans WHERE name = 'Free';

-- Insert plan limits for Usual plan
INSERT INTO plan_limits (plan_id, max_vault_items, max_password_length, can_export, can_import, can_share, max_history_versions, can_attachments, max_devices, exclude_ambiguous)
SELECT id, 200, 32, true, false, false, 3, false, 3, true
FROM service_plans WHERE name = 'Usual';

-- Insert plan limits for Premium plan
INSERT INTO plan_limits (plan_id, max_vault_items, max_password_length, can_export, can_import, can_share, max_history_versions, can_attachments, max_devices, exclude_ambiguous)
SELECT id, 2000, 64, true, true, true, 10, true, 10, true
FROM service_plans WHERE name = 'Premium';

