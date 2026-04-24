# WP-CORE-01 — Persistence & Aesthetic Storage: Session Guide

> **4 sessions** to build the complete Persistence & Aesthetic Storage (1 implementation + 2 testing + 1 bugfix & finalization).

## Dependency Analysis
- **Schema targets**: Infrastructure setup (PostgreSQL, pgvector)
- **FK dependencies**: none
- **Build order**: Core logic -> Tests -> Finalization
- **Flyway versions**: V1

## Build Order

| Session | PRD Requirement | Flyway Version |
|---|---|---|
| Session 1 | Implementation of Persistence & Aesthetic Storage | V1 |
| Session 2 | Unit Tests | N/A |
| Session 3 | Integration Tests | N/A |
| Session 4 | Bugfix & Refinement | N/A |

## Implementation Sessions

```markdown
Read `docs/agents/guides/CONTEXT-BLOCK.md` for project context.

WP-Specific Context:
- Package: `com.luxury.core.persistence`
- Spec Target: Infrastructure setup (PostgreSQL, pgvector)

# Task: Build Persistence & Aesthetic Storage

## Scope
- **Spec**: Infrastructure setup (PostgreSQL, pgvector)
- **Flyway**: V1
- **Dependencies**: none

## What to Build
1. **Docker Compose**: `docker-compose.yml` for PostgreSQL 16 + pgvector.
2. **Spring Boot Scaffolding**: Web, Data JPA, Postgres, Flyway.
3. **Application Properties**: Local DB connection configuration.
4. **Flyway**: `V1__init_foundation.sql` (CREATE EXTENSION IF NOT EXISTS vector; sys_db_object, sys_dictionary).
5. **Repository**: Spring Data repositories supporting JSONB binding and HNSW indices.

## Notes
- Use `@JdbcTypeCode(SqlTypes.JSON)` for unstructured attributes.
- Store 1536-dimensional OpenAI embeddings using `pgvector`.
```

## Session 2: Unit Tests
- Use `@Mock` for `ChatLanguageModel` or `EmbeddingModel` from LangChain4j. Do not attempt to hit OpenAI in unit tests.
- Verify that ambient parsing events are fired using Spring's `ApplicationEventPublisher`.

## Session 3: Integration Tests
- Standard `postgres:16` image is **insufficient**.
- You MUST use `pgvector/pgvector:pg16` for Testcontainers to test the semantic search repositories.

## Session 4: Bugfix & Finalization
- Create a `walkthrough.md` in the `docs/` folder documenting what was built, files created, schema additions, and decisions made.
