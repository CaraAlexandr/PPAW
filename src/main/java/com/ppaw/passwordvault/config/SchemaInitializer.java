package com.ppaw.passwordvault.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1) // Run before DataInitializer
public class SchemaInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        // Create schema if it doesn't exist
        try {
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS vault_schema");
            System.out.println("✅ Schema 'vault_schema' created or already exists");
        } catch (Exception e) {
            System.err.println("⚠️ Could not create schema: " + e.getMessage());
            // In case of permission issues, try with current user
            try {
                jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS vault_schema AUTHORIZATION CURRENT_USER");
                System.out.println("✅ Schema 'vault_schema' created with current user");
            } catch (Exception e2) {
                System.err.println("❌ Failed to create schema. Please create manually: CREATE SCHEMA vault_schema;");
            }
        }
    }
}

