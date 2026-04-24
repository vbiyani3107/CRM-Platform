# WP-CORE-03: The Dynamic API — Implementation Summary

## Purpose
Metadata-driven, generic RESTful API for performing CRUD operations on any entity
registered in `sys_db_object`. Payloads are validated at runtime against `sys_dictionary`
field definitions, eliminating the need for per-entity controllers or DTOs.

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/entities/{entityName}` | List records with pagination (`?page=0&size=20`) |
| `GET` | `/api/v1/entities/{entityName}/{id}` | Retrieve single record by UUID |
| `POST` | `/api/v1/entities/{entityName}` | Create record with validated attributes |
| `PUT` | `/api/v1/entities/{entityName}/{id}` | Update record (partial update supported) |
| `DELETE` | `/api/v1/entities/{entityName}/{id}` | Soft-delete record |

## Request/Response Format

### Request
```json
{
  "attributes": {
    "first_name": "Arabella",
    "last_name": "Ashton-Whitley",
    "email": "arabella@luxury.com"
  }
}
```

### Success Response
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "entity_name": "hyper_profiles",
    "attributes": {
      "first_name": "Arabella",
      "last_name": "Ashton-Whitley"
    },
    "created_at": "2026-04-24T15:00:00Z",
    "updated_at": "2026-04-24T15:00:00Z",
    "created_by": "concierge@luxury.com",
    "updated_by": "concierge@luxury.com"
  }
}
```

### Validation Error Response (RFC 7807)
```json
{
  "success": false,
  "error": {
    "type": "https://luxury.com/errors/dynamic-validation",
    "title": "Dynamic Validation Error",
    "detail": "Unknown field 'bogus'; Required field 'first_name' is missing",
    "status": 400
  }
}
```

## Validation Rules
- **Required fields**: Enforced on CREATE; relaxed on UPDATE (partial update)
- **Type checking**: string, integer, decimal, boolean, date, timestamp, jsonb, vector, uuid
- **Max length**: Enforced for string fields with `max_length` set in `sys_dictionary`
- **Unknown fields**: Rejected when `sys_dictionary` definitions exist for the entity

## Security
- All endpoints require authentication (`@PreAuthorize("isAuthenticated()")`)
- Table names validated against `sys_db_object` registry (SQL injection protection)
- Column names validated against `sys_dictionary` definitions
- Audit fields (`created_by`, `updated_by`) populated from JWT security context

## Test Coverage
- **50 tests total** (31 unit + 9 integration + 10 controller)
- Integration tests use `pgvector/pgvector:pg16` via Testcontainers
