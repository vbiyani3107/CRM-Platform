# WP-CORE-01 — Persistence & Aesthetic Storage: Walkthrough

> **Date**: April 2026
> **Work Package**: WP-CORE-01
> **Status**: ✅ Complete

---

## What Was Built

The complete persistence and aesthetic storage foundation for the Bespoke Luxury Platform, enabling:
1. **PostgreSQL 16 with pgvector** — containerized database supporting relational, JSONB, and vector data types
2. **Spring Boot 3.3 application scaffold** — production-ready Java 21 project with all core dependencies
3. **Dynamic metadata engine** — entity and attribute registries (`sys_db_object`, `sys_dictionary`) powering the platform's schema-on-write capability
4. **Flyway V1 migration** — idempotent, forward-compatible schema setup with pgvector extension, GIN indexes, and seed data
5. **Comprehensive test suite** — unit tests (H2), integration tests (Testcontainers + pgvector), and event publisher verification

---

## Files Created

### Infrastructure
| File | Purpose |
|---|---|
| `docker-compose.yml` | PostgreSQL 16 + pgvector container provisioning |
| `pom.xml` | Maven build with Spring Boot 3.3, JPA, Flyway, pgvector, Testcontainers |

### Application Source (`src/main/java/com/luxury/`)
| File | Purpose |
|---|---|
| `LuxuryCoreApplication.java` | Spring Boot entry point with `@EnableAsync` |
| `common/model/BaseEntity.java` | Abstract base with UUID id, audit fields, soft-delete, optimistic locking |
| `common/dto/ApiResponse.java` | Generic response wrapper with RFC 7807 error detail |
| `common/exception/GlobalExceptionHandler.java` | `@RestControllerAdvice` for consistent error handling |
| `core/persistence/model/SysDbObject.java` | Entity Registry — tracks luxury domain tables |
| `core/persistence/model/SysDictionary.java` | Attribute Registry — tracks fields, types, sensitivity |
| `core/persistence/repository/SysDbObjectRepository.java` | Spring Data JPA for entity registry |
| `core/persistence/repository/SysDictionaryRepository.java` | Spring Data JPA with native JSONB queries |
| `core/persistence/config/PersistenceConfig.java` | pgvector JDBC type registration + Flyway strategy |

### Configuration (`src/main/resources/`)
| File | Purpose |
|---|---|
| `application.yml` | Datasource, Flyway, Jackson snake_case, Hibernate validate |
| `db/migration/V1__init_foundation.sql` | pgvector extension + sys_db_object + sys_dictionary + seed data |

### Unit Tests (`src/test/java/`)
| File | Purpose |
|---|---|
| `common/model/BaseEntityTest.java` | Lifecycle callbacks, defaults, audit fields |
| `common/dto/ApiResponseTest.java` | Factory methods, error structure validation |
| `core/persistence/repository/SysDbObjectRepositoryTest.java` | CRUD, JSONB, existence checks |
| `core/persistence/repository/SysDictionaryRepositoryTest.java` | Entity filtering, sensitivity, custom attrs |
| `core/persistence/event/AmbientEventPublisherTest.java` | Spring event publish/receive verification |

### Integration Tests (`src/test/java/`)
| File | Purpose |
|---|---|
| `core/persistence/integration/AbstractPostgresIT.java` | Shared Testcontainers base (pgvector/pgvector:pg16) |
| `core/persistence/integration/FlywayMigrationIT.java` | Migration verification: tables, indexes, seeds |
| `core/persistence/integration/RepositoryIT.java` | JSONB binding, containment queries, soft-delete, versioning |
| `core/persistence/integration/VectorSearchIT.java` | Vector DDL, HNSW indexes, L2 + cosine queries |

### Test Configuration
| File | Purpose |
|---|---|
| `src/test/resources/application-test.yml` | H2 in-memory config for unit tests |

---

## Schema Additions (Flyway V1)

### `sys_db_object`
| Column | Type | Notes |
|---|---|---|
| id | UUID (PK) | Auto-generated |
| name | VARCHAR(100) | Unique entity identifier |
| label | VARCHAR(255) | Human-readable name |
| description | TEXT | Documentation |
| is_extensible | BOOLEAN | Whether dynamic fields can be added |
| custom_attributes | JSONB | Extensible metadata (GIN indexed) |
| created_at / updated_at | TIMESTAMP | Audit timestamps |
| created_by / updated_by | VARCHAR(255) | Audit actors |
| version | BIGINT | Optimistic locking |
| is_deleted | BOOLEAN | Soft-delete flag |

### `sys_dictionary`
| Column | Type | Notes |
|---|---|---|
| id | UUID (PK) | Auto-generated |
| entity_name | VARCHAR(100) | FK-like reference to sys_db_object.name |
| attribute_name | VARCHAR(100) | Field name |
| data_type | VARCHAR(20) | string, boolean, jsonb, vector, reference, etc. |
| is_custom | BOOLEAN | Dynamically added field flag |
| is_sensitive | BOOLEAN | FLS trigger — fields stripped by discretion interceptor |
| is_required | BOOLEAN | Validation enforcement |
| default_value | VARCHAR(500) | Default on creation |
| max_length | INTEGER | String length cap |
| description | TEXT | Documentation |
| validation_rules | JSONB | Complex rules (GIN indexed) |
| Unique constraint | | `(entity_name, attribute_name)` |

---

## Design Decisions

| Decision | Rationale |
|---|---|
| **pgvector/pgvector:pg16 image** | Pre-bundles the vector extension; avoids manual `apt-get` in Dockerfiles |
| **Flyway owns schema, Hibernate validates** | `ddl-auto: validate` prevents drift between migrations and entities |
| **JSONB for dynamic attributes** | Allows schema-on-write without ALTER TABLE; GIN-indexed for fast filtering |
| **BaseEntity with soft-delete** | Luxury data must never be physically deleted; full audit trail required |
| **@JdbcTypeCode(SqlTypes.JSON)** | Proper JSONB ↔ Map<String,Object> binding without custom converters |
| **H2 for unit tests, Testcontainers for IT** | Fast unit test cycle; full pgvector fidelity for integration tests |
| **RFC 7807 error responses** | Industry-standard error format; consistent for future API consumers |
| **@EnableAsync on main class** | Foundation for ambient intelligence event processing in later WPs |
| **snake_case Jackson globally** | Matches PostgreSQL column naming and luxury API conventions |
