package com.luxury.core.security.service;

import com.luxury.core.security.model.LuxuryRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SecurityContextService}.
 * Uses direct SecurityContextHolder manipulation (no Spring context needed).
 */
@DisplayName("SecurityContextService — Unit Tests")
class SecurityContextServiceTest {

    private final SecurityContextService service = new SecurityContextService();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ─── Helper Methods ──────────────────────────────────────────────────────

    private void authenticateWithRoles(String username, String... roles) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .claim("preferred_username", username)
                .claim("sub", "sub-" + username)
                .claim("roles", List.of(roles))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void authenticateAnonymous() {
        SecurityContextHolder.clearContext();
    }

    // ─── getCurrentUsername ───────────────────────────────────────────────────

    @Nested
    @DisplayName("getCurrentUsername()")
    class GetCurrentUsername {

        @Test
        @DisplayName("Should return preferred_username from JWT")
        void shouldReturnPreferredUsername() {
            authenticateWithRoles("sophia.laurent@luxury.com", "ROLE_ASSOCIATE");

            assertThat(service.getCurrentUsername()).isEqualTo("sophia.laurent@luxury.com");
        }

        @Test
        @DisplayName("Should return 'anonymous' when not authenticated")
        void shouldReturnAnonymousWhenNotAuthenticated() {
            authenticateAnonymous();

            assertThat(service.getCurrentUsername()).isEqualTo("anonymous");
        }

        @Test
        @DisplayName("Should fall back to subject when preferred_username is missing")
        void shouldFallBackToSubject() {
            Jwt jwt = Jwt.withTokenValue("test-token")
                    .header("alg", "RS256")
                    .claim("sub", "sub-12345")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);

            assertThat(service.getCurrentUsername()).isEqualTo("sub-12345");
        }
    }

    // ─── hasRole ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("hasRole()")
    class HasRole {

        @Test
        @DisplayName("Should return true when user has the requested role")
        void shouldReturnTrueForMatchingRole() {
            authenticateWithRoles("associate@luxury.com", "ROLE_ASSOCIATE");

            assertThat(service.hasRole(LuxuryRole.ROLE_ASSOCIATE)).isTrue();
        }

        @Test
        @DisplayName("Should return false when user lacks the requested role")
        void shouldReturnFalseForMissingRole() {
            authenticateWithRoles("associate@luxury.com", "ROLE_ASSOCIATE");

            assertThat(service.hasRole(LuxuryRole.ROLE_VIP_DIRECTOR)).isFalse();
        }

        @Test
        @DisplayName("Should return false when not authenticated")
        void shouldReturnFalseWhenNotAuthenticated() {
            authenticateAnonymous();

            assertThat(service.hasRole(LuxuryRole.ROLE_ASSOCIATE)).isFalse();
        }
    }

    // ─── hasVipDirectorAccess ────────────────────────────────────────────────

    @Nested
    @DisplayName("hasVipDirectorAccess()")
    class HasVipDirectorAccess {

        @Test
        @DisplayName("Should return true for VIP Director")
        void shouldReturnTrueForVipDirector() {
            authenticateWithRoles("director@luxury.com", "ROLE_VIP_DIRECTOR");

            assertThat(service.hasVipDirectorAccess()).isTrue();
        }

        @Test
        @DisplayName("Should return false for regular associate")
        void shouldReturnFalseForAssociate() {
            authenticateWithRoles("associate@luxury.com", "ROLE_ASSOCIATE");

            assertThat(service.hasVipDirectorAccess()).isFalse();
        }
    }

    // ─── hasPhantomClearance ─────────────────────────────────────────────────

    @Nested
    @DisplayName("hasPhantomClearance()")
    class HasPhantomClearance {

        @Test
        @DisplayName("Should return true for Phantom Clearance holder")
        void shouldReturnTrueForPhantomClearance() {
            authenticateWithRoles("phantom@luxury.com", "ROLE_PHANTOM_CLEARANCE");

            assertThat(service.hasPhantomClearance()).isTrue();
        }

        @Test
        @DisplayName("Should return false for VIP Director without Phantom Clearance")
        void shouldReturnFalseForVipDirectorWithoutPhantom() {
            authenticateWithRoles("director@luxury.com", "ROLE_VIP_DIRECTOR");

            assertThat(service.hasPhantomClearance()).isFalse();
        }
    }

    // ─── getScopes ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getScopes()")
    class GetScopes {

        @Test
        @DisplayName("Should return all authorities for multi-role user")
        void shouldReturnAllAuthorities() {
            authenticateWithRoles("director@luxury.com",
                    "ROLE_ASSOCIATE", "ROLE_VIP_DIRECTOR");

            Set<String> scopes = service.getScopes();

            assertThat(scopes).containsExactlyInAnyOrder(
                    "ROLE_ASSOCIATE", "ROLE_VIP_DIRECTOR");
        }

        @Test
        @DisplayName("Should return empty set when not authenticated")
        void shouldReturnEmptySetWhenNotAuthenticated() {
            authenticateAnonymous();

            assertThat(service.getScopes()).isEmpty();
        }
    }
}
