# WP-CORE-04 — Ambient Intelligence: Session Guide

> **4 sessions** to build the complete Ambient Intelligence (1 implementation + 2 testing + 1 bugfix & finalization).

## Dependency Analysis
- **Schema targets**: LangChain4j bridge configuration
- **FK dependencies**: WP-CORE-03
- **Build order**: Core logic -> Tests -> Finalization
- **Flyway versions**: V4

## Build Order

| Session | PRD Requirement | Flyway Version |
|---|---|---|
| Session 1 | Implementation of Ambient Intelligence | V4 |
| Session 2 | Unit Tests | N/A |
| Session 3 | Integration Tests | N/A |
| Session 4 | Bugfix & Refinement | N/A |

## Implementation Sessions

```markdown
Read `docs/agents/guides/CONTEXT-BLOCK.md` for project context.

WP-Specific Context:
- Package: `com.luxury.core.intelligence`
- Spec Target: LangChain4j bridge configuration

# Task: Build Ambient Intelligence

## Scope
- **Spec**: LangChain4j bridge configuration
- **Flyway**: V4
- **Dependencies**: WP-CORE-03

## What to Build
1. **Dependencies**: Add LangChain4j dependencies.
2. **Embedding Model**: Configure Embedding Model bean.
3. **Chat Model**: Configure Chat Language Model bean for text extraction.
4. **Service**: Develop Semantic Search service translating text to vectors.

## Notes
- Ambient Intelligence LangChain4j calls must happen asynchronously via Spring Events.
```

## Session 2: Unit Tests
- Use `@Mock` for `ChatLanguageModel` or `EmbeddingModel` from LangChain4j. Do not attempt to hit OpenAI in unit tests.
- Verify that ambient parsing events are fired using Spring's `ApplicationEventPublisher`.

## Session 3: Integration Tests
- Standard `postgres:16` image is **insufficient**.
- You MUST use `pgvector/pgvector:pg16` for Testcontainers to test the semantic search repositories.

## Session 4: Bugfix & Finalization
- Create a `walkthrough.md` in the `docs/` folder documenting what was built, files created, schema additions, and decisions made.
