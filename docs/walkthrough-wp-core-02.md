# WP-CORE-02 — Discretion & Identity (Security): Walkthrough

> **Work Package**: WP-CORE-02
> **Completed**: April 2026
> **Dependency**: WP-CORE-01 (Persistence & Aesthetic Storage)

---

## Summary

Built the complete Discretion & Identity security layer for the Bespoke Luxury Platform. This includes Spring Security with Entra ID (OIDC/JWT) integration, Role-Based Access Control (RBAC), a Field-Level Security (FLS) interceptor that strips sensitive fields from API responses, and a Phantom Enclave encryption service (AES-256-GCM) for application-level data protection.

---

## Files Created

### Implementation (Session 1)

| File | Purpose |
|---|---|
| `src/main/resources/db/migration/V2__security_seed.sql` | Flyway V2 migration seeding sensitive field definitions and domain entities |
| `src/main/java/com/luxury/core/security/model/LuxuryRole.java` | Enum defining 3 RBAC roles: ASSOCIATE, VIP_DIRECTOR, PHANTOM_CLEARANCE |
| `src/main/java/com/luxury/core/security/config/SecurityConfig.java` | Spring Security config: stateless JWT, Entra ID role mapping, method security |
| `src/main/java/com/luxury/core/security/service/SecurityContextService.java` | Wrapper for SecurityContextHolder with role check convenience methods |
| `src/main/java/com/luxury/core/security/fls/FlsInterceptor.java` | ResponseBodyAdvice stripping `is_sensitive=true` fields from API responses |
| `src/main/java/com/luxury/core/security/encryption/KmsEncryptionService.java` | AES-256-GCM encryption service for Phantom Enclave data |
| `src/main/java/com/luxury/core/security/encryption/PhantomEnclaveConverter.java` | JPA AttributeConverter for role-gated encrypt/decrypt |

### Unit Tests (Session 2)

| File | Tests | Status |
|---|---|---|
| `src/test/java/com/luxury/core/security/service/SecurityContextServiceTest.java` | 12 tests | ✅ Pass |
| `src/test/java/com/luxury/core/security/encryption/KmsEncryptionServiceTest.java` | 9 tests | ✅ Pass |
| `src/test/java/com/luxury/core/security/encryption/PhantomEnclaveConverterTest.java` | 5 tests | ✅ Pass |
| `src/test/java/com/luxury/core/security/fls/FlsInterceptorTest.java` | 4 tests | ✅ Pass |
| `src/test/java/com/luxury/core/security/config/TestSecurityConfig.java` | Test config: mock JwtDecoder for unit tests | N/A |

### Integration Tests (Session 3)

| File | Tests | Status |
|---|---|---|
| `src/test/java/com/luxury/core/security/integration/SecurityFlsIT.java` | 4 tests | ✅ Pass |
| `src/test/java/com/luxury/core/security/integration/PhantomEnclaveIT.java` | 5 tests | ✅ Pass |
| `src/test/java/com/luxury/core/security/integration/FlywayV2MigrationIT.java` | 5 tests | ⚠️ Testcontainer pool issue |

### Files Modified

| File | Change |
|---|---|
| `pom.xml` | Added spring-boot-starter-security, spring-boot-starter-oauth2-resource-server, spring-security-test; added surefire IT exclusion |
| `src/main/resources/application.yml` | Added Entra ID OIDC config, encryption key property, FLS toggle |
| `src/test/resources/application-test.yml` | Added test encryption key and FLS config |

---

## Schema Additions (V2)

### sys_dictionary — Sensitive Field Definitions

| Entity | Attribute | Data Type | Sensitive |
|---|---|---|---|
| `hyper_profiles` | `influence_score` | decimal | ✅ |
| `hyper_profiles` | `net_worth_band` | string | ✅ |
| `hyper_profiles` | `wealth_indicators` | jsonb | ✅ |
| `hyper_profiles` | `concierge_notes` | string | ✅ |
| `hyper_profiles` | `family_network` | jsonb | ✅ |
| `curated_vault` | `acquisition_price` | decimal | ✅ |
| `curated_vault` | `provenance_notes` | string | ✅ |
| `experiential_requests` | `budget_ceiling` | decimal | ✅ |

### sys_db_object — Domain Entity Registrations

- `hyper_profiles` — Core clienteling entity
- `curated_vault` — Luxury product catalog
- `experiential_requests` — Concierge requests

---

## Architecture Decisions

1. **Mock JwtDecoder for tests**: Created `TestSecurityConfig` with `@Profile("test")` providing a no-op JwtDecoder. This avoids requiring a real Entra ID endpoint during testing while still exercising the full SecurityFilterChain.

2. **FLS via ResponseBodyAdvice**: Chose `ResponseBodyAdvice` over Jackson `@JsonFilter` for FLS implementation. This is more flexible as it can strip both Java fields (via reflection) and JSONB map keys without requiring custom serializers on every entity.

3. **AES-256-GCM with prepended IV**: Each encryption call generates a unique 96-bit IV (NIST recommended). The IV is prepended to the ciphertext before Base64 encoding, making the output self-contained for decryption.

4. **Surefire/Failsafe separation**: Added explicit `*IT.java` exclusion to surefire plugin so integration tests only run during `mvn verify` (failsafe phase). This prevents unit test runs from requiring Docker.

5. **Encryption key source**: Local dev uses a 32-byte UTF-8 string from `application.yml`. Production would source from Azure Key Vault via `${LUXURY_ENCRYPTION_KEY}` environment variable.

---

## Test Results

```
# Unit Tests (mvn test)
Tests run: 50, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

# Integration Tests (mvn verify — new security ITs only)
SecurityFlsIT:     4/4 ✅
PhantomEnclaveIT:  5/5 ✅
```
