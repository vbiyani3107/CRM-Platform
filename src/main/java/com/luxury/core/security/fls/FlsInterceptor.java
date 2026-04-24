package com.luxury.core.security.fls;

import com.luxury.common.dto.ApiResponse;
import com.luxury.core.persistence.model.SysDictionary;
import com.luxury.core.persistence.repository.SysDictionaryRepository;
import com.luxury.core.security.service.SecurityContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Field-Level Security (FLS) Interceptor for the Bespoke Luxury Platform.
 *
 * <p>Implements {@link ResponseBodyAdvice} to intercept all {@link ApiResponse}
 * payloads before serialization. If the current user lacks
 * {@code ROLE_VIP_DIRECTOR}, sensitive fields (as defined in {@code sys_dictionary}
 * with {@code is_sensitive = true}) are nullified or stripped from the response.</p>
 *
 * <p>This ensures that financial indicators (influence scores, net worth bands)
 * and other discretion-tier data are never sent to unauthorized associates.</p>
 *
 * @see SysDictionaryRepository#findByEntityNameAndIsSensitiveTrue(String)
 * @see SecurityContextService#hasVipDirectorAccess()
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class FlsInterceptor implements ResponseBodyAdvice<Object> {

    private final SecurityContextService securityContextService;
    private final SysDictionaryRepository sysDictionaryRepository;

    @Value("${luxury.security.fls.enabled:true}")
    private boolean flsEnabled;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Apply to all controller responses
        return flsEnabled;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        // Skip if user already has VIP Director access
        if (securityContextService.hasVipDirectorAccess()) {
            log.debug("FLS: VIP Director access detected — returning full payload");
            return body;
        }

        // Only intercept ApiResponse wrappers
        if (!(body instanceof ApiResponse<?> apiResponse)) {
            return body;
        }

        Object data = apiResponse.getData();
        if (data == null) {
            return body;
        }

        // Strip sensitive fields from the response data
        stripSensitiveFields(data);

        return body;
    }

    /**
     * Strips sensitive fields from the response data object.
     * Handles both entity objects (via reflection) and Map-based JSONB data.
     */
    @SuppressWarnings("unchecked")
    private void stripSensitiveFields(Object data) {
        if (data instanceof List<?> list) {
            list.forEach(this::stripSensitiveFields);
            return;
        }

        // Determine the entity name from the object's simple class name
        String entityName = resolveEntityName(data);
        if (entityName == null) {
            return;
        }

        // Fetch sensitive field definitions from sys_dictionary
        List<SysDictionary> sensitiveFields =
                sysDictionaryRepository.findByEntityNameAndIsSensitiveTrue(entityName);

        if (sensitiveFields.isEmpty()) {
            return;
        }

        Set<String> sensitiveFieldNames = sensitiveFields.stream()
                .map(SysDictionary::getAttributeName)
                .collect(Collectors.toSet());

        log.debug("FLS: Stripping {} sensitive fields from entity '{}'",
                sensitiveFieldNames.size(), entityName);

        // Strip via reflection on the Java object
        for (String fieldName : sensitiveFieldNames) {
            nullifyField(data, fieldName);
        }

        // Also strip from custom_attributes JSONB map if present
        stripFromCustomAttributes(data, sensitiveFieldNames);
    }

    /**
     * Resolves the entity name from the object.
     * Converts PascalCase class name to snake_case table convention.
     */
    private String resolveEntityName(Object data) {
        if (data instanceof Map) {
            // Map-based payloads may contain an _entity_name key
            Object entityName = ((Map<?, ?>) data).get("_entity_name");
            return entityName != null ? entityName.toString() : null;
        }

        // Convert class name: HyperProfile -> hyper_profiles (pluralized snake_case)
        String className = data.getClass().getSimpleName();
        String snakeCase = className.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        return snakeCase + "s";
    }

    /**
     * Sets a field to null via reflection.
     */
    private void nullifyField(Object target, String fieldName) {
        String camelCaseFieldName = snakeToCamel(fieldName);
        try {
            Field field = findField(target.getClass(), camelCaseFieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(target, null);
                log.trace("FLS: Nullified field '{}' on {}", fieldName, target.getClass().getSimpleName());
            }
        } catch (IllegalAccessException e) {
            log.warn("FLS: Could not nullify field '{}': {}", fieldName, e.getMessage());
        }
    }

    /**
     * Recursively searches for a field in the class hierarchy.
     */
    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Removes sensitive keys from the custom_attributes JSONB map.
     */
    @SuppressWarnings("unchecked")
    private void stripFromCustomAttributes(Object data, Set<String> sensitiveFieldNames) {
        try {
            Field customAttrsField = findField(data.getClass(), "customAttributes");
            if (customAttrsField != null) {
                customAttrsField.setAccessible(true);
                Object value = customAttrsField.get(data);
                if (value instanceof Map<?, ?> map) {
                    Map<String, Object> mutableMap = (Map<String, Object>) map;
                    sensitiveFieldNames.forEach(mutableMap::remove);
                }
            }
        } catch (IllegalAccessException e) {
            log.warn("FLS: Could not strip custom_attributes: {}", e.getMessage());
        }
    }

    /**
     * Converts snake_case to camelCase.
     */
    private String snakeToCamel(String snakeCase) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
