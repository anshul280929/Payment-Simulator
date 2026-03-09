# 💳 Mini Payment Processing System (Payment Gateway Simulator)

A microservice-based payment platform that simulates how a real payment gateway processes transactions between merchants and banks. Built with Java 21, Spring Boot 3, PostgreSQL, RabbitMQ, and React.

## 🏗️ Architecture

```
                        ┌──────────────┐
                        │   React UI   │
                        │   (:3000)    │
                        └──────┬───────┘
                               │
                        ┌──────▼───────┐
                        │  API Gateway │
                        │   (:8080)    │
                        │  JWT Filter  │
                        └──────┬───────┘
                               │
                        ┌──────▼───────┐
                        │   Eureka     │
                        │   (:8761)    │
                        └──────┬───────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
 ┌──────▼──────┐  ┌───────────▼──────────┐  ┌───────▼────────┐
 │  Merchant   │  │  Payment Gateway     │  │  Transaction   │
 │  Service    │  │  Service             │  │  Service       │
 │  (:8081)    │  │  (:8082)             │  │  (:8084)       │
 └─────────────┘  │  • Tokenization      │  │  • Lifecycle   │
                  │  • Orchestration      │  │  • Chargeback  │
                  └──┬─────────┬─────────┘  └────────────────┘
                     │         │
            ┌────────▼──┐  ┌──▼───────────┐
            │  Fraud    │  │  Issuer Bank │
            │  Detection│  │  Simulator   │
            │  (:8085)  │  │  (:8083)     │
            └───────────┘  └──────────────┘

        ┌───────────────┐    ┌────────────┐    ┌────────────┐
        │  Settlement   │    │  RabbitMQ  │    │ PostgreSQL │
        │  Service      │◄───│  (:5672)   │    │  (:5432)   │
        │  (:8086)      │    └────────────┘    └────────────┘
        └───────────────┘
```

## 🧩 Services

| Service | Port | Database | Description |
|---------|------|----------|-------------|
| Discovery Server | 8761 | — | Eureka service registry |
| API Gateway | 8080 | — | Routing, JWT auth, rate limiting |
| Auth Service | 8087 | auth_db | JWT authentication, user management |
| Merchant Service | 8081 | merchant_db | Merchant registration, API keys |
| Payment Gateway | 8082 | payment_db | Core payment orchestration, tokenization |
| Issuer Bank Simulator | 8083 | bank_db | Simulated bank auth/capture/refund |
| Transaction Service | 8084 | transaction_db | Transaction lifecycle, chargebacks |
| Fraud Detection | 8085 | fraud_db | Rule-based fraud checks |
| Settlement Service | 8086 | settlement_db | Batch settlement, reconciliation |
| Frontend | 3000 | — | React checkout simulator & dashboard |

## 💳 Payment Transaction Lifecycle

```
AUTHORIZE ──► CAPTURED ──► SETTLED
    │             │
    ▼             ▼
 DECLINED      REFUNDED
                  │
                  ▼
            CHARGEBACKED
```

1. **Authorization** — Validate request → Fraud check → Tokenize card → Bank authorization
2. **Capture** — Capture authorized funds (full or partial)
3. **Settlement** — Daily batch settlement per merchant
4. **Refund** — Refund captured payments (full or partial)
5. **Chargeback** — Customer-initiated dispute simulation

## 🔌 API Endpoints

### Authentication
```
POST /api/auth/register    — Register a new user
POST /api/auth/login       — Login and receive JWT token
```

### Payments (via API Gateway)
```
POST /api/payments/authorize         — Authorize a payment
POST /api/payments/{id}/capture      — Capture an authorized payment
POST /api/payments/{id}/refund       — Refund a captured payment
GET  /api/payments/{id}/status       — Get payment status
```

### Transactions
```
GET  /api/transactions/{id}          — Get transaction details
GET  /api/transactions               — Search/filter transactions
POST /api/transactions/{id}/chargeback — Initiate chargeback
```

### Merchants
```
POST /api/merchants                  — Register merchant
GET  /api/merchants/{id}             — Get merchant details
POST /api/merchants/{id}/api-key     — Regenerate API key
```

### Settlements
```
POST /api/settlements/batch                    — Trigger batch settlement
GET  /api/settlements/batches                  — List settlement batches
GET  /api/settlements/batches/{id}             — Batch details
GET  /api/settlements/reconciliation/{batchId} — Reconciliation report
```

## 🧪 Test Card Numbers

| Card Number | Behavior |
|-------------|----------|
| 4111 1111 1111 1234 | ✅ Always approved |
| 4111 1111 1111 0000 | ❌ Declined — Insufficient funds |
| 4111 1111 1111 1111 | ❌ Declined — Card expired |
| 4111 1111 1111 9999 | ⏱️ Timeout simulation |
| Any other valid card | 95% approval rate |

## 🔒 Security Features (PCI DSS Aligned)

- **Card Tokenization** — Card numbers are replaced with UUID tokens; raw card data is never stored or returned
- **AES-256 Encryption** — Sensitive card data encrypted at rest
- **Card Masking** — All API responses show `**** **** **** 1234`
- **JWT Authentication** — Role-based access control (ADMIN, MERCHANT, SYSTEM)
- **Audit Logging** — AOP-based audit trail for all sensitive operations
- **Never log full card numbers** — PAN data excluded from all log output

### PCI DSS Compliance Alignment

| PCI DSS Requirement | Implementation |
|---------------------|----------------|
| Req 3: Protect stored cardholder data | AES-256 encryption, tokenization |
| Req 4: Encrypt transmission | HTTPS (TLS), encrypted payloads |
| Req 6: Secure systems and applications | Input validation, parameterized queries |
| Req 7: Restrict access | Role-based access control via JWT |
| Req 8: Identify and authenticate access | JWT authentication, BCrypt passwords |
| Req 10: Track and monitor access | Audit logging on all operations |

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 21 (for local development)
- Node.js 18+ (for frontend development)
- Maven 3.9+

### Run with Docker Compose
```bash
docker-compose up --build
```

All services will start with proper dependency ordering. Access:
- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)

### Local Development
```bash
# Build common library
cd common-lib && mvn clean install

# Start each service individually
cd <service-dir> && mvn spring-boot:run
```

## 🛠️ Tech Stack

- **Backend**: Java 21, Spring Boot 3.2, Spring Cloud 2023.0
- **Database**: PostgreSQL 16 (separate DB per service)
- **Messaging**: RabbitMQ 3 (settlement events)
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Frontend**: React 18, TypeScript, Vite, Tailwind CSS
- **Security**: JWT, AES-256, BCrypt
- **DevOps**: Docker, Docker Compose
