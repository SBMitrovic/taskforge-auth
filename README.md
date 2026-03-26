# taskforge-auth

# taskforge-authservice

> This service is part of the **TaskForge** microservices architecture — a project management platform inspired by Jira. Full project coming soon.

## About

`taskforge-authservice` handles user authentication and authorization for the TaskForge platform. It provides JWT-based auth via a REST API.

**Endpoints:**
- `POST /api/auth/register` — register a new user
- `POST /api/auth/login` — login and receive a JWT token
- `GET /api/auth/me` — get current user info (requires token)

## Tech Stack

- Java 21, Spring Boot 3.4.1
- Spring Security + JWT (jjwt 0.12.3)
- Spring Data JPA + MySQL 8
- Lombok, Gradle

## Running Locally

**Prerequisites:** Java 21, Docker

**1. Start the database** (from [taskforge-infra](https://github.com/tvoje-ime/taskforge-infra)):
```bash
docker-compose up -d
```

**2. Run the service:**
```bash
./gradlew bootRun
```

Service runs on `http://localhost:1111`
