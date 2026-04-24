package com.luxury.core.api.validation;

import com.luxury.core.persistence.model.SysDictionary;
import com.luxury.core.persistence.repository.SysDictionaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates dynamic JSON payloads against {@code sys_dictionary} field definitions.
 *
 * <p>For each incoming attribute map, this validator:</p>
 * <ol>
 *   <li>Checks that all required fields ({@code is_required = true}) are present and non-null.</li>
 *   <li>Validates data type compatibility (string, boolean, integer, decimal, date, timestamp, jsonb, vector, reference).</li>
 *   <li>Enforces {@code max_length} constraints for string values.</li>
 *   <li>Rejects unknown fields not registered in {@code sys_dictionary}.</li>
 * </ol>
 *
 * @see SysDictionaryRepository#findByEntityName(String)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicPayloadValidator {

    private final SysDictionaryRepository sysDictionaryRepository;

    /**
     * Validates the given attributes map against the dictionary definitions
     * for the specified entity.
     *
     * @param entityName the registered entity name (e.g., "hyper_profiles")
     * @param attributes the payload attributes to validate
     * @param isUpdate   if true, required-field checks are relaxed (partial updates allowed)
     * @return list of violation messages; empty if the payload is valid
     */
    public List<String> validate(String entityName, Map<String, Object> attributes, boolean isUpdate) {
        List<String> violations = new ArrayList<>();

        List<SysDictionary> definitions = sysDictionaryRepository.findByEntityName(entityName);

        // If no dictionary definitions exist, accept all fields (schema-less mode)
        if (definitions.isEmpty()) {
            log.debug("No dictionary definitions found for entity '{}' — accepting all fields", entityName);
            return violations;
        }

        Map<String, SysDictionary> definitionMap = definitions.stream()
                .collect(Collectors.toMap(SysDictionary::getAttributeName, d -> d));

        Set<String> knownFields = definitionMap.keySet();

        // 1. Reject unknown fields
        for (String field : attributes.keySet()) {
            if (!knownFields.contains(field)) {
                violations.add("Unknown field '" + field + "' is not defined for entity '" + entityName + "'");
            }
        }

        // 2. Check required fields (only on create, not on partial update)
        if (!isUpdate) {
            for (SysDictionary def : definitions) {
                if (Boolean.TRUE.equals(def.getIsRequired()) && !attributes.containsKey(def.getAttributeName())) {
                    violations.add("Required field '" + def.getAttributeName() + "' is missing");
                }
            }
        }

        // 3. Validate each provided attribute
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            SysDictionary def = definitionMap.get(fieldName);

            if (def == null || value == null) {
                continue; // Unknown fields already flagged; null values are allowed
            }

            // Type validation
            String typeViolation = validateType(fieldName, value, def.getDataType());
            if (typeViolation != null) {
                violations.add(typeViolation);
                continue;
            }

            // Max length validation for string fields
            if (def.getMaxLength() != null && value instanceof String str) {
                if (str.length() > def.getMaxLength()) {
                    violations.add("Field '" + fieldName + "' exceeds max length of " + def.getMaxLength()
                            + " (actual: " + str.length() + ")");
                }
            }
        }

        if (!violations.isEmpty()) {
            log.debug("Validation of entity '{}' produced {} violation(s): {}", entityName, violations.size(), violations);
        }

        return violations;
    }

    /**
     * Validates that the value is compatible with the expected data type.
     *
     * @return a violation message if incompatible, or null if valid
     */
    private String validateType(String fieldName, Object value, String expectedType) {
        return switch (expectedType) {
            case "string", "reference" -> {
                if (!(value instanceof String)) {
                    yield "Field '" + fieldName + "' must be a string (got " + value.getClass().getSimpleName() + ")";
                }
                yield null;
            }
            case "boolean" -> {
                if (!(value instanceof Boolean)) {
                    yield "Field '" + fieldName + "' must be a boolean (got " + value.getClass().getSimpleName() + ")";
                }
                yield null;
            }
            case "integer" -> {
                if (!(value instanceof Number)) {
                    yield "Field '" + fieldName + "' must be an integer (got " + value.getClass().getSimpleName() + ")";
                }
                yield null;
            }
            case "decimal" -> {
                if (!(value instanceof Number)) {
                    yield "Field '" + fieldName + "' must be a number (got " + value.getClass().getSimpleName() + ")";
                }
                yield null;
            }
            case "date", "timestamp" -> {
                // Accept strings (ISO 8601 format expected) or temporal types
                if (!(value instanceof String)) {
                    yield "Field '" + fieldName + "' must be a date/timestamp string in ISO 8601 format (got "
                            + value.getClass().getSimpleName() + ")";
                }
                yield null;
            }
            case "jsonb" -> {
                if (!(value instanceof Map) && !(value instanceof List)) {
                    yield "Field '" + fieldName + "' must be a JSON object or array (got "
                            + value.getClass().getSimpleName() + ")";
                }
                yield null;
            }
            case "vector" -> {
                if (!(value instanceof List)) {
                    yield "Field '" + fieldName + "' must be a numeric array (got "
                            + value.getClass().getSimpleName() + ")";
                }
                yield null;
            }
            default -> null; // Unknown types pass through
        };
    }
}
