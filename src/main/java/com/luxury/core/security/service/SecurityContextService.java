package com.luxury.core.security.service;

import com.luxury.core.security.model.LuxuryRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service wrapping {@link SecurityContextHolder} for convenient access
 * to the current user's identity and RBAC authorities.
 *
 * <p>Used by the FLS Interceptor and Phantom Enclave Converter to make
 * discretion decisions based on the associate's JWT scope.</p>
 */
@Slf4j
@Service
public class SecurityContextService {

    /**
     * Returns the username (principal name) from the current JWT.
     *
     * @return the authenticated username, or "anonymous" if not authenticated
     */
    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "anonymous";
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            // Prefer 'preferred_username' claim (Entra ID standard)
            String username = jwt.getClaimAsString("preferred_username");
            return username != null ? username : jwt.getSubject();
        }

        return auth.getName();
    }

    /**
     * Checks if the current user has the specified luxury role.
     *
     * @param role the role to check
     * @return true if the user has the role
     */
    public boolean hasRole(LuxuryRole role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(role.getAuthority()));
    }

    /**
     * Checks if the current user has VIP Director access.
     * VIP Directors can see hidden financial indicators (influence_score, net_worth_band).
     *
     * @return true if the user has ROLE_VIP_DIRECTOR
     */
    public boolean hasVipDirectorAccess() {
        return hasRole(LuxuryRole.ROLE_VIP_DIRECTOR);
    }

    /**
     * Checks if the current user has Phantom Clearance.
     * Required for decrypting Phantom Enclave data (application-level encryption).
     *
     * @return true if the user has ROLE_PHANTOM_CLEARANCE
     */
    public boolean hasPhantomClearance() {
        return hasRole(LuxuryRole.ROLE_PHANTOM_CLEARANCE);
    }

    /**
     * Returns all authority strings for the current user.
     *
     * @return set of authority strings (e.g., "ROLE_ASSOCIATE", "ROLE_VIP_DIRECTOR")
     */
    public Set<String> getScopes() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Collections.emptySet();
        }

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}
