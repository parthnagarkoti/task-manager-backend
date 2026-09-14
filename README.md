# Task Management Backend

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green.svg)](https://spring.io/projects/spring-boot)
[![MySQL 8](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Overview

**Task Management Backend** is a JWT-secured REST API for managing tasks across teams and projects, built with Spring Boot and MySQL. It deliberately implements a small, focused surface — 5 task endpoints plus authentication — rather than a sprawling feature set, so that every piece of it (security, service layer, data model, tests, containerization) is complete, tested, and explainable end to end.

The project was built step by step against a written implementation plan, with each milestone committed separately, verified by hand (via curl and automated tests), and containerized with a measured — not estimated — Docker image size.

---

## 🎯 Key Features

### 🔐 JWT Authentication
- **Stateless sessions**: no server-side session state, every request carries its own bearer token
- **BCrypt password hashing**: industry-standard, work-factor 10
- **Minimal by design**: login + token validation only — no refresh tokens, no fine-grained role guards

### 📌 Task Management API
- **Create**: tasks are created under a project, with the creator pulled from the JWT — never from client input
- **Assign**: reassign a task to any registered user
- **Status tracking with audit trail**: every status change writes a row to `task_status_history` in the same transaction — a full, queryable history of every state transition
- **Dynamic filtering + pagination**: filter tasks by status, priority, and/or project — any combination, all optional — via Spring's `JpaSpecificationExecutor`, with standard `page`/`size`/`sort` support

### 🗄️ Relational Data Model
- **8 tables**, fully normalized, no `@ManyToMany` hidden join tables
- `users`, `teams`, `team_members`, `projects`, `tasks` are the operative tables
- `subtasks`, `comments`, `task_status_history` exist for relational completeness and audit purposes — deliberately without dedicated endpoints, keeping the API surface small while the schema stays realistic

### ✅ Tested, Not Just Written
- Unit tests (Mockito) for every service method — happy paths and not-found error cases
- Repository tests (`@DataJpaTest` + H2) proving the dynamic filtering actually works against a real database, not just mocks
- Every one of the 5 endpoints additionally verified by hand via curl, including negative paths (404, 400, 403)

### 🐳 Containerized, One Command
- Multi-stage `Dockerfile` — the shipped image contains only a JRE and the built jar, never the JDK or Maven used to build it
- `docker compose up` starts MySQL and the app together, wired over the internal Docker network
- Image size **measured** via `docker inspect`, not guessed

---

## 🏗️ Architecture

### Request flow

```
┌──────────────────────────────────────────────────────────────┐
│                          Client                               │
│           (curl / Postman / any HTTP client)                  │
└───────────────────────────┬────────────────────────────────────┘
                             │  Authorization: Bearer <JWT>
                             ↓
┌──────────────────────────────────────────────────────────────┐
│                    Spring Security Filter Chain               │
│  ┌────────────────┐   ┌──────────────────────────────────┐   │
│  │ JwtAuthFilter   │ → │ AuthorizationFilter               │   │
│  │ (validates JWT, │   │ (permits /api/auth/**,            │   │
│  │  loads user)    │   │  requires auth on everything else)│   │
│  └────────────────┘   └──────────────────────────────────┘   │
└───────────────────────────┬────────────────────────────────────┘
                             ↓
┌──────────────────────────────────────────────────────────────┐
│                          Controllers                          │
│        AuthController            TaskController               │
│     (thin — no business logic, delegates to services)         │
└───────────────────────────┬────────────────────────────────────┘
                             ↓
┌──────────────────────────────────────────────────────────────┐
│                            Services                            │
│   UserDetailsServiceImpl          TaskService                  │
│   (Spring Security bridge)   (all business logic lives here:   │
│                                validation, status-history       │
│                                writes, dynamic filtering)        │
└───────────────────────────┬────────────────────────────────────┘
                             ↓
┌──────────────────────────────────────────────────────────────┐
│                     Spring Data JPA Repositories                │
│      (JpaRepository + JpaSpecificationExecutor<Task>)           │
└───────────────────────────┬────────────────────────────────────┘
                             ↓
                     ┌───────────────┐
                     │    MySQL 8    │
                     │   (8 tables)  │
                     └───────────────┘
```

### Error handling

A `@RestControllerAdvice` (`GlobalExceptionHandler`) converts `TaskNotFoundException` into a clean 404 and `@Valid` failures into a 400 with per-field error messages — controllers stay free of try/catch blocks.

---

## 📦 Project Structure

```
task-management-backend/
├── src/
│   ├── main/
│   │   ├── java/com/taskmanagement/
│   │   │   ├── TaskManagementApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java        # POST /api/auth/login
│   │   │   │   └── TaskController.java        # the 5 task endpoints
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   ├── CreateTaskRequest.java
│   │   │   │   ├── AssignTaskRequest.java
│   │   │   │   ├── UpdateStatusRequest.java
│   │   │   │   └── TaskResponse.java          # never exposes entities directly
│   │   │   ├── entity/                        # 8 @Entity classes
│   │   │   │   ├── User.java
│   │   │   │   ├── Team.java
│   │   │   │   ├── TeamMember.java
│   │   │   │   ├── Project.java
│   │   │   │   ├── Task.java
│   │   │   │   ├── Subtask.java
│   │   │   │   ├── Comment.java
│   │   │   │   └── TaskStatusHistory.java
│   │   │   ├── enums/
│   │   │   │   ├── TaskStatus.java            # TODO → IN_PROGRESS → DONE → CANCELLED
│   │   │   │   ├── Priority.java              # LOW / MEDIUM / HIGH
│   │   │   │   └── TeamRole.java              # OWNER / MEMBER / VIEWER
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── TaskNotFoundException.java
│   │   │   ├── repository/                    # 8 JpaRepository interfaces
│   │   │   ├── security/
│   │   │   │   ├── JwtUtil.java               # generate/validate HS256 tokens
│   │   │   │   ├── JwtAuthFilter.java          # OncePerRequestFilter
│   │   │   │   └── SecurityConfig.java
│   │   │   └── service/
│   │   │       ├── TaskService.java            # all business logic
│   │   │       └── UserDetailsServiceImpl.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data.sql                        # seeds users/team/project for testing
│   └── test/
│       └── java/com/taskmanagement/
│           ├── service/TaskServiceTest.java
│           └── repository/TaskRepositoryTest.java
├── Dockerfile                                   # multi-stage: JDK build → JRE runtime
├── docker-compose.yml                           # mysql + app services
├── .dockerignore
├── .env.example
├── pom.xml
└── README.md
```

---

## 🔌 API Endpoints

### Base URL
```
http://localhost:8082          # via docker compose
http://localhost:8081          # via ./mvnw spring-boot:run
```

All endpoints below except `/api/auth/login` require `Authorization: Bearer <token>`.

### Authentication

**`POST /api/auth/login`** — authenticate, returns a JWT

```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "test@example.com",
  "name": "Test User"
}
```

### 1. Create a Task

**`POST /api/tasks`**

```bash
curl -X POST http://localhost:8082/api/tasks \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Write unit tests","description":"Cover TaskService","priority":"HIGH","dueDate":"2026-10-01","projectId":1}'
```
Returns `201 Created` with the full task, including default `status: "TODO"` and the creator resolved from the JWT.

### 2. Assign a Task

**`POST /api/tasks/{id}/assign`**

```bash
curl -X POST http://localhost:8082/api/tasks/1/assign \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"userId":2}'
```
Returns `200 OK` with `assignedToId`/`assignedToName` populated.

### 3. Update Task Status

**`PATCH /api/tasks/{id}/status`**

```bash
curl -X PATCH http://localhost:8082/api/tasks/1/status \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"status":"IN_PROGRESS"}'
```
Returns `200 OK` with the updated task — and writes an audit row to `task_status_history` (old status, new status, who changed it, when) in the same transaction.

### 4. Filter + Paginate Tasks

**`GET /api/tasks?status=&priority=&projectId=&page=0&size=10&sort=dueDate`**

```bash
curl "http://localhost:8082/api/tasks?status=IN_PROGRESS&priority=HIGH&page=0&size=10" \
  -H "Authorization: Bearer <token>"
```
All query parameters are optional and combinable. Returns Spring's standard `Page<T>` envelope (`content`, `totalElements`, `totalPages`, etc).

### 5. Get a Single Task

**`GET /api/tasks/{id}`**

```bash
curl http://localhost:8082/api/tasks/1 -H "Authorization: Bearer <token>"
```
Returns `404` with a clean JSON error body if the task doesn't exist.

---

## 📖 Usage Guide

### 1. Prerequisites

```
- Docker Desktop (for the one-command setup)
- JDK 17 (only needed if running outside Docker)
```

### 2. Installation

```bash
git clone https://github.com/parthnagarkoti/task-manager-backend.git
cd task-manager-backend
```

### 3. Environment Configuration

Copy `.env.example` to `.env` and fill in real values:

```bash
cp .env.example .env
```

```env
# MySQL credentials
DB_USERNAME=taskuser
DB_PASSWORD=yourpassword
DB_ROOT_PASSWORD=yourrootpassword

# JWT secret — must be at least 32 characters (256 bits) for HMAC-SHA256
JWT_SECRET=replace-this-with-a-long-random-secret-key-min-32-chars
```

### 4. Running the Application

**Option A — One command, everything containerized:**
```bash
docker compose up -d
```
The app will be reachable at `http://localhost:8082`.

**Option B — Run the app locally, MySQL still in Docker:**
```bash
docker compose up -d mysql
# set DB_USERNAME, DB_PASSWORD, JWT_SECRET as environment variables, matching .env
./mvnw spring-boot:run
```
Reachable at `http://localhost:8081`.

> **A note on ports**: `server.port=8081` and the MySQL host mapping (`3307:3306`, app container mapped to host `8082`) were chosen because ports `8080`/`8081`/`3306` were already in use by other local services during development. If those are free on your machine, feel free to simplify them back to the defaults in `application.properties` and `docker-compose.yml`.

### 5. Try It Immediately

`data.sql` seeds two users, a team, and a project on startup, so the API is testable with zero setup:

```bash
# Log in as the seeded test user
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Use the token to list tasks
curl http://localhost:8082/api/tasks -H "Authorization: Bearer <token>"
```

---

## 🗄️ Data Model

8 tables, fully relational, no hidden `@ManyToMany` join tables:

| # | Table | Role |
|---|---|---|
| 1 | `users` | FK on `tasks.assigned_to` / `tasks.created_by`; JWT subject |
| 2 | `teams` | FK on `projects.team_id` |
| 3 | `team_members` | Joins users to teams with a role (`OWNER`/`MEMBER`/`VIEWER`) |
| 4 | `projects` | FK on `tasks.project_id` |
| 5 | `tasks` | Core table — all 5 task APIs operate on this |
| 6 | `subtasks` | FK to `tasks` — persisted, no dedicated endpoint |
| 7 | `comments` | FK to `tasks` — persisted, no dedicated endpoint |
| 8 | `task_status_history` | Written automatically on every status update |

Schema is generated by Hibernate (`spring.jpa.hibernate.ddl-auto=create-drop`) — this intentionally resets on every restart, matching the project's scope as a demo/interview artifact rather than a production system with migration tooling.

---

## 🐳 Docker Image Size

Measured — not estimated — via `docker inspect --format='{{.Size}}'` immediately after a full `docker build`:

```
139,826,789 bytes  (~140 MB)
```

Achieved with a multi-stage `Dockerfile`: the build stage uses a full JDK to compile and package the app; the final runtime stage starts fresh from a JRE-only base image and copies in just the built jar — the JDK and Maven used to build it never ship.

---

## 🧪 Testing

```bash
./mvnw test
```

**14 tests, all passing:**
- `TaskServiceTest` (Mockito) — 10 tests covering happy paths and error cases (not-found) for all 5 `TaskService` methods, including an `ArgumentCaptor`-based assertion that `updateStatus` writes the *correct* `TaskStatusHistory` row, not just *a* row
- `TaskRepositoryTest` (`@DataJpaTest` + in-memory H2) — 4 tests proving the `Specification`-based dynamic filtering and pagination work against a real database, not mocks

---

## 🔐 Security Considerations

- Passwords hashed with BCrypt (work factor 10) — never stored in plaintext
- JWT signed with HS256, secret loaded from an environment variable, never hardcoded
- Stateless sessions (`SessionCreationPolicy.STATELESS`) — no server-side session fixation surface
- `.env` is git-ignored; only `.env.example` (no real secrets) is committed
- CSRF disabled deliberately — appropriate for a stateless, token-authenticated REST API, not a cookie-based one

---

## 🤝 Contributing

This started as a focused, scope-locked interview/portfolio project, but contributions or suggestions are welcome:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/YourFeature`)
3. Commit with descriptive messages (`git commit -m 'feat: add YourFeature'`)
4. Push to your branch (`git push origin feature/YourFeature`)
5. Open a Pull Request

Please keep additions consistent with the project's intentionally minimal scope (see **What This Project Deliberately Does Not Do** below) unless discussed first.

---

## 📚 Technology Stack

| Component | Technology | Version |
|---|---|---|
| **Language** | Java | 17 |
| **Framework** | Spring Boot | 3.2.5 |
| **Build tool** | Maven | (via wrapper) |
| **Database** | MySQL | 8.0 |
| **ORM** | Spring Data JPA / Hibernate | 6.4.4 |
| **Security** | Spring Security + jjwt | 0.11.5 |
| **Testing** | JUnit 5, Mockito, H2 | — |
| **Containerization** | Docker + Docker Compose | — |

---

## ❓ FAQ

**Q: Why only 5 task endpoints?**
A: Deliberate scope control. The goal was a small, complete, fully-tested surface rather than a sprawling half-finished one — see the excluded features below.

**Q: Why do `subtasks`, `comments`, and `team_members` exist with no endpoints?**
A: They round out the relational schema (8 tables, matching real domain modeling) without expanding the API surface. They're fair game for a "how would you extend this" interview discussion.

**Q: Is the JWT secret safe to see in `.env.example`?**
A: No real secret is committed — `.env.example` only shows the *shape* of the value expected; the real `.env` is git-ignored.

**Q: Why does `ddl-auto=create-drop` reset the schema on every restart?**
A: Intentional for this project's scope (fast iteration during development/demo). A production system would use versioned migrations (Flyway/Liquibase) instead.

---

## 💬 Support & Contact

For issues, questions, or suggestions:
- Open an [Issue](https://github.com/parthnagarkoti/task-manager-backend/issues)
- Reach out directly — see **Author** below

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Parth Nagarkoti**
- GitHub: [@parthnagarkoti](https://github.com/parthnagarkoti)
- LinkedIn: [parth-nagarkoti](https://www.linkedin.com/in/parth-nagarkoti/)
- Email: [parthnagarkoticareer@gmail.com](mailto:parthnagarkoticareer@gmail.com)
- Project: [task-manager-backend](https://github.com/parthnagarkoti/task-manager-backend)

---

## 📈 Project Status

- ✅ JWT authentication (login, stateless sessions)
- ✅ 5 task REST endpoints (create, assign, status + audit trail, filter/paginate, get by id)
- ✅ 8-table relational schema
- ✅ Unit + repository tests (14 passing)
- ✅ Multi-stage Docker image (measured: ~140 MB)
- ✅ One-command Docker Compose setup, verified end-to-end
- ✅ Documented and pushed to GitHub

## 🚫 What This Project Deliberately Does Not Do

To keep the scope honest and the implementation complete rather than sprawling:
- No endpoints for subtasks, comments, or team management (tables exist, endpoints don't)
- No refresh tokens or token revocation
- No pagination on anything except the task list
- No file uploads or attachments
- No email notifications
- No Swagger/OpenAPI
- No environment-specific config profiles (no dev/prod split)
- No database migration tooling (Flyway/Liquibase) — schema is Hibernate-generated

---

**Last Updated**: September 14, 2026
