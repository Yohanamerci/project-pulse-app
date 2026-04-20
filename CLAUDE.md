# Project Pulse – Team Collaboration Guide

> Keep this file updated as features are completed. It is the single source of truth for
> what is done, what is in progress, and what still needs to be built.

---

## Quick Start (Every Dev Session)

```bash
# 1. Start infrastructure (MySQL + Mailpit)
docker compose up -d mysql mailpit

# 2. Start backend (Flyway auto-applies V1 schema + V2 seed data on fresh DB)
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dmaven.test.skip=true

# 3. Start frontend (separate terminal)
cd frontend
npm install   # first time only
npm run dev
```

| Service | URL | Credentials |
|---------|-----|-------------|
| Frontend | http://localhost:5173 | – |
| Backend API | http://localhost:8080 | – |
| Mailpit (email) | http://localhost:8025 | – |
| MySQL | localhost:3307 | root / secret |

---

## Test Accounts (Seed Data — Password: `Password1!`)

| Username | Role | Team |
|----------|------|------|
| `admin` | ADMIN | — (password: `Admin1234!`) |
| `dr.wei` | INSTRUCTOR | Team Alpha & Beta |
| `prof.smith` | INSTRUCTOR | Team Gamma |
| `alice.j` | STUDENT | Team Alpha |
| `bob.m` | STUDENT | Team Alpha |
| `carol.k` | STUDENT | Team Alpha |
| `david.p` | STUDENT | Team Alpha |
| `eva.r` | STUDENT | Team Beta |
| `frank.n` | STUDENT | Team Beta |
| `grace.l` | STUDENT | Team Beta |
| `henry.c` | STUDENT | Team Beta |

**Section 1 (CS 4391-001):** Week 10 (2026-03-23 → 2026-03-29) is the active week.  
**Section 2 (CS 4391-002):** Week 10 is also active. Team Gamma has no students yet.

---

## Project Structure

```
project-pulse-app/
├── backend/src/main/java/edu/tcu/cs/projectpulse/
│   ├── auth/            # UC-1  JWT login
│   ├── user/            # UC-2–5  User CRUD (Admin)
│   ├── section/         # UC-6–12  Section + active week management
│   ├── team/            # UC-13–19  Team + membership management
│   ├── rubric/          # UC-20–24  Rubric + criteria management
│   ├── activity/        # UC-25–27  Weekly Activity Reports
│   ├── evaluation/      # UC-28–34  Peer evaluations + grading
│   ├── shared/          # Result<T>, StatusCode, GlobalExceptionHandler
│   └── system/          # SecurityConfig, FlywayConfig
│
├── backend/src/main/resources/
│   └── db/migration/
│       ├── V1__baseline.sql    # Schema (tables + admin user)
│       └── V2__seed_data.sql   # Sample data for dev/testing
│
├── frontend/src/
│   ├── pages/           # Route-level views (one per feature)
│   ├── apis/            # Axios API modules (one per backend domain)
│   ├── stores/          # Pinia: auth.ts
│   ├── router/          # Vue Router 4 with auth guard
│   └── plugins/         # Vuetify theme config
│
└── docker-compose.yml   # MySQL 8 + Mailpit
```

---

## Architecture Rules

### Backend
- Domain-first package layout: `activity/`, `evaluation/`, `team/`, etc.
- Each domain has: `Entity.java`, `Repository.java`, `Service.java`, `Controller.java`, `dto/`
- Business logic lives in **Service**, not Controller
- Controllers only: validate input, call service, wrap in `Result<T>`
- All API responses use `Result<T>` — never return raw entity or throw uncaught exceptions
- Use `@PreAuthorize("hasRole('...')")` at controller level for auth

### Frontend
- API calls only in `src/apis/*.ts` — never inline in components
- State management via `src/stores/auth.ts` (Pinia)
- Route-level pages in `src/pages/` — extract subcomponents only when reused
- Vuetify components only (no raw HTML cards/tables if a Vuetify equivalent exists)
- Always show loading state (`v-progress-linear` or `:loading`) and error alerts

