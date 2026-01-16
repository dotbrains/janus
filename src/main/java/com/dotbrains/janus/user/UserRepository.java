package com.dotbrains.janus.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by Keycloak ID
     * @param keycloakId the Keycloak user ID
     * @return Optional containing the user if found
     */
    Optional<User> findByKeycloakId(String keycloakId);

    /**
     * Check if a user exists by Keycloak ID
     * @param keycloakId the Keycloak user ID
     * @return true if a user exists, false otherwise
     */
    boolean existsByKeycloakId(String keycloakId);

    /**
     * Find an active user by Keycloak ID with roles
     * Uses JPQL with JOIN FETCH to avoid N+1 queries
     * @param keycloakId the Keycloak user ID
     * @return Optional containing the user with roles if found and active
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.keycloakId = :keycloakId AND u.isActive = true")
    Optional<User> findActiveUserWithRolesByKeycloakId(@Param("keycloakId") String keycloakId);

    /**
     * Find user by username with roles
     * Uses JPQL with JOIN FETCH to avoid N+1 queries
     * @param username the username
     * @return Optional containing the user with roles if found
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);
}
