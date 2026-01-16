package com.dotbrains.janus.api.v1;

import com.dotbrains.janus.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * User Management API Interface - Version 1
 * 
 * Provides user management endpoints for querying and managing users.
 * 
 * @version 1.0
 * @since 1.0
 */
@Tag(
    name = "User Management v1",
    description = "User management endpoints for querying and managing users"
)
@RequestMapping("/api/v1/users")
public interface UserAPI {

    @Operation(
        summary = "Get User by Keycloak ID",
        description = "Retrieves a user by their Keycloak ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "User not found",
                          "keycloakId": "kc-user-001"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Not authenticated",
            content = @Content(mediaType = "application/json")
        )
    })
    @SecurityRequirement(name = "oauth2")
    @GetMapping("/keycloak/{keycloakId}")
    Map<String, Object> getUserByKeycloakId(
        @Parameter(description = "Keycloak user ID", required = true)
        @PathVariable String keycloakId
    );

    @Operation(
        summary = "Get User by Username",
        description = "Retrieves a user by their username with all associated roles"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "User not found",
                          "username": "john.doe"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Not authenticated",
            content = @Content(mediaType = "application/json")
        )
    })
    @SecurityRequirement(name = "oauth2")
    @GetMapping("/username/{username}")
    Map<String, Object> getUserByUsername(
        @Parameter(description = "Username", required = true)
        @PathVariable String username
    );

    @Operation(
        summary = "Check User Existence",
        description = "Checks if a user exists by Keycloak ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Check completed",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "exists": true,
                          "keycloakId": "kc-user-001"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Not authenticated",
            content = @Content(mediaType = "application/json")
        )
    })
    @SecurityRequirement(name = "oauth2")
    @GetMapping("/exists/{keycloakId}")
    Map<String, Object> checkUserExists(
        @Parameter(description = "Keycloak user ID", required = true)
        @PathVariable String keycloakId
    );

    @Operation(
        summary = "Deactivate User",
        description = "Deactivates a user account by setting their active status to false"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User deactivated successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "status": "success",
                          "message": "User deactivated successfully",
                          "keycloakId": "kc-user-001"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "User not found",
                          "keycloakId": "kc-user-001"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Not authenticated",
            content = @Content(mediaType = "application/json")
        )
    })
    @SecurityRequirement(name = "oauth2")
    @PostMapping("/{keycloakId}/deactivate")
    Map<String, Object> deactivateUser(
        @Parameter(description = "Keycloak user ID", required = true)
        @PathVariable String keycloakId
    );
}
