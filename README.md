# YazidWMS

YazidWMS is a full-stack Warehouse Management System with a Spring Boot API and a React enterprise dashboard. It uses PostgreSQL at runtime, JWT authentication with refresh tokens, role-based authorization, Flyway migrations, Docker, and a professional Material UI frontend.

This repository is safe to publish publicly as a portfolio project because it does not contain production secrets. The credentials and secrets shown here are local demo defaults only and must be replaced in any real deployment.

## Stack

- Java 21, Spring Boot 3, Maven
- PostgreSQL 16, Spring Data JPA, Flyway
- Spring Security, JWT access tokens, refresh tokens
- React, TypeScript, Vite
- Material UI, MUI Data Grid, Recharts
- React Router, React Query, Axios
- Docker, Docker Compose, Nginx

## Features

- Login, logout, protected routes, profile menu, notifications UI
- Role-based UI permissions for admin/management screens
- Dashboard KPI cards, charts, and recent operational activity
- Products, categories, suppliers, customers, warehouses, inventory
- Stock movement history and inventory transfer/adjustment workflows
- Purchase orders and sales orders with status actions
- Reports for inventory, low stock, purchases, sales, and stock movements
- User management and settings
- Dark/light mode and responsive enterprise layout
- Loading, error, empty, validation, filtering, sorting, and pagination states

## Ports

The Docker setup avoids the local `8080` conflict seen on this machine.

| Service | URL |
| --- | --- |
| Frontend | `http://localhost:5173` |
| API | `http://localhost:8081` |
| Swagger UI | `http://localhost:8081/swagger-ui.html` |
| PostgreSQL host port | `localhost:5434` |
| pgAdmin | `http://localhost:5050` |
| MailHog | `http://localhost:8025` |

Inside Docker, the API listens on `8080` and PostgreSQL listens on `5432`.

## One-Command Run

```bash
docker compose up --build
```

Open `http://localhost:5173`.

Default seeded admin:

- Email: `admin@yazidwms.local`
- Password: `Admin@12345`

These credentials are intentionally demo-only for local recruiter review.

For a production-like run, copy the environment template and change every secret before starting:

```bash
copy .env.example .env
docker compose up --build
```

## Local Development

Start the database with Docker:

```bash
docker compose up -d postgres mailhog pgadmin
```

Run the backend against PostgreSQL:

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

Open `http://localhost:5173`.

## PostgreSQL Runtime

The application runtime is PostgreSQL-first:

- `spring.datasource.url` uses `jdbc:postgresql://...`
- Docker Compose runs PostgreSQL 16
- Flyway applies schema migrations on startup
- H2 is test-scope only and is not available to the running application

Business demo records are disabled by default. Roles and the admin user are seeded so you can log in. Create real products, warehouses, suppliers, customers, orders, and inventory through the UI or API.

To enable demo business records for local demos only:

```bash
$env:SEED_DEMO_DATA="true"
```

## API

All application endpoints are under:

```text
/api/v1
```

Login:

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@yazidwms.local",
  "password": "Admin@12345"
}
```

Use the returned token:

```http
Authorization: Bearer <accessToken>
```

## Reports

Each endpoint supports `?format=CSV`, `?format=EXCEL`, or `?format=PDF`.

- `GET /api/v1/reports/inventory`
- `GET /api/v1/reports/low-stock`
- `GET /api/v1/reports/purchase`
- `GET /api/v1/reports/sales`
- `GET /api/v1/reports/stock-movements`

## Testing And Builds

Backend:

```bash
mvn test
```

Frontend:

```bash
cd frontend
npm run build
```

Docker configuration:

```bash
docker compose config
```

## Production Checklist

- Replace `JWT_SECRET`, database passwords, pgAdmin password, and seeded admin password.
- Prefer `SPRING_PROFILES_ACTIVE=prod` outside local development.
- Use managed PostgreSQL with backups, monitoring, and restore testing.
- Put the frontend/API behind TLS.
- Use a real SMTP provider and secure credentials.
- Keep `SEED_DEMO_DATA=false`.
- Disable or protect pgAdmin in non-local environments.
- Store secrets in the deployment platform, not in source control.
- Monitor `/actuator/health`, `/actuator/metrics`, logs, database size, and error rates.

## Public Repo Safety

- `.env` files are ignored and are not meant to be committed.
- `.env.example` contains placeholders only.
- Docker and application config use local demo defaults, not personal or production credentials.
- Before any real deployment, replace all passwords, JWT secrets, SMTP settings, and admin credentials.

## Recruiter Demo

Use this short demo flow to show the project clearly:

1. Log in as admin.
2. Show the dashboard, KPIs, and recent activity.
3. Create a supplier, category, and product.
4. Create a warehouse structure and verify inventory locations.
5. Create and receive a purchase order to increase stock.
6. Create a customer and sales order, then ship it to decrease stock.
7. Open stock movement history and reports to show traceability.

## Tech Used

- Backend: Java 21, Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Maven
- Database: PostgreSQL
- Frontend: React, TypeScript, Vite, Material UI, MUI Data Grid, React Router, React Query, Axios, Recharts
- Infra: Docker, Docker Compose, Nginx
