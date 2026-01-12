# API Testing Guide

## Quick Start

### 1. Pornește aplicația
```bash
mvn spring-boot:run
```

### 2. Accesează Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### 3. Testează endpoint-urile

---

## Testing Flow Recomandat

### Pasul 1: Verifică Health
```bash
curl http://localhost:8080/api/health
```

### Pasul 2: Obține Statistici
```bash
curl http://localhost:8080/api/stats
```

### Pasul 3: Listează Planurile de Servicii
```bash
curl http://localhost:8080/api/service-plans
curl http://localhost:8080/api/service-plans?active=true
curl http://localhost:8080/api/service-plans/1/with-limits
```

### Pasul 4: Creează Utilizator
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "email": "test@example.com",
    "password": "password123",
    "servicePlanId": 1
  }'
```

Salvează `id`-ul returnat pentru pașii următori (ex: `userId=1`).

### Pasul 5: Listează Utilizatorii
```bash
curl http://localhost:8080/api/users
curl http://localhost:8080/api/users/1
```

### Pasul 6: Actualizează Utilizator
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "username": "updated_user",
    "isActive": true
  }'
```

### Pasul 7: Înregistrează Login
```bash
curl -X POST "http://localhost:8080/api/users/1/login?ipAddress=192.168.1.1"
```

### Pasul 8: Creează Vault Item
```bash
curl -X POST http://localhost:8080/api/users/1/vault-items \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Gmail Account",
    "username": "test@gmail.com",
    "password": "myPassword123",
    "url": "https://gmail.com",
    "notes": "Main email account",
    "folder": "Email",
    "tags": "email,personal",
    "isFavorite": true
  }'
```

### Pasul 9: Listează Vault Items
```bash
curl http://localhost:8080/api/users/1/vault-items
curl http://localhost:8080/api/users/1/vault-items?favorite=true
curl http://localhost:8080/api/users/1/vault-items/favorites
```

### Pasul 10: Actualizează Vault Item
```bash
curl -X PUT http://localhost:8080/api/users/1/vault-items/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Gmail Account Updated",
    "isFavorite": false
  }'
```

### Pasul 11: Șterge Vault Item
```bash
curl -X DELETE http://localhost:8080/api/users/1/vault-items/1
```

### Pasul 12: Verifică Audit Logs
```bash
curl http://localhost:8080/api/audit-logs/user/1
curl http://localhost:8080/api/audit-logs/action/LOGIN
curl http://localhost:8080/api/audit-logs/action/CREATE_VAULT_ITEM
```

---

## Testare Scenarii de Eroare

### Validare Eroare - Username prea scurt
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ab",
    "email": "test@example.com",
    "password": "password123",
    "servicePlanId": 1
  }'
```

### Validare Eroare - Email invalid
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "email": "invalid-email",
    "password": "password123",
    "servicePlanId": 1
  }'
```

### Not Found - Utilizator inexistent
```bash
curl http://localhost:8080/api/users/999
```

### Business Exception - Limită depășită
Creează 21+ vault items pentru un utilizator cu plan Free (limita este 20):
```bash
# Rulează de 21 de ori:
for i in {1..21}; do
  curl -X POST http://localhost:8080/api/users/1/vault-items \
    -H "Content-Type: application/json" \
    -d "{\"title\": \"Item $i\", \"password\": \"pass123\"}"
done
```

---

## Testare cu Postman

1. Importă collection-ul (crează manual):
   - `GET /api/health`
   - `GET /api/stats`
   - `GET /api/service-plans`
   - `POST /api/users`
   - `GET /api/users`
   - `POST /api/users/{userId}/vault-items`
   - `GET /api/users/{userId}/vault-items`
   - `GET /api/audit-logs/user/{userId}`

2. Setează variabile:
   - `baseUrl`: `http://localhost:8080/api`
   - `userId`: ID-ul utilizatorului creat

---

## Testare cu Swagger UI

1. Accesează: `http://localhost:8080/swagger-ui.html`
2. Explorează endpoint-urile disponibile
3. Testează fiecare endpoint direct din interfață
4. Verifică schema-urile request/response

---

## Verificare în Baza de Date

### Conectează-te la PostgreSQL
```bash
psql -h localhost -U postgres -d password_vault
```

### Verifică datele
```sql
-- Utilizatori
SELECT * FROM vault_schema.users;

-- Vault items
SELECT * FROM vault_schema.vault_items;

-- Audit logs
SELECT * FROM vault_schema.audit_logs ORDER BY created_at DESC LIMIT 10;

-- Service plans
SELECT sp.*, pl.max_vault_items, pl.max_password_length 
FROM vault_schema.service_plans sp
LEFT JOIN vault_schema.plan_limits pl ON pl.plan_id = sp.id;
```

---

## Checklist Testare

- [ ] Health check funcționează
- [ ] Statistici se returnează corect
- [ ] Service plans se listează corect
- [ ] Utilizator nou se creează cu succes
- [ ] Validările funcționează (username, email, password)
- [ ] Utilizator se actualizează corect
- [ ] Login se înregistrează în audit logs
- [ ] Vault item se creează cu succes
- [ ] Limitările planului sunt verificate
- [ ] Vault items se listează corect
- [ ] Favorite items se filtrează corect
- [ ] Vault item se actualizează corect
- [ ] Vault item se șterge corect
- [ ] Audit logs se listează corect
- [ ] Erorile se returnează în format corect
- [ ] Not found funcționează pentru resurse inexistente

---

## Tips

1. **Folosește Swagger UI** pentru testare interactivă
2. **Verifică audit logs** pentru a vedea toate acțiunile înregistrate
3. **Testează limitările planurilor** creând item-uri până la limită
4. **Verifică validările** cu date invalide
5. **Testează filtrarea** (favorite, active, etc.)

