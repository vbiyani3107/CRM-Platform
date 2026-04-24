# OpenAPI / API Contract

This document outlines the RESTful API endpoints, particularly the dynamic entity endpoints that drive the Luxury Shell frontend.

## Base URL
`/api/v1`

## Dynamic Entity Endpoints
- `GET /entities/{entityName}`: List entities.
- `GET /entities/{entityName}/{id}`: Get specific entity.
- `POST /entities/{entityName}`: Create entity with dynamic schema validation.
- `PUT /entities/{entityName}/{id}`: Update entity.
- `DELETE /entities/{entityName}/{id}`: Delete entity.

## Response Format
Responses adhere to JSON API standards. Errors adhere to RFC 7807 Problem Details for HTTP APIs.
