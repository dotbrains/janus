package com.dotbrains.janus.token;

import com.dotbrains.janus.user.User;
import com.dotbrains.janus.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CustomClaimsMapper Unit Tests")
class CustomClaimsMapperTest {

    private CustomClaimsMapper claimsMapper;
    private User testUser;

    @BeforeEach
    void setUp() {
        claimsMapper = new CustomClaimsMapper();

        testUser = User.builder()
                .id(1L)
                .keycloakId("kc-123")
                .username("john.doe")
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .department("Engineering")
                .jobTitle("Software Engineer")
                .phoneNumber("+1-555-0101")
                .employeeId("EMP001")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should map all user attributes to claims")
    void shouldMapAllUserAttributesToClaims() {
        // When
        Map<String, Object> claims = claimsMapper.mapUserToClaims(testUser);

        // Then
        assertThat(claims).isNotEmpty();
        assertThat(claims).containsEntry("user_id", 1L);
        assertThat(claims).containsEntry("keycloak_id", "kc-123");
        assertThat(claims).containsEntry("username", "john.doe");
        assertThat(claims).containsEntry("email", "john.doe@example.com");
        assertThat(claims).containsEntry("full_name", "John Doe");
        assertThat(claims).containsEntry("employee_id", "EMP001");
        assertThat(claims).containsEntry("department", "Engineering");
        assertThat(claims).containsEntry("job_title", "Software Engineer");
        assertThat(claims).containsEntry("phone_number", "+1-555-0101");
        assertThat(claims).containsEntry("is_active", true);
    }

    @Test
    @DisplayName("Should include roles when user has roles")
    void shouldIncludeRolesWhenUserHasRoles() {
        // Given
        UserRole adminRole = new UserRole();
        adminRole.setRoleName("ADMIN");
        adminRole.setUser(testUser);

        UserRole userRole = new UserRole();
        userRole.setRoleName("USER");
        userRole.setUser(testUser);

        Set<UserRole> roles = new HashSet<>();
        roles.add(adminRole);
        roles.add(userRole);
        testUser.setRoles(roles);

        // When
        Map<String, Object> claims = claimsMapper.mapUserToClaims(testUser);

        // Then
        assertThat(claims).containsKey("roles");
        assertThat(claims).containsEntry("is_admin", true);
        @SuppressWarnings("unchecked")
        Set<String> roleClaims = (Set<String>) claims.get("roles");
        assertThat(roleClaims).containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    @DisplayName("Should set is_admin to false when user is not admin")
    void shouldSetIsAdminToFalseWhenUserIsNotAdmin() {
        // Given
        UserRole userRole = new UserRole();
        userRole.setRoleName("USER");
        userRole.setUser(testUser);

        Set<UserRole> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);

        // When
        Map<String, Object> claims = claimsMapper.mapUserToClaims(testUser);

        // Then
        assertThat(claims).containsEntry("is_admin", false);
    }

    @Test
    @DisplayName("Should not include roles when user has no roles")
    void shouldNotIncludeRolesWhenUserHasNoRoles() {
        // When
        Map<String, Object> claims = claimsMapper.mapUserToClaims(testUser);

        // Then
        assertThat(claims).doesNotContainKey("roles");
        assertThat(claims).doesNotContainKey("is_admin");
    }

    @Test
    @DisplayName("Should omit null fields from claims")
    void shouldOmitNullFieldsFromClaims() {
        // Given
        User minimalUser = User.builder()
                .id(1L)
                .keycloakId("kc-123")
                .username("john.doe")
                .email("john.doe@example.com")
                .isActive(true)
                .build();

        // When
        Map<String, Object> claims = claimsMapper.mapUserToClaims(minimalUser);

        // Then
        assertThat(claims).containsKey("user_id");
        assertThat(claims).containsKey("keycloak_id");
        assertThat(claims).containsKey("username");
        assertThat(claims).containsKey("email");
        assertThat(claims).doesNotContainKey("employee_id");
        assertThat(claims).doesNotContainKey("department");
        assertThat(claims).doesNotContainKey("job_title");
        assertThat(claims).doesNotContainKey("phone_number");
    }

    @Test
    @DisplayName("Should generate full name with SpEL even when one is null")
    void shouldGenerateFullNameWithSpelEvenWhenOneIsNull() {
        // Given - SpEL will concatenate even with null, resulting in "John null"
        User userWithOnlyFirstName = User.builder()
                .username("john.doe")
                .firstName("John")
                .isActive(true)
                .build();

        // When
        Map<String, Object> claims = claimsMapper.mapUserToClaims(userWithOnlyFirstName);

        // Then
        assertThat(claims).containsKey("full_name");
        // SpEL concatenates firstName + ' ' + null, resulting in "John null"
        assertThat(claims.get("full_name")).isEqualTo("John null");
    }

    @Test
    @DisplayName("Should include timestamps when present")
    void shouldIncludeTimestampsWhenPresent() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        testUser.setCreatedAt(now);
        testUser.setUpdatedAt(now);

        // When
        Map<String, Object> claims = claimsMapper.mapUserToClaims(testUser);

        // Then
        assertThat(claims).containsEntry("created_at", now);
        assertThat(claims).containsEntry("updated_at", now);
    }

    @Test
    @DisplayName("Should correctly evaluate is_active SpEL expression")
    void shouldCorrectlyEvaluateIsActiveSpelExpression() {
        // Given
        testUser.setIsActive(false);

        // When
        Map<String, Object> claims = claimsMapper.mapUserToClaims(testUser);

        // Then
        assertThat(claims).containsEntry("is_active", false);
    }
}
