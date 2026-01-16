# API Versioning Guide

## Overview

Janus uses **package-based API versioning** combined with URL path versioning to provide clear, maintainable API evolution. This approach allows multiple API versions to coexist while keeping code organized and isolated.

## Package Structure

```
src/main/java/com/dotbrains/janus/
├── api/
│   ├── v1/
│   │   └── AuthAPI.java          # v1 API interface
│   └── v2/                        # Future v2 interfaces
│       └── AuthAPI.java
├── auth/
│   ├── v1/
│   │   └── AuthController.java   # v1 implementation
│   └── v2/                        # Future v2 implementation
│       └── AuthController.java
└── [shared packages]/
    ├── token/                     # Shared across versions
    ├── user/                      # Shared across versions
    ├── config/                    # Shared configuration
    └── exception/                 # Shared error handling
```

## Key Principles

### 1. **Package-Based Organization**
Each API version has its own package namespace:
- `com.dotbrains.janus.api.v1` - Version 1 API interfaces
- `com.dotbrains.janus.auth.v1` - Version 1 controllers
- `com.dotbrains.janus.api.v2` - Version 2 API interfaces (when created)
- `com.dotbrains.janus.auth.v2` - Version 2 controllers (when created)

### 2. **Interface-Driven Design**
API interfaces define the contract with `@RequestMapping`:

```java
package com.dotbrains.janus.api.v1;

@Tag(name = "Authentication v1")
@RequestMapping("/api/v1/auth")
public interface AuthAPI {
    
    @GetMapping("/user")
    Map<String, Object> getCurrentUser(@AuthenticationPrincipal OidcUser oidcUser);
}
```

### 3. **Clean Controllers**
Controllers implement interfaces without duplicating annotations:

```java
package com.dotbrains.janus.auth.v1;

import com.dotbrains.janus.api.v1.AuthAPI;

@RestController
public class AuthController implements AuthAPI {
    
    @Override
    public Map<String, Object> getCurrentUser(OidcUser oidcUser) {
        // Implementation only - no annotations needed
        return userService.getUserInfo(oidcUser);
    }
}
```

### 4. **Shared Services**
Domain logic, utilities, and infrastructure are shared across versions:
- `TokenCustomizer` - Token enhancement logic
- `UserService` - User data access
- `SecurityConfig` - Security configuration
- `DatabaseConfig` - Database configuration

## Current Version: v1

### Endpoints

All v1 endpoints are prefixed with `/api/v1`:

```
GET  /api/v1/auth/success   - OAuth2 login success callback
GET  /api/v1/auth/failure   - OAuth2 login failure callback
GET  /api/v1/auth/user      - Get current authenticated user
GET  /api/v1/auth/token     - Get enhanced JWT token
GET  /api/v1/auth/health    - Health check
```

### Files

- **Interface**: `src/main/java/com/dotbrains/janus/api/v1/AuthAPI.java`
- **Controller**: `src/main/java/com/dotbrains/janus/auth/v1/AuthController.java`

## Adding a New Version (v2)

### Step 1: Create Packages

```bash
mkdir -p src/main/java/com/dotbrains/janus/api/v2
mkdir -p src/main/java/com/dotbrains/janus/auth/v2
```

### Step 2: Create v2 API Interface

```java
package com.dotbrains.janus.api.v2;

@Tag(name = "Authentication v2", description = "Enhanced authentication with new features")
@RequestMapping("/api/v2/auth")
public interface AuthAPI {
    
    @Operation(summary = "Get Current User (v2)")
    @GetMapping("/user")
    UserResponseV2 getCurrentUser(@AuthenticationPrincipal OidcUser oidcUser);
    
    // New v2-specific endpoints
    @Operation(summary = "Refresh Token")
    @PostMapping("/refresh")
    TokenResponse refreshToken(@RequestBody RefreshRequest request);
}
```

### Step 3: Create v2 Controller

```java
package com.dotbrains.janus.auth.v2;

import com.dotbrains.janus.api.v2.AuthAPI;

@RestController
public class AuthController implements AuthAPI {
    
    @Override
    public UserResponseV2 getCurrentUser(OidcUser oidcUser) {
        // v2-specific implementation with enhanced response format
        return userServiceV2.getEnhancedUserInfo(oidcUser);
    }
    
    @Override
    public TokenResponse refreshToken(RefreshRequest request) {
        // New feature only in v2
        return tokenService.refresh(request);
    }
}
```

