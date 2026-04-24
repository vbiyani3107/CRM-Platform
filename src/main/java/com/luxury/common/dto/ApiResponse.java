package com.luxury.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Standard API response wrapper following RFC 7807 error conventions.
 * All controller responses are wrapped in this envelope.
 *
 * @param <T> The type of the response payload.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ErrorDetail error;
    private Instant timestamp;

    /**
     * Factory method for successful responses.
     */
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Factory method for error responses (RFC 7807 aligned).
     */
    public static <T> ApiResponse<T> fail(String type, String title, String detail, int status) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .type(type)
                        .title(title)
                        .detail(detail)
                        .status(status)
                        .build())
                .timestamp(Instant.now())
                .build();
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetail {
        private String type;
        private String title;
        private String detail;
        private int status;
    }
}
