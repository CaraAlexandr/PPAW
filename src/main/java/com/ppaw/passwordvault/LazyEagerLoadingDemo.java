package com.ppaw.passwordvault;

import com.ppaw.passwordvault.model.User;
import com.ppaw.passwordvault.model.VaultItem;
import com.ppaw.passwordvault.repository.UserRepository;
import com.ppaw.passwordvault.repository.VaultItemRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Demo pentru Lazy Loading vs Eager Loading
 * Echivalent Exercițiul 7 din laborator
 * 
 * Demonstrează diferențele între:
 * - Lazy Loading (implicit în JPA pentru @ManyToOne, @OneToMany)
 * - Eager Loading (explicit cu JOIN FETCH sau @EntityGraph)
 */
@Component
@Order(4) // Rulează după ConsoleDataDisplayRunner
public class LazyEagerLoadingDemo implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VaultItemRepository vaultItemRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional // Necesar pentru lazy loading să funcționeze
    public void run(String... args) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO: LAZY LOADING vs EAGER LOADING");
        System.out.println("=".repeat(80) + "\n");

        // Găsim un user existent (sau folosim primul)
        User user = userRepository.findAll().stream().findFirst().orElse(null);
        
        if (user == null) {
            System.out.println("⚠️ Nu există utilizatori în baza de date pentru demo.");
            System.out.println("   Creează utilizatori mai întâi pentru a vedea demo-ul complet.");
            return;
        }

        System.out.println("Utilizator selectat: " + user.getUsername() + " (ID: " + user.getId() + ")\n");

        // Demo 1: Lazy Loading (implicit)
        demonstrateLazyLoading(user.getId());

        // Demo 2: Eager Loading cu JOIN FETCH
        demonstrateEagerLoadingWithJoinFetch(user.getId());

        // Demo 3: Eager Loading cu @EntityGraph
        demonstrateEagerLoadingWithEntityGraph(user.getId());

        // Demo 4: Lazy Loading cu VaultItem
        demonstrateLazyLoadingVaultItem(user.getId());

        System.out.println("\n" + "=".repeat(80));
        System.out.println("FINALIZAT: Demo Lazy vs Eager Loading");
        System.out.println("=".repeat(80) + "\n");
    }

    /**
     * Demo 1: Lazy Loading (implicit)
     * Relația @ManyToOne(fetch = FetchType.LAZY) se încarcă doar când e accesată
     */
    @Transactional
    private void demonstrateLazyLoading(Long userId) {
        System.out.println("📌 DEMO 1: LAZY LOADING (Implicit)");
        System.out.println("-".repeat(80));
        System.out.println("Comportament:");
        System.out.println("- Relația servicePlan este LAZY (implicit pentru @ManyToOne)");
        System.out.println("- Query-ul inițial NU încarcă servicePlan");
        System.out.println("- servicePlan se încarcă când e accesat (generând un query separat)\n");

        System.out.println("Executare: findById(" + userId + ")");
        User user = userRepository.findById(userId).orElse(null);
        
        if (user != null) {
            System.out.println("✅ User încărcat: " + user.getUsername());
            System.out.println("   Status: servicePlan NU este încă încărcat (lazy)");
            
            // Accesăm servicePlan - aceasta va genera un query separat (N+1 problem)
            System.out.println("\nAccesăm user.getServicePlan() - va genera un query SQL separat:");
            if (user.getServicePlan() != null) {
                System.out.println("✅ ServicePlan încărcat: " + user.getServicePlan().getName());
                System.out.println("   (Acest query a fost executat LAZY - doar când era necesar)");
            }
        }
        
        System.out.println("\n");
    }

    /**
     * Demo 2: Eager Loading cu JOIN FETCH
     * Folosim JOIN FETCH pentru a încărca relația în același query
     */
    @Transactional
    private void demonstrateEagerLoadingWithJoinFetch(Long userId) {
        System.out.println("📌 DEMO 2: EAGER LOADING cu JOIN FETCH");
        System.out.println("-".repeat(80));
        System.out.println("Comportament:");
        System.out.println("- Folosim JOIN FETCH pentru a încărca servicePlan în același query");
        System.out.println("- Un singur query SQL cu JOIN");
        System.out.println("- Evită problema N+1\n");

        System.out.println("Executare: findByIdWithEagerLoading(" + userId + ")");
        User user = userRepository.findByIdWithEagerLoading(userId).orElse(null);
        
        if (user != null) {
            System.out.println("✅ User încărcat: " + user.getUsername());
            System.out.println("   Status: servicePlan este DEJA încărcat (eager cu JOIN FETCH)");
            
            // Accesăm servicePlan - NU va genera un query separat
            if (user.getServicePlan() != null) {
                System.out.println("✅ ServicePlan deja încărcat: " + user.getServicePlan().getName());
                System.out.println("   (Acesta a fost încărcat într-un singur query cu JOIN FETCH)");
            }
        }
        
        System.out.println("\n");
    }

    /**
     * Demo 3: Eager Loading cu @EntityGraph
     * Folosim @EntityGraph pentru a specifica ce relații să fie încărcate eager
     */
    @Transactional
    private void demonstrateEagerLoadingWithEntityGraph(Long userId) {
        System.out.println("📌 DEMO 3: EAGER LOADING cu @EntityGraph");
        System.out.println("-".repeat(80));
        System.out.println("Comportament:");
        System.out.println("- Folosim @EntityGraph pentru a încărca multiple relații");
        System.out.println("- Mai flexibil decât JOIN FETCH pentru relații multiple");
        System.out.println("- Poate încărca servicePlan + vaultItems într-un singur query\n");

        System.out.println("Executare: findByIdWithRelations(" + userId + ")");
        User user = userRepository.findByIdWithRelations(userId).orElse(null);
        
        if (user != null) {
            System.out.println("✅ User încărcat: " + user.getUsername());
            System.out.println("   Status: servicePlan + vaultItems sunt DEJA încărcate");
            
            if (user.getServicePlan() != null) {
                System.out.println("✅ ServicePlan încărcat: " + user.getServicePlan().getName());
            }
            
            if (user.getVaultItems() != null) {
                System.out.println("✅ VaultItems încărcate: " + user.getVaultItems().size() + " item-uri");
                System.out.println("   (Toate încărcate cu @EntityGraph într-un singur query)");
            }
        }
        
        System.out.println("\n");
    }

    /**
     * Demo 4: Lazy Loading cu VaultItem -> User
     * Demonstrează lazy loading pe relația inversă
     */
    @Transactional
    private void demonstrateLazyLoadingVaultItem(Long userId) {
        System.out.println("📌 DEMO 4: LAZY LOADING cu VaultItem");
        System.out.println("-".repeat(80));
        
        // Găsim un vault item pentru acest user
        VaultItem item = vaultItemRepository.findByUserId(userId).stream().findFirst().orElse(null);
        
        if (item == null) {
            System.out.println("⚠️ Nu există vault items pentru acest user.");
            System.out.println("   Lazy loading va funcționa, dar nu putem demonstra accesarea user-ului.");
            System.out.println("\n");
            return;
        }

        System.out.println("Executare: findByUserId(" + userId + ") - va returna VaultItem cu user LAZY");
        System.out.println("✅ VaultItem încărcat: " + item.getTitle());
        System.out.println("   Status: user NU este încă încărcat (lazy)");
        
        // Accesăm user - va genera un query separat
        System.out.println("\nAccesăm item.getUser() - va genera un query SQL separat:");
        if (item.getUser() != null) {
            System.out.println("✅ User încărcat: " + item.getUser().getUsername());
            System.out.println("   (Acest query a fost executat LAZY - doar când era necesar)");
        }
        
        System.out.println("\n");
    }
}

