package com.ppaw.passwordvault.config;

import com.ppaw.passwordvault.model.PlanLimits;
import com.ppaw.passwordvault.model.ServicePlan;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@Order(2) // Run after SchemaInitializer
public class DataInitializer implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        // Check if table exists first
        if (!tableExists("vault_schema", "service_plans")) {
            System.out.println("⚠️ Table service_plans does not exist yet. Hibernate will create it on first entity access.");
            // Force table creation by accessing an entity
            try {
                entityManager.createQuery("SELECT 1 FROM ServicePlan", Integer.class).getResultList();
            } catch (Exception e) {
                // Ignore - table will be created
            }
        }

        // Check if plans already exist
        try {
            Long existingPlans = entityManager.createQuery(
                "SELECT COUNT(s) FROM ServicePlan s", Long.class
            ).getSingleResult();

            if (existingPlans == 0) {
                seedServicePlans();
            } else {
                System.out.println("✅ Service plans already exist, skipping initialization");
            }
        } catch (jakarta.persistence.PersistenceException e) {
            // Table might not exist yet, wait and retry
            System.out.println("⚠️ Waiting for tables to be created...");
            try {
                Thread.sleep(2000);
                Long existingPlans = entityManager.createQuery(
                    "SELECT COUNT(s) FROM ServicePlan s", Long.class
                ).getSingleResult();
                
                if (existingPlans == 0) {
                    seedServicePlans();
                }
            } catch (Exception ex) {
                System.err.println("⚠️ Error initializing data after retry: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error initializing data: " + e.getMessage());
        }
    }

    private boolean tableExists(String schema, String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?",
                Integer.class,
                schema, tableName
            );
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void seedServicePlans() {
        // Create Free Plan
        ServicePlan freePlan = new ServicePlan();
        freePlan.setName("Free");
        freePlan.setPrice(BigDecimal.ZERO);
        freePlan.setCurrency("USD");
        freePlan.setIsActive(true);
        entityManager.persist(freePlan);
        entityManager.flush();

        PlanLimits freeLimits = new PlanLimits();
        freeLimits.setServicePlan(freePlan);
        freeLimits.setMaxVaultItems(20);
        freeLimits.setMaxPasswordLength(16);
        freeLimits.setCanExport(false);
        freeLimits.setCanImport(false);
        freeLimits.setCanShare(false);
        freeLimits.setMaxHistoryVersions(0);
        freeLimits.setCanAttachments(false);
        freeLimits.setMaxDevices(1);
        freeLimits.setExcludeAmbiguous(false);
        entityManager.persist(freeLimits);

        // Create Usual Plan
        ServicePlan usualPlan = new ServicePlan();
        usualPlan.setName("Usual");
        usualPlan.setPrice(new BigDecimal("4.99"));
        usualPlan.setCurrency("USD");
        usualPlan.setIsActive(true);
        entityManager.persist(usualPlan);
        entityManager.flush();

        PlanLimits usualLimits = new PlanLimits();
        usualLimits.setServicePlan(usualPlan);
        usualLimits.setMaxVaultItems(200);
        usualLimits.setMaxPasswordLength(32);
        usualLimits.setCanExport(true);
        usualLimits.setCanImport(false);
        usualLimits.setCanShare(false);
        usualLimits.setMaxHistoryVersions(3);
        usualLimits.setCanAttachments(false);
        usualLimits.setMaxDevices(3);
        usualLimits.setExcludeAmbiguous(true);
        entityManager.persist(usualLimits);

        // Create Premium Plan
        ServicePlan premiumPlan = new ServicePlan();
        premiumPlan.setName("Premium");
        premiumPlan.setPrice(new BigDecimal("9.99"));
        premiumPlan.setCurrency("USD");
        premiumPlan.setIsActive(true);
        entityManager.persist(premiumPlan);
        entityManager.flush();

        PlanLimits premiumLimits = new PlanLimits();
        premiumLimits.setServicePlan(premiumPlan);
        premiumLimits.setMaxVaultItems(2000);
        premiumLimits.setMaxPasswordLength(64);
        premiumLimits.setCanExport(true);
        premiumLimits.setCanImport(true);
        premiumLimits.setCanShare(true);
        premiumLimits.setMaxHistoryVersions(10);
        premiumLimits.setCanAttachments(true);
        premiumLimits.setMaxDevices(10);
        premiumLimits.setExcludeAmbiguous(true);
        entityManager.persist(premiumLimits);

        entityManager.flush();
        System.out.println("✅ Service plans initialized: Free, Usual, Premium");
    }
}

