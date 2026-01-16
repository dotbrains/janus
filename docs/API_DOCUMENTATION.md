# API Documentation Architecture

## Overview

Janus uses a clean separation between API documentation and implementation through the **API Interface Pattern**. This approach keeps controllers focused on business logic while maintaining comprehensive, type-safe API documentation.

## Architecture Pattern

### API Interface Layer (`/api/v1` package)

The `/api` package contains versioned subpackages (e.g., `/api/v1`, `/api/v2`) with interfaces that define:
- Endpoint signatures
- Swagger/OpenAPI annotations
- Request/response documentation
- Authentication requirements
- Example responses
- API version in `@RequestMapping`

**Example: `api/v1/AuthAPI.java`**
```java
package com.dotbrains.janus.api.v1;

@Tag(name = "Authentication v1", description = "OAuth2/OIDC authentication endpoints")
@RequestMapping("/api/v1/auth")
public interface AuthAPI {
    
    @Operation(summary = "Get Enhanced Token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success")
    })
    @SecurityRequirement(name = "oauth2")
    @GetMapping("/token")
    Map<String, Object> getEnhancedToken(@AuthenticationPrincipal OidcUser oidcUser);
}
```

### Controller Implementation (`/auth/v1` package)

Controllers are organized in versioned packages and implement the corresponding API interface:

```java
package com.dotbrains.janus.auth.v1;

import com.dotbrains.janus.api.v1.AuthAPI;

@RestController
public class AuthController implements AuthAPI {
    
    @Override
    public Map<String, Object> getEnhancedToken(OidcUser oidcUser) {
        // Clean implementation without annotations
        // Request mapping inherited from AuthAPI interface
        return tokenCustomizer.enhanceToken(oidcUser);
    }
}
```

## Benefits

### 1. **Separation of Concerns**
- **API Definition**: Interface defines the contract
- **Implementation**: Controller focuses on business logic
- **Documentation**: Swagger annotations in one place

### 2. **Type Safety**
- Compile-time verification of API contracts
- IDE autocomplete for interface methods
- Refactoring safety across API and implementation

### 3. **Maintainability**
- Single source of truth for API documentation
- Easy to update documentation without touching implementation
- Clear contract between API and implementation

### 4. **Readability**
Controllers remain clean and focused:

**Before (cluttered):**
```java
@Operation(summary = "Get Enhanced Token", description = "...")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "...", 
        content = @Content(mediaType = "application/json", ...))
})
@SecurityRequirement(name = "oauth2")
@GetMapping("/token")
public Map<String, Object> getEnhancedToken(@AuthenticationPrincipal OidcUser user) {
    // Business logic
}
```

**After (clean):**
```java
@Override
public Map<String, Object> getEnhancedToken(OidcUser user) {
    // Business logic only
}
```

### 5. **Reusability**
- Multiple implementations can share the same API definition
- Useful for versioning (v1, v2) or different implementations
- Test implementations can reuse the same interface

## Swagger/OpenAPI Configuration

### SpringDoc Dependency

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### Configuration Classes

1. **OpenApiConfig.java** - Global API metadata
2. **SecurityConfig.java** - Permits Swagger UI endpoints
3. **application.yml** - SpringDoc settings

### Access Points

- **Swagger UI**: http://localhost:9090/swagger-ui.html
- **OpenAPI JSON**: http://localhost:9090/v3/api-docs
- **OpenAPI YAML**: http://localhost:9090/v3/api-docs.yaml

## Creating New API Interfaces

### Step 1: Create Interface in Versioned `/api/v1` Package

```java
package com.dotbrains.janus.api.v1;

import io.swagger.v3.oas.annotations.*;

@Tag(name = "User Management v1", description = "User CRUD operations")
@RequestMapping("/api/v1/users")
public interface UserAPI {
    
    @Operation(
        summary = "Get User by ID",
        description = "Retrieves user information by their unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    @GetMapping("/{id}")
    User getUserById(@PathVariable Long id);
}
```

### Step 2: Implement in Versioned Controller Package

```java
package com.dotbrains.janus.user.v1;

import com.dotbrains.janus.api.v1.UserAPI;

@RestController
public class UserController implements UserAPI {
    
    @Override
    public User getUserById(Long id) {
        return userService.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
}
```

## Swagger Annotations Reference

### Common Annotations

