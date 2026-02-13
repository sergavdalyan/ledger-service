# Ledger Service

A double-entry bookkeeping microservice built with Spring Boot. 
It enforces the fundamental accounting invariant — every transaction must have equal debits and credits — and computes account balances on the fly from the journal entries.

## Architecture

### Layered Architecture

```
API Layer (Controllers, DTOs, Mappers)
        ↓
Service Layer (Business orchestration)
        ↓
Domain Layer
        ↓
Persistence Layer (JPA Entities, Repositories, Entity Mappers)
        ↓
PostgreSQL (Flyway-managed schema)
```

### Tech Stack

Java 17, Spring Boot 3.2.5, PostgreSQL 16, Flyway, Spring Data JPA, Testcontainers, Docker

## How to Build and Run

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose

### Run with Docker Compose

```bash
docker compose up --build
```

This starts PostgreSQL and the application. The API is available at `http://localhost:8080`.

### Run locally (with Docker for PostgreSQL only)

```bash
# Start PostgreSQL
docker compose up -d postgres

# Build and run the application
mvn spring-boot:run
```

### Swagger UI

Once running, open `http://localhost:8080/swagger-ui/index.html` to explore and test the API.

## How to Run Tests

```bash
# Unit + integration tests (requires Docker for Testcontainers)
mvn test
```

- **Unit tests** use Mockito to test service-layer logic in isolation.
- **Integration tests** use Testcontainers to spin up a real PostgreSQL instance and test the full stack via MockMvc.

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/accounts` | Create an account |
| `GET` | `/api/accounts` | List accounts (paginated) |
| `GET` | `/api/accounts/{id}` | Get account with computed balance |
| `GET` | `/api/accounts/{id}/transactions` | List account transactions (paginated) |
| `POST` | `/api/transactions` | Create a balanced transaction |
| `GET` | `/api/transactions/{id}` | Get transaction by ID |


## Assumptions and Limitations

- Single currency only
- No authentication — assumes an API gateway handles security
- No soft deletes; corrections via compensating transactions
- Balances computed per request, no caching

## What I Would Improve with More Time

- Balance caching (Redis)
- Audit trail and optimistic locking
- Multi-currency support
- Authentication
- Observability (metrics, tracing)
- Date range filtering for transactions

## Sample API Requests

### 1. Create accounts

```bash
# Create an Asset account
curl -s -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"name": "Cash", "type": "ASSET"}'

# Create a Revenue account
curl -s -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"name": "Sales Revenue", "type": "REVENUE"}'

# Create an Expense account
curl -s -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"name": "Rent Expense", "type": "EXPENSE"}'
```

### 2. Create a balanced transaction (debit = credit)

```bash
curl -s -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Cash sale of goods",
    "date": "2025-01-15T10:00:00",
    "entries": [
      {"accountId": 1, "type": "DEBIT", "amount": 500.00},
      {"accountId": 2, "type": "CREDIT", "amount": 500.00}
    ]
  }'
```

### 3. Reject an unbalanced transaction

```bash
curl -s -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Bad transaction",
    "date": "2025-01-15T10:00:00",
    "entries": [
      {"accountId": 1, "type": "DEBIT", "amount": 500.00},
      {"accountId": 2, "type": "CREDIT", "amount": 300.00}
    ]
  }'
# Returns 400: Total debits (500.0000) do not equal total credits (300.0000)
```

### 4. Verify balance after multiple transactions

```bash
# Second transaction: pay rent
curl -s -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Monthly rent payment",
    "date": "2025-01-20T10:00:00",
    "entries": [
      {"accountId": 3, "type": "DEBIT", "amount": 200.00},
      {"accountId": 1, "type": "CREDIT", "amount": 200.00}
    ]
  }'

# Check Cash balance: 500 (debit) - 200 (credit) = 300
curl -s http://localhost:8080/api/accounts/1
# Response includes "balance": 300.0000
```

### 5. Error: reference a non-existent account

```bash
curl -s -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Ghost account",
    "date": "2025-01-15T10:00:00",
    "entries": [
      {"accountId": 999, "type": "DEBIT", "amount": 100.00},
      {"accountId": 1, "type": "CREDIT", "amount": 100.00}
    ]
  }'
# Returns 404: Account not found with id: 999
```
