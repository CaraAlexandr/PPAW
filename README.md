# Aplicație Password Vault

Aplicație Spring Boot pentru gestionarea parolelor cu planuri de monetizare (Free, Usual, Premium).

## Fișier Principal

Fișierul principal al aplicației este:
- **`src/main/java/com/ppaw/passwordvault/PasswordVaultApplication.java`** - Punctul de intrare al aplicației Spring Boot

## Pornirea Aplicației

Pentru a porni aplicația, rulează următoarea comandă în directorul curent (unde se află fișierul `docker-compose.yml`):

```bash
docker compose up
```

Această comandă va porni:
1. Baza de date PostgreSQL pe portul `5432`
2. Aplicația Spring Boot pe portul `8080`
3. Portul de debug Java pe `5005`

**Servicii disponibile:**
- Aplicație: `http://localhost:8080`
- Port Debug: `localhost:5005`
- Baza de date: `localhost:5432`

**Pentru a opri aplicația:**
```bash
docker compose down
```

## API-uri Disponibile

### 1. Autentificare (`/api/auth`)

- **POST `/api/auth/login`**
  - Autentifică un utilizator în sistem
  - Primește: username și parolă
  - Returnează: token de autentificare și informații despre utilizator

### 2. Utilizatori (`/api/users`)

- **GET `/api/users`**
  - Obține lista tuturor utilizatorilor

- **GET `/api/users/{id}`**
  - Obține detalii despre un utilizator specific

- **POST `/api/users`**
  - Creează un nou utilizator în sistem

- **PUT `/api/users/{id}`**
  - Actualizează informațiile unui utilizator existent

- **DELETE `/api/users/{id}`**
  - Șterge un utilizator din sistem

### 3. Elemente Seif (`/api/users/{userId}/vault-items`)

- **GET `/api/users/{userId}/vault-items`**
  - Obține toate elementele din seif pentru un utilizator
  - Parametru opțional: `favorite=true` pentru a obține doar elementele favorite

- **GET `/api/users/{userId}/vault-items/{id}`**
  - Obține un element specific din seif

- **POST `/api/users/{userId}/vault-items`**
  - Creează un nou element în seif (parolă criptată)

- **PUT `/api/users/{userId}/vault-items/{id}`**
  - Actualizează un element din seif

- **DELETE `/api/users/{userId}/vault-items/{id}`**
  - Șterge un element din seif

- **GET `/api/users/{userId}/vault-items/favorites`**
  - Obține doar elementele marcate ca favorite

### 4. Planuri de Serviciu (`/api/service-plans`)

- **GET `/api/service-plans`**
  - Obține lista tuturor planurilor de serviciu
  - Parametru opțional: `active=true` pentru a obține doar planurile active

- **GET `/api/service-plans/{id}`**
  - Obține detalii despre un plan de serviciu specific

- **GET `/api/service-plans/{id}/with-limits`**
  - Obține un plan de serviciu împreună cu limitele sale (număr maxim de elemente, lungime parolă, etc.)

### 5. Statistici (`/api/stats`)

- **GET `/api/stats`**
  - Obține statistici generale despre aplicație
  - Returnează: numărul total de utilizatori, planuri, elemente din seif, log-uri de audit

### 6. Health Check (`/api/health`)

- **GET `/api/health`**
  - Verifică starea aplicației
  - Returnează: status aplicație, timestamp, nume aplicație

### 7. Log-uri de Audit (`/api/audit-logs`)

- **GET `/api/audit-logs/user/{userId}`**
  - Obține toate log-urile de audit pentru un utilizator specific

- **GET `/api/audit-logs/action/{action}`**
  - Obține log-urile de audit pentru o acțiune specifică (ex: CREATE, UPDATE, DELETE)

- **GET `/api/audit-logs/{id}`**
  - Obține un log de audit specific

- **GET `/api/audit-logs/date-range`**
  - Obține log-urile de audit dintr-un interval de date
  - Parametri: `start` și `end` (format ISO DateTime)

### 8. Interfețe Web (MVC)

Aplicația include și interfețe web pentru administrare:

- **GET `/companies`** - Listă companii
- **GET `/companies/{id}`** - Detalii companie
- **GET `/companies/create`** - Formular creare companie
- **POST `/companies/create`** - Salvare companie nouă
- **GET `/companies/{id}/edit`** - Formular editare companie
- **POST `/companies/{id}/edit`** - Salvare modificări companie
- **POST `/companies/{id}/delete`** - Ștergere companie

- **GET `/users`** - Listă utilizatori (admin)
- **GET `/users/{id}`** - Detalii utilizator
- **GET `/users/create`** - Formular creare utilizator
- **POST `/users/create`** - Salvare utilizator nou
- **GET `/users/{id}/edit`** - Formular editare utilizator
- **POST `/users/{id}/edit`** - Salvare modificări utilizator
- **POST `/users/{id}/delete`** - Ștergere utilizator

- **GET `/employees`** - Listă angajați
- **GET `/employees/create`** - Formular creare angajat
- **POST `/employees/create`** - Salvare angajat nou

## Funcționalități

- **Planuri de Serviciu**: Planuri Free, Usual și Premium cu limitări diferite
- **Seif Parole**: Stocare securizată a parolelor criptate
- **Generator Parole**: Generare parole securizate bazată pe limitările planului
- **Migrări Baze de Date**: Flyway pentru schema bazei de date controlată prin versiuni

## Tehnologii

- Java 21
- Spring Boot 3.2.0
- PostgreSQL 16
- Flyway (Migrări baze de date)
- JPA/Hibernate

## Cerințe

- Java 21 sau mai nou
- Maven 3.6+
- Docker și Docker Compose

## Documentație API

Documentația completă a API-urilor este disponibilă la:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/api-docs`

## Structură Proiect

```
.
├── src/
│   ├── main/
│   │   ├── java/com/ppaw/passwordvault/
│   │   │   ├── PasswordVaultApplication.java  (Fișier principal)
│   │   │   ├── controller/                    (API REST)
│   │   │   ├── controller/mvc/                (Controlere MVC pentru interfețe web)
│   │   │   ├── service/                       (Logica de business)
│   │   │   ├── repository/                    (Acces la bază de date)
│   │   │   ├── model/                         (Entități JPA)
│   │   │   └── dto/                           (Obiecte de transfer)
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/                     (Template-uri Thymeleaf)
├── docker-compose.yml
├── Dockerfile
├── Dockerfile.dev
└── pom.xml
```
