# Testing Strategy

## Backend Testing
- **Unit Tests**: Minimum 85% coverage via JUnit 5.
- **Integration Tests**: Testcontainers for PostgreSQL + pgvector validation.

## Frontend Testing
- **Component Tests**: React Testing Library.
- **E2E Tests**: Playwright for critical paths (e.g., Bespoke Commission creation).

## AI / LangChain4j Testing
- AI prompt outputs must be evaluated against a suite of known ground-truth answers to prevent hallucination regressions.
