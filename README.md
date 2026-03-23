# Janus - Federated Authentication Service 🔐

[![CI](https://github.com/dotbrains/janus/actions/workflows/ci.yml/badge.svg)](https://github.com/dotbrains/janus/actions/workflows/ci.yml)
[![Docker Build](https://github.com/dotbrains/janus/actions/workflows/docker.yml/badge.svg)](https://github.com/dotbrains/janus/actions/workflows/docker.yml)
[![Template](https://img.shields.io/badge/Template-Use%20This-blue?style=flat-square)](https://github.com/dotbrains/janus/generate)
[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-green?style=flat-square)](https://spring.io/projects/spring-boot)
[![License: PolyForm Shield 1.0.0](https://img.shields.io/badge/License-PolyForm%20Shield%201.0.0-blue.svg)](https://polyformproject.org/licenses/shield/1.0.0/)

> Named after the Roman god of gates, transitions, and beginnings

## 🎯 About This Template

**This is a GitHub template repository!** Use it to quickly create your own federated authentication service.

Janus is a production-ready federated authentication service that acts as a gateway between client applications and Keycloak, enriching JWTs with custom user data from a PostgreSQL database. It provides a unified authentication experience by combining Keycloak's identity management with application-specific user attributes.

### ⚡ Quick Start with This Template

1. **Click "Use this template"** button above or [click here](https://github.com/dotbrains/janus/generate)
2. **Clone your new repository**
3. **Run the initialization script:**
   ```bash
   ./scripts/init-template.sh
   ```
4. **Follow the setup guide:** See [TEMPLATE_SETUP.md](TEMPLATE_SETUP.md) for detailed instructions

### 📚 Template Documentation

- **[Template Setup Guide](TEMPLATE_SETUP.md)** - Complete guide to customize this template
- **[Package Renaming Guide](docs/PACKAGE_RENAMING.md)** - Detailed package customization instructions

---

## 🎯 Features

- **Stateless REST API**: True RESTful design with JWT Bearer token authentication
- **Keycloak Integration**: Seamless OAuth2/OIDC authentication with Keycloak
- **Token Enhancement**: Enriches JWTs with custom user attributes from PostgreSQL
- **User Management API**: Query and manage users via RESTful endpoints
- **API Versioning**: Package-based versioning with multiple versions coexisting
- **SpEL Support**: Dynamic claim generation using Spring Expression Language
- **Vertical Slice Architecture**: Organized by feature for maintainability
- **Swagger/OpenAPI**: Interactive API documentation with try-it-out functionality
- **Database Seeding**: Pre-configured sample data for development
- **Docker Support**: Easy local development with Docker Compose
- **Automated Configuration**: Zero-config local setup with pre-configured Keycloak realm
- **Security First**: Built with Spring Security 6.x best practices

## 🏗️ Architecture

### High-Level Flow

```
Client Application → Janus → Keycloak (Authentication)
                  ↓
              PostgreSQL (Custom User Attributes)
                  ↓
Client Application ← Enriched JWT Token
```

### Technology Stack

- **Spring Boot 4.0.1** - Application framework
- **Spring Security 6.x** - Security and OAuth2 support
- **Keycloak 23.0.4** - Identity and access management
- **PostgreSQL 15** - User data storage
- **Hibernate 6.x** - ORM with optimistic locking
- **Lombok** - Reduces boilerplate code
- **SpEL** - Dynamic expression evaluation
- **Docker Compose** - Local development environment
- **SpringDoc OpenAPI 2.3.0** - API documentation (Swagger)

### Vertical Slice Architecture with API Versioning

The codebase is organized by feature (vertical slices) with package-based API versioning:

- `/api/v1` - Version 1 API interfaces with Swagger documentation
- `/auth/v1` - Version 1 authentication flow implementation
- `/token` - Token customization and enhancement (shared across versions)
- `/user` - User domain logic and data access (shared across versions)
- `/config` - Security and infrastructure configuration
- `/exception` - Global error handling

**Versioning Strategy**: Each API version has its own package (e.g., `v1`, `v2`). Controllers in versioned packages implement corresponding API interfaces, enabling multiple API versions to coexist.

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven 3.8+

### 1. Start Infrastructure Services

Start PostgreSQL and Keycloak using Docker Compose:

```bash
docker-compose up -d
```

This will automatically:
- Start PostgreSQL on `localhost:5432`
- Create both `janus` and `keycloak` databases
- Start Keycloak on `localhost:8080`
- Import the pre-configured `janus` realm with:
  - Client: `janus-client` (secret: `change-me`)
  - Sample users (admin/admin, john.doe/password, jane.smith/password)
  - All required roles

Wait for services to be healthy (~60 seconds for Keycloak).

### 2. Verify Keycloak Setup (Optional)

Access Keycloak Admin Console: http://localhost:8080
- Login: `admin` / `admin`
- Select the `janus` realm from the dropdown
- Verify the `janus-client` exists with the correct settings

**Note**: The realm, client, and test users are automatically configured via `keycloak-realm.json`. No manual setup required!

📖 **Learn more**: See [Automated Setup Documentation](docs/AUTOMATED_SETUP.md) for details on configuration files and customization.

### 3. Build and Run

```bash
# Build the application
mvn clean package

# Run with dev profile (default - detailed logging, no SSL)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run with production profile (secure settings, SSL enabled)
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Run without specifying profile (defaults to dev)
mvn spring-boot:run
```

The application will start on http://localhost:9090

### 4. Test Authentication

```bash
# Health check (public endpoint)
curl http://localhost:9090/api/v1/auth/health

# Get access token from Keycloak using pre-configured test user
curl -X POST http://localhost:8080/realms/janus/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=janus-client" \
  -d "client_secret=change-me" \
  -d "username=john.doe" \
  -d "password=password"

# Use the access token to call protected endpoints
curl http://localhost:9090/api/v1/auth/token \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

curl http://localhost:9090/api/v1/auth/user \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 5. Test User Management (Optional)

```bash
# All user management endpoints require Bearer token authentication

# Get user by Keycloak ID
curl http://localhost:9090/api/v1/users/keycloak/kc-user-001 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Get user by username
curl http://localhost:9090/api/v1/users/username/john.doe \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Check if user exists
curl http://localhost:9090/api/v1/users/exists/kc-user-001 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Deactivate user
curl -X POST http://localhost:9090/api/v1/users/kc-user-001/deactivate \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## 📁 Project Structure

```
janus/
├── src/main/java/com/dotbrains/janus/
│   ├── JanusApplication.java           # Main application class
│   ├── api/
│   │   └── v1/
│   │       ├── AuthAPI.java            # v1 Auth API interface
│   │       └── UserAPI.java            # v1 User API interface
│   ├── auth/
│   │   └── v1/
│   │       └── AuthController.java     # v1 authentication endpoints
│   ├── config/
│   │   ├── SecurityConfig.java         # Spring Security configuration
│   │   ├── DatabaseConfig.java         # Database & environment config
│   │   └── OpenApiConfig.java          # Swagger/OpenAPI configuration
│   ├── token/
│   │   ├── TokenCustomizer.java        # JWT enhancement logic
│   │   └── CustomClaimsMapper.java     # SpEL-based claims mapping
│   ├── user/
│   │   ├── v1/
│   │   │   └── UserController.java     # v1 user management endpoints
│   │   ├── User.java                   # User entity
│   │   ├── UserRole.java               # Role entity
│   │   ├── UserRepository.java         # Data access
│   │   └── UserService.java            # Business logic
│   └── exception/
│       └── GlobalExceptionHandler.java # Error handling
├── src/main/resources/
│   ├── application.yml                 # Main configuration
│   ├── application-dev.yml             # Development profile
│   ├── application-prod.yml            # Production profile
│   ├── schema.sql                      # Database schema
│   └── data.sql                        # Seed data
├── docker-compose.yml                  # Local infrastructure
├── pom.xml                             # Maven dependencies
└── README.md                           # This file
```

## 🌍 Environment Profiles

Janus automatically detects the environment and applies appropriate security settings.

### Development Profile (`dev`)

**Default** - Used when no profile is specified or when explicitly set.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Features:**
- ✅ Detailed logging (DEBUG level)
- ✅ SQL queries logged
- ✅ Swagger UI enabled
- ✅ Database seeding enabled
- ✅ DevTools hot-reload
- ⚠️ SSL disabled (localhost only)
- ⚠️ Permissive error messages

### Production Profile (`prod`)

**Secure** - Automatically enables production-grade security.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

**Features:**
- 🔒 SSL/TLS enforced for database
- 🔒 HTTPS-only cookies (secure flag)
- 🔒 Strict CORS policy
- 🔒 Swagger UI disabled
- 🔒 Database seeding disabled
- 🔒 Minimal logging (WARN level)
- 🔒 No error stack traces
- 🔒 Connection leak detection
- 🔒 Connection validation

### Environment Detection

The application automatically detects the environment on startup:

```
========================================
Environment: prod
Production Mode: true
========================================
Production environment detected - enabling SSL for database connections
```

Environment is determined by:
1. Active Spring profiles (`spring.profiles.active`)
2. Checks for `prod` or `production` in profile names
3. Defaults to development if no production profile found

### Running in Production

```bash
# Set environment variable
export SPRING_PROFILES_ACTIVE=prod

# Or use command line
java -jar janus.jar --spring.profiles.active=prod

# Or with Maven
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 🔐 Security Configuration

⚠️ **IMPORTANT**: All sensitive credentials (database passwords, Keycloak secrets) are externalized using environment variables with `@Value` annotations. See [SECURITY.md](docs/SECURITY.md) for detailed security configuration and best practices.

### Credential Management

Use environment variables or `.env` file (copy from `.env.example`):

```bash
export DATABASE_PASSWORD=secure-password
export KEYCLOAK_CLIENT_SECRET=client-secret-from-keycloak
```

### Authentication

Janus is a **stateless REST API** using JWT Bearer token authentication:
- `SessionCreationPolicy.STATELESS` - No server-side sessions
- All requests authenticated via `Authorization: Bearer <token>` header
- Tokens obtained directly from Keycloak OAuth2 token endpoint
- True RESTful design - fully stateless and scalable

### CORS Configuration

Configure allowed origins in `application.yml`:

```yaml
janus:
  cors:
    allowed-origins: http://localhost:3000,http://localhost:8080
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    allow-credentials: true
```

### Token Enhancement

Configure which data to include in enhanced tokens:

```yaml
janus:
  token:
    enhancement:
      enabled: true
      include-user-roles: true
      include-user-attributes: true
```

## 🗄️ Database

### Schema

The database includes:
- `users` - User profile information
- `user_roles` - User role assignments

### Indexes

Proper indexing is implemented to prevent deadlocks:
- `idx_users_keycloak_id`
- `idx_users_username`
- `idx_users_email`
- `idx_users_employee_id`

### Seed Data

Sample users are pre-loaded:
- `john.doe` - Senior Software Engineer
- `jane.smith` - Product Manager
- `bob.johnson` - DevOps Engineer
- `alice.williams` - UX Designer
- `charlie.brown` - Junior Developer
- `diana.prince` - Security Architect
- `admin.user` - System Administrator

## 🧪 Testing

### Manual Testing

1. Start services: `docker-compose up -d`
2. Run application: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
3. Navigate to: http://localhost:9090/oauth2/authorization/keycloak
4. Login with Keycloak credentials
5. Check enhanced token: http://localhost:9090/api/v1/auth/token

### Integration Tests

```bash
mvn test
```

## 📊 API Documentation

### Swagger UI

Janus includes comprehensive interactive API documentation powered by Swagger/OpenAPI 3.0.

**Access Swagger UI**: http://localhost:9090/swagger-ui.html

The Swagger interface provides:
- Interactive API testing
- Complete request/response examples
- Authentication flow documentation
- Schema definitions
- Try-it-out functionality

### API Endpoints

#### Authentication (v1)

- `GET /oauth2/authorization/keycloak` - Initiate OAuth2 login
- `GET /api/v1/auth/success` - OAuth2 login success callback
- `GET /api/v1/auth/failure` - OAuth2 login failure callback
- `GET /api/v1/auth/user` - Get current user with enhanced claims
- `GET /api/v1/auth/token` - Get enhanced JWT token
- `GET /api/v1/auth/health` - Health check

#### User Management (v1)

- `GET /api/v1/users/keycloak/{keycloakId}` - Get user by Keycloak ID
- `GET /api/v1/users/username/{username}` - Get user by username with roles
- `GET /api/v1/users/exists/{keycloakId}` - Check if user exists
- `POST /api/v1/users/{keycloakId}/deactivate` - Deactivate user account

#### Documentation

- `GET /swagger-ui.html` - Swagger UI interface
- `GET /v3/api-docs` - OpenAPI 3.0 JSON specification

#### Public Endpoints

- `GET /` - Root endpoint
- `GET /actuator/health` - Application health

## 🔀 API Versioning

Janus uses URL-based API versioning to provide a clear and explicit API contract. This ensures backward compatibility and allows for future API evolution without breaking existing clients.

### Current Version: v1

All API endpoints are prefixed with `/api/v1`:

```
/api/v1/auth/*    # Authentication endpoints
/api/v1/users/*   # User management endpoints
```

### Versioning Strategy

- **Package-based organization**: Each version has its own package (`api/v1`, `auth/v1`, etc.)
- **URL-based API paths**: Explicit version in the URL path (`/api/v1/auth`)
- **Interface-driven**: API interfaces define version-specific contracts
- **Stable contracts**: v1 endpoints remain stable even when v2 is added
- **Coexistence**: Multiple versions can run simultaneously
- **Documentation**: Each version has separate Swagger documentation

### Creating a New API Version (v2)

To add version 2 of the API:

1. Create new packages: `api/v2`, `auth/v2`, and `user/v2`
2. Create API interfaces:
   - `api/v2/AuthAPI.java` with `@RequestMapping("/api/v2/auth")`
   - `api/v2/UserAPI.java` with `@RequestMapping("/api/v2/users")`
3. Create controllers:
   - `auth/v2/AuthController.java` implementing `api/v2/AuthAPI`
   - `user/v2/UserController.java` implementing `api/v2/UserAPI`
4. Update SecurityConfig to permit v2 endpoints
5. Both v1 and v2 will coexist and be served simultaneously

### Example Usage

```bash
# v1 Authentication endpoints
curl http://localhost:9090/api/v1/auth/health
curl http://localhost:9090/api/v1/auth/user
curl http://localhost:9090/api/v1/auth/token

# v1 User Management endpoints
curl http://localhost:9090/api/v1/users/keycloak/kc-user-001
curl http://localhost:9090/api/v1/users/username/john.doe
curl http://localhost:9090/api/v1/users/exists/kc-user-001
```

### Version History

- **v1.0** (Current) - Initial release with OAuth2/Keycloak integration and User Management
  - **Authentication Endpoints:**
    - `/api/v1/auth/success` - OAuth2 login success callback
    - `/api/v1/auth/failure` - OAuth2 login failure callback
    - `/api/v1/auth/user` - Get current authenticated user
    - `/api/v1/auth/token` - Get enhanced JWT token
    - `/api/v1/auth/health` - Health check endpoint
  - **User Management Endpoints:**
    - `/api/v1/users/keycloak/{keycloakId}` - Get user by Keycloak ID
    - `/api/v1/users/username/{username}` - Get user by username
    - `/api/v1/users/exists/{keycloakId}` - Check user existence
    - `/api/v1/users/{keycloakId}/deactivate` - Deactivate user account

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Application port | `9090` |
| `KEYCLOAK_CLIENT_ID` | Keycloak client ID | `janus-client` |
| `KEYCLOAK_CLIENT_SECRET` | Keycloak client secret | `change-me` |
| `KEYCLOAK_ISSUER_URI` | Keycloak issuer URI | `http://localhost:8080/realms/janus` |
| `POSTGRES_DB` | Database name | `janus` |
| `POSTGRES_USER` | Database user | `janus` |
| `POSTGRES_PASSWORD` | Database password | `janus123` |

### Application Properties

Key configuration sections in `application.yml`:
- Spring Security OAuth2 client
- PostgreSQL datasource with HikariCP
- Hibernate/JPA configuration
- Janus-specific settings (CORS, token enhancement)

## 🐛 Known Issues & Solutions

### 1. Database Deadlocks

**Issue**: Row-level deadlocks on `users` table

**Solution**:
- Proper indexing on frequently queried columns
- READ_COMMITTED isolation level
- Optimistic locking with `@Version`

### 2. Stale User Data

**Issue**: User data not refreshed after database updates

**Solution**: Session policy allows cache, but user sync logic updates data on each login if needed.

## 📚 SpEL Usage Examples

Janus uses Spring Expression Language (SpEL) for dynamic claim generation:

```java
// Full name expression
Expression fullNameExpression = spelParser.parseExpression("firstName + ' ' + lastName");

// Admin role check
Expression hasAdminRoleExpression = spelParser.parseExpression("roleNames.contains('ADMIN')");

// Active status
Expression isActiveExpression = spelParser.parseExpression("isActive == true");
```

## 📚 Documentation

Comprehensive documentation is available in the `docs/` directory:

- **[Security Guide](docs/SECURITY.md)** - Complete security guide covering credentials, JWT authentication, Keycloak setup, SSL/TLS, and incident response
- **[API Documentation](docs/API_DOCUMENTATION.md)** - Swagger/OpenAPI setup and interface pattern for clean API design
- **[API Versioning Guide](docs/API_VERSIONING.md)** - Package-based versioning strategy with examples
- **[Documentation Index](docs/README.md)** - Complete documentation overview and quick links

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Follow the vertical slice architecture
4. Add tests for new functionality
5. Submit a pull request

## 📝 License

This project is licensed under the [PolyForm Shield License 1.0.0](https://polyformproject.org/licenses/shield/1.0.0/) — see [LICENSE](LICENSE) for details.
## 🙏 Acknowledgments

- Inspired by the original FAS (Federated Authentication Service)
- Named after Janus, the Roman god of gates and transitions
- Built with Spring Boot and Keycloak best practices

## 📧 Support

For issues and questions, please open a GitHub issue.

---

**Janus** - The gateway to your applications 🚪✨
