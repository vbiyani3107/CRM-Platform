# WP-HUB-06 — The Private Wardrobe: Session Guide

> **4 sessions** to build the complete The Private Wardrobe (1 implementation + 2 testing + 1 bugfix & finalization).

## Dependency Analysis
- **Schema targets**: SPEC.md: private_wardrobes schema
- **FK dependencies**: WP-HUB-05
- **Build order**: Core logic -> Tests -> Finalization
- **Flyway versions**: V11

## Build Order

| Session | PRD Requirement | Flyway Version |
|---|---|---|
| Session 1 | Implementation of The Private Wardrobe | V11 |
| Session 2 | Unit Tests | N/A |
| Session 3 | Integration Tests | N/A |
| Session 4 | Bugfix & Refinement | N/A |

## Implementation Sessions

```markdown
Read `docs/agents/guides/CONTEXT-BLOCK.md` for project context.

WP-Specific Context:
- Package: `com.luxury.wardrobe`
- Spec Target: SPEC.md: private_wardrobes schema

# Task: Build The Private Wardrobe

## Scope
- **Spec**: SPEC.md: private_wardrobes schema
- **Flyway**: V11
- **Dependencies**: WP-HUB-05

## What to Build
1. **Entity**: `PrivateWardrobeItem` (extend BaseEntity).
2. **Repository**: Repository for wardrobe.
3. **Service**: Update Semantic Search to optionally factor Wardrobe baseline.
4. **Controller**: Endpoints for logging competitor items.
5. **Flyway**: `V11__create_private_wardrobes.sql`.

## Notes
- Factor Wardrobe baseline into aesthetic searches.
```

## Session 2: Unit Tests
- Use `@Mock` for `ChatLanguageModel` or `EmbeddingModel` from LangChain4j. Do not attempt to hit OpenAI in unit tests.
- Verify that ambient parsing events are fired using Spring's `ApplicationEventPublisher`.

## Session 3: Integration Tests
- Standard `postgres:16` image is **insufficient**.
- You MUST use `pgvector/pgvector:pg16` for Testcontainers to test the semantic search repositories.

## Session 4: Bugfix & Finalization
- Create a `walkthrough.md` in the `docs/` folder documenting what was built, files created, schema additions, and decisions made.
