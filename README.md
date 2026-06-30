# second-brain

A backend ingestion service built with Spring Boot, exposing REST APIs for authentication, log streams, and events.

## Prerequisites

- Java 25 (GraalVM or standard JDK)
- Maven 3.9+
- Docker & Docker Compose (for local PostgreSQL)

## Running Locally

### 1. Start PostgreSQL

```bash
docker compose up postgres -d
```

### 2. Set environment variables (optional overrides)

| Variable | Default | Description |
|---|---|---|
| `PORT` | `8080` | HTTP server port |
| `DB_URL` | `jdbc:postgresql://localhost:5432/second_brain` | JDBC URL |
| `DB_USERNAME` | `second_brain` | DB username |
| `DB_PASSWORD` | `second_brain` | DB password |
| `JWT_SECRET` | `change-me-in-production-must-be-at-least-32-chars-long` | HS256 signing secret (min 32 chars) |
| `JWT_EXPIRATION_MS` | `86400000` | Token TTL in milliseconds (default 24 h) |

> **Important:** Always set `JWT_SECRET` to a strong random value in production.

### 3. Run the application

```bash
mvn -f server/pom.xml spring-boot:run
```

The server starts on `http://localhost:8080`. Android emulator can reach it at `http://10.0.2.2:8080`.

---

## API Reference

All endpoints are prefixed with `/api`.

### Auth (public)

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register a new user, returns JWT |
| `POST` | `/api/auth/login` | Authenticate, returns JWT |

**Register body:**
```json
{ "name": "Alice", "email": "alice@example.com", "password": "secret123" }
```

**Login body:**
```json
{ "email": "alice@example.com", "password": "secret123" }
```

### Streams (protected — `Authorization: ******

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/streams` | List all streams for the authenticated user |
| `POST` | `/api/streams` | Create a new stream |

**Create stream body:**
```json
{ "name": "My Stream" }
```

### Events (protected — `Authorization: ******

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/streams/{streamId}/events` | List all events for a stream |
| `POST` | `/api/streams/{streamId}/events` | Push a new event to a stream |

**Create event body:**
```json
{ "timestamp": "2025-01-01T00:00:00Z", "level": "INFO", "message": "Application started" }
```

---

## Running with Docker Compose (full stack)

```bash
JWT_SECRET=your-strong-secret docker compose up --build
```

This builds the native binary and starts both PostgreSQL and the server.