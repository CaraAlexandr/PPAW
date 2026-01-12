# Password Vault Application

A Spring Boot application for password management with monetization plans (Free, Usual, Premium).

## Features

- **Service Plans**: Free, Usual, and Premium plans with different limitations
- **Password Vault**: Secure storage of encrypted passwords
- **Password Generator**: Generate secure passwords based on plan limitations
- **Database Migrations**: Flyway for version-controlled database schema

## Technology Stack

- Java 21
- Spring Boot 3.2.0
- PostgreSQL 16
- Flyway (Database migrations)
- JPA/Hibernate

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker and Docker Compose

## Getting Started

### Option 1: Docker Compose (Recommended)

The easiest way to run the entire application is using Docker Compose. This will start both PostgreSQL and the Spring Boot application:

```bash
docker-compose up --build
```

This will:
1. Start PostgreSQL database on port `5432`
2. Build and start the Spring Boot application on port `8080`
3. Enable Java debug port on `5005`

**Note:** The application is configured to build on container start using `Dockerfile.dev`, which runs:
```bash
mvn clean package -DskipTests && java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar target/*.jar
```

**Services:**
- Application: `http://localhost:8080`
- Debug Port: `localhost:5005`
- Database: `localhost:5432`

**To run in detached mode:**
```bash
docker-compose up -d --build
```

**To view logs:**
```bash
docker-compose logs -f app
```

**To stop:**
```bash
docker-compose down
```

### Option 2: Local Development

#### 1. Start PostgreSQL Database

```bash
docker-compose up -d postgres
```

This will start a PostgreSQL container on port 5432 with:
- Database: `password_vault`
- Username: `postgres`
- Password: `postgres`

#### 2. Run the Application Locally

```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Debugging

The application exposes JDWP debug port `5005` when running in Docker. To connect a debugger:

**IntelliJ IDEA:**
1. Run → Edit Configurations → Add New → Remote JVM Debug
2. Host: `localhost`, Port: `5005`
3. Debug mode: `Attach`

**VS Code:**
```json
{
  "type": "java",
  "name": "Debug Spring Boot",
  "request": "attach",
  "hostName": "localhost",
  "port": 5005
}
```

### 3. Database Schema

**Code-First Approach**: The database schema is automatically generated from JPA entities on application startup using Hibernate.

- **Schema Creation**: Hibernate will create/update tables based on `@Entity` classes
- **Initial Data**: Service plans (Free, Usual, Premium) are automatically seeded via `DataInitializer`
- **Configuration**: Set `spring.jpa.hibernate.ddl-auto=update` in `application.properties`

**Note**: Flyway migrations are disabled. Old migration files in `db/migration/` are kept for reference only.

## Database Schema

### Service Plans

The application comes pre-configured with three plans:

- **Free**: 20 vault items, max 16 char passwords, no export/import/share
- **Usual**: 200 vault items, max 32 char passwords, export enabled, 3 history versions
- **Premium**: 2000 vault items, max 64 char passwords, all features enabled

### Entity Models

- `ServicePlan`: Service plan definitions
- `PlanLimits`: Limitations per plan
- `User`: User accounts linked to plans
- `VaultItem`: Encrypted password entries
- `PasswordHistory`: Historical password versions
- `SharedVaultItem`: Shared vault items (Premium feature)

## Docker Files

- **Dockerfile**: Production-ready multi-stage build
- **Dockerfile.dev**: Development build that compiles on container start
- **docker-compose.yml**: Orchestrates PostgreSQL and Spring Boot services

## Project Structure

```
.
├── src/
│   ├── main/
│   │   ├── java/com/ppaw/passwordvault/
│   │   │   ├── PasswordVaultApplication.java
│   │   │   └── model/
│   │   │       ├── ServicePlan.java
│   │   │       ├── PlanLimits.java
│   │   │       ├── User.java
│   │   │       ├── VaultItem.java
│   │   │       ├── PasswordHistory.java
│   │   │       └── SharedVaultItem.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/
│   │           ├── V1__Create_service_plans_table.sql
│   │           ├── V2__Create_users_table.sql
│   │           ├── V3__Create_vault_items_table.sql
│   │           ├── V4__Create_password_history_table.sql
│   │           ├── V5__Create_shared_vault_items_table.sql
│   │           └── V6__Seed_initial_plans.sql
├── docs/
│   ├── database-schema.puml
│   ├── user-plan-limitation-flow.puml
│   └── ... (other PlantUML diagrams)
├── Dockerfile
├── Dockerfile.dev
├── docker-compose.yml
└── pom.xml
```

## Next Steps

- Implement service layer (PasswordGeneratorService, VaultService, PlanService)
- Create REST controllers for API endpoints
- Add authentication and authorization
- Implement password encryption/decryption logic
- Add validation and business rules

## Database Connection

The application is configured to connect to PostgreSQL. Update `src/main/resources/application.properties` if you need different connection settings.

