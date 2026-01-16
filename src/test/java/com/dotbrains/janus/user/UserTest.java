package com.dotbrains.janus.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Entity Tests")
class UserTest {

    @Test
    @DisplayName("Should create user with builder")
    void shouldCreateUserWithBuilder() {
        // When
        User user = User.builder()
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

        // Then
        assertThat(user.getKeycloakId()).isEqualTo("kc-123");
        assertThat(user.getUsername()).isEqualTo("john.doe");
        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getDepartment()).isEqualTo("Engineering");
        assertThat(user.getJobTitle()).isEqualTo("Software Engineer");
        assertThat(user.getPhoneNumber()).isEqualTo("+1-555-0101");
        assertThat(user.getEmployeeId()).isEqualTo("EMP001");
        assertThat(user.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should get full name when first and last name are set")
    void shouldGetFullNameWhenFirstAndLastNameAreSet() {
        // Given
        User user = User.builder()
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .build();

        // When
        String fullName = user.getFullName();

        // Then
        assertThat(fullName).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should return username when first or last name is missing")
    void shouldReturnUsernameWhenFirstOrLastNameIsMissing() {
        // Given
        User user = User.builder()
                .username("john.doe")
                .firstName("John")
                .build();

        // When
        String fullName = user.getFullName();

        // Then
        assertThat(fullName).isEqualTo("john.doe");
    }

    @Test
    @DisplayName("Should return username when first name is missing")
    void shouldReturnUsernameWhenFirstNameIsMissing() {
        // Given
        User user = User.builder()
                .username("john.doe")
                .lastName("Doe")
                .build();

        // When
        String fullName = user.getFullName();

        // Then
        assertThat(fullName).isEqualTo("john.doe");
    }

    @Test
    @DisplayName("Should get role names from user roles")
    void shouldGetRoleNamesFromUserRoles() {
        // Given
        User user = User.builder()
                .username("john.doe")
                .build();

        UserRole role1 = new UserRole();
        role1.setRoleName("USER");
        role1.setUser(user);

        UserRole role2 = new UserRole();
        role2.setRoleName("ADMIN");
        role2.setUser(user);

        Set<UserRole> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);
        user.setRoles(roles);

        // When
        Set<String> roleNames = user.getRoleNames();

        // Then
        assertThat(roleNames).containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    @DisplayName("Should return empty set when no roles")
    void shouldReturnEmptySetWhenNoRoles() {
        // Given
        User user = User.builder()
                .username("john.doe")
                .build();

        // When
        Set<String> roleNames = user.getRoleNames();

        // Then
        assertThat(roleNames).isEmpty();
    }

    @Test
    @DisplayName("Should use equals based on id and keycloakId")
    void shouldUseEqualsBasedOnIdAndKeycloakId() {
        // Given
        User user1 = User.builder()
                .id(1L)
                .keycloakId("kc-123")
                .username("john.doe")
                .build();

        User user2 = User.builder()
                .id(1L)
                .keycloakId("kc-123")
                .username("different.username")
                .build();

        User user3 = User.builder()
                .id(2L)
                .keycloakId("kc-456")
                .username("john.doe")
                .build();

        // Then
        assertThat(user1).isEqualTo(user2);
        assertThat(user1).isNotEqualTo(user3);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("Should default isActive to true")
    void shouldDefaultIsActiveToTrue() {
        // When
        User user = new User();

        // Then
        assertThat(user.getIsActive()).isTrue();
    }
}
