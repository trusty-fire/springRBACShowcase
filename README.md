# Spring RBAC Showcase

A small Spring Boot reference application that demonstrates **multi-tenant, role-based access control** on top of [essencium-backend](https://github.com/Frachtwerk/essencium-backend). It exposes a typical domain (organizations → projects → tasks) as a REST API, with every endpoint guarded by fine-grained rights.

The goal is not to ship a product, but to give a working, readable example you can clone, run, poke at via Swagger or Postman, and learn from.

---

## Stack

- Java 21 / Spring Boot 3.5
- essencium-backend 3.3.2 (users, roles, rights, JWT, mail, base controllers)
- Spring Data JPA + Hibernate
- PostgreSQL 17 (production-style) or H2 in-memory (default)
- springdoc-openapi (Swagger UI)
- Maven

---

## Quick start

### Run with the in-memory H2 database (default)

```bash
mvn spring-boot:run
```

The application starts on **http://localhost:8098** with the `development, h2` profile active.

### Run against PostgreSQL

```bash
cd database
docker compose up -d
cd ..
mvn spring-boot:run -Dspring-boot.run.profiles=development,postgres
```

Postgres listens on `127.0.0.1:5432` (`crafted` / `code` / db `showcase`); pgAdmin is at `http://localhost:3001`.

---

## Default credentials

On first start, essencium seeds one administrator:

| Field    | Value                              |
| -------- | ---------------------------------- |
| Username | `showcaseAdmin@crafted-code.org`   |
| Password | `adminAdminAdmin`                  |
| Roles    | `ADMIN`, `DEFAULT`                 |

Obtain a JWT:

```bash
curl -X POST http://localhost:8098/auth/token \
  -H 'Content-Type: application/json' \
  -d '{"username":"showcaseAdmin@crafted-code.org","password":"adminAdminAdmin"}'
```

## Demo data

When the `demo` profile is active (it is by default), two organizations are seeded with one user per role so that all four RBAC tiers can be exercised immediately. All demo users share the password **`demo123`**.

| Organization | Email                              | Role    |
| ------------ | ---------------------------------- | ------- |
| Acme Inc     | `admin@acme.crafted-code.org`      | ADMIN   |
| Acme Inc     | `manager@acme.crafted-code.org`    | MANAGER |
| Acme Inc     | `member@acme.crafted-code.org`     | MEMBER  |
| Acme Inc     | `viewer@acme.crafted-code.org`     | VIEWER  |
| Globex Corp  | `admin@globex.crafted-code.org`    | ADMIN   |
| Globex Corp  | `manager@globex.crafted-code.org`  | MANAGER |
| Globex Corp  | `member@globex.crafted-code.org`   | MEMBER  |
| Globex Corp  | `viewer@globex.crafted-code.org`   | VIEWER  |

Each tenant also gets three projects (one archived) and a handful of tasks across statuses and priorities. Logging in as different users from different organizations is the quickest way to see both **role-based** restrictions and **tenant isolation** in action.

To start the application without seeded demo data, run with a profile list that omits `demo`, e.g. `-Dspring-boot.run.profiles=development,h2`. The seeder is idempotent: it skips if any organization already exists.

---

## Swagger / OpenAPI

- Swagger UI: <http://localhost:8098/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8098/v3/api-docs>

The spec declares a `bearerAuth` JWT security scheme — click **Authorize** in Swagger and paste your token to call protected endpoints.

---

## Postman

A pre-built collection and matching environment live under [`docs/postman/`](docs/postman/):

- `SpringRBACShowcase.postman_collection.json`
- `SpringRBACShowcase.postman_environment.json`

Import both into Postman, select the **Spring RBAC Showcase (local)** environment, and run **Auth → Login** first. A test script stores the JWT into the `{{token}}` collection variable; every other request inherits Bearer auth from the collection root.

To switch personas, change the `username` variable on the environment to any of the demo accounts (see [Demo data](#demo-data)) and re-run **Login**. Listing endpoints (organizations / projects / tasks) populate `{{organizationId}}`, `{{projectId}}` and `{{taskId}}` automatically, so the CRUD-by-id requests are runnable right after the corresponding list.

---

## Endpoints

| Resource       | Path                  | Notes                                                  |
| -------------- | --------------------- | ------------------------------------------------------ |
| Auth           | `/auth/**`            | Provided by essencium (login, refresh, password reset) |
| Users          | `/v1/users`           | CRUD + me-endpoint                                     |
| Organizations  | `/v1/organizations`   | Tenant-scoped CRUD                                     |
| Projects       | `/v1/projects`        | CRUD                                                   |
| Tasks          | `/v1/tasks`           | CRUD                                                   |

All listing endpoints support paging (`?page=`, `?size=`, `?sort=`) and JPA Specifications (filter by any field as query parameter).

---

## RBAC model

Four roles are seeded from `application.yaml`:

| Right                  | ADMIN | MANAGER | MEMBER | VIEWER |
| ---------------------- | :---: | :-----: | :----: | :----: |
| `ORGANIZATION_CREATE`  |   ✔   |         |        |        |
| `ORGANIZATION_READ`    |   ✔   |    ✔    |   ✔    |   ✔    |
| `ORGANIZATION_UPDATE`  |   ✔   |         |        |        |
| `ORGANIZATION_DELETE`  |   ✔   |         |        |        |
| `PROJECT_CREATE`       |   ✔   |    ✔    |        |        |
| `PROJECT_READ`         |   ✔   |    ✔    |   ✔    |   ✔    |
| `PROJECT_UPDATE`       |   ✔   |    ✔    |        |        |
| `PROJECT_DELETE`       |   ✔   |    ✔    |        |        |
| `TASK_CREATE`          |   ✔   |    ✔    |   ✔    |        |
| `TASK_READ`            |   ✔   |    ✔    |   ✔    |   ✔    |
| `TASK_UPDATE`          |   ✔   |    ✔    |   ✔    |        |
| `TASK_DELETE`          |   ✔   |    ✔    |   ✔    |        |
| `USER_CREATE`          |   ✔   |         |        |        |
| `USER_READ`            |   ✔   |    ✔    |   ✔    |   ✔    |
| `USER_UPDATE`          |   ✔   |         |        |        |
| `USER_DELETE`          |   ✔   |         |        |        |

`VIEWER` is the default role assigned to new users. `DEFAULT` is essencium's built-in role for authenticated-only access.

Rights are enforced on every controller method via `@Secured`; check any controller in `org.craftedcode.backend.controller` for the exact mapping.

---

## Multi-tenancy

Every user belongs to at most one `Organization`. The `TenantContext` resolves the caller's organization from the JWT principal and `OrganizationService` injects a tenant filter into every query — users only see data within their own org. See `src/testIntegration/.../UserControllerTenantTest` for examples of the isolation guarantees.

---

## Project layout

```
src/main/java/org/craftedcode/backend
├── configuration/         OpenAPI + right initialization
├── controller/            REST endpoints (extend AbstractRestController)
├── model/                 JPA entities (Organization, Project, Task, User, …)
│   ├── dto/               Input DTOs
│   └── representation/    Output DTOs + HATEOAS assemblers
├── repository/            Spring Data repositories + JPA Specifications
├── security/              TenantContext
└── service/               Business logic
```

---

## Running the tests

```bash
mvn test                   # unit tests
mvn verify                 # unit + integration tests (src/testIntegration)
```

Integration tests boot the full application against H2 and exercise the controllers via `MockMvc`.

---

## License

Licensed under the **GNU Lesser General Public License v3.0 (or later)** — see [`LICENSE.txt`](LICENSE.txt).
