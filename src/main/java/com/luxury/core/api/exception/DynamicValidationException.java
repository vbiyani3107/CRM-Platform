package com.luxury.core.api.exception;

import java.util.List;

/**
 * Thrown when dynamic payload validation against {@code sys_dictionary}
 * definitions fails. Contains one or more human-readable violation messages.
 *
 * <p>Handled by {@code GlobalExceptionHandler} to produce an RFC 7807
 * error response with all violations listed in the detail field.</p>
 */
public class DynamicValidationException extends RuntimeException {

    private final List<String> violations;

    public DynamicValidationException(List<String> violations) {
        super("Dynamic validation failed: " + String.join("; ", violations));
        this.violations = List.copyOf(violations);
    }

    /**
     * Returns an unmodifiable list of all validation violations.
     */
    public List<String> getViolations() {
        return violations;
    }
}