---

## Business Rules (DO NOT VIOLATE)

| Rule | Description | Enforced In |
|------|-------------|-------------|
| BR-1 | A team must have at least one assigned instructor | `TeamService.create()` |
| BR-2 | Fall semester: weeks 5–15 only. Spring: weeks 1–15 only | `SectionService.setActiveWeek()` |
| BR-3 | Peer evaluations cannot be edited once submitted | `EvaluationService.submit()` |
| BR-4 | WAR/evaluation submissions only allowed during the active week | `ActivityService.submit()`, `EvaluationService.submit()` |
| BR-5 | Students can only view their own evaluation scores | Frontend routing + `EvaluationController /my-scores` |

---

## Implementation Status

### ✅ Backend — Completed

| Domain | Endpoints | BR Enforced |
|--------|-----------|-------------|
| **Auth** | `POST /api/v1/auth/login` | JWT HMAC |
| **Users** | `GET /users`, `GET /users/{id}`, `POST /users`, `PUT /users/{id}` | Admin-only create/update |
| **Sections** | `GET/POST/PUT /sections`, `GET/POST /sections/{id}/active-week`, `GET /sections/{id}/active-weeks` | BR-2 |
| **Rubrics** | `GET/POST/PUT /rubrics`, `GET/POST/PUT /rubrics/{id}/criteria` | — |
| **Teams** | `GET/POST /teams`, `GET /teams/{id}`, `GET /teams/section/{id}`, `GET /teams/my-team`, `POST/DELETE /teams/{id}/students/{sid}`, `PUT /teams/{id}/instructor/{iid}`, `PUT /teams/{id}/rubric/{rid}` | BR-1 |
| **Activities** | `POST /activities`, `GET /activities/my`, `GET /activities/team/{id}`, `GET /activities/team/{id}/week/{id}`, `GET /activities` | BR-4 |
| **Evaluations** | `POST /evaluations`, `GET /evaluations/my`, `GET /evaluations/my-scores`, `GET /evaluations/team/{tid}/week/{wid}`, `GET /evaluations/grade/team/{tid}/week/{wid}/student/{sid}` | BR-3, BR-4, BR-5 |
| **Error Handling** | GlobalExceptionHandler handles: validation (400), not found (404), bad credentials (401), forbidden (403), illegal state/argument (400), catch-all (500) | All domains |
| **DB Migrations** | Flyway V1 (schema) + V2 (seed data, `INSERT IGNORE` idempotent) | — |

### ✅ Frontend — Completed

| Page | Path | Roles | Features |
|------|------|-------|---------|
| **Login** | `/login` | All | JWT login form, validation, error display |
| **Dashboard** | `/dashboard` | All | Role-aware welcome, navigation cards |
| **Activities** | `/activities` | All | Student: submit WAR + history; Admin/Instructor: team/week filter view |
| **Evaluations** | `/evaluations` | All | Student: evaluate teammates, view own scores; Instructor/Admin: team overview, grade view |
| **Teams** | `/teams` | All | Student: my team card; Instructor: read-only list; Admin: full CRUD + member management |
| **Sections** | `/sections` | Admin | Full CRUD + active week management dialog |
| **Users** | `/users` | Admin | Role-tabbed table, Add/Edit dialogs |
| **Navigation** | `App.vue` | All | Role-filtered drawer, JWT-aware, logout |

---

## 🔴 TODO — Needs Implementation

### High Priority (Required Features)

| # | Area | Task | Notes |
|---|------|------|-------|
| ✅ 1 | **Frontend** | ~~Create `RubricsPage.vue`~~ | Done — create/delete rubrics, add/edit/delete criteria |
| ✅ 2 | **Frontend** | ~~Add `/rubrics` to router and nav~~ | Done — Admin-only nav item and route |
| ✅ 3 | **Frontend** | ~~Grade Report tab for Instructors~~ | Done — "Grade Report" tab on EvaluationsPage with per-student score cards |
| ✅ 4 | **Backend** | ~~Delete Team endpoint~~ | Done — `DELETE /api/v1/teams/{id}`, blocks if students still assigned |
| ✅ 5 | **Backend** | ~~Delete Section endpoint~~ | Done — `DELETE /api/v1/sections/{id}`, blocks if teams exist |
| ✅ 6 | **Backend** | ~~Disable User endpoint~~ | Done — `DELETE /api/v1/users/{id}` soft-deletes (sets `enabled=false`) |

