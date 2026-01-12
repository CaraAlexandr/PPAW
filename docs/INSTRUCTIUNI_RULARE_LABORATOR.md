# Instrucțiuni Rulare - Laborator ORM Code First

## Pregătire

1. **Baza de date PostgreSQL**:
   ```bash
   # Verifică că PostgreSQL rulează
   docker-compose up -d postgres
   
   # Sau conectează-te la o bază de date existentă
   ```

2. **Configurare connection string** în `application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/password_vault
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```

## Rulare aplicație

### Opțiunea 1: Maven
```bash
mvn clean spring-boot:run
```

### Opțiunea 2: Docker Compose
```bash
docker-compose up
```

### Opțiunea 3: Compilare și rulare JAR
```bash
mvn clean package
java -jar target/password-vault-1.0.0.jar
```

## Ce se întâmplă la pornire

1. **Flyway rulează migrațiile**:
   - Creează schema `vault_schema`
   - Creează toate tabelele
   - Creează indexurile
   - Inserează date seed (ServicePlan + PlanLimits)

2. **SchemaInitializer**:
   - Verifică că schema există

3. **DataInitializer** (dacă e necesar):
   - Verifică dacă există planuri de servicii
   - Inserează date doar dacă nu există

4. **ConsoleDataDisplayRunner**:
   - Afișează datele din tabele în consolă

5. **LazyEagerLoadingDemo**:
   - Demonstrează lazy vs eager loading

## Verificare migrații

### Verificare în baza de date:
```sql
SELECT * FROM vault_schema.flyway_schema_history;
```

### Verificare tabele create:
```sql
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'vault_schema';
```

### Verificare date seed:
```sql
SELECT * FROM vault_schema.service_plans;
SELECT * FROM vault_schema.plan_limits;
```

## Testare modificări entități

După modificarea modelelor (ex: User cu `lastLoginAt` și `loginCount`):

1. Migrația `V10__20241201_modify_entities.sql` va rula automat
2. Verifică modificările:
   ```sql
   SELECT column_name, data_type 
   FROM information_schema.columns 
   WHERE table_schema = 'vault_schema' 
   AND table_name = 'users';
   ```

## Testare Lazy/Eager Loading

1. Rulare aplicație - `LazyEagerLoadingDemo` va afișa în consolă exemple
2. Observă query-urile SQL în consolă (pentru că `spring.jpa.show-sql=true`)
3. Compară numărul de query-uri între lazy și eager

## Backup bazei de date

### Backup complet:
```bash
pg_dump -h localhost -U postgres -d password_vault -F c -f backup_$(date +%Y%m%d).dump
```

### Restore:
```bash
pg_restore -h localhost -U postgres -d password_vault -c backup_YYYYMMDD.dump
```

## Troubleshooting

### Eroare: Schema nu există
```sql
CREATE SCHEMA IF NOT EXISTS vault_schema;
```

### Eroare: Migrație deja aplicată
Flyway nu va rula din nou migrații deja aplicate. Dacă vrei să rulezi din nou:
```sql
DELETE FROM vault_schema.flyway_schema_history WHERE version = '10';
```

### Eroare: Tabele există deja
Dacă folosești `ddl-auto=update`, dezactivează-l și folosește doar Flyway:
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```

## Fișiere importante

- **Migrații**: `src/main/resources/db/migration/V*.sql`
- **Modele**: `src/main/java/com/ppaw/passwordvault/model/*.java`
- **Repository**: `src/main/java/com/ppaw/passwordvault/repository/*.java`
- **Configurație**: `src/main/resources/application.properties`
- **Documentație**: `docs/LABORATOR_ORM_CODE_FIRST.md`

