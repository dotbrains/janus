package com.dotbrains.janus.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .keycloakId("kc-123")
                .username("john.doe")
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should find user by Keycloak ID")
    void shouldFindUserByKeycloakId() {
        // Given
        when(userRepository.findByKeycloakId("kc-123")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByKeycloakId("kc-123");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getKeycloakId()).isEqualTo("kc-123");
        assertThat(result.get().getUsername()).isEqualTo("john.doe");
        verify(userRepository, times(1)).findByKeycloakId("kc-123");
    }

    @Test
    @DisplayName("Should return empty when user not found by Keycloak ID")
    void shouldReturnEmptyWhenUserNotFoundByKeycloakId() {
        // Given
        when(userRepository.findByKeycloakId("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByKeycloakId("nonexistent");

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findByKeycloakId("nonexistent");
    }

    @Test
    @DisplayName("Should find active user with roles")
    void shouldFindActiveUserWithRoles() {
        // Given
        when(userRepository.findActiveUserWithRolesByKeycloakId("kc-123"))
                .thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findActiveUserWithRoles("kc-123");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getIsActive()).isTrue();
        verify(userRepository, times(1)).findActiveUserWithRolesByKeycloakId("kc-123");
    }

    @Test
    @DisplayName("Should find user by username with roles")
    void shouldFindUserByUsernameWithRoles() {
        // Given
        when(userRepository.findByUsernameWithRoles("john.doe")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByUsernameWithRoles("john.doe");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("john.doe");
        verify(userRepository, times(1)).findByUsernameWithRoles("john.doe");
    }

    @Test
    @DisplayName("Should save user")
    void shouldSaveUser() {
        // Given
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.save(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john.doe");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Should check if user exists by Keycloak ID")
    void shouldCheckIfUserExistsByKeycloakId() {
        // Given
        when(userRepository.existsByKeycloakId("kc-123")).thenReturn(true);

        // When
        boolean result = userService.existsByKeycloakId("kc-123");

        // Then
        assertThat(result).isTrue();
        verify(userRepository, times(1)).existsByKeycloakId("kc-123");
    }

    @Test
    @DisplayName("Should sync existing user from Keycloak")
    void shouldSyncExistingUserFromKeycloak() {
        // Given
        when(userRepository.findByKeycloakId("kc-123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.syncUserFromKeycloak(
                "kc-123",
                "john.doe.updated",
                "john.updated@example.com",
                "John",
                "Updated"
        );

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).findByKeycloakId("kc-123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should create new user from Keycloak when not exists")
    void shouldCreateNewUserFromKeycloakWhenNotExists() {
        // Given
        when(userRepository.findByKeycloakId("kc-new")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        // When
        User result = userService.syncUserFromKeycloak(
                "kc-new",
                "jane.doe",
                "jane.doe@example.com",
                "Jane",
                "Doe"
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getKeycloakId()).isEqualTo("kc-new");
        assertThat(result.getUsername()).isEqualTo("jane.doe");
        assertThat(result.getEmail()).isEqualTo("jane.doe@example.com");
        verify(userRepository, times(1)).findByKeycloakId("kc-new");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should deactivate user")
    void shouldDeactivateUser() {
        // Given
        when(userRepository.findByKeycloakId("kc-123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.deactivateUser("kc-123");

        // Then
        verify(userRepository, times(1)).findByKeycloakId("kc-123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should not fail when deactivating nonexistent user")
    void shouldNotFailWhenDeactivatingNonexistentUser() {
        // Given
        when(userRepository.findByKeycloakId("nonexistent")).thenReturn(Optional.empty());

        // When
        userService.deactivateUser("nonexistent");

        // Then
        verify(userRepository, times(1)).findByKeycloakId("nonexistent");
        verify(userRepository, never()).save(any(User.class));
    }
}
