package com.luxury.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the ApiResponse wrapper.
 */
@DisplayName("ApiResponse Unit Tests")
class ApiResponseTest {

    @Test
    @DisplayName("ok() should create successful response with data")
    void okShouldCreateSuccessResponse() {
        ApiResponse<String> response = ApiResponse.ok("test-data");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("test-data");
        assertThat(response.getError()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("fail() should create error response with RFC 7807 detail")
    void failShouldCreateErrorResponse() {
        ApiResponse<Void> response = ApiResponse.fail(
                "https://luxury.com/errors/not-found",
                "Not Found",
                "Entity hyper_profiles/123 not found",
                404
        );

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getError()).isNotNull();
        assertThat(response.getError().getType()).isEqualTo("https://luxury.com/errors/not-found");
        assertThat(response.getError().getTitle()).isEqualTo("Not Found");
        assertThat(response.getError().getDetail()).contains("hyper_profiles/123");
        assertThat(response.getError().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("ok() with null data should still be successful")
    void okWithNullDataShouldBeSuccessful() {
        ApiResponse<Object> response = ApiResponse.ok(null);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNull();
    }
}
