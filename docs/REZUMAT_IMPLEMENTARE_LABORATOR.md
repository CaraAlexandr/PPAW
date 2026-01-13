# Rezumat Implementare - Laborator ORM Code First

## ✅ Cerințe implementate

### 1. ORM - Object Relational Mapping – Code First

#### ✅ Exercițiul 1: Utilizarea conceptului "Code First"
- **Modele definite** (`src/main/java/com/ppaw/passwordvault/model/`):
  - `User.java` - utilizatorii aplicației
  - `ServicePlan.java` - planurile de servicii
  - `PlanLimits.java` - limitările planurilor
  - `VaultItem.java` - item-urile din vault
  - `PasswordHistory.java` - istoricul parolelor
  - `SharedVaultItem.java` - item-uri partajate
  - `AuditLog.java` - **NOU** - loguri de auditare

- **Repository interfaces** (`src/main/java/com/ppaw/passwordvault/repository/`):
  - `UserRepository.java`
  - `ServicePlanRepository.java`
  - `VaultItemRepository.java`
  - `PlanLimitsRepository.java`
  - `AuditLogRepository.java`

- **Configurare DB** (`application.properties`):
  - Connection string PostgreSQL
  - Configurare Flyway pentru migrații
  - Configurare JPA/Hibernate

#### ✅ Exercițiul 2: Generarea migrărilor și crearea tabelelor

**Migrații Flyway create** (`src/main/resources/db/migration/`):

1. `V1__20241201_create_schema.sql` - Creare schema `vault_schema`
2. `V2__20241201_create_service_plans.sql` - Creare tabela `service_plans`
3. `V3__20241201_create_plan_limits.sql` - Creare tabela `plan_limits`
4. `V4__20241201_create_users.sql` - Creare tabela `users`
5. `V5__20241201_create_vault_items.sql` - Creare tabela `vault_items`
6. `V6__20241201_create_password_history.sql` - Creare tabela `password_history`
7. `V7__20241201_create_shared_vault_items.sql` - Creare tabela `shared_vault_items`
8. `V8__20241201_create_indexes.sql` - Creare indexuri
9. `V9__20241201_seed_service_plans.sql` - **Seed data** în 2 tabele

**Configurare Flyway**:
- `spring.flyway.enabled=true` - activare migrații
- `spring.jpa.hibernate.ddl-auto=validate` - validare schema (nu mai creează automat)

#### ✅ Exercițiul 3: Seed data în 2 tabele

**Implementare**: `V9__20241201_seed_service_plans.sql`

**Date inserate**:
1. **Tabela `service_plans`** (părinte):
   - Free (0.00 USD)
   - Usual (4.99 USD)
   - Premium (9.99 USD)

2. **Tabela `plan_limits`** (copil cu FK):
   - Limitări pentru fiecare plan (20/200/2000 items, etc.)

**Ordinea inserării**: Părinte apoi copil (evită probleme FK)

#### ✅ Exercițiul 4: Console Application pentru afișare date

**Implementare**: `ConsoleDataDisplayRunner.java`

- Implementează `CommandLineRunner`
- Afișează date din tabele: `service_plans`, `users`, `vault_items`
- Rulează automat la pornirea aplicației

---

### 2. ORM – Code First – Efectuarea modificărilor

#### ✅ Exercițiul 5: Modificări modele și update DB

**Modificări efectuate**:

1. **Adăugare 2 proprietăți la `User`**:
   - `lastLoginAt` (LocalDateTime) - data ultimei autentificări
   - `loginCount` (Integer) - numărul de autentificări

2. **Modificare tip date în `ServicePlan`**:
   - `currency`: de la `VARCHAR(3)` la `VARCHAR(10)`

3. **Model nou**: `AuditLog`
   - Tabela pentru înregistrarea acțiunilor utilizatorilor
   - Relație `@ManyToOne` cu `User`

**Migrație pentru modificări**: `V10__20241201_modify_entities.sql`
- `ALTER TABLE users ADD COLUMN ...` - adăugare coloane noi
- `ALTER TABLE service_plans ALTER COLUMN currency TYPE ...` - modificare tip
- `CREATE TABLE audit_logs ...` - creare tabel nou

#### ✅ Exercițiul 6: Actualizare cod pentru noile proprietăți

- Repository actualizat: `AuditLogRepository.java`
- Relații actualizate în `User.java`: `@OneToMany` cu `AuditLog`

---

### 3. ORM – Lazy loading vs Eager loading

#### ✅ Exercițiul 7: Testare Lazy loading vs Eager loading

**Implementare**: `LazyEagerLoadingDemo.java`

**Demo-uri incluse**:

1. **Lazy Loading (implicit)**:
   - Demonstrează că relația `servicePlan` se încarcă doar când e accesată
   - Generează query SQL separat (potențială problemă N+1)

2. **Eager Loading cu JOIN FETCH**:
   - `@Query` cu `JOIN FETCH` pentru încărcare într-un singur query
   - Evită problema N+1

3. **Eager Loading cu @EntityGraph**:
   - `@EntityGraph` pentru încărcare flexibilă a multiple relații
   - Poate încărca `servicePlan` + `vaultItems` simultan

4. **Lazy Loading cu VaultItem**:
   - Demonstrează lazy loading pe relația inversă `VaultItem -> User`

**Repository methods pentru eager loading**:
- `UserRepository.findByIdWithEagerLoading()` - JOIN FETCH
- `UserRepository.findByIdWithRelations()` - @EntityGraph
- `ServicePlanRepository.findByIdWithLimits()` - JOIN FETCH

