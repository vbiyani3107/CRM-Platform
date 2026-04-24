# WP-CORE-06 — Multi-Modal AI Engine: Session Guide

> **4 sessions** to build the complete Multi-Modal AI Engine (1 implementation + 2 testing + 1 bugfix & finalization).

## Dependency Analysis
- **Schema targets**: Vision Model integration
- **FK dependencies**: WP-CORE-04
- **Build order**: Core logic -> Tests -> Finalization
- **Flyway versions**: V5

## Build Order

| Session | PRD Requirement | Flyway Version |
|---|---|---|
| Session 1 | Implementation of Multi-Modal AI Engine | V5 |
| Session 2 | Unit Tests | N/A |
| Session 3 | Integration Tests | N/A |
| Session 4 | Bugfix & Refinement | N/A |

## Implementation Sessions

```markdown
Read `docs/agents/guides/CONTEXT-BLOCK.md` for project context.

WP-Specific Context:
- Package: `com.luxury.core.multimodal`
- Spec Target: Vision Model integration

# Task: Build Multi-Modal AI Engine

## Scope
- **Spec**: Vision Model integration
- **Flyway**: V5
- **Dependencies**: WP-CORE-04

## What to Build
1. **Integration**: Vision Model API for generating image embeddings.
2. **Controller Update**: Add `multipart/form-data` support to Semantic Search controller.
3. **Vector Sync**: Unify text and image embedding dimensions for `pgvector` querying.

## Notes
- Ambient execution required for image processing.
```

## Session 2: Unit Tests
- Use `@Mock` for `ChatLanguageModel` or `EmbeddingModel` from LangChain4j. Do not attempt to hit OpenAI in unit tests.
- Verify that ambient parsing events are fired using Spring's `ApplicationEventPublisher`.

## Session 3: Integration Tests
- Standard `postgres:16` image is **insufficient**.
- You MUST use `pgvector/pgvector:pg16` for Testcontainers to test the semantic search repositories.

## Session 4: Bugfix & Finalization
- Create a `walkthrough.md` in the `docs/` folder documenting what was built, files created, schema additions, and decisions made.
