# YazidWMS

YazidWMS is a full-stack warehouse management system built with Spring Boot, React, and PostgreSQL. It includes authentication, inventory operations, warehouse management, purchasing, sales, reporting, and a responsive dashboard UI.

## Run With Docker

```bash
docker compose up --build
```

Available services:

- Frontend: `http://localhost:5173`
- API: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- pgAdmin: `http://localhost:5050`
- MailHog: `http://localhost:8025`

Create a local `.env` file from `.env.example` before starting the stack. The seeded admin account is configured through your local environment variables.

## Run Locally

Start support services:

```bash
docker compose up -d postgres mailhog pgadmin
```

Run the backend:

Copy `.env.example` to `.env` and set your local values first.

```bash
$env:SERVER_PORT="8081"
$env:DB_HOST="localhost"
$env:DB_PORT="5434"
$env:DB_NAME="yazidwms"
$env:DB_USERNAME="yazid"
$env:DB_PASSWORD="<your-local-db-password>"
mvn spring-boot:run
```

Run the frontend:

```bash
cd frontend
npm install
$env:VITE_API_PROXY_TARGET="http://127.0.0.1:8081"
npm run dev
```
