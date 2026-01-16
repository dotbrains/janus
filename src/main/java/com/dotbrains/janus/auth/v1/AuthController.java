package com.dotbrains.janus.auth.v1;

import com.dotbrains.janus.api.v1.AuthAPI;
import com.dotbrains.janus.token.TokenCustomizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller - Version 1
 * 
 * Implements OAuth2/OIDC authentication endpoints with Keycloak integration.
 * All Swagger documentation and request mapping are defined in the AuthAPI interface.
 * 
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController implements AuthAPI {

    private final TokenCustomizer tokenCustomizer;

    @Override
    public Map<String, Object> loginSuccess(@AuthenticationPrincipal OidcUser oidcUser) {
        log.info("User logged in successfully: {}", oidcUser.getPreferredUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Authentication successful");
        response.put("username", oidcUser.getPreferredUsername());
        response.put("email", oidcUser.getEmail());

        // Enhance token with database attributes
        Map<String, Object> enhancedClaims = tokenCustomizer.enhanceToken(oidcUser);
        if (!enhancedClaims.isEmpty()) {
            response.put("custom_claims", enhancedClaims);
        }

        return response;
    }

    @Override
    public Map<String, Object> loginFailure() {
        log.warn("User login failed");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Authentication failed");

        return response;
    }

    @Override
    public Map<String, Object> getCurrentUser(@AuthenticationPrincipal OidcUser oidcUser) {
        log.debug("Fetching current user info: {}", oidcUser.getPreferredUsername());

        Map<String, Object> userInfo = new HashMap<>(oidcUser.getClaims());

        // Add enhanced claims
        Map<String, Object> enhancedClaims = tokenCustomizer.enhanceToken(oidcUser);
        userInfo.putAll(enhancedClaims);

        return userInfo;
    }

    @Override
    public Map<String, Object> getEnhancedToken(@AuthenticationPrincipal OidcUser oidcUser) {
        log.debug("Generating enhanced token for user: {}", oidcUser.getPreferredUsername());

        Map<String, Object> response = new HashMap<>();
        
        // Standard OIDC claims
        response.put("sub", oidcUser.getSubject());
        response.put("preferred_username", oidcUser.getPreferredUsername());
        response.put("email", oidcUser.getEmail());
        response.put("email_verified", oidcUser.getEmailVerified());
        response.put("given_name", oidcUser.getGivenName());
        response.put("family_name", oidcUser.getFamilyName());

        // Enhanced claims from database
        Map<String, Object> enhancedClaims = tokenCustomizer.enhanceToken(oidcUser);
        response.putAll(enhancedClaims);

        return response;
    }

    @Override
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
