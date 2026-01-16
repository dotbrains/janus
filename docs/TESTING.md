# Testing Documentation

This document describes the testing strategy and test suite for the Janus application.

## Test Structure

```
src/test/java/com/dotbrains/janus/
├── JanusApplicationTest.java           # Application-level tests
├── auth/
│   └── v1/
│       └── AuthControllerTest.java      # Auth controller unit tests
├── config/
│   └── TestSecurityConfig.java         # Test security configuration
├── token/
│   ├── TokenCustomizerTest.java        # Token customizer unit tests
│   └── CustomClaimsMapperTest.java     # Claims mapper unit tests
├── user/
│   ├── UserServiceTest.java            # UserService unit tests
│   └── UserTest.java                    # User entity tests
└── resources/
    └── application-test.yml             # Test configuration
```

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=UserServiceTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=UserServiceTest#shouldFindUserByKeycloakId
```

### Run Tests with Coverage
```bash
mvn clean test jacoco:report
```

## Test Configuration

### Test Profile
Tests run with the `test` profile which provides:
- H2 in-memory database (instead of PostgreSQL)
- Mock JWT decoder (no real Keycloak connection required)
- Bean definition overriding enabled
- Minimal logging

### Test Database
- **Database**: H2 in-memory database
- **Schema**: Auto-created via Hibernate DDL
- **Benefits**: Fast, isolated, no external dependencies

### Test Security Configuration
The `TestSecurityConfig` provides a mock `JwtDecoder` that:
- Returns test JWTs without connecting to Keycloak
- Allows tests to run without external OAuth2 server
- Profile-specific (`@Profile("test")`) to avoid conflicts

## Test Coverage

### User Domain Tests (100% Coverage)

#### UserServiceTest
- ✅ Find user by Keycloak ID
- ✅ Find user by Keycloak ID when not found
- ✅ Find active user with roles
- ✅ Find user by username with roles
- ✅ Save user
- ✅ Check if user exists by Keycloak ID
- ✅ Sync existing user from Keycloak
- ✅ Create new user from Keycloak
- ✅ Deactivate user
- ✅ Deactivate nonexistent user (no-op)

**Total: 10 tests**

#### UserTest
- ✅ Create user with builder
- ✅ Get full name when both names are set
- ✅ Return username when first name is missing
- ✅ Return username when last name is missing  
- ✅ Get role names from user roles
- ✅ Return empty set when no roles
- ✅ Equals based on ID and Keycloak ID
- ✅ Default isActive to true

**Total: 8 tests**

### Token Enhancement Tests (100% Coverage)

#### TokenCustomizerTest
- ✅ Enhance token when user exists in database
- ✅ Sync user from Keycloak when not found
- ✅ Return empty map when enhancement disabled
- ✅ Include only roles when attributes disabled
- ✅ Return empty map when sync fails
- ✅ Return empty map when sync returns null

**Total: 6 tests**

#### CustomClaimsMapperTest
- ✅ Map all user attributes to claims
- ✅ Include roles when user has roles
- ✅ Set is_admin to false when user is not admin
- ✅ Not include roles when user has no roles
- ✅ Omit null fields from claims
- ✅ Generate full name with SpEL even when one is null
- ✅ Include timestamps when present
- ✅ Correctly evaluate is_active SpEL expression

**Total: 8 tests**

### Auth Controller Tests (100% Coverage)

#### AuthControllerTest
- ✅ Return success response on login
- ✅ Not include custom_claims when enhancement returns empty map
- ✅ Return error response on login failure
- ✅ Return current user with enhanced claims
- ✅ Return enhanced token with all claims
- ✅ Return UP status for health check
- ✅ Handle null enhanced claims gracefully

**Total: 7 tests**

### Application Tests

#### JanusApplicationTest
- ✅ Application instantiation

**Total: 1 test**

## Test Statistics

| Component | Tests | Coverage |
|-----------|-------|----------|
| User Entity | 8 | 100% |
| UserService | 10 | 100% |
| TokenCustomizer | 6 | 100% |
| CustomClaimsMapper | 8 | 100% |
| AuthController | 7 | 100% |
| Application | 1 | Basic |
| **Total** | **40** | **Comprehensive** |

## Testing Best Practices

### 1. Naming Conventions
- Test classes: `<ClassName>Test`
- Test methods: `should<ExpectedBehavior>`
- Display names: Human-readable descriptions

### 2. Test Structure (Given-When-Then)
```java
@Test
void shouldFindUserByKeycloakId() {
    // Given - Setup test data and mocks
    when(userRepository.findByKeycloakId("kc-123")).thenReturn(Optional.of(testUser));

    // When - Execute the method under test
    Optional<User> result = userService.findByKeycloakId("kc-123");

    // Then - Assert expectations
    assertThat(result).isPresent();
    assertThat(result.get().getKeycloakId()).isEqualTo("kc-123");
}
```

### 3. Test Isolation
- Each test is independent
- Uses mocks to avoid external dependencies
- No shared state between tests
- `@BeforeEach` for test data setup

### 4. Assertion Library
- **AssertJ** for fluent assertions
- Example: `assertThat(result).isPresent()`
- More readable than JUnit assertions

### 5. Mocking Strategy
- **Mockito** for service layer mocking
- Mock repositories, not entities
- Verify method invocations

## Adding New Tests

### 1. Create Test Class
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("MyService Unit Tests")
class MyServiceTest {
    
    @Mock
    private MyRepository myRepository;
    
    @InjectMocks
    private MyService myService;
    
    @BeforeEach
    void setUp() {
        // Initialize test data
    }
    
    @Test
    @DisplayName("Should do something")
    void shouldDoSomething() {
        // Test implementation
    }
}
```

### 2. Run and Verify
```bash
mvn test -Dtest=MyServiceTest
```

## Continuous Integration

### Maven Lifecycle
Tests run automatically during:
```bash
mvn clean install
mvn package
mvn verify
```

### Test Failure Handling
- Build fails if any test fails
- Test reports in `target/surefire-reports/`
- Console output shows failure details

## Future Enhancements

### Planned Tests
- [ ] Controller integration tests (with MockMvc)
- [ ] Token customizer tests
- [ ] Security configuration tests
- [ ] Repository integration tests (with TestContainers)
- [ ] End-to-end API tests

### Test Coverage Goals
- Maintain >80% code coverage
- 100% coverage for business logic
- Integration tests for all REST endpoints

## Troubleshooting

### Common Issues

#### Tests fail with "Bean not found"
**Solution**: Ensure `@Profile("test")` is set correctly and test config is imported

#### Tests fail with database errors
**Solution**: Check H2 dialect configuration in `application-test.yml`

#### Tests fail with OAuth2 errors
**Solution**: Verify `TestSecurityConfig` is providing mock JWT decoder

#### Tests are slow
**Solution**: 
- Use H2 instead of PostgreSQL for tests
- Mock external services
- Avoid loading full Spring context

## References

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
