# Bespoke Luxury Platform — Work Packages

> **Version**: 2.0 (The Luxury Pivot)
> **Date**: April 2026

> [!NOTE]
> This document segments the execution of the Bespoke Luxury Platform. Development is strictly divided into **Core Foundation** (the metadata and AI engine) and **Luxury Hubs** (the domain logic). All tasks are atomic and actionable.

---

## Part 1: Core Foundation (The Engine)

These work packages build the underlying platform. They must be completed before any luxury-specific business logic is implemented.

### WP-CORE-01: Persistence & Aesthetic Storage
- **Objective**: Establish the PostgreSQL environment with vector and dynamic schema support.
- **Tasks**:
  - `[ ]` Generate `docker-compose.yml` provisioning PostgreSQL with the `pgvector` extension.
  - `[ ]` Scaffold Spring Boot 3.x project with Web, Data JPA, Postgres, and Flyway dependencies.
  - `[ ]` Configure application properties for local Postgres connection.
  - `[ ]` Create initial Flyway migration `V1__init_foundation.sql` (ext: vector, `sys_db_object`, `sys_dictionary`).
  - `[ ]` Implement Spring Data repositories supporting `JSONB` binding and HNSW indices.

### WP-CORE-02: Discretion & Identity (Security)
- **Objective**: Implement absolute data privacy and access control, including Phantom Enclaves.
- **Tasks**:
  - `[ ]` Configure Entra ID (OIDC) integration in Spring Security.
  - `[ ]` Define and implement Role-Based Access Control (RBAC) authorities.
  - `[ ]` Implement Field-Level Security (FLS) interceptor to strip `is_sensitive=true` fields.
  - `[ ]` Implement application-level encryption/decryption service for Phantom Enclave attributes.

### WP-CORE-03: The Dynamic API
- **Objective**: Build the dynamic CRUD REST endpoints.
- **Tasks**:
  - `[ ]` Develop `GenericEntityController` mapping to `/api/v1/entities/{entityName}`.
  - `[ ]` Implement dynamic JSON payload validation interceptor querying `sys_dictionary`.
  - `[ ]` Implement RFC 7807 compliant `@ControllerAdvice` for pristine error handling.

### WP-CORE-04: Ambient Intelligence
- **Objective**: Configure the LangChain4j bridge.
- **Tasks**:
  - `[ ]` Add LangChain4j dependencies to `pom.xml` / `build.gradle`.
  - `[ ]` Configure Embedding Model bean (e.g., local ONNX or OpenAI).
  - `[ ]` Configure Chat Language Model bean for text extraction.
  - `[ ]` Develop Semantic Search service translating text to vectors.

### WP-CORE-05: The Luxury Shell (Frontend)
- **Objective**: Establish the React SPA foundation with Showroom/Backstage modes.
- **Tasks**:
  - `[ ]` Scaffold React 18 + TypeScript using Vite.
  - `[ ]` Install and configure Tailwind CSS with Luxury Design Tokens (colors, fonts).
  - `[ ]` Implement React Router with root layouts for 'Showroom Mode' and 'Backstage Mode'.
  - `[ ]` Build dynamic layout components driven by JSON configuration.

### WP-CORE-06: Multi-Modal AI Engine
- **Objective**: Enhance the AI bridge to support Image-to-Vector translation.
- **Tasks**:
  - `[ ]` Integrate Vision Model API for generating image embeddings.
  - `[ ]` Add `multipart/form-data` support to Semantic Search controller.
  - `[ ]` Unify text and image embedding dimensions for `pgvector` querying.

---

## Part 2: Luxury Hubs (The Business Value)

These packages implement the bespoke brand experiences utilizing the Core Foundation.

### WP-HUB-01: Clienteling Hub
- **Objective**: Implement HNWI management, engagement memory, and Influence Graphing.
- **Tasks**:
  - `[ ]` Create Flyway migration for `hyper_profiles` and `client_engagements`.
  - `[ ]` Build React list and detail views for Hyper-Profiles.
  - `[ ]` Implement async LangChain4j worker to extract persona/sentiment from engagement notes.

### WP-HUB-02: The Vault
- **Objective**: Implement the AI-searchable curated inventory system.
- **Tasks**:
  - `[ ]` Create Flyway migration for `curated_vault`.
  - `[ ]` Add backend hook to auto-generate `aesthetic_embedding` on vault item save.
  - `[ ]` Build React Vibe Search interface (image/text input).

### WP-HUB-03: Atelier (Bespoke Commissions)
- **Objective**: Manage the lifecycle of custom creations.
- **Tasks**:
  - `[ ]` Create Flyway migration for `bespoke_commissions`.
  - `[ ]` Integrate Camunda 8 client and deploy commission BPMN.
  - `[ ]` Build React dashboard tracking milestones and artisan notes.

### WP-HUB-04: Concierge & Experiential Management
- **Objective**: Drive white-glove service, lifestyle management, and predictive engagement.
- **Tasks**:
  - `[ ]` Create Flyway migration for `experiential_requests`.
  - `[ ]` Implement Spring Batch job to scan `significant_dates` +21 days.
  - `[ ]` Construct React 'Daily Concierge Alerts' widget suggesting curated gifts.

### WP-HUB-05: Provenance & Digital Twins
- **Objective**: Guarantee authenticity and track the secondary market lifecycle.
- **Tasks**:
  - `[ ]` Create Flyway migration for `provenance_ledger`.
  - `[ ]` Build blockchain integration service for Digital Product Passports.
  - `[ ]` Build React UI visualizing cryptographic item history.

### WP-HUB-06: The Private Wardrobe
- **Objective**: Manage the client's holistic asset collection to model net worth.
- **Tasks**:
  - `[ ]` Create Flyway migration for `private_wardrobes`.
  - `[ ]` Build React UI for logging competitor items.
  - `[ ]` Update Semantic Search to optionally factor Wardrobe baseline.
