# WP-CORE-02 — Discretion & Identity (Security): Session Guide

> **4 sessions** to build the complete Discretion & Identity (Security) (1 implementation + 2 testing + 1 bugfix & finalization).

## Dependency Analysis
- **Schema targets**: Security configuration, Entra ID, RBAC
- **FK dependencies**: WP-CORE-01
- **Build order**: Core logic -> Tests -> Finalization
- **Flyway versions**: V2

## Build Order

| Session | PRD Requirement | Flyway Version |
|---|---|---|
| Session 1 | Implementation of Discretion & Identity (Security) | V2 |
| Session 2 | Unit Tests | N/A |
| Session 3 | Integration Tests | N/A |
| Session 4 | Bugfix & Refinement | N/A |

## Implementation Sessions

```markdown
Read `docs/agents/guides/CONTEXT-BLOCK.md` for project context.

WP-Specific Context:
- Package: `com.luxury.core.security`
- Spec Target: Security configuration, Entra ID, RBAC

# Task: Build Discretion & Identity (Security)

## Scope
- **Spec**: Security configuration, Entra ID, RBAC
- **Flyway**: V2
- **Dependencies**: WP-CORE-01

## What to Build
1. **Security Configuration**: Entra ID (OIDC) integration in Spring Security.
2. **RBAC**: Define and implement Role-Based Access Control authorities.
3. **FLS Interceptor**: Implement Field-Level Security to strip `is_sensitive=true` fields.
4. **Encryption Service**: Application-level encryption/decryption service for Phantom Enclave attributes.

## Notes
- Ensure strict access control.
- Implement FLS accurately.
```

## Session 2: Unit Tests
- Use `@Mock` for `ChatLanguageModel` or `EmbeddingModel` from LangChain4j. Do not attempt to hit OpenAI in unit tests.
- Verify that ambient parsing events are fired using Spring's `ApplicationEventPublisher`.

## Session 3: Integration Tests
- Standard `postgres:16` image is **insufficient**.
- You MUST use `pgvector/pgvector:pg16` for Testcontainers to test the semantic search repositories.

## Session 4: Bugfix & Finalization
- Create a `walkthrough.md` in the `docs/` folder documenting what was built, files created, schema additions, and decisions made.
