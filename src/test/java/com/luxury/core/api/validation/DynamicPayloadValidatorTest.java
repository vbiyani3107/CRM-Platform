package com.luxury.core.api.validation;

import com.luxury.core.persistence.model.SysDictionary;
import com.luxury.core.persistence.repository.SysDictionaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DynamicPayloadValidator}.
 *
 * <p>Verifies required field checks, type validation, max length constraints,
 * unknown field rejection, and schema-less mode when no definitions exist.</p>
 */
@ExtendWith(MockitoExtension.class)
class DynamicPayloadValidatorTest {

    @Mock
    private SysDictionaryRepository sysDictionaryRepository;

    @InjectMocks
    private DynamicPayloadValidator validator;

    private static final String ENTITY = "hyper_profiles";

    private SysDictionary createDef(String attrName, String dataType,
                                     boolean required, Integer maxLength) {
        SysDictionary def = new SysDictionary();
        def.setEntityName(ENTITY);
        def.setAttributeName(attrName);
        def.setDataType(dataType);
        def.setIsRequired(required);
        def.setMaxLength(maxLength);
        return def;
    }

    @Nested
    @DisplayName("Required Field Checks")
    class RequiredFieldTests {

        @Test
        @DisplayName("Missing required field produces violation on create")
        void missingRequiredFieldOnCreate() {
            when(sysDictionaryRepository.findByEntityName(ENTITY))
                    .thenReturn(List.of(createDef("first_name", "string", true, 100)));

            Map<String, Object> payload = new HashMap<>(); // empty

            List<String> violations = validator.validate(ENTITY, payload, false);

            assertThat(violations).hasSize(1);
            assertThat(violations.getFirst()).contains("Required field 'first_name' is missing");
        }

