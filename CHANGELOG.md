# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Template repository features
  - Initialization scripts (bash and fish)
  - Comprehensive template setup guide
  - GitHub templates (issue, PR, workflows)
  - Enhanced documentation for template usage

## [1.0.0] - 2024-01-13

### Added
- Initial release of federated authentication service
- Spring Boot 4.0.1 with Spring Security 6.x
- Keycloak OAuth2/OIDC integration
- JWT token enhancement with custom claims
- PostgreSQL database integration with Hibernate
- User management API (v1)
- Authentication endpoints (v1)
- API versioning support (package-based)
- Swagger/OpenAPI documentation
- Docker Compose setup for local development
- Environment profiles (dev/prod)
- Vertical slice architecture
- Database seeding with sample data
- SpEL support for dynamic claims
- Comprehensive security configuration
- HikariCP connection pooling
- Optimistic locking for user entities

### Documentation
- README with quick start guide
- Security guide (docs/SECURITY.md)
- API documentation (docs/API_DOCUMENTATION.md)
- API versioning guide (docs/API_VERSIONING.md)
- Environment configuration examples

### Infrastructure
- PostgreSQL 15 setup
- Keycloak 23.0 setup
- Docker Compose configuration
- Database schema and seed data
- Health check endpoints

---

## Template Usage Notes

After customizing this template for your project:

1. **Update version numbers** to match your releases
2. **Document all changes** following the Keep a Changelog format
3. **Use semantic versioning**: MAJOR.MINOR.PATCH
   - MAJOR: Breaking changes
   - MINOR: New features (backward compatible)
   - PATCH: Bug fixes (backward compatible)

### Changelog Sections

Use these standard sections:
- **Added**: New features
- **Changed**: Changes to existing functionality
- **Deprecated**: Soon-to-be removed features
- **Removed**: Removed features
- **Fixed**: Bug fixes
- **Security**: Security improvements

### Example Entry

```markdown
## [1.1.0] - 2024-02-15

### Added
- New user profile endpoint
- Email verification feature
- Rate limiting middleware

### Changed
- Updated Spring Boot to 4.0.2
- Improved error messages

### Fixed
- Fixed JWT expiration bug
- Corrected database connection pool configuration

### Security
- Updated dependencies with security patches
- Added input validation for user endpoints
```
