package com.luxury.core.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.List;

/**
 * Test configuration providing a mock {@link JwtDecoder} for unit tests
 * that boot the full Spring context.
 *
 * <p>Active only in the "test" profile. Prevents the need for a real
 * Entra ID issuer-uri during testing.</p>
 */
@Configuration
@Profile("test")
public class TestSecurityConfig {

    /**
     * Provides a no-op JwtDecoder that creates a valid Jwt from any token string.
     * This allows the SecurityFilterChain to be constructed without needing
     * a real OIDC issuer endpoint.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> Jwt.withTokenValue(token)
                .header("alg", "none")
                .claim("sub", "test-user")
                .claim("preferred_username", "test@luxury.com")
                .claim("roles", List.of("ROLE_ASSOCIATE"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
