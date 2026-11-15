# Technical Design Document

## Overview

The Candidate Manager is a RESTful API for managing recruitment candidate data. It supports full CRUD operations, keyword search, and multi-criteria filtering with pagination.

## Architecture

### Layered Design

```
Client → Controller → Service → Repository → Database
                ↕            ↕
              DTOs      Specifications
```

| Layer           | Responsibility                                                 |
|-----------------|----------------------------------------------------------------|
| **Controller**  | HTTP handling, request validation, response formatting         |
| **Service**     | Business rules, transaction management, duplicate detection    |
| **Repository**  | Data access, custom queries, specification execution           |
| **Specification** | Dynamic query construction for multi-criteria filtering      |

This separation keeps each layer focused on a single concern, making the code easier to test, maintain, and evolve.

### Key Design Decisions

#### 1. Spring Boot + Spring Data JPA

Spring Boot was chosen for its mature ecosystem, convention-over-configuration approach, and production-readiness features (health checks, externalized config). Spring Data JPA eliminates boilerplate CRUD code while JPA Specifications provide a type-safe way to build dynamic queries.

#### 2. DTO Layer with MapStruct

Request and response DTOs decouple the API contract from the internal entity schema. MapStruct generates the mapping code at compile time — no reflection overhead, full type safety, and easy to inspect in the generated source.

#### 3. JPA Specifications for Filtering

Rather than writing a separate JPQL query for each filter combination, `CandidateSpecification` builds predicates dynamically. Each non-null filter parameter adds a predicate; all are ANDed together. This scales linearly with the number of filter fields without combinatorial query explosion.

#### 4. Optimistic Locking

The `@Version` field on the entity prevents lost updates when two users edit the same candidate concurrently. Hibernate automatically checks the version on save and throws `OptimisticLockException` if it has changed.

#### 5. H2 for Development, PostgreSQL for Production

H2 provides a zero-setup development and testing experience. The prod profile switches to PostgreSQL with `ddl-auto=validate` (schema managed externally in production).

## Data Model

```
candidates
├── id (PK, BIGINT, auto-increment)
├── first_name (VARCHAR 100, NOT NULL)
├── last_name (VARCHAR 100, NOT NULL)
├── email (VARCHAR 255, NOT NULL, UNIQUE)
├── phone (VARCHAR 20)
├── current_company (VARCHAR 200)
├── current_title (VARCHAR 150)
├── status (VARCHAR 20, NOT NULL, default NEW)
├── years_of_experience (INT)
├── expected_salary (VARCHAR 500)
├── notes (VARCHAR 1000)
├── version (BIGINT — optimistic lock)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

candidate_skills (collection table)
├── candidate_id (FK → candidates.id)
└── skill (VARCHAR 100)
```

### Indexes

- `idx_candidate_email` — unique index for fast email lookups and duplicate detection
- `idx_candidate_status` — supports status-based filtering
- `idx_candidate_name` — composite index on (firstName, lastName) for name search

## Error Handling Strategy

A centralized `GlobalExceptionHandler` translates all exceptions into a uniform JSON structure:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": {
    "email": "Email must be valid",
    "firstName": "First name is required"
  }
}
```

| Exception                  | HTTP Status | When                               |
|----------------------------|-------------|-------------------------------------|
| Validation failure         | 400         | Invalid request body fields         |
| Malformed JSON             | 400         | Unparseable request body            |
| Type mismatch              | 400         | Wrong type in path/query parameters |
| CandidateNotFoundException | 404         | ID doesn't exist                    |
| DuplicateEmailException    | 409         | Email already in use                |
| Unexpected errors          | 500         | Catch-all (logged with stack trace) |

## Testing Strategy

| Level           | Tool            | Coverage                                          |
|-----------------|-----------------|---------------------------------------------------|
| **Unit**        | JUnit 5, Mockito | Service logic in isolation (12 tests)            |
| **Controller**  | MockMvc         | HTTP semantics, validation, error codes (12 tests)|
| **Integration** | SpringBootTest  | End-to-end with real H2 database (5 tests)        |

Total: **29+ automated tests** covering happy paths, edge cases, validation errors, and error responses.

## Scaling Strategy

As the application grows to support significantly more users and records, the following changes would be appropriate:

### Short-Term (10K–100K records)

1. **Database Migration** — Switch from H2 to PostgreSQL (already supported via prod profile).
2. **Connection Pooling** — Configure HikariCP pool sizes based on load testing.
3. **Database Indexes** — The current indexes cover the primary query patterns. Add composite indexes for common filter combinations identified through query analysis.
4. **Caching** — Add Redis/Caffeine caching for frequently accessed candidates (cache invalidation on updates).

### Medium-Term (100K–10M records)

5. **Full-Text Search** — Replace `LIKE '%keyword%'` queries with Elasticsearch or PostgreSQL full-text search (`tsvector`/`tsquery`). The LIKE-based approach requires full table scans and doesn't scale.
6. **Skill Normalization** — Move skills to a dedicated `skills` table with a many-to-many join table. Enables efficient skill-based queries and analytics.
7. **Read Replicas** — Separate read and write databases. Route search/filter queries to read replicas.
8. **API Rate Limiting** — Add rate limiting (e.g., Bucket4j or API gateway) to protect against abuse.

### Long-Term (10M+ records, high concurrency)

9. **CQRS Pattern** — Separate the write model (PostgreSQL) from the read model (Elasticsearch). Event-driven sync between them.
10. **Microservice Extraction** — Split candidate management from search into separate services if they scale differently.
11. **Database Sharding** — Shard the candidates table by a suitable key (e.g., region or tenant) if vertical scaling is exhausted.
12. **Async Processing** — Use message queues (Kafka/RabbitMQ) for non-critical operations like audit logging and notifications.

### Infrastructure

- **Containerization** — Dockerize the application (Dockerfile + docker-compose with PostgreSQL).
- **Orchestration** — Deploy on Kubernetes with horizontal pod autoscaling.
- **Observability** — Add distributed tracing (Micrometer + Zipkin), structured logging (JSON), and dashboards (Grafana).
- **CI/CD** — Automate build → test → deploy with GitHub Actions.

## Security Considerations

The current implementation focuses on core functionality. For production deployment:

- **Authentication** — Add Spring Security with JWT or OAuth2.
- **Authorization** — Role-based access (e.g., recruiters can edit, viewers can only read).
- **Input Sanitization** — JPA parameterized queries already prevent SQL injection. Add XSS protection for any rendered output.
- **HTTPS** — Enforce TLS in production.
- **Audit Logging** — Track who created/modified/deleted each candidate.
- **Data Encryption** — Encrypt sensitive fields (email, phone) at rest.