### Medium Priority (Quality / Polish)

| # | Area | Task | Notes |
|---|------|------|-------|
| 7 | **Frontend** | Type-check pass | Run `npm run type-check` and fix all TS errors in pages/apis. |
| 8 | **Backend** | Fix `AuthControllerTest.java` | `@AutoConfigureMockMvc` is in `spring-boot-test-autoconfigure` — add explicit dependency or remove test file and rewrite. |
| 9 | **Backend** | OpenAPI/Swagger docs | Add `springdoc-openapi-starter-webmvc-ui` to pom.xml. Annotate controllers with `@Operation`. Available at `/swagger-ui.html`. |
| 10 | **Frontend** | Rubric assignment in Teams UI | After creating rubric, allow Admin to assign it to a team from TeamsPage (calls `PUT /teams/{id}/rubric/{rid}`). |
| 11 | **Both** | Validation feedback on all forms | Ensure all API 400 errors surface as user-visible messages, not silent console errors. |

### Lower Priority (Enhancement)

| # | Area | Task | Notes |
|---|------|------|-------|
| 12 | **Backend** | Email notifications via Mailpit | Notify students when active week opens. `spring-boot-starter-mail` is already in `pom.xml`. Wire `JavaMailSender` in `system/`. |
| 13 | **Both** | Azure deployment | Add `.github/workflows/deploy.yml`. Backend: `mvnw package` → JAR → Azure App Service. Frontend: `npm run build` → Azure Static Web Apps. |
| 14 | **Backend** | Integration tests | Fix `AuthControllerTest` then add tests for `ActivityController` and `EvaluationController`. Requires DB running. |
| 15 | **Frontend** | Pagination on data tables | Activities and Evaluations tables can grow large. Add `items-per-page` control to `v-data-table`. |

---

## API Quick Reference

All responses use: `{ flag: boolean, code: number, message: string, data: T }`

### Auth
```
POST   /api/v1/auth/login          { username, password } → { userId, username, role, token }
```

### Users (Admin only for write)
```
GET    /api/v1/users               → UserDto[]
GET    /api/v1/users/{id}          → UserDto
POST   /api/v1/users               { username, email, password, firstName, lastName, role }
PUT    /api/v1/users/{id}          { firstName, lastName, email, enabled }
```

### Sections (Admin for write)
```
GET    /api/v1/sections            → SectionDto[]
POST   /api/v1/sections            { name, semester, year }
PUT    /api/v1/sections/{id}       { name, semester, year }
GET    /api/v1/sections/{id}/active-week
POST   /api/v1/sections/{id}/active-week  { weekNumber, startDate, endDate }
GET    /api/v1/sections/{id}/active-weeks
```

### Teams (Admin for write, Instructor read)
```
GET    /api/v1/teams               → TeamDto[]
GET    /api/v1/teams/{id}          → TeamDto
GET    /api/v1/teams/section/{id}  → TeamDto[]
GET    /api/v1/teams/my-team       → TeamDto  (Student JWT)
POST   /api/v1/teams               { name, sectionId, rubricId, instructorId }
POST   /api/v1/teams/{id}/students/{sid}
DELETE /api/v1/teams/{id}/students/{sid}
PUT    /api/v1/teams/{id}/instructor/{iid}
PUT    /api/v1/teams/{id}/rubric/{rid}
```

### Rubrics (Admin for write)
```
GET    /api/v1/rubrics             → RubricDto[]
POST   /api/v1/rubrics             { name, description, sectionId }
PUT    /api/v1/rubrics/{id}        { name, description }
POST   /api/v1/rubrics/{id}/criteria  { name, description, maxScore, displayOrder }
PUT    /api/v1/rubrics/{id}/criteria/{cid}
```

