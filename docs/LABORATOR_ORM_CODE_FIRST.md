# Laborator ORM Code First - Spring Boot

## Obiective
1. ORM - Object Relational Mapping – Code First
2. ORM - Code First – Efectuarea modificărilor
3. ORM – Lazy loading vs Eager loading

---

## 1. ORM - Object Relational Mapping – Code First

### Exercițiul 1: Utilizarea conceptului "Code First"

**Echivalent Entity Framework**: Repository_CodeFirst (ClassLibrary) + LibrarieModele (ClassLibrary)

**În Spring Boot**:
- **Modelele**: `src/main/java/com/ppaw/passwordvault/model/` - clasele `@Entity`
- **Repository**: `src/main/java/com/ppaw/passwordvault/repository/` - interfețele `JpaRepository`
- **Configuration**: `application.properties` - configurarea bazei de date

#### Etape:
1. ✅ **Modele definite**: `User`, `ServicePlan`, `PlanLimits`, `VaultItem`, `PasswordHistory`, `SharedVaultItem`, `AuditLog`
2. ✅ **Repository interfaces**: `UserRepository`, `ServicePlanRepository`, `VaultItemRepository`, etc.
3. ✅ **Configurare DB**: `application.properties` cu connection string și setări JPA/Hibernate
4. ✅ **DBContext echivalent**: Spring Data JPA gestionează automat `EntityManager` și `PersistenceContext`

#### Connection String în `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/password_vault
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
```

---

### Exercițiul 2: Generarea migrărilor și crearea tabelelor

**Echivalent Entity Framework**:
- `Enable-Migrations` → Activare Flyway
- `Add-Migration nume_migrare` → Creare fișier SQL de migrare
- `Update-Database` → Pornirea aplicației (Flyway rulează automat)

**În Spring Boot cu Flyway**:

#### 1. Activare migrări (Enable-Migrations)
Configurare în `application.properties`:
```properties
# Flyway Configuration - ENABLED pentru migrații Code First
spring.flyway.enabled=true
spring.flyway.schemas=vault_schema
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# JPA/Hibernate - validate (nu mai creează automat tabele)
spring.jpa.hibernate.ddl-auto=validate
```

#### 2. Generarea migrărilor (Add-Migration)
Migrațiile se creează manual în `src/main/resources/db/migration/`:

**Structura migrațiilor create**:
- `V1__20241201_create_schema.sql` - Creare schema `vault_schema`
- `V2__20241201_create_service_plans.sql` - Creare tabela `service_plans`
- `V3__20241201_create_plan_limits.sql` - Creare tabela `plan_limits`
- `V4__20241201_create_users.sql` - Creare tabela `users`
- `V5__20241201_create_vault_items.sql` - Creare tabela `vault_items`
- `V6__20241201_create_password_history.sql` - Creare tabela `password_history`
- `V7__20241201_create_shared_vault_items.sql` - Creare tabela `shared_vault_items`
- `V8__20241201_create_indexes.sql` - Creare indexuri
- `V9__20241201_seed_service_plans.sql` - Seed data (ServicePlan + PlanLimits)

**Convenții Flyway**:
- Nume fișier: `V{versiune}__{descriere}.sql`
- Versiunea trebuie să fie unică și crescătoare
- Underscore dublu (`__`) separă versiunea de descriere

#### 3. Aplicarea migrărilor (Update-Database)
Pornirea aplicației Spring Boot:
```bash
# Maven
mvn spring-boot:run

