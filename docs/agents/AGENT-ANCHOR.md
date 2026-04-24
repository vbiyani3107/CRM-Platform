# Bespoke Luxury Platform — Agent Anchor

> **Version**: 2.0 (The Luxury Pivot)
> **Role**: The Single Source of Truth for Development Context.

> [!NOTE]
> This file must be read by any developer or AI Agent at the start of an agent session. It tracks the exact state of the project, the core stack decisions, and the immediate next steps to prevent context loss.

---

## 1. The Core Stack & Architecture

- **Backend**: Java 21, Spring Boot 3.x, Spring Data JPA/JDBC.
- **Frontend**: React 18, TypeScript, Vite, Tailwind CSS (Luxury Design Tokens).
- **Database**: PostgreSQL with `pgvector` (HNSW indices) and `JSONB` (GIN indices).
- **AI / LLM Bridge**: LangChain4j. Vector embeddings are targeted at 1536 dimensions (OpenAI standard, though configurable).
- **Workflow / BPMN**: Camunda 8 (for complex luxury lifecycles).

## 2. Environment Setup & Execution

### Running Locally
1. Start the database (Postgres + pgvector):
   ```bash
   docker-compose up -d
   ```
2. Start the Backend:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Start the Frontend:
   ```bash
   npm run dev
   ```

## 3. Current Progress & Context

We are currently operating in **Phase 0: Core Foundation**.

| Domain | Status | Active Work Package |
|---|---|---|
| Core Foundation | Scaffolding | `WP-CORE-01` |
| Clienteling Hub | Pending | - |
| The Vault | Pending | - |
| Atelier | Pending | - |
| Concierge | Pending | - |

## 4. Immediate Next Steps

*(Update this section at the end of every agent session)*

1. [ ] Generate `docker-compose.yml` for Postgres + pgvector in the root project folder.
2. [ ] Scaffold the Spring Boot `foundation` service.
3. [ ] Scaffold the React `luxury-shell` frontend.

---
**Definition of Done (DoD) for current task:**
The PostgreSQL database is accessible on port 5432 with the `vector` extension installed, and a basic Spring Boot application context loads successfully.
