# Project Pulse

A full-stack web application for managing senior design course teams — tracking weekly activity reports, peer evaluations, rubrics, and team management.

**Stack:** Spring Boot 4 (Java 21) · Vue 3 + Vuetify · MySQL · Azure

---

## Team Setup

1. **Fork or clone** this repo and set it as your team's `main` branch on GitHub.
2. Each member creates a feature branch: `git checkout -b feature/your-name/feature-name`
3. Open PRs into `main` — require at least one teammate review before merging.

---

## Project Structure

```
project-pulse-app/
├── backend/          # Spring Boot 4 REST API
│   └── src/main/java/edu/tcu/cs/projectpulse/
│       ├── auth/         # JWT authentication
│       ├── user/         # User management (Admin, Instructor, Student)
│       ├── section/      # Course sections
│       ├── team/         # Teams & membership
│       ├── activity/     # Weekly Activity Reports (WAR)
│       ├── evaluation/   # Peer evaluations & scores
│       ├── rubric/       # Rubrics & criteria
│       └── system/       # Email, schedulers, data init
├── frontend/         # Vue 3 + Vuetify SPA
│   └── src/
│       ├── pages/        # Route-level views
│       ├── components/   # Shared UI components
│       ├── stores/       # Pinia state (auth, user)
│       ├── apis/         # Axios API modules
│       ├── router/       # Vue Router 4
│       └── plugins/      # Vuetify plugin config
└── docker-compose.yml  # MySQL + Mailpit for local dev
```

---

## Local Development

### Prerequisites
- Java 21
- Node.js 20+
- Docker Desktop

### Backend

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Runs on **http://localhost:8080**

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Runs on **http://localhost:5173**

### Database (Docker)

```bash
docker compose up -d mysql mailpit
```

- MySQL: `localhost:3306` — DB: `project_pulse`, user: `root`, pass: `secret`
- Mailpit (email testing): `http://localhost:8025`

---

## API Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/login` | Login, returns JWT |
| GET | `/api/v1/users` | List users (Admin) |
| GET | `/api/v1/sections` | List sections |
| GET | `/api/v1/teams` | List teams |
| POST | `/api/v1/activities` | Submit WAR |
| POST | `/api/v1/evaluations` | Submit peer evaluation |

Full API documentation coming soon (Swagger at `/swagger-ui.html` in dev).

---

## Roles & Use Cases

| Role | Key Actions |
|------|-------------|
| **Admin** | Manage users, sections, teams, rubrics, active weeks |
| **Instructor** | View teams, view evaluations, manage their sections |
| **Student** | Submit WARs, submit peer evaluations, view own scores |

**Business Rules:**
- BR-1: A team must have an assigned instructor
- BR-2: Active weeks: Fall semesters 5–15, Spring semesters 1–15
- BR-3: Peer evaluations cannot be edited after submission
- BR-4: WAR/evaluation submissions only allowed within the active week window
- BR-5: Students can only view their own evaluation scores

---

## Grading Algorithm (UC-31)

A student's grade = average of all teammates' total criterion scores for that week.

Example: Student John, 6-criterion rubric (max 10 each = 60 total)
- Tim's scores for John: 10+9+10+9+10+10 = 58
- Lily's scores for John: 5+5+10+10+10+10 = 50
- John's grade = (58 + 50) / 2 = **54 / 60**

---

## Deployment (Azure)

GitHub Actions workflow in `.github/workflows/` will:
1. Build backend JAR
2. Build frontend dist
3. Deploy to Azure App Service

Configure secrets in GitHub: `AZURE_CREDENTIALS`, `DB_URL`, `DB_PASSWORD`, `JWT_SECRET_KEY`

---

## Assignment Requirements Checklist

- [ ] UC-1 through UC-34 implemented
- [ ] 3 roles: Admin, Instructor, Student
- [ ] JWT authentication (HMAC)
- [ ] Weekly Activity Reports (WAR)
- [ ] Peer evaluations with rubric/criteria
- [ ] Active week management
- [ ] Deployed to Azure
- [ ] Both frontend (Vue+Vuetify) and backend (Spring Boot 4) contributions from all members
