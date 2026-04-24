# Bespoke Luxury Platform — Product Requirements Document (PRD)

> **Version**: 2.0 (The Luxury Pivot)
> **Date**: April 2026

> [!NOTE]
> This PRD derives from the [VISION](VISION.md). It outlines the functional requirements for a platform dedicated to High-Net-Worth Individuals (HNWIs), replacing generic CRM logic with bespoke luxury workflows.

---

## 1. Product Overview

The platform comprises four luxury-native domains operating on a shared, AI-First PostgreSQL foundation. It leverages **LangChain4j** for ambient intelligence and **pgvector** for semantic aesthetic matching.

### 1.1 Scope Summary

| Domain | Key Capabilities |
|---|---|
| **Clienteling Hub** | Hyper-Profiles, Influence Graphing, Sentiment Tracking, Evolving Taste Vectors |
| **The Vault & Wardrobe**| Curated Collections, Immutable Provenance (DPP), Private Wardrobes, Multi-Modal Vibe Search |
| **Atelier** | Bespoke Commissions lifecycle, Artisan tracking, Milestone approvals |
| **Concierge** | Experiential Requests, Lifestyle Management, Spa & Repair, Proactive Gifting Alerts |
| **Foundation** | Dynamic Dictionary (JSONB/Vector), Extreme Discretion (Enclaves), Workflow |

---

## 2. Foundation Capability Layer

### FND-01 — The Dynamic Dictionary (Metadata Engine)
*Replaces rigid enterprise schemas.*

| ID | Requirement | Priority |
|---|---|---|
| FND-01.1 | **Entity & Field Registry** — Central registry (`sys_db_object`, `sys_dictionary`) defining all core luxury entities and their properties. | **Must** |
| FND-01.2 | **Hybrid Schema Extension** — Admins can instantly add `JSONB` fields (e.g., "Dial Color", "Wrist Size") and `vector` fields to any entity without downtime. | **Must** |
| FND-01.3 | **Generic REST API** — Universal endpoint (`/api/data/v1/sobjects/{objectName}`) that dynamically validates inputs against the registry. | **Must** |

### FND-02 — Ambient Intelligence (AI & Vectors)
*Replaces manual data entry and keyword search.*

| ID | Requirement | Priority |
|---|---|---|
| FND-02.1 | **Multi-Modal Aesthetic Matching** — The ability to query The Vault using semantic text (e.g., "Avant-garde, sharp edges") or by uploading inspiration images (processed via Vision models like CLIP) mapped to `pgvector`. | **Must** |
| FND-02.2 | **Ambient Note & Sentiment Extraction** — When an associate logs a Client Engagement, LangChain4j parses the text, extracts shifting preferences, urgency, and sentiment, silently updating the client's `style_vector` and profile. | **Must** |

### FND-03 — White-Glove Automations (Camunda 8)
*Replaces complex Salesforce Flows.*

| ID | Requirement | Priority |
|---|---|---|
| FND-03.1 | **Bespoke Process Definitions** — Visual workflows for complex luxury lifecycles (e.g., the 6-month process of designing a custom timepiece). | **Must** |
| FND-03.2 | **Proactive Triggers** — Automations that trigger on specific lifecycle events (e.g., initiating a "White-Glove Delivery" workflow when a bespoke piece is finalized). | **Must** |

---

## 3. Luxury Domain Requirements

### DOM-CLIENT — Clienteling Hub

| ID | Requirement | Priority |
|---|---|---|
| DOM-CLIENT.1 | **Hyper-Profiles** — Comprehensive HNWI profiles tracking significant dates, lifestyle personas, communication protocols (e.g., "WhatsApp only"), and real-time `style_vector` calculations. | **Must** |
| DOM-CLIENT.2 | **Influence Graphing & Networks** — Visual relational links mapping not just family, but "Tastemaker" influence, corporate entities, and social networks to predict purchasing ripples. | **Must** |
| DOM-CLIENT.3 | **Waitlists & VIP Invitations** — Managing access to highly restricted, limited-edition releases or private viewing events. | **Must** |

### DOM-VAULT — The Vault & Private Wardrobes

| ID | Requirement | Priority |
|---|---|---|
| DOM-VAULT.1 | **Curated Collections & Immutable Provenance** — Tracking unique pieces with Digital Product Passports (DPP) or blockchain hashes to guarantee authenticity and secondary market value. | **Must** |
| DOM-VAULT.2 | **The Private Wardrobe** — Tracking the client's holistic asset collection, including pieces acquired from competitors, to build a complete picture of net worth and taste. | **Must** |
| DOM-VAULT.3 | **Multi-Modal Embeddings** — Automated generation of 1536-dimensional embeddings (text and image) for every piece to empower the Aesthetic Matching engine. | **Must** |
| DOM-VAULT.4 | **Location Tracking** — Tracking pieces as they move between global flagship boutiques, private viewing rooms, and high-security vaults. | **Must** |

### DOM-ATELIER — Atelier (Bespoke Commissions)

| ID | Requirement | Priority |
|---|---|---|
| DOM-ATELIER.1 | **Commission Lifecycle** — Tracking a piece from Initial Consultation → Sketch Approval → Material Sourcing → Artisan Crafting → Final QA → Delivery. | **Must** |
| DOM-ATELIER.2 | **Artisan Collaboration** — Internal notes bridging the gap between the Sales Associate and the Master Artisan. | **Should** |

### DOM-CONCIERGE — Concierge (Experiential & Support)

| ID | Requirement | Priority |
|---|---|---|
| DOM-CONCIERGE.1 | **Experiential & Lifestyle Management** — Handling highly bespoke non-product requests (e.g., chartering yachts, securing exclusive runway seats, booking 3-Michelin-star tables) with absolute discretion. | **Must** |
| DOM-CONCIERGE.2 | **Spa & Repair** — Specialized workflows for intaking luxury goods for servicing, tracking condition reports, and managing return logistics. | **Must** |
| DOM-CONCIERGE.3 | **Proactive Gifting Alerts** — A daily AI-driven dashboard that flags upcoming anniversaries and uses the multi-modal `style_vector` to suggest 3 curated items from The Vault. | **Must** |

---

## 4. User Experience & Aesthetics

| # | Category | Requirement |
|---|---|---|
| UX-01 | **Showroom vs Backstage (React + Tailwind)** | The frontend offers a "Showroom Mode" (safe for client viewing on a tablet, highly visual) and a "Backstage Mode" (dense with financial/influence data). Both eschew corporate grids for editorial layouts and fluid micro-animations. |
| UX-02 | **Extreme Discretion (Phantom Enclaves)** | Associates experience a strict "Need to Know" interface. Ultra-sensitive HNWI data (offshore entities, confidential relationships) is stored in cryptographic enclaves and dynamically masked unless explicitly authorized via high-level RBAC. |
| UX-03 | **Speed & Grace** | All interactions, especially AI-driven semantic image searches, must feel instantaneous (sub-500ms) to maintain the illusion of seamless white-glove service. |