---

## 📁 Structura fișierelor create/modificate

### Fișiere noi create:

#### Migrații Flyway:
- `src/main/resources/db/migration/V1__20241201_create_schema.sql`
- `src/main/resources/db/migration/V2__20241201_create_service_plans.sql`
- `src/main/resources/db/migration/V3__20241201_create_plan_limits.sql`
- `src/main/resources/db/migration/V4__20241201_create_users.sql`
- `src/main/resources/db/migration/V5__20241201_create_vault_items.sql`
- `src/main/resources/db/migration/V6__20241201_create_password_history.sql`
- `src/main/resources/db/migration/V7__20241201_create_shared_vault_items.sql`
- `src/main/resources/db/migration/V8__20241201_create_indexes.sql`
- `src/main/resources/db/migration/V9__20241201_seed_service_plans.sql`
- `src/main/resources/db/migration/V10__20241201_modify_entities.sql`

#### Java Classes:
- `src/main/java/com/ppaw/passwordvault/model/AuditLog.java` - **NOU MODEL**
- `src/main/java/com/ppaw/passwordvault/repository/ServicePlanRepository.java`
- `src/main/java/com/ppaw/passwordvault/repository/UserRepository.java`
- `src/main/java/com/ppaw/passwordvault/repository/VaultItemRepository.java`
- `src/main/java/com/ppaw/passwordvault/repository/PlanLimitsRepository.java`
- `src/main/java/com/ppaw/passwordvault/repository/AuditLogRepository.java`
- `src/main/java/com/ppaw/passwordvault/ConsoleDataDisplayRunner.java`
- `src/main/java/com/ppaw/passwordvault/LazyEagerLoadingDemo.java`

#### Documentație:
- `docs/LABORATOR_ORM_CODE_FIRST.md` - documentație completă
- `docs/INSTRUCTIUNI_RULARE_LABORATOR.md` - instrucțiuni de rulare
- `docs/REZUMAT_IMPLEMENTARE_LABORATOR.md` - acest fișier

### Fișiere modificate:

- `src/main/resources/application.properties` - activare Flyway, configurare migrații
- `src/main/java/com/ppaw/passwordvault/model/User.java` - adăugare 2 proprietăți noi
- `src/main/java/com/ppaw/passwordvault/model/ServicePlan.java` - modificare tip `currency`

---

## 🔧 Configurare aplicație

### application.properties (modificări):
```properties
# Flyway activat
spring.flyway.enabled=true
spring.flyway.schemas=vault_schema
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# JPA - validate (nu mai creează automat)
spring.jpa.hibernate.ddl-auto=validate
```

---

## 📊 Date seed inserate

### Tabela `service_plans`:
| ID | Name    | Price | Currency |
|----|---------|-------|----------|
| 1  | Free    | 0.00  | USD      |
| 2  | Usual   | 4.99  | USD      |
| 3  | Premium | 9.99  | USD      |

### Tabela `plan_limits`:
- Limitări corespunzătoare pentru fiecare plan (FK către `service_plans.id`)

---

## 🧪 Testare

### Rulare aplicație:
```bash
mvn spring-boot:run
```

### Verificare output:
1. Migrații Flyway aplicate
2. ConsoleDataDisplayRunner afișează date
3. LazyEagerLoadingDemo demonstrează lazy/eager loading

### Verificare în DB:
```sql
-- Verificare migrații
SELECT * FROM vault_schema.flyway_schema_history;

-- Verificare tabele
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'vault_schema';

-- Verificare date seed
SELECT * FROM vault_schema.service_plans;
SELECT * FROM vault_schema.plan_limits;
```

---

## 📦 Backup baza de date

### Comandă backup:
```bash
pg_dump -h localhost -U postgres -d password_vault -F c -f backup_password_vault_$(date +%Y%m%d_%H%M%S).dump
```

### Restore:
```bash
pg_restore -h localhost -U postgres -d password_vault -c backup_password_vault_YYYYMMDD_HHMMSS.dump
```

---

## ✅ Checklist final

- [x] Exercițiul 1: Modele și Repository definite
- [x] Exercițiul 2: Migrații Flyway create și configurate
- [x] Exercițiul 3: Seed data în 2 tabele
- [x] Exercițiul 4: Console Runner pentru afișare date
- [x] Exercițiul 5: Modificări modele (2 proprietăți, tip date, model nou)
- [x] Exercițiul 6: Cod actualizat pentru noile proprietăți
- [x] Exercițiul 7: Demo Lazy vs Eager Loading
- [x] Documentație completă
- [x] Instrucțiuni de rulare
- [x] Backup DB (instrucțiuni)

---

## 🎯 Fișiere pentru temă

1. ✅ **Codul scris pentru modelarea entităților**: `src/main/java/com/ppaw/passwordvault/model/*.java`
2. ✅ **Migrările create**: `src/main/resources/db/migration/V*.sql`
3. ✅ **Fișier cu pașii și setări**: `docs/LABORATOR_ORM_CODE_FIRST.md`
4. ✅ **Backup bazei de date**: vezi instrucțiuni în `docs/INSTRUCTIUNI_RULARE_LABORATOR.md`

---

**Data implementării**: 2024-12-01
**Tehnologii**: Spring Boot 3.2.0, JPA/Hibernate, Flyway, PostgreSQL

