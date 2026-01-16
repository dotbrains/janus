package com.dotbrains.janus.token;

import com.dotbrains.janus.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Maps user attributes to JWT claims using SpEL expressions
 */
@Component
@Slf4j
public class CustomClaimsMapper {

    // SpelExpressionParser is thread-safe and can be reused
    private final SpelExpressionParser spelParser = new SpelExpressionParser();

    /**
     * Generate custom claims from a user object
     * Uses SpEL expressions for dynamic claim generation
     *
     * @param user the user object
     * @return Map of custom claims
     */
    public Map<String, Object> mapUserToClaims(User user) {
        log.debug("Mapping user to custom claims: {}", user.getUsername());

        Map<String, Object> claims = new HashMap<>();
        StandardEvaluationContext context = new StandardEvaluationContext(user);

        // Basic user information
        addClaimIfPresent(claims, "user_id", user.getId());
        addClaimIfPresent(claims, "keycloak_id", user.getKeycloakId());
        addClaimIfPresent(claims, "username", user.getUsername());
        addClaimIfPresent(claims, "email", user.getEmail());

        // Full name using SpEL expression
        Expression fullNameExpression = spelParser.parseExpression("firstName + ' ' + lastName");
        try {
            String fullName = fullNameExpression.getValue(context, String.class);
            addClaimIfPresent(claims, "full_name", fullName);
        } catch (Exception e) {
            log.debug("Could not evaluate full name expression, using fallback");
            addClaimIfPresent(claims, "full_name", user.getFullName());
        }

        // Employee information
        addClaimIfPresent(claims, "employee_id", user.getEmployeeId());
        addClaimIfPresent(claims, "department", user.getDepartment());
        addClaimIfPresent(claims, "job_title", user.getJobTitle());
        addClaimIfPresent(claims, "phone_number", user.getPhoneNumber());

        // User status using SpEL expression
        Expression isActiveExpression = spelParser.parseExpression("isActive == true");
        Boolean isActive = isActiveExpression.getValue(context, Boolean.class);
        addClaimIfPresent(claims, "is_active", isActive);

        // Roles
        Set<String> roles = user.getRoleNames();
        if (!roles.isEmpty()) {
            claims.put("roles", roles);

            // Add role-based claims using SpEL
            Expression hasAdminRoleExpression = spelParser.parseExpression("roleNames.contains('ADMIN')");
            Boolean hasAdminRole = hasAdminRoleExpression.getValue(context, Boolean.class);
            claims.put("is_admin", hasAdminRole != null && hasAdminRole);
        }

        // Timestamps
        addClaimIfPresent(claims, "created_at", user.getCreatedAt());
        addClaimIfPresent(claims, "updated_at", user.getUpdatedAt());

        log.debug("Generated {} custom claims for user: {}", claims.size(), user.getUsername());
        return claims;
    }

    /**
     * Conditionally add claim if the value is not null
     */
    private void addClaimIfPresent(Map<String, Object> claims, String key, Object value) {
        if (value != null) {
            claims.put(key, value);
        }
    }
}