        @Test
        @DisplayName("Missing required field is allowed on update (partial)")
        void missingRequiredFieldOnUpdate() {
            when(sysDictionaryRepository.findByEntityName(ENTITY))
                    .thenReturn(List.of(createDef("first_name", "string", true, 100)));

            Map<String, Object> payload = new HashMap<>(); // empty

            List<String> violations = validator.validate(ENTITY, payload, true);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Present required field produces no violation")
        void presentRequiredField() {
            when(sysDictionaryRepository.findByEntityName(ENTITY))
                    .thenReturn(List.of(createDef("first_name", "string", true, 100)));

            Map<String, Object> payload = Map.of("first_name", "Arabella");

            List<String> violations = validator.validate(ENTITY, payload, false);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Type Validation")
    class TypeValidationTests {

        @Test
        @DisplayName("String field with non-string value produces violation")
        void stringFieldWrongType() {
            when(sysDictionaryRepository.findByEntityName(ENTITY))
                    .thenReturn(List.of(createDef("first_name", "string", false, null)));

            Map<String, Object> payload = Map.of("first_name", 42);

            List<String> violations = validator.validate(ENTITY, payload, false);

            assertThat(violations).hasSize(1);
            assertThat(violations.getFirst()).contains("must be a string");
        }

        @Test
        @DisplayName("Boolean field with non-boolean value produces violation")
        void booleanFieldWrongType() {
            when(sysDictionaryRepository.findByEntityName(ENTITY))
                    .thenReturn(List.of(createDef("is_active", "boolean", false, null)));

            Map<String, Object> payload = Map.of("is_active", "yes");

            List<String> violations = validator.validate(ENTITY, payload, false);

            assertThat(violations).hasSize(1);
            assertThat(violations.getFirst()).contains("must be a boolean");
        }

        @Test
        @DisplayName("Integer field with non-numeric value produces violation")
        void integerFieldWrongType() {
            when(sysDictionaryRepository.findByEntityName(ENTITY))
                    .thenReturn(List.of(createDef("age", "integer", false, null)));

            Map<String, Object> payload = Map.of("age", "twenty");

            List<String> violations = validator.validate(ENTITY, payload, false);

            assertThat(violations).hasSize(1);
            assertThat(violations.getFirst()).contains("must be an integer");
        }

        @Test
        @DisplayName("Decimal field accepts Number values")
        void decimalFieldAcceptsNumber() {
            when(sysDictionaryRepository.findByEntityName(ENTITY))
                    .thenReturn(List.of(createDef("score", "decimal", false, null)));

            Map<String, Object> payload = Map.of("score", 98.5);

            List<String> violations = validator.validate(ENTITY, payload, false);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("JSONB field accepts Map values")
        void jsonbFieldAcceptsMap() {
            when(sysDictionaryRepository.findByEntityName(ENTITY))
                    .thenReturn(List.of(createDef("taste_vectors", "jsonb", false, null)));

            Map<String, Object> payload = Map.of("taste_vectors", Map.of("key", "value"));

            List<String> violations = validator.validate(ENTITY, payload, false);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("JSONB field rejects primitive values")
        void jsonbFieldRejectsPrimitive() {
            when(sysDictionaryRepository.findByEntityName(ENTITY))
                    .thenReturn(List.of(createDef("taste_vectors", "jsonb", false, null)));

            Map<String, Object> payload = Map.of("taste_vectors", "not-json");

            List<String> violations = validator.validate(ENTITY, payload, false);

            assertThat(violations).hasSize(1);
            assertThat(violations.getFirst()).contains("must be a JSON object or array");
        }

        @Test
        @DisplayName("Vector field accepts List values")
        void vectorFieldAcceptsList() {
            when(sysDictionaryRepository.findByEntityName(ENTITY))
                    .thenReturn(List.of(createDef("embedding", "vector", false, null)));

            Map<String, Object> payload = Map.of("embedding", List.of(0.1, 0.2, 0.3));

            List<String> violations = validator.validate(ENTITY, payload, false);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Date field accepts string values")
        void dateFieldAcceptsString() {
            when(sysDictionaryRepository.findByEntityName(ENTITY))
                    .thenReturn(List.of(createDef("date_of_birth", "date", false, null)));

            Map<String, Object> payload = Map.of("date_of_birth", "1985-06-15");

            List<String> violations = validator.validate(ENTITY, payload, false);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Max Length Constraints")
    class MaxLengthTests {

        @Test
        @DisplayName("String exceeding max length produces violation")
        void stringExceedsMaxLength() {
            when(sysDictionaryRepository.findByEntityName(ENTITY))
                    .thenReturn(List.of(createDef("first_name", "string", false, 5)));

            Map<String, Object> payload = Map.of("first_name", "Arabella");

            List<String> violations = validator.validate(ENTITY, payload, false);

            assertThat(violations).hasSize(1);
            assertThat(violations.getFirst()).contains("exceeds max length of 5");
        }

        @Test
        @DisplayName("String within max length produces no violation")
        void stringWithinMaxLength() {
            when(sysDictionaryRepository.findByEntityName(ENTITY))
                    .thenReturn(List.of(createDef("first_name", "string", false, 100)));

            Map<String, Object> payload = Map.of("first_name", "Arabella");

            List<String> violations = validator.validate(ENTITY, payload, false);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Unknown Field Handling")
    class UnknownFieldTests {

        @Test
        @DisplayName("Unknown field produces violation")
        void unknownFieldRejected() {
            when(sysDictionaryRepository.findByEntityName(ENTITY))
                    .thenReturn(List.of(createDef("first_name", "string", false, 100)));

            Map<String, Object> payload = Map.of("unknown_field", "value");

            List<String> violations = validator.validate(ENTITY, payload, false);

            assertThat(violations).hasSize(1);
            assertThat(violations.getFirst()).contains("Unknown field 'unknown_field'");
        }
    }

    @Nested
    @DisplayName("Schema-less Mode")
    class SchemaLessTests {

        @Test
        @DisplayName("Empty dictionary definitions accept all fields")
        void emptyDefinitionsAcceptAll() {
            when(sysDictionaryRepository.findByEntityName(ENTITY))
                    .thenReturn(List.of());

            Map<String, Object> payload = Map.of(
                    "any_field", "any_value",
                    "another", 42
            );

            List<String> violations = validator.validate(ENTITY, payload, false);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Multiple Violations")
    class MultipleViolationTests {

        @Test
        @DisplayName("Multiple violations are collected")
        void multipleViolationsCollected() {
            when(sysDictionaryRepository.findByEntityName(ENTITY))
                    .thenReturn(List.of(
                            createDef("first_name", "string", true, 100),
                            createDef("email", "string", false, 5)
                    ));

            Map<String, Object> payload = Map.of(
                    "email", "very-long-email@example.com",
                    "bogus", "field"
            );

            List<String> violations = validator.validate(ENTITY, payload, false);

            // Expect: missing required 'first_name', max_length on 'email', unknown 'bogus'
            assertThat(violations).hasSize(3);
        }
    }
}
