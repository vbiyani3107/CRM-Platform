# WP-HUB-01 — Clienteling Hub: Session Guide

> **4 sessions** to build the complete Clienteling Hub (1 implementation + 2 testing + 1 bugfix & finalization).

## Dependency Analysis
- **Schema targets**: SPEC.md: hyper_profiles and client_engagements schema
- **FK dependencies**: WP-CORE-06
- **Build order**: Core logic -> Tests -> Finalization
- **Flyway versions**: V6

## Build Order

| Session | PRD Requirement | Flyway Version |
|---|---|---|
| Session 1 | Implementation of Clienteling Hub | V6 |
| Session 2 | Unit Tests | N/A |
| Session 3 | Integration Tests | N/A |
| Session 4 | Bugfix & Refinement | N/A |

## Implementation Sessions

```markdown
Read `docs/agents/guides/CONTEXT-BLOCK.md` for project context.

WP-Specific Context:
- Package: `com.luxury.clienteling`
- Spec Target: SPEC.md: hyper_profiles and client_engagements schema

# Task: Build Clienteling Hub

## Scope
- **Spec**: SPEC.md: hyper_profiles and client_engagements schema
- **Flyway**: V6
- **Dependencies**: WP-CORE-06

## What to Build
1. **Entity**: `HyperProfile` and `ClientEngagement` (extend BaseEntity). Use `@JdbcTypeCode(SqlTypes.JSON)` for dynamic fields.
2. **Repository**: Repositories for hyper profiles and engagements.
3. **Service**: CRUD capabilities + async LangChain4j worker to extract persona/sentiment from engagement notes.
4. **Controller**: Discretion-first endpoints (filter data via FLS).
5. **Flyway**: `V6__create_clienteling.sql`.

## Notes
- Async LangChain4j hooks to trigger on save for sentiment extraction.
```

## Session 2: Unit Tests
- Use `@Mock` for `ChatLanguageModel` or `EmbeddingModel` from LangChain4j. Do not attempt to hit OpenAI in unit tests.
- Verify that ambient parsing events are fired using Spring's `ApplicationEventPublisher`.

## Session 3: Integration Tests
- Standard `postgres:16` image is **insufficient**.
- You MUST use `pgvector/pgvector:pg16` for Testcontainers to test the semantic search repositories.

## Session 4: Bugfix & Finalization
- Create a `walkthrough.md` in the `docs/` folder documenting what was built, files created, schema additions, and decisions made.
