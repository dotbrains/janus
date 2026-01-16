package com.dotbrains.janus.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Find user by Keycloak ID
     * @param keycloakId the Keycloak user ID
     * @return Optional containing the user if found
     */
    @Transactional(readOnly = true)
    public Optional<User> findByKeycloakId(String keycloakId) {
        log.debug("Finding user by Keycloak ID: {}", keycloakId);
        return userRepository.findByKeycloakId(keycloakId);
    }

    /**
     * Find active user with roles by Keycloak ID
     * @param keycloakId the Keycloak user ID
     * @return Optional containing the user with roles if found and active
     */
    @Transactional(readOnly = true)
    public Optional<User> findActiveUserWithRoles(String keycloakId) {
        log.debug("Finding active user with roles by Keycloak ID: {}", keycloakId);
        return userRepository.findActiveUserWithRolesByKeycloakId(keycloakId);
    }

    /**
     * Find a user by username with roles
     * @param username the username
     * @return Optional containing the user with roles if found
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsernameWithRoles(String username) {
        log.debug("Finding user with roles by username: {}", username);
        return userRepository.findByUsernameWithRoles(username);
    }

    /**
     * Create or update user
     * @param user the user to save
     * @return the saved user
     */
    @Transactional
    public User save(User user) {
        log.debug("Saving user: {}", user.getUsername());
        return userRepository.save(user);
    }

    /**
     * Check if user exists by Keycloak ID
     * @param keycloakId the Keycloak user ID
     * @return true if user exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsByKeycloakId(String keycloakId) {
        return userRepository.existsByKeycloakId(keycloakId);
    }

    /**
     * Synchronize user from Keycloak
     * Creates a new user or updates existing user based on Keycloak data
     * @param keycloakId the Keycloak user ID
     * @param username the username
     * @param email the email
     * @param firstName the first name
     * @param lastName the last name
     * @return the synchronized user
     */
    @Transactional
    public User syncUserFromKeycloak(String keycloakId, String username, String email,
                                      String firstName, String lastName) {
        log.debug("Synchronizing user from Keycloak: {}", username);

        Optional<User> existingUser = userRepository.findByKeycloakId(keycloakId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setIsActive(true);
            return userRepository.save(user);
        }

        User newUser = User.builder()
                    .keycloakId(keycloakId)
                    .username(username)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .isActive(true)
                    .build();
        return userRepository.save(newUser);
    }

    /**
     * Deactivate user
     * @param keycloakId the Keycloak user ID
     */
    @Transactional
    public void deactivateUser(String keycloakId) {
        log.debug("Deactivating user with Keycloak ID: {}", keycloakId);
        userRepository.findByKeycloakId(keycloakId).ifPresent(user -> {
            user.setIsActive(false);
            userRepository.save(user);
        });
    }
}
