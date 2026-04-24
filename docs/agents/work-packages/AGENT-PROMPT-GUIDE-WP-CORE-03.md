# WP-CORE-03 — The Dynamic API: Session Guide

> **4 sessions** to build the complete The Dynamic API (1 implementation + 2 testing + 1 bugfix & finalization).

## Dependency Analysis
- **Schema targets**: Dynamic CRUD API endpoints
- **FK dependencies**: WP-CORE-02
- **Build order**: Core logic -> Tests -> Finalization
- **Flyway versions**: V3

## Build Order

| Session | PRD Requirement | Flyway Version |
|---|---|---|
| Session 1 | Implementation of The Dynamic API | V3 |
| Session 2 | Unit Tests | N/A |
| Session 3 | Integration Tests | N/A |
| Session 4 | Bugfix & Refinement | N/A |

## Implementation Sessions

```markdown
Read `docs/agents/guides/CONTEXT-BLOCK.md` for project context.

WP-Specific Context:
- Package: `com.luxury.core.api`
- Spec Target: Dynamic CRUD API endpoints

# Task: Build The Dynamic API

## Scope
- **Spec**: Dynamic CRUD API endpoints
- **Flyway**: V3
- **Dependencies**: WP-CORE-02

## What to Build
1. **Controller**: `GenericEntityController` mapping to `/api/v1/entities/{entityName}`.
2. **Validation**: Dynamic JSON payload validation interceptor querying `sys_dictionary`.
3. **Error Handling**: RFC 7807 compliant `@ControllerAdvice` for pristine error handling.

## Notes
- Responses must be wrapped in `ApiResponse<T>`.
```

## Session 2: Unit Tests
- Use `@Mock` for `ChatLanguageModel` or `EmbeddingModel` from LangChain4j. Do not attempt to hit OpenAI in unit tests.
- Verify that ambient parsing events are fired using Spring's `ApplicationEventPublisher`.

## Session 3: Integration Tests
- Standard `postgres:16` image is **insufficient**.
- You MUST use `pgvector/pgvector:pg16` for Testcontainers to test the semantic search repositories.

## Session 4: Bugfix & Finalization
- Create a `walkthrough.md` in the `docs/` folder documenting what was built, files created, schema additions, and decisions made.
