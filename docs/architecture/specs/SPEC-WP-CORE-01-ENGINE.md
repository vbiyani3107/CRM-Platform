# SPEC-WP-CORE-01 — Engine & Foundation

> Technical specification for the Core Database Engine, Base Entities, Dynamic Dictionary, and Number Generation (WP-CORE-01).

---

## 1. The Base Entity

Every luxury domain table MUST inherit from `BaseEntity`. This ensures standardized auditing, soft-deletion, and optimistic locking across the entire platform.

### 1.1 Java Model (`com.luxury.common.model.BaseEntity`)

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
```

---

## 2. Number Generation

Asset tracking is paramount. Distinct entities get prefixed sequential IDs (e.g., Bespoke Commissions get `BSK0000001`, Experiential Requests get `EXP0000001`).

### 2.1 Database Schema

```sql
CREATE TABLE number_sequences (
    prefix          VARCHAR(10) PRIMARY KEY, -- BSK, EXP, VLT
    current_value   BIGINT DEFAULT 0 NOT NULL
);

-- Atomic number generation function
CREATE OR REPLACE FUNCTION next_number(p_prefix VARCHAR)
RETURNS VARCHAR AS $$
DECLARE
    v_next BIGINT;
BEGIN
    UPDATE number_sequences SET current_value = current_value + 1
    WHERE prefix = p_prefix RETURNING current_value INTO v_next;
    RETURN p_prefix || LPAD(v_next::TEXT, 7, '0');
END;
$$ LANGUAGE plpgsql;
```

---

## 3. The Dynamic Dictionary

To avoid database migrations every time the brand adds a new tracking attribute (e.g., "Exotic Skin Type" or "Dietary Restriction"), all entities utilize a `custom_attributes` JSONB column. The `sys_dictionary` manages the validation of these fields.

### 3.1 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/dictionary/tables/{tableName}` | Get custom fields for a table |
| POST | `/api/v1/dictionary/tables/{tableName}/fields` | Add a new custom field definition |

### 3.2 Database Schema

```sql
CREATE TABLE sys_dictionary (
    id              UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    table_name      VARCHAR(100) NOT NULL, -- e.g., 'hyper_profiles'
    field_name      VARCHAR(100) NOT NULL, -- e.g., 'dietary_restrictions'
    field_type      VARCHAR(50) NOT NULL,  -- String, Integer, Boolean, Reference
    is_required     BOOLEAN DEFAULT FALSE,
    choice_list     JSONB, -- Array of allowed values if type is Choice
    created_at      TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at      TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    UNIQUE(table_name, field_name)
);
```

### 3.3 Dynamic Validation Interceptor
In the Spring Boot Service layer, before saving an entity with `custom_attributes`, a generic `DictionaryValidatorService` intercepts the payload. It queries `sys_dictionary` to ensure no illegal keys are stored in the JSONB column, throwing an `RFC 7807` formatted exception if validation fails.