# Sau compilează și rulează
mvn clean package
java -jar target/password-vault-1.0.0.jar
```

Flyway va rula automat migrațiile la pornirea aplicației.

**Verificare migrări**:
Flyway creează tabela `flyway_schema_history` în baza de date pentru a ține evidența migrațiilor aplicate.

---

### Exercițiul 3: Seed data în 2 tabele

**Echivalent Entity Framework**: `Configuration.Seed()` method

**În Spring Boot**: Migrație Flyway `V9__20241201_seed_service_plans.sql`

#### Date inserate:
1. **Tabela `service_plans`** (tabela părinte - fără FK):
   - Free (0.00 USD)
   - Usual (4.99 USD)
   - Premium (9.99 USD)

2. **Tabela `plan_limits`** (tabela copil - cu FK către `service_plans`):
   - Limitări pentru fiecare plan

#### Ordinea inserării:
**IMPORTANT**: Datele se inserează în ordine - mai întâi `service_plans` (părinte), apoi `plan_limits` (copil cu FK).

În SQL, folosim `INSERT ... SELECT` pentru a insera `plan_limits` bazat pe `service_plans` existente, evitând problemele de ordine.

---

### Exercițiul 4: Console Application pentru afișare date

**Echivalent Entity Framework**: ConsoleApplication care afișează date

**În Spring Boot**: `ConsoleDataDisplayRunner` - implementează `CommandLineRunner`

#### Implementare:
Clasa `ConsoleDataDisplayRunner`:
- Rulează automat la pornirea aplicației
- Afișează date din tabele: `service_plans`, `users`, `vault_items`
- Folosește Repository interfaces pentru a citi datele

#### Configurare:
- `@Component` - face clasa un bean Spring
- `@Order(3)` - controlează ordinea de rulare (după DataInitializer)

---

## 2. ORM – Code First – Efectuarea modificărilor

### Exercițiul 5: Modificări modele și update DB

**Modificări efectuate**:

1. **Adăugare 2 proprietăți la `User`**:
   - `lastLoginAt` (LocalDateTime) - data ultimei autentificări
   - `loginCount` (Integer) - numărul de autentificări

2. **Modificare tip date**:
   - `ServicePlan.currency`: de la `VARCHAR(3)` la `VARCHAR(10)`

3. **Model nou**:
   - `AuditLog` - tabela pentru înregistrarea acțiunilor utilizatorilor

#### Migrație pentru modificări:
`V10__20241201_modify_entities.sql`:
- `ALTER TABLE users ADD COLUMN ...` - adăugare coloane noi
- `ALTER TABLE service_plans ALTER COLUMN currency TYPE ...` - modificare tip
- `CREATE TABLE audit_logs ...` - creare tabel nou

#### Aplicare migrație:
Pornirea aplicației va aplica automat migrația `V10`, modificând structura bazei de date.

---

## 3. ORM – Lazy loading vs Eager loading

### Exercițiul 7: Testare Lazy loading vs Eager loading

**Demo implementat**: `LazyEagerLoadingDemo` - demonstrează diferențele

#### Lazy Loading (implicit în JPA):

**Configurare implicită**:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "service_plan_id")
private ServicePlan servicePlan;
```

**Comportament**:
- Relația NU se încarcă imediat
- Se încarcă DOAR când este accesată (lazy)
- Generează un query SQL separat (potențială problemă N+1)

**Exemplu**:
```java
// Query 1: SELECT * FROM users WHERE id = 1
User user = userRepository.findById(1L).orElse(null);

// Query 2: SELECT * FROM service_plans WHERE id = ? (executat LAZY când accesăm)
ServicePlan plan = user.getServicePlan(); // Query separat!
```

#### Eager Loading (explicit):

**Metoda 1: JOIN FETCH în query**:
```java
@Query("SELECT u FROM User u JOIN FETCH u.servicePlan WHERE u.id = :id")
Optional<User> findByIdWithEagerLoading(@Param("id") Long id);
```

**Metoda 2: @EntityGraph**:
```java
@EntityGraph(attributePaths = {"servicePlan", "vaultItems"})
@Query("SELECT u FROM User u WHERE u.id = :id")
Optional<User> findByIdWithRelations(@Param("id") Long id);
```

**Comportament**:
- Relațiile se încarcă ÎN ACELAȘI query
- Un singur query SQL cu JOIN
- Evită problema N+1

**Exemplu**:
```java
// Un singur query: SELECT u.*, sp.* FROM users u JOIN service_plans sp ON ...
User user = userRepository.findByIdWithEagerLoading(1L).orElse(null);

// NU generează query separat - deja încărcat
ServicePlan plan = user.getServicePlan(); // Deja în memorie!
```

#### Setări pentru comutare Lazy/Eager:

**Opțiunea 1: Modificare în entitate**:
```java
// LAZY (default pentru @ManyToOne)
@ManyToOne(fetch = FetchType.LAZY)
private ServicePlan servicePlan;

// EAGER (nu recomandat - încarcă mereu)
@ManyToOne(fetch = FetchType.EAGER)
private ServicePlan servicePlan;
```

**Opțiunea 2: Query cu JOIN FETCH (recomandat)**:
```java
// Lazy by default în entity, dar eager doar când ai nevoie
@Query("SELECT u FROM User u JOIN FETCH u.servicePlan WHERE u.id = :id")
Optional<User> findByIdWithPlan(@Param("id") Long id);
```

**Opțiunea 3: @EntityGraph (flexibil pentru multiple relații)**:
```java
@EntityGraph(attributePaths = {"servicePlan", "vaultItems", "auditLogs"})
Optional<User> findById(Long id);
```

#### Reguli generale:
- ✅ **Lazy by default** pe `@OneToMany` și `@ManyToMany`
- ✅ **Lazy by default** pe `@ManyToOne` (de obicei)
- ✅ **Eager doar când e nevoie** - folosește JOIN FETCH sau @EntityGraph
- ❌ **Evită FetchType.EAGER** în entitate - încarcă mereu, chiar dacă nu ai nevoie

---

## 4. Backup bazei de date

### PostgreSQL Backup:

