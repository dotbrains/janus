package com.dotbrains.janus.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * Authentication API Interface - Version 1
 * 
 * This interface defines all authentication-related endpoints with comprehensive
 * Swagger/OpenAPI documentation. Controllers implement this interface to keep
 * the implementation classes clean and focused on business logic.
 * 
 * @version 1.0
 * @since 1.0
 */
@Tag(
    name = "Authentication v1",
    description = "OAuth2/OIDC authentication endpoints with Keycloak integration and token enhancement"
)
@RequestMapping("/api/v1/auth")
public interface AuthAPI {

    @Operation(
        summary = "OAuth2 Login Success",
        description = "Called after successful OAuth2 authentication with Keycloak. Returns user information " +
                     "with enhanced claims from the database including roles, department, and custom attributes."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authentication successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                        {
                          "status": "success",
                          "message": "Authentication successful",
                          "username": "john.doe",
                          "email": "john.doe@dotbrains.com",
                          "custom_claims": {
                            "user_id": 1,
                            "employee_id": "EMP001",
                            "department": "Engineering",
                            "job_title": "Senior Software Engineer",
                            "roles": ["USER", "DEVELOPER", "SENIOR"],
                            "is_admin": false
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication failed",
            content = @Content(mediaType = "application/json")
        )
    })
    @SecurityRequirement(name = "oauth2")
    @GetMapping("/success")
    Map<String, Object> loginSuccess(
        @Parameter(hidden = true)
        @AuthenticationPrincipal OidcUser oidcUser
    );

    @Operation(
        summary = "OAuth2 Login Failure",
        description = "Called when OAuth2 authentication fails. Returns error information."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authentication failure response",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "Failure Response",
                    value = """
                        {
                          "status": "error",
                          "message": "Authentication failed"
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/failure")
    Map<String, Object> loginFailure();

    @Operation(
        summary = "Get Current User",
        description = "Retrieves complete user information for the currently authenticated user, " +
                     "including all standard OIDC claims and enhanced claims from the database."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User information retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "User Info Response",
                    value = """
                        {
                          "sub": "kc-user-001",
                          "preferred_username": "john.doe",
                          "email": "john.doe@dotbrains.com",
                          "email_verified": true,
                          "given_name": "John",
                          "family_name": "Doe",
                          "user_id": 1,
                          "employee_id": "EMP001",
                          "department": "Engineering",
                          "job_title": "Senior Software Engineer",
                          "roles": ["USER", "DEVELOPER", "SENIOR"],
                          "is_admin": false,
                          "is_active": true
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "User not authenticated",
            content = @Content(mediaType = "application/json")
        )
    })
    @SecurityRequirement(name = "oauth2")
    @GetMapping("/user")
    Map<String, Object> getCurrentUser(
        @Parameter(hidden = true)
        @AuthenticationPrincipal OidcUser oidcUser
    );

    @Operation(
        summary = "Get Enhanced Token",
        description = "Generates an enhanced JWT token containing both standard OIDC claims from Keycloak " +
                     "and custom claims from the database. This token includes user roles, department information, " +
                     "and other application-specific attributes."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Enhanced token generated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "Enhanced Token Response",
                    value = """
                        {
                          "sub": "kc-user-001",
                          "preferred_username": "john.doe",
                          "email": "john.doe@dotbrains.com",
                          "email_verified": true,
                          "given_name": "John",
                          "family_name": "Doe",
                          "user_id": 1,
                          "keycloak_id": "kc-user-001",
                          "username": "john.doe",
                          "full_name": "John Doe",
                          "employee_id": "EMP001",
                          "department": "Engineering",
                          "job_title": "Senior Software Engineer",
                          "phone_number": "+1-555-0101",
                          "is_active": true,
                          "roles": ["USER", "DEVELOPER", "SENIOR"],
                          "is_admin": false,
                          "created_at": "2024-01-01T00:00:00",
                          "updated_at": "2024-01-12T00:00:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "User not authenticated",
            content = @Content(mediaType = "application/json")
        )
    })
    @SecurityRequirement(name = "oauth2")
    @GetMapping("/token")
    Map<String, Object> getEnhancedToken(
        @Parameter(hidden = true)
        @AuthenticationPrincipal OidcUser oidcUser
    );

    @Operation(
        summary = "Health Check",
        description = "Simple health check endpoint to verify the authentication service is running."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Service is healthy",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "Health Response",
                    value = """
                        {
                          "status": "UP"
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/health")
    Map<String, String> health();
}
