# AGENT-PROMPT-GUIDE — AI Session Prompts for Bespoke Luxury Platform

> **Version**: 1.0
> **Purpose**: Reusable methodology, templates, and best practices for converting our bespoke Luxury Work Packages into flawless AI agent sessions.

---

## 1. Create Your Session Guide — Ready-to-Use Prompt

When you are ready to implement a Luxury Hub (e.g., Clienteling, The Vault), use the prompt below to **auto-generate a complete agent guide** in one AI session. Paste the entire code fence into a fresh conversation.

### How to Use

1. Replace `[XX]` with your Work Package number from `WORK_PACKAGES.md` (e.g., `02`).
2. Replace `[NAME]` with the Hub name (e.g., `CLIENTELING` or `THE_VAULT`).
3. Replace `[DOMAIN]` with the Java package domain (e.g., `clienteling`, `vault`, `atelier`, `concierge`).
4. Replace `[V_START]` with the next available Flyway version (check `src/main/resources/db/migration/`).
5. Paste the entire block into a new AI conversation.
6. **Review the draft** the AI produces before telling it to finalize.

### The Prompt

> Copy everything inside the code fence below and paste into a new conversation.

```markdown
# Task: Create Session Guide for WP[XX] — [NAME] Hub

## What You Are Creating
Generate a complete, ready-to-use **agent guide** file following our established AI-first methodology. The output file must be:

**Filename**: `docs/agent-guides/README-WP[XX]-[NAME]-AGENT-GUIDE.md`

## Reference Files — READ ALL BEFORE STARTING

### Template & Process
- **Session prompt template**: `docs/agent-guides/AGENT-PROMPT-GUIDE.md` — follow the two-block prompt structure (Block 1: Anchor File + Block 2: Session-Specific) and all luxury architecture rules exactly.
- **Anchor File**: `docs/agent-guides/CONTEXT-BLOCK.md` — the universal context for the Bespoke Luxury Platform.

### Source Specs
- **Work Packages**: `WORK_PACKAGES.md` — verify the scope of WP[XX].
- **Technical Specs**: `SPEC.md` — this is your PRIMARY schema source. Read the tables mapping to this domain.
- **Architecture**: `ARCH.md` — verify ambient intelligence and pgvector integration details.
- **PRD**: `PRD.md` — cross-reference the business value for the specific Luxury Hub.

## What to Do — Step by Step

### Step 1: Analyse the Spec
1. Read `SPEC.md` for schemas relating to this hub (e.g., `hyper_profiles`, `curated_vault`).
2. List all database tables, especially noting `JSONB` fields and `vector` fields.
3. Identify **all foreign key dependencies** (e.g., engagements depend on profiles).

### Step 2: Determine Build Order
1. Draw the dependency graph (parent tables before child tables).
2. Assign **sequential Flyway version numbers** starting from V[V_START].

### Step 3: Generate the Session Guide
Create `docs/agent-guides/README-WP[XX]-[NAME]-AGENT-GUIDE.md` with this exact structure:

1. **Title**: `# WP[XX] — [NAME] Hub: Session Guide`
2. **Summary line**: `> **N sessions** to build the complete [NAME] Hub (M implementation + 2 testing + 1 bugfix + 1 finalization).`
3. **Dependency Analysis section**: Schema targets, FK dependencies, build order, Flyway versions.
4. **Build Order table**: Ordered sessions with PRD requirements and Flyway versions.
5. **Implementation Sessions (one per module)**: Each in a copyable markdown code fence containing:
   - **Line 1 Reference**: "Read `docs/agent-guides/CONTEXT-BLOCK.md` for project context."
   - **WP-Specific Context**:
     - Package: `com.luxury.[DOMAIN]`
     - Spec Target: e.g., `SPEC.md: hyper_profiles schema`
   - **Session-Specific Prompt (Block 2)**:
     - Flyway version.
     - What to build (Entity, Repository, Service, Controller).
     - Notes: *Explicitly mention pgvector extensions, JSONB mappings, or LangChain4j ambient execution required.*
6. **Session N+1: Unit Tests** — follow the template for testing services (mocking LangChain4j).
7. **Session N+2: Integration Tests** — using Testcontainers with the `pgvector` image.
8. **Session N+3: Bugfix & Refinement**.
9. **Session N+4: Finalization & Documentation**.

## Quality Checks Before Presenting
- [ ] Ensure the AI does not invent rigid columns where `JSONB` or `vector` should be used.
- [ ] Flyway versions are sequential.
- [ ] Dependencies are mapped (e.g., `hyper_profiles` built before `client_engagements`).

**Present the draft for review before writing the file.**
```

---

## 2. The Two-Part Prompt Structure (Anchor File Pattern)

Every AI agent session uses exactly **two blocks** pasted into a new conversation.
To save tokens and enforce consistency, we use the **Anchor File Pattern**.

```
┌─────────────────────────────────────────────────────────────┐
│  Line 1: "Read docs/agent-guides/CONTEXT-BLOCK.md"        │
│                                                             │
│  WP-Specific Context (~5 lines):                            │
│    - Target Domain, Package, Spec references                │
│                                                             │
│  Block 2: Session-Specific Prompt  (~30 lines)              │
│    - Scope, Flyway version, what to build, and luxury-      │
│      specific instructions (e.g. pgvector queries, async    │
│      LangChain parsers).                                    │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Block 2 Template: Session-Specific Prompt

Create one of these **per module/session** in your agent guide.

```markdown
# Task: Build [Module Name] for [Hub Name]

## Scope
- **Spec**: Implement schema `[table_name]` from `SPEC.md`.
- **Flyway**: V[N] (next available after previous module).
- **Dependencies**: [what must exist before this module, or "none"].

## What to Build
Read the schema and build:
1. **Entity**: `[EntityName]` (extends BaseEntity). Use `@JdbcTypeCode(SqlTypes.JSON)` for dynamic fields and map vector arrays.
2. **Repository**: `[EntityName]Repository`. Include specific pgvector KNN (K-Nearest Neighbors) native queries if this is the Vault or Taste profile.
3. **Service**: `[EntityName]Service` — [CRUD capabilities + Ambient Intelligence async events].
4. **Controller**: `[EntityName]Controller` — Discretion-first endpoints (filter data via FLS).
5. **DTOs**: [CreateRequest, UpdateRequest, Response].
6. **Flyway**: `V[N]__create_[table_name].sql`. Ensure `CREATE EXTENSION IF NOT EXISTS vector;` is handled.

## Notes
- [Special validation or discretion/FLS rules]
- [Async LangChain4j hooks to trigger on save]
```

---

## 4. Specialized Luxury Testing

When running testing sessions, you must account for the AI/PostgreSQL components:

### Unit Tests
- Always use `@Mock` for `ChatLanguageModel` or `EmbeddingModel` from LangChain4j. Do not attempt to hit OpenAI in unit tests.
- Verify that ambient parsing events are fired using Spring's `ApplicationEventPublisher`.

### Integration Tests
- Standard `postgres:16` image is **insufficient**.
- You MUST use `pgvector/pgvector:pg16` for Testcontainers to test the semantic search repositories.

## 5. Rules for Good Prompts
- **Always enforce JSONB**: Do not let the AI build rigid relational tables for data that is inherently unstructured (like a client's evolving taste profile).
- **Discretion checks**: Remind the AI to implement authorization checks in the Service layer (e.g., stripping `wealth_band` from DTOs for standard associate views).
- **Asynchronous AI**: Never block a Controller waiting for an LLM response. The AI must design systems that return HTTP 202 Accepted while LangChain4j processes data in the background.