#### Backup complet:
```bash
pg_dump -h localhost -U postgres -d password_vault -F c -f backup_password_vault_$(date +%Y%m%d_%H%M%S).dump
```

#### Backup doar schema (fără date):
```bash
pg_dump -h localhost -U postgres -d password_vault --schema-only -f schema_backup.sql
```

#### Backup doar date (fără schema):
```bash
pg_dump -h localhost -U postgres -d password_vault --data-only -f data_backup.sql
```

#### Restore:
```bash
pg_restore -h localhost -U postgres -d password_vault -c backup_password_vault_YYYYMMDD_HHMMSS.dump
```

#### Backup SQL plain:
```bash
pg_dump -h localhost -U postgres -d password_vault -f backup.sql
```

#### Restore SQL plain:
```bash
psql -h localhost -U postgres -d password_vault < backup.sql
```

---

## 5. Structura proiectului

```
src/main/java/com/ppaw/passwordvault/
├── model/                          # Entități (@Entity)
│   ├── User.java
│   ├── ServicePlan.java
│   ├── PlanLimits.java
│   ├── VaultItem.java
│   ├── PasswordHistory.java
│   ├── SharedVaultItem.java
│   └── AuditLog.java              # Model nou adăugat
├── repository/                     # Repository interfaces (JPA)
│   ├── UserRepository.java
│   ├── ServicePlanRepository.java
│   ├── VaultItemRepository.java
│   ├── PlanLimitsRepository.java
│   └── AuditLogRepository.java
├── config/                         # Configurații
│   ├── DataInitializer.java       # Seed data (alternativ la Flyway)
│   └── SchemaInitializer.java
├── ConsoleDataDisplayRunner.java   # Exercițiul 4 - afișare date
├── LazyEagerLoadingDemo.java       # Exercițiul 7 - demo lazy/eager
└── PasswordVaultApplication.java   # Main application

src/main/resources/
├── application.properties          # Configurație DB și Flyway
└── db/migration/                   # Migrații Flyway
    ├── V1__20241201_create_schema.sql
    ├── V2__20241201_create_service_plans.sql
    ├── V3__20241201_create_plan_limits.sql
    ├── V4__20241201_create_users.sql
    ├── V5__20241201_create_vault_items.sql
    ├── V6__20241201_create_password_history.sql
    ├── V7__20241201_create_shared_vault_items.sql
    ├── V8__20241201_create_indexes.sql
    ├── V9__20241201_seed_service_plans.sql
    └── V10__20241201_modify_entities.sql  # Modificări entități
```

---

## 6. Rulare aplicație

### Pornire aplicație:
```bash
# Maven
mvn spring-boot:run

# Sau cu Docker Compose
docker-compose up
```

### Verificare migrații:
Aplicația va afișa în consolă:
```
Flyway Community Edition ... successfully applied 10 migrations
```

### Verificare date:
La pornire, `ConsoleDataDisplayRunner` va afișa:
- Service Plans din baza de date
- Users din baza de date
- Vault Items din baza de date

### Demo Lazy/Eager:
`LazyEagerLoadingDemo` va demonstra diferențele între lazy și eager loading.

---

## 7. Echivalențe Entity Framework ↔ Spring Boot

| Entity Framework | Spring Boot |
|-----------------|-------------|
| `Enable-Migrations` | `spring.flyway.enabled=true` în `application.properties` |
| `Add-Migration nume` | Creare fișier `V{versiune}__{descriere}.sql` în `db/migration/` |
| `Update-Database` | Pornire aplicație (Flyway rulează automat) |
| `Configuration.Seed()` | Migrație SQL `V9__seed_data.sql` sau `CommandLineRunner` |
| `DBContext` | `JpaRepository` + `EntityManager` |
| `DbSet<T>` | `JpaRepository<T, ID>` |
| `Include()` pentru eager | `JOIN FETCH` sau `@EntityGraph` |
| `LazyLoadingEnabled` | `fetch = FetchType.LAZY` (implicit) |

---

## Note importante

1. **Migrații Flyway**: Ordinea este importantă - versiunea din nume (`V1`, `V2`, etc.) trebuie să fie crescătoare
2. **Seed Data**: Ordinea inserării este critică - mai întâi părinții (fără FK), apoi copiii (cu FK)
3. **Lazy Loading**: Necesită sesiune activă (`@Transactional`) pentru a funcționa corect
4. **Eager Loading**: Folosește `JOIN FETCH` sau `@EntityGraph` pentru a evita problema N+1
5. **Backup**: Face backup regulat înainte de aplicarea migrațiilor importante

---

## Contact și referințe

- **Spring Data JPA**: https://spring.io/projects/spring-data-jpa
- **Flyway**: https://flywaydb.org/documentation/
- **Hibernate**: https://hibernate.org/orm/documentation/