| Annotation | Purpose | Location |
|------------|---------|----------|
| `@Tag` | Group related endpoints | Interface |
| `@Operation` | Describe endpoint operation | Method |
| `@ApiResponses` | Define response codes | Method |
| `@ApiResponse` | Individual response | Method |
| `@Parameter` | Document parameter | Parameter |
| `@Schema` | Define data model | Class/Field |
| `@SecurityRequirement` | Authentication needed | Method |

### Example: Complete Method Documentation

```java
@Operation(
    summary = "Create User",
    description = "Creates a new user account with the provided information"
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "201",
        description = "User created successfully",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = User.class),
            examples = @ExampleObject(
                name = "Created User",
                value = """
                    {
                      "id": 1,
                      "username": "john.doe",
                      "email": "john@example.com"
                    }
                    """
            )
        )
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid input"
    ),
    @ApiResponse(
        responseCode = "409",
        description = "User already exists"
    )
})
@SecurityRequirement(name = "oauth2")
@PostMapping
User createUser(
    @Parameter(
        description = "User data to create",
        required = true
    )
    @RequestBody @Valid CreateUserRequest request
);
```

## Best Practices

### 1. **Keep Interfaces Focused**
- One interface per controller/domain
- Group related endpoints together
- Use clear, descriptive names

### 2. **Comprehensive Documentation**
- Include all response codes
- Provide realistic examples
- Document edge cases
- Specify authentication requirements

### 3. **Use Examples**
- JSON examples for complex responses
- Show success and error cases
- Match actual API behavior

### 4. **Security Documentation**
- Mark authenticated endpoints with `@SecurityRequirement`
- Document permission requirements
- Show authentication flow

### 5. **Maintain Consistency**
- Use consistent naming conventions
- Follow HTTP standards (200, 201, 400, 404, etc.)
- Standardize error response formats

## Testing API Documentation

### 1. **Visual Inspection**
Open Swagger UI and verify:
- All endpoints are documented
- Examples are accurate
- Descriptions are clear
- Security schemes work

### 2. **Try It Out**
Use Swagger UI's "Try it out" feature:
- Test each endpoint
- Verify responses match documentation
- Check authentication flows

### 3. **Export OpenAPI Spec**
```bash
curl http://localhost:9090/v3/api-docs -o openapi.json
```

Use tools to validate:
- [Swagger Editor](https://editor.swagger.io/)
- [OpenAPI Validator](https://apitools.dev/swagger-parser/)

## Advanced Features

### Custom Response Types

```java
@Schema(
    description = "Enhanced user response with custom claims",
    example = """
        {
          "user": {...},
          "claims": {...}
        }
        """
)
public record EnhancedUserResponse(
    @Schema(description = "User basic information")
    User user,
    
    @Schema(description = "Custom JWT claims")
    Map<String, Object> claims
) {}
```

### Pagination Documentation

```java
@Operation(summary = "List Users")
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Paginated user list",
        content = @Content(
            schema = @Schema(implementation = PagedResponse.class)
        )
    )
})
@GetMapping
Page<User> listUsers(
    @Parameter(description = "Page number (0-based)")
    @RequestParam(defaultValue = "0") int page,
    
    @Parameter(description = "Page size")
    @RequestParam(defaultValue = "20") int size
);
```

### File Upload Documentation

```java
@Operation(
    summary = "Upload Avatar",
    description = "Uploads a user avatar image"
)
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Upload successful")
})
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
void uploadAvatar(
    @Parameter(description = "Avatar image file")
    @RequestParam("file") MultipartFile file
);
```

## Troubleshooting

### Swagger UI Not Loading

1. Check security configuration allows `/swagger-ui/**`
2. Verify SpringDoc dependency is present
3. Check application logs for errors

### Endpoints Not Appearing

1. Ensure controller implements API interface
2. Verify `@RestController` and `@RequestMapping` annotations
3. Check package scanning in main application class

### Authentication Not Working

1. Verify `@SecurityRequirement(name = "oauth2")` is present
2. Check `OpenApiConfig` has `@SecurityScheme` defined
3. Ensure security scheme name matches

## Resources

- [SpringDoc Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://spec.openapis.org/oas/v3.1.0)
- [Swagger Annotations Guide](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations)

---

**Pattern Credit**: This approach combines Spring's interface-based controllers with Swagger's annotation-driven documentation for optimal code organization.
