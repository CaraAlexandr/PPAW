# PlantUML Diagrams Documentation

This directory contains PlantUML (.puml) files that document the Password Vault application's database schema, system architecture, and business flows.

## Diagrams Overview

### Quick Start - Simplified Diagrams

#### `simple-flow.puml`
**Simplified flow diagram** showing the basic request flow with plan validation.

**What it shows:**
- User request → Controller → Service validation → Database
- Plan limit check (403 error if exceeded)
- Basic processing flow

#### `simple-tables.puml`
**Simplified database schema** showing only essential tables and relationships.

**What it shows:**
- 6 main tables (service_plans, plan_limits, users, vault_items, password_history, shared_vault_items)
- Key fields only
- Relationships between tables

---

### Detailed Diagrams

### 1. `database-schema.puml`
**Entity-Relationship Diagram** showing all database tables, their columns, data types, and relationships.

**What it shows:**
- All 6 database tables (service_plans, plan_limits, users, vault_items, password_history, shared_vault_items)
- Primary keys, foreign keys, and constraints
- Relationships between entities (one-to-one, one-to-many)
- Notes on encryption and plan examples

**How to view:**
```bash
# Using PlantUML CLI
plantuml database-schema.puml

# Or use VS Code extension: PlantUML
# Right-click file -> Preview PlantUML
```

### 2. `user-plan-limitation-flow.puml`
**Sequence Diagram** showing how user actions are validated against plan limitations.

**What it shows:**
- User registration/login flow
- Vault item creation with limit checking
- Password generation with length restrictions
- Export functionality validation
- Sharing feature (Premium only)

**Key flows:**
- Plan limits are checked at the Service Layer
- Validation happens before database operations
- Error responses (403 Forbidden) for exceeded limits

### 3. `plan-comparison.puml`
**Comparison Diagram** showing side-by-side feature comparison of all three plans.

**What it shows:**
- Free Plan: Basic features, 20 items limit
- Usual Plan: Enhanced features, 200 items limit
- Premium Plan: All features, 2000 items limit
- Use case notes for each plan

### 4. `plan-upgrade-flow.puml`
**Sequence Diagram** showing the plan upgrade/downgrade process.

**What it shows:**
- Viewing current plan
- Browsing available plans
- Upgrade process
- Downgrade protection (prevents data loss)

**Business rules:**
- Upgrades: Always allowed
- Downgrades: Validated against current vault item count

### 5. `password-generation-validation.puml`
**Activity Diagram** showing the complete password generation validation process.

**What it shows:**
- Request validation steps
- Plan limit checks
- Character set validation
- Password generation logic
- Error handling

### 6. `system-architecture.puml`
**Component Diagram** showing the overall system architecture and layer separation.

**What it shows:**
- Three-tier architecture:
  - **Presentation Layer**: REST Controllers
  - **Service Layer**: Business logic
  - **Data Access Layer**: Repositories
- Database tables
- Dependencies between components
- Flyway migration notes

## Viewing the Diagrams

### Option 1: VS Code Extension
1. Install "PlantUML" extension in VS Code
2. Open any `.puml` file
3. Press `Alt+D` or right-click → "Preview PlantUML"

### Option 2: PlantUML Server (Online)
1. Copy the `.puml` file content
2. Visit http://www.plantuml.com/plantuml/uml/
3. Paste and view

### Option 3: PlantUML CLI
```bash
# Install PlantUML
# Java required
java -jar plantuml.jar *.puml
```

### Option 4: IntelliJ IDEA
1. Install "PlantUML integration" plugin
2. Open `.puml` file
3. Right-click → "Preview"

## Key Concepts Documented

### Plan Limitation Enforcement
All diagrams show that plan limitations are enforced at the **Service Layer**:
- Controllers receive requests
- Service layer loads user and plan limits
- Validations occur before database operations
- Error responses prevent unauthorized actions

### Database Relationships
- `service_plans` ↔ `plan_limits`: One-to-One
- `service_plans` ↔ `users`: One-to-Many
- `users` ↔ `vault_items`: One-to-Many
- `vault_items` ↔ `password_history`: One-to-Many
- `vault_items` ↔ `shared_vault_items`: One-to-Many

### Security Considerations
- Passwords stored encrypted (encrypted_password, iv, salt)
- Plan limits prevent resource exhaustion
- Validation at multiple layers

## Updating Diagrams

When making changes to:
- **Database schema**: Update `database-schema.puml`
- **Business flows**: Update relevant flow diagrams
- **New features**: Create new flow diagrams
- **Architecture changes**: Update `system-architecture.puml`

## Integration with Documentation

These diagrams complement the main README.md and should be referenced when:
- Onboarding new developers
- Understanding business logic
- Planning new features
- Documenting API behavior

