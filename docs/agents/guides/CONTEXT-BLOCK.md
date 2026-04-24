# Bespoke Luxury Platform — Shared Agent Context

> **Version**: 1.0
> **Purpose**: Read this file at the start of every AI agent to load the global luxury platform context. Your agent prompt only needs to provide the WP-specific overrides and the task-specific Block 2.

---

## Project Overview
- **What**: AI-first Bespoke Luxury Brand Management Platform (Clienteling, Vault, Atelier, Concierge).
- **Tech stack**: Spring Boot 3.x, Java 21, PostgreSQL 16 (with `pgvector`), LangChain4j.
- **Module**: `luxury-core` — monolithic deployment targeting high-availability/discretion.
- **Shared libraries**: `luxury-common` (ApiResponse<T>, BaseEntity), `luxury-intelligence` (LangChain4j wrappers).

## Key Reference Files (READ these, don't ask me to paste them)
- **Architecture**: `ARCH.md` — Core design principles and ambient intelligence flow.
- **Specifications**: `SPEC.md` — Data schemas mapping pure luxury domains.
- **Entity pattern**: `com.luxury.core.model.HyperProfile` — extends BaseEntity, utilizes JSONB and vector arrays.
- **Service pattern**: `com.luxury.core.service.ClientelingService` — discretion checks, ambient async event firing.
- **Controller pattern**: `com.luxury.core.controller.VaultController` — ApiResponse wrapping, semantic search endpoints.

> **Note**: The WP-specific agent prompt will specify: the spec section, the Java package, and which existing module to follow as a pattern reference.

## Architecture Rules
1. Controller → Service → Repository pattern.
2. All responses wrapped in `ApiResponse<T>` — errors strictly follow RFC 7807 (`luxury-error-starter`).
3. Package: `com.luxury.[domain]` (e.g., `.clienteling`, `.vault`, `.atelier`, `.concierge`) with sub-packages: `controller`, `service`, `model`, `repository`, `dto`.
4. Entities extend `com.luxury.common.model.BaseEntity` (id, createdAt, updatedAt, createdBy, updatedBy, version, isDeleted).
5. Lombok (@Getter, @Setter, @NoArgsConstructor), Spring Data JPA, Jakarta Validation (NOT javax).
6. Soft-delete (`isDeleted`) + optimistic locking (`@Version`) via BaseEntity.
7. Pagination: default 20, sort by `createdAt` DESC.
8. JSON: `snake_case` (Jackson global config), Java: `camelCase`, URLs: `kebab-case`.
9. All IDs: UUID. All timestamps: `Instant` (ISO 8601 UTC).
10. Flyway migrations: additive-only, forward-compatible, schema `luxury_brand`.

## AI & Discretion-First Design
- **JSONB Dynamic Metadata**: Use `@JdbcTypeCode(SqlTypes.JSON)` for unstructured attributes (e.g., `taste_vectors`, `family_tree`). No hardcoded rigid columns for evolving client tastes.
- **Vector Embeddings**: Store 1536-dimensional OpenAI/LangChain embeddings using `pgvector` (`vector` datatype) for semantic searches (aesthetic matching).
- **Discretion by Design**: Services must implement explicit Field-Level Security (FLS) checks based on the associate's JWT scope (e.g., stripping wealth bands).
- **Ambient Intelligence**: LangChain4j calls (like parsing interaction notes) must happen asynchronously via Spring Events (`@Async` and `@EventListener`), never blocking the main web thread.

## What NOT to Build (applies to ALL agents)
- No unit tests or integration tests (these are handled in separate, dedicated agents).
- Do NOT modify existing modules unless explicitly requested.
- Do NOT generate frontend (React/Tailwind) code when working on backend packages.
- Do NOT implement live OpenAI API calls; assume LangChain4j services use Stub/Mock properties in local dev.
- **Crucial**: Create a `walkthrough.md` in the `docs/` folder documenting: what was built, files created, schema additions, and decisions made. Commit it alongside the code.
