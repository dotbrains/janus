package com.dotbrains.janus.token;

import com.dotbrains.janus.user.User;
import com.dotbrains.janus.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Customizes JWT tokens by enriching them with database attributes
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCustomizer {

    private final UserService userService;
    private final CustomClaimsMapper claimsMapper;

    @Value("${janus.token.enhancement.enabled}")
    private boolean enhancementEnabled;

    @Value("${janus.token.enhancement.include-user-roles}")
    private boolean includeUserRoles;

    @Value("${janus.token.enhancement.include-user-attributes}")
    private boolean includeUserAttributes;

    /**
     * Enhance token with custom claims from a database
     *
     * @param oidcUser the OIDC user from Keycloak
     * @return Map of enhanced claims
     */
    public Map<String, Object> enhanceToken(OidcUser oidcUser) {
        Map<String, Object> enhancedClaims = new HashMap<>();

        if (!enhancementEnabled) {
            log.debug("Token enhancement is disabled");
            return enhancedClaims;
        }

        // Extract user identifier from Keycloak token
        String keycloakId = oidcUser.getSubject(); // Subject claim contains user ID
        String username = oidcUser.getPreferredUsername();

        log.debug("Enhancing token for user: {} (Keycloak ID: {})", username, keycloakId);

        // Fetch user from a database
        Optional<User> userOptional = userService.findActiveUserWithRoles(keycloakId);

        if (userOptional.isEmpty()) {
            log.warn("User not found in database: {}", keycloakId);
            // Optionally sync user from Keycloak
            User syncedUser = syncUserFromKeycloak(oidcUser);
            if (syncedUser == null) {
                return enhancedClaims;
            }

            userOptional = Optional.of(syncedUser);
        }

        User user = userOptional.get();

        // Add custom claims based on configuration
        if (includeUserAttributes) {
            Map<String, Object> customClaims = claimsMapper.mapUserToClaims(user);
            enhancedClaims.putAll(customClaims);
        }

        // Explicitly add roles if configured (even if attributes are disabled)
        if (includeUserRoles && !includeUserAttributes) {
            enhancedClaims.put("roles", user.getRoleNames());
        }

        log.debug("Enhanced token with {} additional claims", enhancedClaims.size());
        return enhancedClaims;
    }

    /**
     * Synchronize user from Keycloak to a local database
     *
     * @param oidcUser the OIDC user from Keycloak
     * @return the synchronized user
     */
    private User syncUserFromKeycloak(OidcUser oidcUser) {
        try {
            log.info("Syncing user from Keycloak: {}", oidcUser.getPreferredUsername());

            String keycloakId = oidcUser.getSubject();
            String username = oidcUser.getPreferredUsername();
            String email = oidcUser.getEmail();
            String firstName = oidcUser.getGivenName();
            String lastName = oidcUser.getFamilyName();

            return userService.syncUserFromKeycloak(
                    keycloakId,
                    username,
                    email,
                    firstName,
                    lastName
            );
        } catch (Exception e) {
            log.error("Failed to sync user from Keycloak", e);
            return null;
        }
    }
}
