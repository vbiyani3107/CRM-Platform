# WP-HUB-04 — Concierge & Experiential Management: Session Guide

> **4 sessions** to build the complete Concierge & Experiential Management (1 implementation + 2 testing + 1 bugfix & finalization).

## Dependency Analysis
- **Schema targets**: SPEC.md: experiential_requests schema
- **FK dependencies**: WP-HUB-03
- **Build order**: Core logic -> Tests -> Finalization
- **Flyway versions**: V9

## Build Order

| Session | PRD Requirement | Flyway Version |
|---|---|---|
| Session 1 | Implementation of Concierge & Experiential Management | V9 |
| Session 2 | Unit Tests | N/A |
| Session 3 | Integration Tests | N/A |
| Session 4 | Bugfix & Refinement | N/A |

## Implementation Sessions

```markdown
Read `docs/agents/guides/CONTEXT-BLOCK.md` for project context.

WP-Specific Context:
- Package: `com.luxury.concierge`
- Spec Target: SPEC.md: experiential_requests schema

# Task: Build Concierge & Experiential Management

## Scope
- **Spec**: SPEC.md: experiential_requests schema
- **Flyway**: V9
- **Dependencies**: WP-HUB-03

## What to Build
1. **Entity**: `ExperientialRequest` and `SignificantDate` (extend BaseEntity).
2. **Repository**: Repository for requests and dates.
3. **Service**: Implement Spring Batch job to scan `significant_dates` +21 days.
4. **Controller**: Endpoints for Concierge Alerts.
5. **Flyway**: `V9__create_experiential_requests.sql`.

## Notes
- Predictive engagement.
```

## Session 2: Unit Tests
- Use `@Mock` for `ChatLanguageModel` or `EmbeddingModel` from LangChain4j. Do not attempt to hit OpenAI in unit tests.
- Verify that ambient parsing events are fired using Spring's `ApplicationEventPublisher`.

## Session 3: Integration Tests
- Standard `postgres:16` image is **insufficient**.
- You MUST use `pgvector/pgvector:pg16` for Testcontainers to test the semantic search repositories.

## Session 4: Bugfix & Finalization
- Create a `walkthrough.md` in the `docs/` folder documenting what was built, files created, schema additions, and decisions made.
