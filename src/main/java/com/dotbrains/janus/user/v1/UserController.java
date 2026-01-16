package com.dotbrains.janus.user.v1;

import com.dotbrains.janus.api.v1.UserAPI;
import com.dotbrains.janus.user.User;
import com.dotbrains.janus.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * User Management Controller - Version 1
 * 
 * Implements user management endpoints for querying and managing users.
 * All Swagger documentation and request mapping are defined in the UserAPI interface.
 * 
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController implements UserAPI {

    private final UserService userService;

    @Override
    public Map<String, Object> getUserByKeycloakId(String keycloakId) {
        log.debug("Fetching user by Keycloak ID: {}", keycloakId);
        
        Optional<User> userOptional = userService.findByKeycloakId(keycloakId);
        
        if (userOptional.isEmpty()) {
            log.warn("User not found with Keycloak ID: {}", keycloakId);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "User not found");
            errorResponse.put("keycloakId", keycloakId);
            return errorResponse;
        }
        
        return convertUserToMap(userOptional.get());
    }

    @Override
    public Map<String, Object> getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        
        Optional<User> userOptional = userService.findByUsernameWithRoles(username);
        
        if (userOptional.isEmpty()) {
            log.warn("User not found with username: {}", username);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "User not found");
            errorResponse.put("username", username);
            return errorResponse;
        }
        
        return convertUserToMap(userOptional.get());
    }

    @Override
    public Map<String, Object> checkUserExists(String keycloakId) {
        log.debug("Checking if user exists with Keycloak ID: {}", keycloakId);
        
        boolean exists = userService.existsByKeycloakId(keycloakId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("keycloakId", keycloakId);
        
        return response;
    }

    @Override
    public Map<String, Object> deactivateUser(String keycloakId) {
        log.info("Deactivating user with Keycloak ID: {}", keycloakId);
        
        // Check if user exists first
        Optional<User> userOptional = userService.findByKeycloakId(keycloakId);
        
        if (userOptional.isEmpty()) {
            log.warn("Cannot deactivate - user not found with Keycloak ID: {}", keycloakId);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "User not found");
            errorResponse.put("keycloakId", keycloakId);
            return errorResponse;
        }
        
        userService.deactivateUser(keycloakId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "User deactivated successfully");
        response.put("keycloakId", keycloakId);
        
        return response;
    }

    /**
     * Convert User entity to a Map for API response
     * 
     * @param user the user entity
     * @return Map representation of the user
     */
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("keycloakId", user.getKeycloakId());
        userMap.put("username", user.getUsername());
        userMap.put("email", user.getEmail());
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());
        userMap.put("employeeId", user.getEmployeeId());
        userMap.put("department", user.getDepartment());
        userMap.put("jobTitle", user.getJobTitle());
        userMap.put("phoneNumber", user.getPhoneNumber());
        userMap.put("isActive", user.getIsActive());
        userMap.put("roles", user.getRoleNames());
        userMap.put("createdAt", user.getCreatedAt());
        userMap.put("updatedAt", user.getUpdatedAt());
        
        return userMap;
    }
}
