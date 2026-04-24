# Bespoke Luxury Platform — Architecture Document (ARCH)

> **Version**: 2.0 (The Luxury Pivot)
> **Date**: April 2026

> [!NOTE]
> This document describes **how** the Bespoke Luxury Platform is technically structured, entirely abandoning the generic CRM paradigm in favor of an AI-First, PostgreSQL-backed architecture tailored for luxury operations.

---

## 1. Architectural Philosophy

| # | Principle | Description |
|---|---|---|
| P1 | **Multi-Modal Aesthetic Intelligence** | The architecture is fundamentally designed around embeddings and vectors (`pgvector`), utilizing both text and vision models. Taste is treated as a first-class data type. |
| P2 | **Sovereign Open-Source Foundation** | The brand owns its data and logic. Built on PostgreSQL, Java Spring Boot, React, and LangChain4j. |
| P3 | **Hybrid Elastic Storage** | A single PostgreSQL cluster unifies strict relational integrity (for transactions), `JSONB` (for dynamic custom fields), and `vector` (for AI matching). |
| P4 | **Ambient Execution** | Automations (via Camunda) and AI extraction (via LangChain4j) happen asynchronously in the background, ensuring the associate's UI remains lightning fast. |

---

## 2. System Architecture

### 2.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        ASSOCIATES & CLIENTS                             │
│   React SPA (Tailwind Luxury UI) │  Boutique Tablet App                 │
└──────────────────────┬──────────────────────────────────────────────────┘
                       │ HTTPS / REST
┌──────────────────────▼──────────────────────────────────────────────────┐
│              API MANAGEMENT / GATEWAY                                  │
│    Rate limiting · JWT validation · Absolute Discretion (FLS)          │
└──────────────────────┬──────────────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────────────────┐
│              IDENTITY PROVIDER & PHANTOM ENCLAVES                      │
│    SSO / MFA / RBAC / Cryptographic Storage for Ultra-Sensitive Data   │
└──────────────────────┬──────────────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────────────────┐
│           CAMUNDA 8 — EXPERIENTIAL AUTOMATION ENGINE                   │
│  Commission Lifecycles · Lifestyle Management · Proactive Alerts       │
└──────────────────────┬──────────────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────────────────┐
│           LUXURY MICROSERVICES — Java Spring Boot 3.x                  │
│                                                                        │
│  ┌────────────┐┌────────────┐┌────────────┐┌───────────────────────┐   │
│  │ Clienteling││ The Vault  ││ Foundation ││ Multi-Modal Ambient   │   │
│  │ & Concierge││ & Wardrobe ││ (Metadata) ││ Intelligence (AI)     │   │
│  └────────────┘└────────────┘└────────────┘└───────────────────────┘   │
│                                                                        │
└──────────────────────┬──────────────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────────────────┐
│                      ASYNC MESSAGING LAYER                              │
│          Event Bus (RabbitMQ/Kafka) for Ambient Processing              │
└──────────────────────┬──────────────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────────────────┐
│                          DATA TIER                                      │
│                                                                         │
│  ┌──────────────────────────────────────────────────────────┐           │
│  │ PostgreSQL Engine                                        │           │
│  │  • Relational Tables (hyper_profiles, curated_vault)     │           │
│  │  • JSONB (Dynamic Custom Attributes, Sizing specs)       │           │
│  │  • pgvector (Taste Vectors, Aesthetic Embeddings)        │           │
│  └──────────────────────────────────────────────────────────┘           │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Data Architecture (The Dynamic Dictionary)

### 3.1 The Unification of Paradigms in PostgreSQL

The luxury model requires immense flexibility (e.g., tracking the specific cut of a diamond versus the patina of a leather bag). PostgreSQL handles this gracefully:
1. **Relational Links**: Strong Foreign Keys for tracking familial ties or linking a bespoke commission to a specific artisan.
2. **JSONB Extension**: Every core table includes a `custom_attributes` column (JSONB) to allow dynamic schema additions defined by the `sys_dictionary`.
3. **pgvector**: Stores high-dimensional arrays representing aesthetic qualities, enabling ultra-fast Cosine Similarity queries (HNSW indices).

### 3.2 Dynamic Dictionary Schema

| Table | Purpose |
|---|---|
| `sys_db_object` | **Entity Registry**: Defines core modules (`hyper_profiles`, `the_vault`) and dynamically created bespoke tables. |
| `sys_dictionary` | **Attribute Registry**: Defines column types (`string`, `boolean`, `jsonb`, `vector`). Enforces validation before writes. |

### 3.3 Dynamic CRUD API

The Foundation microservice exposes a universal, metadata-driven API:
- `GET /api/data/v1/entities/{entityName}`
- `POST /api/data/v1/entities/{entityName}`
- `POST /api/data/v1/semantic-search/{entityName}` (Vector matching)

---

## 4. Multi-Modal Ambient Intelligence Architecture

The platform does not require associates to interact with a traditional "chatbot". Instead, AI acts as an ambient intelligence layer operating asynchronously across text and images.

### 4.1 Ambient Engagement Parsing
1. A Sales Associate logs a **Client Engagement** (e.g., notes from a private dinner).
2. The `Clienteling` service publishes an `engagement.logged` event.
3. The **Ambient Intelligence** service consumes the event. LangChain4j passes the raw notes to an LLM (e.g., OpenAI or local Ollama) with a strict schema constraint.
4. The LLM extracts shifting preferences, sizing updates, urgency, and sentiment.
5. The service executes an `UPDATE hyper_profiles SET lifestyle_persona = ...` and generates follow-up tasks if necessary.

### 4.2 The Vault Multi-Modal Semantic Search
1. The frontend submits a query: *"Dark, moody, architectural"* OR uploads a `.jpg` inspiration image to the `/semantic-search/the_vault` endpoint.
2. The API routes text to an Embedding Model, and images to a Vision Model (e.g., CLIP) to vectorize the query into a unified 1536-dimensional space.
3. Spring Data JDBC executes a `pgvector` nearest-neighbor query (`<->`) against the `aesthetic_embedding` column in `curated_vault`.
4. Results are returned in sub-500ms.

---

## 5. Discretion & Frontend Architecture

### 5.1 Extreme Discretion (Security & Enclaves)
- **Field-Level Security (FLS)**: Enforced deeply within the Java API layer. Highly sensitive JSONB keys are physically stripped from the response payload before serialization if the associate lacks clearance.
- **Phantom Enclaves**: For ultra-HNWIs, specific relational links (e.g., paramours, highly confidential corporate acquisitions) and "Vaulted Notes" are encrypted at the application level before database insertion. They require explicit, audited, multi-factor "break-glass" authorization to decrypt and view, ensuring even DBAs cannot read them.

### 5.2 The Luxury Frontend (React & Tailwind)
- **Framework**: React 18 + TypeScript.
- **Styling Paradigm**: Tailwind CSS configured with a bespoke design token system. We explicitly avoid "density" (a hallmark of B2B CRMs). The UI emphasizes negative space, high-resolution imagery for The Vault, elegant typography, and distraction-free engagement logging interfaces.
