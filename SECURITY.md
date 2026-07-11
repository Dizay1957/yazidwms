# Security Policy

YazidWMS is a portfolio project and is not currently operated as a hosted production service.

## Reporting Security Issues

Please report security issues privately to the repository owner instead of opening a public issue with exploit details.

## Secrets

Never commit real secrets. Keep local values in `.env`, which is ignored by Git. Use `.env.example` only for safe placeholders.

Sensitive values include:

- `JWT_SECRET`
- Database passwords
- Admin seed credentials
- SMTP credentials
- Production URLs, tokens, or private keys

## Production Hardening

Before deploying publicly, configure strong secrets, HTTPS, restricted CORS, disabled public Swagger, secure token handling, production database backups, centralized logs, monitoring, and a real mail provider.
