package com.dotbrains.janus.auth.v1;

import com.dotbrains.janus.token.TokenCustomizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Mock
    private TokenCustomizer tokenCustomizer;

    @InjectMocks
    private AuthController authController;

    private OidcUser oidcUser;

    @BeforeEach
    void setUp() {
        OidcIdToken idToken = OidcIdToken.withTokenValue("test-token")
                .claim("sub", "kc-123")
                .claim("preferred_username", "john.doe")
                .claim("email", "john.doe@example.com")
                .claim("email_verified", true)
                .claim("given_name", "John")
                .claim("family_name", "Doe")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        oidcUser = new DefaultOidcUser(
                Set.of(() -> "USER"),
                idToken
        );
    }

    @Test
    @DisplayName("Should return success response on login")
    void shouldReturnSuccessResponseOnLogin() {
        // Given
        Map<String, Object> enhancedClaims = new HashMap<>();
        enhancedClaims.put("department", "Engineering");
        enhancedClaims.put("job_title", "Software Engineer");
        when(tokenCustomizer.enhanceToken(oidcUser)).thenReturn(enhancedClaims);

        // When
        Map<String, Object> response = authController.loginSuccess(oidcUser);

        // Then
        assertThat(response).isNotEmpty();
        assertThat(response).containsEntry("status", "success");
        assertThat(response).containsEntry("message", "Authentication successful");
        assertThat(response).containsEntry("username", "john.doe");
        assertThat(response).containsEntry("email", "john.doe@example.com");
        assertThat(response).containsKey("custom_claims");

        @SuppressWarnings("unchecked")
        Map<String, Object> customClaims = (Map<String, Object>) response.get("custom_claims");
        assertThat(customClaims).containsEntry("department", "Engineering");
        assertThat(customClaims).containsEntry("job_title", "Software Engineer");

        verify(tokenCustomizer, times(1)).enhanceToken(oidcUser);
    }

    @Test
    @DisplayName("Should not include custom_claims when enhancement returns empty map")
    void shouldNotIncludeCustomClaimsWhenEnhancementReturnsEmptyMap() {
        // Given
        when(tokenCustomizer.enhanceToken(oidcUser)).thenReturn(new HashMap<>());

        // When
        Map<String, Object> response = authController.loginSuccess(oidcUser);

        // Then
        assertThat(response).containsEntry("status", "success");
        assertThat(response).containsEntry("username", "john.doe");
        assertThat(response).doesNotContainKey("custom_claims");
        verify(tokenCustomizer, times(1)).enhanceToken(oidcUser);
    }

    @Test
    @DisplayName("Should return error response on login failure")
    void shouldReturnErrorResponseOnLoginFailure() {
        // When
        Map<String, Object> response = authController.loginFailure();

        // Then
        assertThat(response).containsEntry("status", "error");
        assertThat(response).containsEntry("message", "Authentication failed");
        verify(tokenCustomizer, never()).enhanceToken(any());
    }

    @Test
    @DisplayName("Should return current user with enhanced claims")
    void shouldReturnCurrentUserWithEnhancedClaims() {
        // Given
        Map<String, Object> enhancedClaims = new HashMap<>();
        enhancedClaims.put("department", "Engineering");
        enhancedClaims.put("roles", Set.of("USER", "ADMIN"));
        when(tokenCustomizer.enhanceToken(oidcUser)).thenReturn(enhancedClaims);

        // When
        Map<String, Object> response = authController.getCurrentUser(oidcUser);

        // Then
        assertThat(response).isNotEmpty();
        assertThat(response).containsEntry("sub", "kc-123");
        assertThat(response).containsEntry("preferred_username", "john.doe");
        assertThat(response).containsEntry("email", "john.doe@example.com");
        assertThat(response).containsEntry("department", "Engineering");
        assertThat(response).containsEntry("roles", Set.of("USER", "ADMIN"));
        verify(tokenCustomizer, times(1)).enhanceToken(oidcUser);
    }

    @Test
    @DisplayName("Should return enhanced token with all claims")
    void shouldReturnEnhancedTokenWithAllClaims() {
        // Given
        Map<String, Object> enhancedClaims = new HashMap<>();
        enhancedClaims.put("department", "Engineering");
        enhancedClaims.put("employee_id", "EMP001");
        when(tokenCustomizer.enhanceToken(oidcUser)).thenReturn(enhancedClaims);

        // When
        Map<String, Object> response = authController.getEnhancedToken(oidcUser);

        // Then
        assertThat(response).isNotEmpty();
        // Standard OIDC claims
        assertThat(response).containsEntry("sub", "kc-123");
        assertThat(response).containsEntry("preferred_username", "john.doe");
        assertThat(response).containsEntry("email", "john.doe@example.com");
        assertThat(response).containsEntry("email_verified", true);
        assertThat(response).containsEntry("given_name", "John");
        assertThat(response).containsEntry("family_name", "Doe");
        // Enhanced claims
        assertThat(response).containsEntry("department", "Engineering");
        assertThat(response).containsEntry("employee_id", "EMP001");
        verify(tokenCustomizer, times(1)).enhanceToken(oidcUser);
    }

    @Test
    @DisplayName("Should return UP status for health check")
    void shouldReturnUpStatusForHealthCheck() {
        // When
        Map<String, String> response = authController.health();

        // Then
        assertThat(response).containsEntry("status", "UP");
        verify(tokenCustomizer, never()).enhanceToken(any());
    }

    @Test
    @DisplayName("Should handle null enhanced claims gracefully")
    void shouldHandleNullEnhancedClaimsGracefully() {
        // Given
        when(tokenCustomizer.enhanceToken(oidcUser)).thenReturn(new HashMap<>());

        // When
        Map<String, Object> response = authController.getEnhancedToken(oidcUser);

        // Then
        assertThat(response).isNotEmpty();
        assertThat(response).containsEntry("sub", "kc-123");
        assertThat(response).containsEntry("preferred_username", "john.doe");
        verify(tokenCustomizer, times(1)).enhanceToken(oidcUser);
    }
}
