package com.ppaw.passwordvault;

import com.ppaw.passwordvault.model.ServicePlan;
import com.ppaw.passwordvault.model.User;
import com.ppaw.passwordvault.model.VaultItem;
import com.ppaw.passwordvault.repository.ServicePlanRepository;
import com.ppaw.passwordvault.repository.UserRepository;
import com.ppaw.passwordvault.repository.VaultItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Console Data Display Runner
 * Echivalent ConsoleApplication din Entity Framework
 * Afișează datele din tabele create
 */
@Component
@Order(3) // Rulează după DataInitializer
public class ConsoleDataDisplayRunner implements CommandLineRunner {

    @Autowired
    private ServicePlanRepository servicePlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VaultItemRepository vaultItemRepository;

    @Override
    public void run(String... args) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CONSOLE DATA DISPLAY - Afișare date din tabele");
        System.out.println("=".repeat(80) + "\n");

        // Afișează Service Plans (tabela service_plans)
        displayServicePlans();
        
        // Afișează Users (tabela users)
        displayUsers();
        
        // Afișează Vault Items (tabela vault_items)
        displayVaultItems();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Finalizat afișarea datelor");
        System.out.println("=".repeat(80) + "\n");
    }

    private void displayServicePlans() {
        System.out.println("📋 SERVICE PLANS (tabela service_plans):");
        System.out.println("-".repeat(80));
        
        List<ServicePlan> plans = servicePlanRepository.findAll();
        
        if (plans.isEmpty()) {
            System.out.println("Nu există planuri de servicii în baza de date.");
        } else {
            System.out.printf("%-5s | %-10s | %-10s | %-8s | %-10s%n", 
                "ID", "Nume", "Preț", "Monedă", "Activ");
            System.out.println("-".repeat(80));
            
            for (ServicePlan plan : plans) {
                System.out.printf("%-5d | %-10s | %-10s | %-8s | %-10s%n",
                    plan.getId(),
                    plan.getName(),
                    plan.getPrice() + " " + plan.getCurrency(),
                    plan.getCurrency(),
                    plan.getIsActive() ? "DA" : "NU"
                );
            }
        }
        
        System.out.println("Total: " + plans.size() + " planuri\n");
    }

    private void displayUsers() {
        System.out.println("👥 USERS (tabela users):");
        System.out.println("-".repeat(80));
        
        List<User> users = userRepository.findAll();
        
        if (users.isEmpty()) {
            System.out.println("Nu există utilizatori în baza de date.");
        } else {
            System.out.printf("%-5s | %-15s | %-25s | %-10s%n", 
                "ID", "Username", "Email", "Plan ID");
            System.out.println("-".repeat(80));
            
            for (User user : users) {
                // Accesăm servicePlan pentru a demonstra lazy loading
                // Dacă servicePlan nu e încărcat, va genera un query separat
                Long planId = user.getServicePlan() != null ? user.getServicePlan().getId() : null;
                
                System.out.printf("%-5d | %-15s | %-25s | %-10s%n",
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    planId != null ? planId.toString() : "N/A"
                );
            }
        }
        
        System.out.println("Total: " + users.size() + " utilizatori\n");
    }

    private void displayVaultItems() {
        System.out.println("🔐 VAULT ITEMS (tabela vault_items):");
        System.out.println("-".repeat(80));
        
        List<VaultItem> items = vaultItemRepository.findAll();
        
        if (items.isEmpty()) {
            System.out.println("Nu există item-uri în vault.");
        } else {
            System.out.printf("%-5s | %-30s | %-20s | %-10s%n", 
                "ID", "Titlu", "Username", "User ID");
            System.out.println("-".repeat(80));
            
            for (VaultItem item : items) {
                Long userId = item.getUser() != null ? item.getUser().getId() : null;
                
                System.out.printf("%-5d | %-30s | %-20s | %-10s%n",
                    item.getId(),
                    item.getTitle() != null && item.getTitle().length() > 30 
                        ? item.getTitle().substring(0, 27) + "..." 
                        : item.getTitle(),
                    item.getUsername() != null && item.getUsername().length() > 20
                        ? item.getUsername().substring(0, 17) + "..."
                        : item.getUsername(),
                    userId != null ? userId.toString() : "N/A"
                );
            }
        }
        
        System.out.println("Total: " + items.size() + " item-uri\n");
    }
}

