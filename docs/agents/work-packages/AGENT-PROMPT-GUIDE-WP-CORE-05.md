# WP-CORE-05 — The Luxury Shell (Frontend): Session Guide

> **4 sessions** to build the complete The Luxury Shell (Frontend) (1 implementation + 2 testing + 1 bugfix & finalization).

## Dependency Analysis
- **Schema targets**: React SPA foundation
- **FK dependencies**: WP-CORE-04
- **Build order**: Core logic -> Tests -> Finalization
- **Flyway versions**: N/A

## Build Order

| Session | PRD Requirement | Flyway Version |
|---|---|---|
| Session 1 | Implementation of The Luxury Shell (Frontend) | N/A |
| Session 2 | Unit Tests | N/A |
| Session 3 | Integration Tests | N/A |
| Session 4 | Bugfix & Refinement | N/A |

## Implementation Sessions

```markdown
Read `docs/agents/guides/CONTEXT-BLOCK.md` for project context.

WP-Specific Context:
- Package: `com.luxury.ui`
- Spec Target: React SPA foundation

# Task: Build The Luxury Shell (Frontend)

## Scope
- **Spec**: React SPA foundation
- **Flyway**: N/A
- **Dependencies**: WP-CORE-04

## What to Build
1. **Scaffolding**: React 18 + TypeScript using Vite.
2. **Styling**: Install and configure Tailwind CSS with Luxury Design Tokens.
3. **Routing**: React Router with root layouts for 'Showroom Mode' and 'Backstage Mode'.
4. **Components**: Build dynamic layout components driven by JSON configuration.

## Notes
- Do NOT generate backend code for this package.
```

## Session 2: Unit Tests
- Use `@Mock` for `ChatLanguageModel` or `EmbeddingModel` from LangChain4j. Do not attempt to hit OpenAI in unit tests.
- Verify that ambient parsing events are fired using Spring's `ApplicationEventPublisher`.

## Session 3: Integration Tests
- Standard `postgres:16` image is **insufficient**.
- You MUST use `pgvector/pgvector:pg16` for Testcontainers to test the semantic search repositories.

## Session 4: Bugfix & Finalization
- Create a `walkthrough.md` in the `docs/` folder documenting what was built, files created, schema additions, and decisions made.
