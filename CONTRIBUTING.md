# Contributing

YazidWMS is a portfolio project, but small improvements are welcome.

## Local Setup

```bash
cp .env.example .env
docker compose up --build
```

For manual development, start PostgreSQL, MailHog, and pgAdmin with Docker Compose, then run the backend with Maven and the frontend with npm.

## Checks

Before opening a pull request, run:

```bash
mvn verify
cd frontend
npm ci
npm run build
```

## Guidelines

- Keep business logic changes focused and covered by tests where practical.
- Do not commit `.env`, database dumps, screenshots with secrets, or local build output.
- Update documentation when ports, environment variables, workflows, or commands change.
- Keep API changes consistent with the `/api/v1` structure.
