# SPEC-WP-CORE-02 — Security & Discretion

> Technical specification for Identity, Field-Level Security (FLS), and Phantom Enclaves (WP-CORE-02).

---

## 1. Identity & RBAC

The platform integrates with an external Identity Provider (e.g., Entra ID) using standard OIDC JWTs.

### 1.1 Core Roles

| Role | Access Level |
|---|---|
| `ROLE_ASSOCIATE` | Read/Write access to assigned profiles and basic vault items. |
| `ROLE_VIP_DIRECTOR` | Global read/write, access to hidden financial indicators (e.g., Net Worth Bands). |
| `ROLE_PHANTOM_CLEARANCE` | Highest tier. Multi-Factor authenticated. Required to decrypt Phantom Enclave data. |

---

## 2. Field-Level Security (FLS) & Payload Stripping

Certain columns (or JSONB keys) exist in the database but must NOT be sent to the frontend if the user lacks clearance.

### 2.1 The FLS Interceptor

We implement a Jackson `@JsonFilter` or a Spring `ResponseBodyAdvice` component. 

1. An Associate requests `GET /api/v1/clienteling/profiles/123`.
2. The Database returns the full `HyperProfile` entity (including `influence_score`).
3. The `FlSInterceptor` checks `SecurityContextHolder` for `ROLE_VIP_DIRECTOR`.
4. If missing, the interceptor mutates the DTO, setting `influenceScore = null` and deleting specific keys from the `custom_attributes` JSON map before returning the HTTP 200 response.

---

## 3. Phantom Enclaves (Application-Level Encryption)

For ultra-sensitive data (e.g., offshore corporate networks, highly discrete concierge requests), simply hiding it from the frontend is insufficient. We must protect against DBA exfiltration.

### 3.1 Encryption Strategy

- **Encryption Standard**: AES-256-GCM.
- **Key Management**: Keys are stored in Azure Key Vault (or equivalent KMS) and are never persisted in the PostgreSQL database.
- **Implementation**: We utilize JPA `@Convert` and an `AttributeConverter`.

### 3.2 JPA Implementation

```java
@Converter
public class PhantomEnclaveConverter implements AttributeConverter<String, String> {

    @Autowired
    private KmsEncryptionService kmsEncryptionService;
    
    @Autowired
    private SecurityContextService securityContext;

    @Override
    public String convertToDatabaseColumn(String rawNotes) {
        if (rawNotes == null) return null;
        return kmsEncryptionService.encrypt(rawNotes); // Writes cipher text to DB
    }

    @Override
    public String convertToEntityAttribute(String cipherText) {
        if (cipherText == null) return null;
        
        // Only decrypt if the current thread has the Phantom Clearance role
        if (securityContext.hasPhantomClearance()) {
            return kmsEncryptionService.decrypt(cipherText);
        }
        
        return "[ENCRYPTED ENCLAVE DATA - CLEARANCE REQUIRED]";
    }
}
```

### 3.3 Application to Entities

In `experiential_requests` or `hyper_profiles`:

```java
@Column(name = "concierge_notes", columnDefinition = "TEXT")
@Convert(converter = PhantomEnclaveConverter.class)
private String conciergeNotes;
```