### Step 4: Update Security Configuration

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
        // v1 endpoints
        .requestMatchers("/api/v1/auth/health").permitAll()
        // v2 endpoints
        .requestMatchers("/api/v2/auth/health").permitAll()
        .anyRequest().authenticated()
    );
    return http.build();
}
```

### Step 5: Test Both Versions

```bash
# v1 endpoints continue working
curl http://localhost:9090/api/v1/auth/health

# v2 endpoints are now available
curl http://localhost:9090/api/v2/auth/health
curl -X POST http://localhost:9090/api/v2/auth/refresh
```

## Benefits

### 1. **Clear Separation**
- Each version is isolated in its own package
- Easy to locate and modify version-specific code
- No risk of accidentally changing the wrong version

### 2. **Simultaneous Support**
- v1 and v2 run side-by-side
- Clients can migrate at their own pace
- No forced upgrades

### 3. **Independent Evolution**
- v2 can have breaking changes without affecting v1
- New features in v2 don't require backporting to v1
- Each version has its own Swagger documentation

### 4. **Code Reuse**
- Shared services reduce duplication
- Common infrastructure (DB, security, etc.) is centralized
- Business logic can be versioned independently

### 5. **Maintainable**
- Clear which code belongs to which version
- Easy to deprecate old versions (delete v1 package)
- Simple mental model for developers

## Best Practices

### 1. **Version in Interface**
Always specify the version in the interface's `@RequestMapping`:

```java
@RequestMapping("/api/v1/auth")  // ✅ Good
@RequestMapping("/api/v2/auth")  // ✅ Good
@RequestMapping("/auth")         // ❌ Bad - no version
```

### 2. **Tag Versions in Swagger**
Include version in Swagger tags for clarity:

```java
@Tag(name = "Authentication v1")  // ✅ Clear which version
@Tag(name = "Authentication")     // ❌ Ambiguous
```

### 3. **Share Common Logic**
Don't duplicate business logic across versions:

```java
// ✅ Good - shared service
public class UserService {
    public User getUser(String id) { /* ... */ }
}

// ❌ Bad - duplicated in each version
public class UserServiceV1 { /* ... */ }
public class UserServiceV2 { /* ... */ }
```

### 4. **Version Response Models When Needed**
If response structure changes significantly:

```java
// v1 uses Map<String, Object>
Map<String, Object> getCurrentUser();

// v2 uses structured DTOs
UserResponseV2 getCurrentUser();
```

### 5. **Document Breaking Changes**
Clearly document what changed between versions:

```java
/**
 * Authentication API Interface - Version 2
 * 
 * Breaking changes from v1:
 * - User response now uses UserResponseV2 DTO instead of Map
 * - Added refresh token endpoint
 * - Removed legacy /failure endpoint
 * 
 * @version 2.0
 * @since 2.0
 */
```

## Migration Guide

### Deprecating Old Versions

When ready to remove v1:

1. Mark v1 as deprecated:
   ```java
   @Deprecated(since = "2.0", forRemoval = true)
   @RequestMapping("/api/v1/auth")
   public interface AuthAPI { }
   ```

2. Add deprecation notice in Swagger:
   ```java
   @Tag(
       name = "Authentication v1 (DEPRECATED)",
       description = "⚠️ This version is deprecated. Use v2 instead."
   )
   ```

3. Monitor v1 usage in logs

4. After migration period, delete v1 packages:
   ```bash
   rm -rf src/main/java/com/dotbrains/janus/api/v1
   rm -rf src/main/java/com/dotbrains/janus/auth/v1
   ```

5. Remove v1 security rules from `SecurityConfig.java`

## Swagger Documentation

Each version appears separately in Swagger UI:

- **v1 Endpoints**: Tagged as "Authentication v1"
- **v2 Endpoints**: Tagged as "Authentication v2"

Access Swagger UI at: http://localhost:9090/swagger-ui.html

## Examples

### Client Using v1

```bash
# OAuth2 login flow
curl http://localhost:9090/oauth2/authorization/keycloak

# Get user info (v1 format)
curl http://localhost:9090/api/v1/auth/user \
  -H "Cookie: JSESSIONID=..."
```

### Client Using v2

```bash
# OAuth2 login flow (same)
curl http://localhost:9090/oauth2/authorization/keycloak

# Get user info (v2 format - enhanced response)
curl http://localhost:9090/api/v2/auth/user \
  -H "Cookie: JSESSIONID=..."

# Use new v2 feature
curl -X POST http://localhost:9090/api/v2/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refresh_token": "..."}'
```

## Summary

Janus uses package-based versioning to provide:
- ✅ Clear code organization
- ✅ Multiple simultaneous versions
- ✅ Independent evolution
- ✅ Maintainable codebase
- ✅ Explicit API contracts

This approach ensures backward compatibility while enabling continuous API improvement.
