# API Documentation - Password Vault

## Base URL
```
http://localhost:8080/api
```

## Swagger UI
```
http://localhost:8080/swagger-ui.html
```

---

## Endpoints

### Authentication

#### POST `/api/auth/login`
Autentificare utilizator cu username și parolă.

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "userId": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "servicePlanId": 1,
    "servicePlanName": "Free",
    "lastLoginAt": "2024-12-01T10:00:00",
    "loginCount": 5,
    "success": true
  }
}
```

**Errors:**
- `400`: Invalid username or password
- `400`: User account is inactive

---

### Health Check

#### GET `/api/health`
Verifică statusul aplicației.

**Response:**
```json
{
  "success": true,
  "message": "Application is healthy",
  "data": {
    "status": "UP",
    "timestamp": "2024-12-01T10:00:00",
    "application": "Password Vault API"
  }
}
```

---

### Statistics

#### GET `/api/stats`
Returnează statistici generale.

**Response:**
```json
{
  "success": true,
  "message": "Statistics retrieved successfully",
  "data": {
    "totalUsers": 10,
    "totalServicePlans": 3,
    "totalVaultItems": 50,
    "totalAuditLogs": 200,
    "activeUsers": 10
  }
}
```

---

### Service Plans

#### GET `/api/service-plans`
Listează toate planurile de servicii.

**Query Parameters:**
- `active` (optional): `true` pentru doar planurile active

**Response:**
```json
{
  "success": true,
  "message": "Service plans retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Free",
      "price": 0.00,
      "currency": "USD",
      "isActive": true,
      "limits": {
        "maxVaultItems": 20,
        "maxPasswordLength": 16,
        "canExport": false,
        "canShare": false
      }
    }
  ]
}
```

#### GET `/api/service-plans/{id}`
Returnează un plan de servicii după ID.

#### GET `/api/service-plans/{id}/with-limits`
Returnează un plan de servicii cu limitările asociate.

---

### Users

#### GET `/api/users`
Listează toți utilizatorii.

**Response:**
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": [
    {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "servicePlanId": 1,
      "servicePlanName": "Free",
      "isActive": true,
      "lastLoginAt": "2024-12-01T10:00:00",
      "loginCount": 5
    }
  ]
}
```

#### GET `/api/users/{id}`
Returnează un utilizator după ID.

#### POST `/api/users`
Creează un utilizator nou.

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "servicePlanId": 1
}
```

**Validation:**
- `username`: required, 3-100 characters
- `email`: required, valid email format
- `password`: required, min 8 characters
- `servicePlanId`: required

#### PUT `/api/users/{id}`
Actualizează un utilizator.

**Request Body:**
```json
{
  "username": "john_updated",
  "email": "john_updated@example.com",
  "isActive": true,
  "servicePlanId": 2
}
```

#### DELETE `/api/users/{id}`
Șterge un utilizator.

---

### Vault Items

#### GET `/api/users/{userId}/vault-items`
Listează toate item-urile din vault pentru un utilizator.

**Query Parameters:**
- `favorite` (optional): `true` pentru doar item-urile favorite

**Response:**
```json
{
  "success": true,
  "message": "Vault items retrieved successfully",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "title": "Gmail Account",
      "username": "john@gmail.com",
      "url": "https://gmail.com",
      "notes": "Main email account",
      "folder": "Email",
      "tags": "email,personal",
      "isFavorite": true
    }
  ]
}
```

#### GET `/api/users/{userId}/vault-items/{id}`
Returnează un item din vault după ID.

#### POST `/api/users/{userId}/vault-items`
Creează un item nou în vault.

**Request Body:**
```json
{
  "title": "Gmail Account",
  "username": "john@gmail.com",
  "password": "myPassword123",
  "url": "https://gmail.com",
  "notes": "Main email account",
  "folder": "Email",
  "tags": "email,personal",
  "isFavorite": true
}
```

**Validation:**
- `title`: required, max 255 characters
- `password`: required
- `username`: max 255 characters
- `url`: max 500 characters
- `folder`: max 100 characters

**Business Rules:**
- Verifică dacă utilizatorul a atins limita de item-uri pentru planul său
- Criptează parola înainte de stocare

#### PUT `/api/users/{userId}/vault-items/{id}`
Actualizează un item din vault.

**Request Body:**
```json
{
  "title": "Gmail Account Updated",
  "password": "newPassword123",
  "isFavorite": false
}
```

#### DELETE `/api/users/{userId}/vault-items/{id}`
Șterge un item din vault.

#### GET `/api/users/{userId}/vault-items/favorites`
Listează doar item-urile favorite.

---

### Audit Logs

#### GET `/api/audit-logs/user/{userId}`
Listează log-urile de audit pentru un utilizator.

**Response:**
```json
{
  "success": true,
  "message": "Audit logs retrieved successfully",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "username": "john_doe",
      "action": "LOGIN",
      "description": "User logged in",
      "ipAddress": "192.168.1.1",
      "createdAt": "2024-12-01T10:00:00"
    }
  ]
}
```

#### GET `/api/audit-logs/action/{action}`
Listează log-urile de audit pentru o acțiune specifică.

**Actions:**
- `LOGIN`
- `LOGOUT`
- `USER_CREATED`
- `USER_UPDATED`
- `USER_DELETED`
- `CREATE_VAULT_ITEM`
- `UPDATE_VAULT_ITEM`
- `DELETE_VAULT_ITEM`

#### GET `/api/audit-logs/{id}`
Returnează un log de audit după ID.

#### GET `/api/audit-logs/date-range`
Listează log-urile de audit dintr-un interval de date.

**Query Parameters:**
- `start`: ISO date-time (required)
- `end`: ISO date-time (required)

**Example:**
```
GET /api/audit-logs/date-range?start=2024-12-01T00:00:00&end=2024-12-01T23:59:59
```

---

## Error Responses

### Validation Error (400)
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "username": "Username must be between 3 and 100 characters",
    "email": "Email must be valid"
  },
  "timestamp": "2024-12-01T10:00:00"
}
```

### Not Found (404)
```json
{
  "success": false,
  "message": "User with id 999 not found",
  "timestamp": "2024-12-01T10:00:00"
}
```

### Business Exception (400)
```json
{
  "success": false,
  "message": "Maximum vault items limit reached for your plan",
  "timestamp": "2024-12-01T10:00:00"
}
```

### Server Error (500)
```json
{
  "success": false,
  "message": "An unexpected error occurred: ...",
  "timestamp": "2024-12-01T10:00:00"
}
```

---

## Testing Examples

### cURL Examples

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "password": "password123"
  }'
```

#### Create User
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

#### Get All Service Plans
```bash
curl http://localhost:8080/api/service-plans
```

#### Create Vault Item
```bash
curl -X POST http://localhost:8080/api/users/1/vault-items \
  -H "Content-Type: application/json" \
  -d '{
    "title": "GitHub",
    "username": "testuser",
    "password": "mypassword123",
    "url": "https://github.com",
    "folder": "Development"
  }'
```

#### Get Audit Logs for User
```bash
curl http://localhost:8080/api/audit-logs/user/1
```

---

## Notes

- Toate endpoint-urile returnează răspunsuri în format JSON
- Validările sunt efectuate automat folosind Bean Validation
- Toate operațiunile de modificare sunt loggate în audit logs
- Parolele sunt hash-uite înainte de stocare (SHA-256 în demo, în producție folosește bcrypt/argon2)
- Limitările planurilor sunt verificate la crearea item-urilor în vault