### Activities (Student submit)
```
POST   /api/v1/activities          { teamId, weekId, category, description, hours }
GET    /api/v1/activities/my                               (Student JWT)
GET    /api/v1/activities/team/{id}                        (Admin/Instructor)
GET    /api/v1/activities/team/{id}/week/{wid}
GET    /api/v1/activities                                  (Admin)
```
Categories: `DEVELOPMENT`, `TESTING`, `DOCUMENTATION`, `DESIGN`, `MEETING`, `BUG_FIX`, `OTHER`

### Evaluations (Student submit)
```
POST   /api/v1/evaluations         { evaluateeId, teamId, weekId, scores: [{criterionId, score}] }
GET    /api/v1/evaluations/my                              (Student JWT — evals I submitted)
GET    /api/v1/evaluations/my-scores                       (Student JWT — evals others gave me)
GET    /api/v1/evaluations/team/{tid}/week/{wid}           (Admin/Instructor)
GET    /api/v1/evaluations/grade/team/{tid}/week/{wid}/student/{sid}
```

---

## Grading Algorithm (UC-31)

`studentGrade = avg(sum of all criterion scores from each teammate evaluating this student)`

Example — 5-criterion rubric, max 45 total:
- Teammate A scores: 9+8+5+9+8 = 39
- Teammate B scores: 10+9+5+10+9 = 43
- Grade = (39 + 43) / 2 = **41 / 45**

---

## Team Workflow

```
main
  └── feature/alice/rubrics-page
  └── feature/bob/grade-report
  └── feature/carol/delete-endpoints
  └── feature/david/email-notifications
```

**Rules:**
1. Branch off `main` → `feature/<your-name>/<feature-name>`
2. One feature or UC group per branch — keep PRs small
3. PR requires at least one teammate review before merging
4. Do not commit directly to `main`
5. Run `npm run type-check` (frontend) before opening PR
6. Test your feature with the seed accounts (see top of this file)

---

## Coding Standards

### Backend
- `@RestController` + `@RequestMapping("/api/v1/...")` on all controllers
- Return `Result.success(data)` or `Result.success("message", data)` — never raw entities
- Throw `EntityNotFoundException` for missing records (caught → 404)
- Throw `IllegalStateException` for business rule violations (caught → 400)
- Throw `IllegalArgumentException` for bad input values (caught → 400)
- Use constructor injection — no `@Autowired` field injection
- Records for DTOs where possible: `record ActivityDto(Long id, ...) {}`

### Frontend
- TypeScript strictly — no `any` types in APIs
- All API functions return the unwrapped `data` field: `return response.data.data`
- Use `ref<T>()` with explicit types on all reactive state
- Error display via `v-alert type="error"` — never `console.error` only
- Loading state on every async action (`loading.value = true` / `finally { loading.value = false }`)

---

## Known Issues

| # | Severity | Description | Fix |
|---|----------|-------------|-----|
| 1 | Medium | `AuthControllerTest.java` fails to compile — `@AutoConfigureMockMvc` not found in Spring Boot 4 test classpath | Add explicit `spring-boot-test-autoconfigure` dep, or rewrite using `@WebMvcTest` |
| 2 | Low | Flyway not auto-configured in Spring Boot 4 | Fixed — manual `FlywayConfig.java` in `system/` package handles migration |
| 3 | Low | Section list shows duplicates if V2 is applied twice | Fixed — `INSERT IGNORE` in V2. Drop/recreate DB if duplicates appear in dev. |

---

## Environment Variables (Production)

Set these as GitHub secrets / Azure App Settings:

```
DB_URL          jdbc:mysql://<host>:3306/project_pulse?useSSL=true&serverTimezone=UTC
DB_USERNAME     <prod-db-user>
DB_PASSWORD     <prod-db-password>
JWT_SECRET_KEY  <256-bit random hex string>
MAIL_HOST       <smtp-host>
MAIL_PORT       587
```
