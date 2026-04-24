# WP-HUB-05 — Provenance & Digital Twins: Session Guide

> **4 sessions** to build the complete Provenance & Digital Twins (1 implementation + 2 testing + 1 bugfix & finalization).

## Dependency Analysis
- **Schema targets**: SPEC.md: provenance_ledger schema
- **FK dependencies**: WP-HUB-04
- **Build order**: Core logic -> Tests -> Finalization
- **Flyway versions**: V10

## Build Order

| Session | PRD Requirement | Flyway Version |
|---|---|---|
| Session 1 | Implementation of Provenance & Digital Twins | V10 |
| Session 2 | Unit Tests | N/A |
| Session 3 | Integration Tests | N/A |
| Session 4 | Bugfix & Refinement | N/A |

## Implementation Sessions

```markdown
Read `docs/agents/guides/CONTEXT-BLOCK.md` for project context.

WP-Specific Context:
- Package: `com.luxury.provenance`
- Spec Target: SPEC.md: provenance_ledger schema

# Task: Build Provenance & Digital Twins

## Scope
- **Spec**: SPEC.md: provenance_ledger schema
- **Flyway**: V10
- **Dependencies**: WP-HUB-04

## What to Build
1. **Entity**: `ProvenanceLedgerEntry` (extend BaseEntity).
2. **Repository**: Repository for provenance ledger.
3. **Service**: Build blockchain integration service for Digital Product Passports.
4. **Controller**: Endpoints visualizing cryptographic item history.
5. **Flyway**: `V10__create_provenance_ledger.sql`.

## Notes
- Ensure robust immutability concepts in the ledger.
```

## Session 2: Unit Tests
- Use `@Mock` for `ChatLanguageModel` or `EmbeddingModel` from LangChain4j. Do not attempt to hit OpenAI in unit tests.
- Verify that ambient parsing events are fired using Spring's `ApplicationEventPublisher`.

## Session 3: Integration Tests
- Standard `postgres:16` image is **insufficient**.
- You MUST use `pgvector/pgvector:pg16` for Testcontainers to test the semantic search repositories.

## Session 4: Bugfix & Finalization
- Create a `walkthrough.md` in the `docs/` folder documenting what was built, files created, schema additions, and decisions made.
