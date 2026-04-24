# Bespoke Luxury Platform — Software Development Lifecycle (SDLC)

> **Version**: 2.0 (The Luxury Pivot)
> **Date**: April 2026

> [!NOTE]
> This document defines how the team builds, tests, deploys, and operates the custom-built Luxury Platform. It replaces the notoriously difficult Salesforce deployment model with standard, cloud-native software engineering practices tailored for an environment requiring extreme data discretion.

---

## 1. Development Philosophy

| # | Principle | Practice |
|---|---|---|
| 1 | **Code Over Configuration (For Complex Logic)** | Unlike Salesforce where declarative logic often spirals out of control, complex business rules are written in standard Java (Camunda Workers) rather than obscure flow tools. |
| 2 | **Standard DevOps** | No Change Sets, no Salesforce CLI. Standard Git, Azure DevOps, Terraform, and Helm charts. |
| 3 | **Local Development** | Developers can run the entire CRM platform (Java services + Postgres/Azure SQL Emulator) locally via Docker Compose. No more "Sandboxes" with mismatched states. |
| 4 | **Agentic Delivery** | AI agents assist in extracting Salesforce metadata, generating Java schemas, and writing unit tests. |

---

## 2. Environment Strategy (Goodbye Sandboxes)

Salesforce environments (Developer, Partial Copy, Full Sandbox, Production) are replaced with a standard cloud-native promotion flow:

| Environment | Purpose | Provisioning | Data |
|---|---|---|---|
| **Local** | Developer testing | Docker Compose | Mock data |
| **Dev** | Feature integration | AKS (Small) | Synthetic seed data |
| **Staging** | UAT / Performance | AKS (Prod-like) | Anonymized production clone |
| **Production**| Live environment | AKS (Auto-scale) | Live customer data |

---

## 3. Deployment Pipeline

Unlike Salesforce deployments which require complex XML metadata manipulation and often fail due to missing dependencies, our deployment is a standard microservice pipeline.

### 3.1 CI/CD Flow (Azure DevOps)

1. **Pull Request**:
   - Compiles Java Spring Boot code.
   - Runs JUnit tests (replacing Apex Test Classes).
   - Verifies Code Coverage (target: 80%).
2. **Merge to Main**:
   - Builds Docker image and pushes to Azure Container Registry (ACR).
   - Applies Flyway database migrations (for core schema changes).
   - Deploys updated microservices via Helm to Staging.
3. **Metadata Promotion**:
   - Changes to the Data Dictionary (Custom Objects/Fields) are treated as data migrations or API payload updates, executed automatically via scripting, decoupling infrastructure deployment from schema configuration.

---

## 4. Metadata Management (The "Admin" Lifecycle)

While code is deployed via Git, declarative changes (like adding a custom field or updating a Page Layout) happen in the UI. 

To prevent environments from drifting:
1. **Metadata Export**: Admins make changes in Staging. A utility exports these Data Dictionary changes to JSON format.
2. **Version Control**: This JSON is committed to the Git repository.
3. **Promotion**: The CI/CD pipeline pushes the JSON metadata to Production via the Generic API, ensuring configuration is version-controlled and auditable.

---

## 5. Testing Strategy

Salesforce requires 75% code coverage for deployment, often leading to dummy assertions. Our approach focuses on meaningful testing and uncompromising security:

- **Unit Tests (JUnit)**: Testing business logic and LangChain4j integrations in isolation.
- **Integration Tests (Testcontainers)**: Spinning up a real PostgreSQL container with `pgvector` to test semantic queries and JSONB operations.
- **E2E Tests (Playwright)**: End-to-end UI testing of the React frontend, ensuring critical Concierge and Vault flows operate correctly.
- **Privacy & Penetration Audits**: Dedicated test suites explicitly designed to attempt to bypass the **Phantom Enclaves** and Field-Level Security. These are run continuously in the CI pipeline to ensure ultra-HNWI data cannot be leaked.
