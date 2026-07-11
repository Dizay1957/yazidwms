# YazidWMS Architecture

YazidWMS is a modular monolith. It is deployed as one Spring Boot API and one React frontend, but the backend code is split by business capability: authentication, users, products, inventory, purchasing, sales, warehouse topology, reporting, audit, and shared infrastructure.

This design keeps deployment simple while still making the codebase maintainable. It avoids distributed-system complexity until the product has a real reason to split services.

## System Overview

```mermaid
flowchart TD
    U[User] --> F[React Frontend]
    F -->|HTTP /api/v1| N[Nginx Proxy in Frontend Container]
    N --> API[Spring Boot API]
    API --> SEC[Spring Security + JWT]
    API --> CTRL[REST Controllers]
    CTRL --> SVC[Transactional Services]
    SVC --> REPO[Spring Data JPA Repositories]
    REPO --> DB[(PostgreSQL)]
    API --> FLY[Flyway Migrations]
    API --> ACT[Actuator]
    API --> DOC[Swagger / OpenAPI]
    API --> MAIL[Spring Mail]
    MAIL --> HOG[MailHog]
```

## Backend Layers

- Controllers expose REST endpoints under `/api/v1`.
- Services hold business rules and transaction boundaries.
- Repositories use Spring Data JPA for persistence.
- Entities map the relational model.
- DTOs define request and response contracts.
- MapStruct mappers translate between entities and DTOs.
- Security filters handle rate limiting and JWT authentication.
- Exception handling produces consistent API errors.

## Frontend Architecture

The frontend is a Vite React application. React Router defines public and protected routes, Axios sends API requests to `/api/v1`, React Query manages server state, Material UI provides the component system, and MUI Data Grid powers management tables. Authentication state is stored by `tokenStore`, with access tokens in `sessionStorage` and refresh tokens in `localStorage`.

## Database Responsibilities

PostgreSQL stores users, roles, refresh tokens, activation tokens, password reset tokens, products, categories, suppliers, customers, warehouses, storage locations, inventory, stock movements, purchase orders, sales orders, audit events, and notification logs. Flyway owns schema creation through `src/main/resources/db/migration`.

## Inventory Transaction Flow

```mermaid
flowchart TD
    R[Inventory Request] --> V[Validate product and bin]
    V --> Q[Load or create inventory row]
    Q --> C{Enough stock when decreasing?}
    C -->|No| E[Reject with BusinessException]
    C -->|Yes| I[Update bin inventory]
    I --> P[Update product quantity]
    P --> M[Persist stock movement]
    M --> A[Write audit event for manual operations]
    A --> D[Evict dashboard cache]
```

Manual adjustments and transfers are handled by `InventoryService`. Purchase-order receiving and sales-order shipping reuse the same inventory service so stock changes are recorded consistently.

## JWT Authentication Flow

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant API
    participant DB

    User->>Frontend: Submit email and password
    Frontend->>API: POST /api/v1/auth/login
    API->>DB: Load user and validate password hash
    API->>DB: Persist refresh token
    API-->>Frontend: Access token and refresh token
    Frontend->>API: API request with Bearer access token
    API-->>Frontend: Protected resource
    Frontend->>API: POST /api/v1/auth/refresh when access token expires
    API->>DB: Validate refresh token
    API-->>Frontend: New access token
```

## Purchase Order Receiving Flow

```mermaid
sequenceDiagram
    participant User
    participant PurchaseOrderController
    participant PurchaseOrderService
    participant InventoryService
    participant DB

    User->>PurchaseOrderController: PATCH /api/v1/purchase-orders/{id}/receive
    PurchaseOrderController->>PurchaseOrderService: receive(id)
    PurchaseOrderService->>DB: Load order with items
    PurchaseOrderService->>InventoryService: receive product quantities
    InventoryService->>DB: Increase bin inventory
    InventoryService->>DB: Increase product quantity
    InventoryService->>DB: Save IN stock movement
    PurchaseOrderService->>DB: Mark order RECEIVED
    PurchaseOrderService-->>User: Updated purchase order
```

## Sales Order Shipping Flow

```mermaid
sequenceDiagram
    participant User
    participant SalesOrderController
    participant SalesOrderService
    participant InventoryService
    participant DB

    User->>SalesOrderController: PATCH /api/v1/sales-orders/{id}/ship
    SalesOrderController->>SalesOrderService: ship(id)
    SalesOrderService->>DB: Load order with items
    SalesOrderService->>InventoryService: issue product quantities
    InventoryService->>DB: Validate bin stock
    InventoryService->>DB: Decrease bin inventory
    InventoryService->>DB: Decrease product quantity
    InventoryService->>DB: Save OUT stock movement
    SalesOrderService->>DB: Mark order SHIPPED
    SalesOrderService-->>User: Updated sales order
```

## Audit Logging

Audit events are persisted for authentication, user lifecycle, product updates, inventory adjustments, and transfers. Audit data is exposed through `/api/v1/audit` for users with `ADMIN` or `MANAGER` roles.

## Docker Environment

Docker Compose starts:

- `postgres` on host port `5434`
- `api` on host port `8081`
- `frontend` on host port `5173`
- `pgadmin` on host port `5050`
- `mailhog` on host ports `1025` and `8025`
