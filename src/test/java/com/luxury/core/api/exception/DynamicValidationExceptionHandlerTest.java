package com.luxury.core.api.exception;

import com.luxury.common.dto.ApiResponse;
import com.luxury.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that {@link GlobalExceptionHandler} correctly handles
 * {@link DynamicValidationException} and produces RFC 7807 error responses.
 */
class DynamicValidationExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("DynamicValidationException produces 400 with all violations")
    void producesRfc7807WithViolations() {
        List<String> violations = List.of(
                "Required field 'first_name' is missing",
                "Unknown field 'bogus' is not defined for entity 'hyper_profiles'"
        );
        DynamicValidationException ex = new DynamicValidationException(violations);

        ResponseEntity<ApiResponse<Void>> response = handler.handleDynamicValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError().getType())
                .isEqualTo("https://luxury.com/errors/dynamic-validation");
        assertThat(response.getBody().getError().getTitle())
                .isEqualTo("Dynamic Validation Error");
        assertThat(response.getBody().getError().getDetail())
                .contains("Required field 'first_name' is missing")
                .contains("Unknown field 'bogus'");
        assertThat(response.getBody().getError().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("Single violation produces clean detail string")
    void singleViolationProducesCleanDetail() {
        DynamicValidationException ex = new DynamicValidationException(
                List.of("Field 'email' exceeds max length of 255"));

        ResponseEntity<ApiResponse<Void>> response = handler.handleDynamicValidation(ex);

        assertThat(response.getBody().getError().getDetail())
                .isEqualTo("Field 'email' exceeds max length of 255");
    }

    @Test
    @DisplayName("Exception message includes all violations")
    void exceptionMessageIncludesViolations() {
        List<String> violations = List.of("violation1", "violation2");
        DynamicValidationException ex = new DynamicValidationException(violations);

        assertThat(ex.getMessage()).contains("violation1").contains("violation2");
        assertThat(ex.getViolations()).hasSize(2);
        assertThat(ex.getViolations()).containsExactly("violation1", "violation2");
    }
}
