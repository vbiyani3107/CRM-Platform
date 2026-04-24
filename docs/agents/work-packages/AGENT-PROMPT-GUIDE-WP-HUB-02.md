# WP-HUB-02 — The Vault: Session Guide

> **4 sessions** to build the complete The Vault (1 implementation + 2 testing + 1 bugfix & finalization).

## Dependency Analysis
- **Schema targets**: SPEC.md: curated_vault schema
- **FK dependencies**: WP-HUB-01
- **Build order**: Core logic -> Tests -> Finalization
- **Flyway versions**: V7

## Build Order

| Session | PRD Requirement | Flyway Version |
|---|---|---|
| Session 1 | Implementation of The Vault | V7 |
| Session 2 | Unit Tests | N/A |
| Session 3 | Integration Tests | N/A |
| Session 4 | Bugfix & Refinement | N/A |

## Implementation Sessions

```markdown
Read `docs/agents/guides/CONTEXT-BLOCK.md` for project context.

WP-Specific Context:
- Package: `com.luxury.vault`
- Spec Target: SPEC.md: curated_vault schema

# Task: Build The Vault

## Scope
- **Spec**: SPEC.md: curated_vault schema
- **Flyway**: V7
- **Dependencies**: WP-HUB-01

## What to Build
1. **Entity**: `CuratedVaultItem` (extend BaseEntity). Map vector arrays.
2. **Repository**: Include specific pgvector KNN (K-Nearest Neighbors) native queries.
3. **Service**: Auto-generate `aesthetic_embedding` on vault item save.
4. **Controller**: Endpoints for semantic vibe search.
5. **Flyway**: `V7__create_curated_vault.sql`.

## Notes
- pgvector extensions must be utilized for vibe searches.
```

## Session 2: Unit Tests
- Use `@Mock` for `ChatLanguageModel` or `EmbeddingModel` from LangChain4j. Do not attempt to hit OpenAI in unit tests.
- Verify that ambient parsing events are fired using Spring's `ApplicationEventPublisher`.

## Session 3: Integration Tests
- Standard `postgres:16` image is **insufficient**.
- You MUST use `pgvector/pgvector:pg16` for Testcontainers to test the semantic search repositories.

## Session 4: Bugfix & Finalization
- Create a `walkthrough.md` in the `docs/` folder documenting what was built, files created, schema additions, and decisions made.
