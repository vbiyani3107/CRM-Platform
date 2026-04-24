package com.luxury.core.security.model;

/**
 * Core RBAC roles for the Bespoke Luxury Platform.
 *
 * <p>These roles map to Entra ID group claims in the JWT and determine
 * the associate's access level across all luxury domain operations.</p>
 *
 * <ul>
 *   <li>{@link #ROLE_ASSOCIATE} — Standard boutique associate access.</li>
 *   <li>{@link #ROLE_VIP_DIRECTOR} — Access to hidden financial indicators.</li>
 *   <li>{@link #ROLE_PHANTOM_CLEARANCE} — Highest tier, MFA-authenticated, can decrypt enclaves.</li>
 * </ul>
 */
public enum LuxuryRole {

    /**
     * Read/write access to assigned profiles and basic vault items.
     */
    ROLE_ASSOCIATE("ROLE_ASSOCIATE"),

    /**
     * Global read/write, access to hidden financial indicators
     * (e.g., Net Worth Bands, Influence Scores).
     */
    ROLE_VIP_DIRECTOR("ROLE_VIP_DIRECTOR"),

    /**
     * Highest tier. Multi-Factor authenticated.
     * Required to decrypt Phantom Enclave data (application-level encryption).
     */
    ROLE_PHANTOM_CLEARANCE("ROLE_PHANTOM_CLEARANCE");

    private final String authority;

    LuxuryRole(String authority) {
        this.authority = authority;
    }

    /**
     * Returns the Spring Security authority string for this role.
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * Parse a Spring Security authority string back to a LuxuryRole.
     *
     * @param authority the authority string (e.g., "ROLE_ASSOCIATE")
     * @return the matching LuxuryRole
     * @throws IllegalArgumentException if no matching role is found
     */
    public static LuxuryRole fromAuthority(String authority) {
        for (LuxuryRole role : values()) {
            if (role.authority.equals(authority)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown luxury role: " + authority);
    }
}
