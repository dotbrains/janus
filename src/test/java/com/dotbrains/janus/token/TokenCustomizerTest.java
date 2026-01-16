package com.dotbrains.janus.token;

import com.dotbrains.janus.user.User;
import com.dotbrains.janus.user.UserService;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenCustomizer Unit Tests")
class TokenCustomizerTest {

    @Mock
    private UserService userService;

    @Mock
    private CustomClaimsMapper claimsMapper;

    @InjectMocks
    private TokenCustomizer tokenCustomizer;

    private OidcUser oidcUser;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Set configuration values
        ReflectionTestUtils.setField(tokenCustomizer, "enhancementEnabled", true);
        ReflectionTestUtils.setField(tokenCustomizer, "includeUserRoles", true);
        ReflectionTestUtils.setField(tokenCustomizer, "includeUserAttributes", true);

        // Create test OIDC user
        OidcIdToken idToken = OidcIdToken.withTokenValue("test-token")
                .claim("sub", "kc-123")
                .claim("preferred_username", "john.doe")
                .claim("email", "john.doe@example.com")
                .claim("given_name", "John")
                .claim("family_name", "Doe")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        oidcUser = new DefaultOidcUser(
                Set.of(() -> "USER"),
                idToken
        );

        // Create test user
        testUser = User.builder()
                .id(1L)
                .keycloakId("kc-123")
                .username("john.doe")
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .department("Engineering")
                .jobTitle("Software Engineer")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should enhance token when user exists in database")
    void shouldEnhanceTokenWhenUserExists() {
        // Given
        Map<String, Object> customClaims = Map.of(
                "department", "Engineering",
                "job_title", "Software Engineer"
        );
        when(userService.findActiveUserWithRoles("kc-123")).thenReturn(Optional.of(testUser));
        when(claimsMapper.mapUserToClaims(testUser)).thenReturn(customClaims);

        // When
        Map<String, Object> result = tokenCustomizer.enhanceToken(oidcUser);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).containsEntry("department", "Engineering");
        assertThat(result).containsEntry("job_title", "Software Engineer");
        verify(userService, times(1)).findActiveUserWithRoles("kc-123");
        verify(claimsMapper, times(1)).mapUserToClaims(testUser);
    }

    @Test
    @DisplayName("Should sync user from Keycloak when not found in database")
    void shouldSyncUserFromKeycloakWhenNotFound() {
        // Given
        Map<String, Object> customClaims = Map.of("department", "Engineering");
        when(userService.findActiveUserWithRoles("kc-123")).thenReturn(Optional.empty());
        when(userService.syncUserFromKeycloak(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testUser);
        when(claimsMapper.mapUserToClaims(testUser)).thenReturn(customClaims);

        // When
        Map<String, Object> result = tokenCustomizer.enhanceToken(oidcUser);

        // Then
        assertThat(result).isNotEmpty();
        verify(userService, times(1)).findActiveUserWithRoles("kc-123");
        verify(userService, times(1)).syncUserFromKeycloak(
                "kc-123",
                "john.doe",
                "john.doe@example.com",
                "John",
                "Doe"
        );
        verify(claimsMapper, times(1)).mapUserToClaims(testUser);
    }

    @Test
    @DisplayName("Should return empty map when enhancement is disabled")
    void shouldReturnEmptyMapWhenEnhancementIsDisabled() {
        // Given
        ReflectionTestUtils.setField(tokenCustomizer, "enhancementEnabled", false);

        // When
        Map<String, Object> result = tokenCustomizer.enhanceToken(oidcUser);

        // Then
        assertThat(result).isEmpty();
        verify(userService, never()).findActiveUserWithRoles(anyString());
        verify(claimsMapper, never()).mapUserToClaims(any());
    }

    @Test
    @DisplayName("Should include only roles when attributes disabled")
    void shouldIncludeOnlyRolesWhenAttributesDisabled() {
        // Given
        ReflectionTestUtils.setField(tokenCustomizer, "includeUserAttributes", false);
        ReflectionTestUtils.setField(tokenCustomizer, "includeUserRoles", true);
        when(userService.findActiveUserWithRoles("kc-123")).thenReturn(Optional.of(testUser));

        // When
        Map<String, Object> result = tokenCustomizer.enhanceToken(oidcUser);

        // Then
        assertThat(result).containsKey("roles");
        verify(userService, times(1)).findActiveUserWithRoles("kc-123");
        verify(claimsMapper, never()).mapUserToClaims(any());
    }

    @Test
    @DisplayName("Should return empty map when sync fails")
    void shouldReturnEmptyMapWhenSyncFails() {
        // Given
        when(userService.findActiveUserWithRoles("kc-123")).thenReturn(Optional.empty());
        when(userService.syncUserFromKeycloak(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Sync failed"));

        // When
        Map<String, Object> result = tokenCustomizer.enhanceToken(oidcUser);

        // Then
        assertThat(result).isEmpty();
        verify(userService, times(1)).findActiveUserWithRoles("kc-123");
        verify(userService, times(1)).syncUserFromKeycloak(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(claimsMapper, never()).mapUserToClaims(any());
    }

    @Test
    @DisplayName("Should return empty map when sync returns null")
    void shouldReturnEmptyMapWhenSyncReturnsNull() {
        // Given
        when(userService.findActiveUserWithRoles("kc-123")).thenReturn(Optional.empty());
        when(userService.syncUserFromKeycloak(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);

        // When
        Map<String, Object> result = tokenCustomizer.enhanceToken(oidcUser);

        // Then
        assertThat(result).isEmpty();
        verify(userService, times(1)).syncUserFromKeycloak(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(claimsMapper, never()).mapUserToClaims(any());
    }
}
