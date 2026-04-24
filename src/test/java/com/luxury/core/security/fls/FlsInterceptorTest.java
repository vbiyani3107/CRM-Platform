package com.luxury.core.security.fls;

import com.luxury.common.dto.ApiResponse;
import com.luxury.core.persistence.model.SysDictionary;
import com.luxury.core.persistence.repository.SysDictionaryRepository;
import com.luxury.core.security.service.SecurityContextService;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link FlsInterceptor}.
 * Verifies that sensitive fields are stripped from API responses
 * based on the user's RBAC clearance level.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FlsInterceptor — Unit Tests")
class FlsInterceptorTest {

    @Mock
    private SecurityContextService securityContextService;

    @Mock
    private SysDictionaryRepository sysDictionaryRepository;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @InjectMocks
    private FlsInterceptor flsInterceptor;

    // ─── Test DTO mimicking HyperProfile ─────────────────────────────────────

    @Getter
    @Setter
    static class HyperProfile {
        private String name;
        private BigDecimal influenceScore;
        private String netWorthBand;
        private Map<String, Object> customAttributes;
    }

    // ─── Helper Methods ──────────────────────────────────────────────────────

    private SysDictionary sensitiveField(String entityName, String attributeName) {
        SysDictionary dict = new SysDictionary();
        dict.setEntityName(entityName);
        dict.setAttributeName(attributeName);
        dict.setDataType("string");
        dict.setIsSensitive(true);
        return dict;
    }

    private HyperProfile createTestProfile() {
        HyperProfile profile = new HyperProfile();
        profile.setName("Sophia Laurent");
        profile.setInfluenceScore(new BigDecimal("9.7"));
        profile.setNetWorthBand("$50M-100M");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("preferred_style", "minimalist");
        attrs.put("wealth_indicators", Map.of("offshore", true));
        profile.setCustomAttributes(attrs);
        return profile;
    }

    // ─── Tests ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("When user has VIP Director access")
    class WithVipAccess {

        @Test
        @DisplayName("Should return full payload without stripping")
        void shouldReturnFullPayload() {
            ReflectionTestUtils.setField(flsInterceptor, "flsEnabled", true);
            when(securityContextService.hasVipDirectorAccess()).thenReturn(true);

            HyperProfile profile = createTestProfile();
            ApiResponse<HyperProfile> body = ApiResponse.ok(profile);

            Object result = flsInterceptor.beforeBodyWrite(
                    body, null, MediaType.APPLICATION_JSON, null, request, response);

            @SuppressWarnings("unchecked")
            ApiResponse<HyperProfile> apiResult = (ApiResponse<HyperProfile>) result;
            assertThat(apiResult.getData().getInfluenceScore()).isNotNull();
            assertThat(apiResult.getData().getNetWorthBand()).isNotNull();
            assertThat(apiResult.getData().getCustomAttributes()).containsKey("wealth_indicators");
        }
    }

    @Nested
    @DisplayName("When user lacks VIP Director access")
    class WithoutVipAccess {

        @Test
        @DisplayName("Should strip sensitive fields from the response")
        void shouldStripSensitiveFields() {
            ReflectionTestUtils.setField(flsInterceptor, "flsEnabled", true);
            when(securityContextService.hasVipDirectorAccess()).thenReturn(false);

            // Mock sys_dictionary returning sensitive field definitions
            when(sysDictionaryRepository.findByEntityNameAndIsSensitiveTrue("hyper_profiles"))
                    .thenReturn(List.of(
                            sensitiveField("hyper_profiles", "influence_score"),
                            sensitiveField("hyper_profiles", "net_worth_band"),
                            sensitiveField("hyper_profiles", "wealth_indicators")
                    ));

            HyperProfile profile = createTestProfile();
            ApiResponse<HyperProfile> body = ApiResponse.ok(profile);

            flsInterceptor.beforeBodyWrite(
                    body, null, MediaType.APPLICATION_JSON, null, request, response);

            // Sensitive fields should be nullified
            assertThat(profile.getInfluenceScore()).isNull();
            assertThat(profile.getNetWorthBand()).isNull();
            // Non-sensitive fields should remain
            assertThat(profile.getName()).isEqualTo("Sophia Laurent");
            assertThat(profile.getCustomAttributes()).containsKey("preferred_style");
            // Sensitive JSONB keys should be removed
            assertThat(profile.getCustomAttributes()).doesNotContainKey("wealth_indicators");
        }

        @Test
        @DisplayName("Should not strip when no sensitive fields are defined")
        void shouldNotStripWhenNoSensitiveFieldsDefined() {
            ReflectionTestUtils.setField(flsInterceptor, "flsEnabled", true);
            when(securityContextService.hasVipDirectorAccess()).thenReturn(false);
            when(sysDictionaryRepository.findByEntityNameAndIsSensitiveTrue(anyString()))
                    .thenReturn(List.of());

            HyperProfile profile = createTestProfile();
            ApiResponse<HyperProfile> body = ApiResponse.ok(profile);

            flsInterceptor.beforeBodyWrite(
                    body, null, MediaType.APPLICATION_JSON, null, request, response);

            assertThat(profile.getInfluenceScore()).isNotNull();
            assertThat(profile.getNetWorthBand()).isNotNull();
        }
    }

    @Nested
    @DisplayName("FLS Disabled")
    class FlsDisabled {

        @Test
        @DisplayName("Should pass through when FLS is disabled")
        void shouldPassThroughWhenDisabled() {
            ReflectionTestUtils.setField(flsInterceptor, "flsEnabled", false);

            assertThat(flsInterceptor.supports(null, null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Non-ApiResponse bodies")
    class NonApiResponse {

        @Test
        @DisplayName("Should pass through non-ApiResponse bodies unchanged")
        void shouldPassThroughNonApiResponse() {
            ReflectionTestUtils.setField(flsInterceptor, "flsEnabled", true);
            when(securityContextService.hasVipDirectorAccess()).thenReturn(false);

            String rawBody = "plain string response";
            Object result = flsInterceptor.beforeBodyWrite(
                    rawBody, null, MediaType.APPLICATION_JSON, null, request, response);

            assertThat(result).isEqualTo(rawBody);
        }
    }
}
