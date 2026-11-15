# Candidate Manager

A RESTful Java application for managing recruitment candidate data — built with **Spring Boot 3**, **JPA/Hibernate**, and **H2** (with PostgreSQL production support).

## Features

- **Full CRUD** — Create, read, update, and delete candidates
- **Keyword Search** — Free-text search across name, email, company, and title
- **Multi-Criteria Filtering** — Filter by status, experience range, skill, and company (all combinable)
- **Pagination & Sorting** — Configurable page size with default sorting by creation date
- **Input Validation** — Comprehensive request validation with clear error messages
- **Optimistic Locking** — `@Version` field prevents lost updates on concurrent edits
- **API Documentation** — Swagger UI auto-generated via SpringDoc OpenAPI
- **Automated Tests** — Unit, controller, and full integration tests

## Tech Stack

| Layer         | Technology                        |
|---------------|-----------------------------------|
| Language      | Java 21                           |
| Framework     | Spring Boot 3.3.5                 |
| Persistence   | Spring Data JPA + Hibernate       |
| Database      | H2 (dev/test), PostgreSQL (prod)  |
| Validation    | Jakarta Bean Validation           |
| DTO Mapping   | MapStruct 1.6                     |
| API Docs      | SpringDoc OpenAPI (Swagger UI)    |
| Build         | Maven                             |
| Testing       | JUnit 5, Mockito, MockMvc         |

## Prerequisites

- **Java 21** or later
- **Maven 3.8+**

## Quick Start

```bash
# Clone the repository
git clone https://github.com/shubham680tiwari/crm-recruit.git
cd crm-recruit

# Build and run (uses H2 in-memory database — zero setup)
mvn clean install
mvn spring-boot:run

# Or run with dev profile (seeds sample data)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The application starts at **http://localhost:8080**.

## API Documentation

Once running, open the interactive Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

## API Endpoints

| Method   | Endpoint                       | Description                          |
|----------|--------------------------------|--------------------------------------|
| `POST`   | `/api/v1/candidates`           | Create a new candidate               |
| `GET`    | `/api/v1/candidates`           | List all candidates (paginated)      |
| `GET`    | `/api/v1/candidates/{id}`      | Get a candidate by ID                |
| `PUT`    | `/api/v1/candidates/{id}`      | Update a candidate                   |
| `DELETE` | `/api/v1/candidates/{id}`      | Delete a candidate                   |
| `GET`    | `/api/v1/candidates/search`    | Search by keyword                    |
| `GET`    | `/api/v1/candidates/filter`    | Filter by multiple criteria          |

### Example: Create a Candidate

```bash
curl -X POST http://localhost:8080/api/v1/candidates \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Johnson",
    "email": "alice@example.com",
    "phone": "+1-555-0101",
    "currentCompany": "TechCorp",
    "currentTitle": "Senior Java Developer",
    "yearsOfExperience": 8,
    "skills": ["Java", "Spring Boot", "AWS"],
    "notes": "Strong backend experience"
  }'
```

### Example: Filter Candidates

```bash
# Candidates with status=NEW, 5+ years experience, who know Java
curl "http://localhost:8080/api/v1/candidates/filter?status=NEW&minExperience=5&skill=Java"
```

### Pagination & Sorting

```bash
# Page 0, 10 items per page, sorted by lastName ascending
curl "http://localhost:8080/api/v1/candidates?page=0&size=10&sort=lastName,asc"
```

## Running Tests

```bash
# Run all tests (unit + integration)
mvn test

# Run only unit tests
mvn test -Dtest="*ServiceTest,*ControllerTest"

# Run only integration tests
mvn test -Dtest="*IntegrationTest"
```

## Project Structure

```
src/main/java/com/recruitcrm/candidatemanager/
├── CandidateManagerApplication.java    # Entry point
├── config/
│   ├── OpenApiConfig.java              # Swagger configuration
│   └── DataSeeder.java                 # Sample data (dev profile only)
├── controller/
│   └── CandidateController.java        # REST endpoints
├── dto/
│   ├── CandidateRequest.java           # Inbound DTO with validation
│   ├── CandidateResponse.java          # Outbound DTO
│   ├── CandidateSearchCriteria.java    # Search/filter parameters
│   ├── CandidateMapper.java            # MapStruct entity↔DTO mapper
│   └── ErrorResponse.java             # Standardized error body
├── entity/
│   ├── Candidate.java                  # JPA entity
│   └── CandidateStatus.java           # Pipeline status enum
├── exception/
│   ├── CandidateNotFoundException.java
│   ├── DuplicateEmailException.java
│   └── GlobalExceptionHandler.java     # Centralized error handling
├── repository/
│   └── CandidateRepository.java        # Spring Data JPA repository
├── service/
│   └── CandidateService.java           # Business logic
└── specification/
    └── CandidateSpecification.java     # Dynamic JPA query builder
```

## H2 Console (Dev)

Available when running with the default or dev profile:

```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:file:./data/candidatedb
Username: sa
Password: (empty)
```

## Production Deployment (PostgreSQL)

```bash
# Set environment variables
export DB_USERNAME=your_user
export DB_PASSWORD=your_password

# Run with prod profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Ensure a PostgreSQL database named `candidatedb` exists and the schema has been created (use `ddl-auto=update` for first run, then switch to `validate`).

## License

This project was built as a take-home assignment.
