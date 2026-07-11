# YazidWMS

YazidWMS is a full-stack warehouse management system built with Spring Boot, React, and PostgreSQL. It is designed as an enterprise-style portfolio project that demonstrates authentication, role-based access control, inventory operations, purchasing, sales, reporting, and a modern dashboard UI.

## Overview

The project models a realistic warehouse workflow:

- manage products, categories, suppliers, and customers
- organize warehouses into zones, aisles, shelves, and bins
- track inventory and stock movements
- create and receive purchase orders
- create and ship sales orders
- view KPIs, reports, and audit-friendly operational history

The backend exposes a REST API under `/api/v1`, and the frontend consumes the real API rather than mock data.

## Tech Stack

- Backend: Java 21, Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Maven
- Database: PostgreSQL
- Frontend: React, TypeScript, Vite, Material UI, MUI Data Grid, React Router, React Query, Axios, Recharts
- Infrastructure: Docker, Docker Compose, Nginx

## Features

- JWT authentication with refresh tokens
- Protected routes and role-based UI permissions
- Dashboard KPIs and charts
- Product, category, supplier, and customer management
- Warehouse topology management
- Inventory tracking and stock movement history
- Purchase order and sales order workflows
- Reporting endpoints for inventory, low stock, purchases, sales, and stock movements
- User management and settings
- Responsive enterprise-style interface with dark and light mode

## Running With Docker

Start the full stack:

```bash
docker compose up --build
```

Application URLs:

- Frontend: `http://localhost:5173`
- API: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- PostgreSQL host port: `localhost:5434`
- pgAdmin: `http://localhost:5050`
- MailHog: `http://localhost:8025`

Demo credentials:

- Email: `admin@yazidwms.local`
- Password: `Admin@12345`

These credentials are for local demonstration only.

## Local Development

Start support services:

```bash
docker compose up -d postgres mailhog pgadmin
```

Run the backend:

```bash
$env:SERVER_PORT="8081"
$env:DB_HOST="localhost"
$env:DB_PORT="5434"
$env:DB_NAME="yazidwms"
$env:DB_USERNAME="yazid"
$env:DB_PASSWORD="yazid_secret"
mvn spring-boot:run
```

Run the frontend:

```bash
cd frontend
npm install
$env:VITE_API_PROXY_TARGET="http://127.0.0.1:8081"
npm run dev
```

## Demo Flow

For a short product walkthrough:

1. Log in as admin.
2. Open the dashboard and review KPIs and recent activity.
3. Create a supplier, category, and product.
4. Create a warehouse structure and inspect storage locations.
5. Create and receive a purchase order to increase stock.
6. Create a customer and sales order, then ship it to reduce stock.
7. Review stock movement history and reports.

## API

All endpoints are available under:

```text
/api/v1
```

Example login request:

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@yazidwms.local",
  "password": "Admin@12345"
}
```

## Build And Test

Backend tests:

```bash
mvn test
```

Frontend production build:

```bash
cd frontend
npm run build
```

Docker configuration check:

```bash
docker compose config
```

## Security Notes

- No production secrets are stored in this repository.
- `.env` files are ignored.
- `.env.example` contains placeholders only.
- Any real deployment should replace database passwords, JWT secrets, admin credentials, and SMTP settings.

## Notes

- Runtime is PostgreSQL-first.
- Flyway manages schema migrations.
- H2 is used only in tests.
- Demo business data is disabled by default; roles and the admin user are seeded for first login.
