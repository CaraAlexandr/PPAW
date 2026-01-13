-- Flyway Migration V1: Create Schema
-- Echivalent Enable-Migrations din Entity Framework
-- Creează schema vault_schema dacă nu există

CREATE SCHEMA IF NOT EXISTS vault_schema;

COMMENT ON SCHEMA vault_schema IS 'Schema pentru aplicația Password Vault - Code First Approach';

