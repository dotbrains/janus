# Janus Documentation

Comprehensive documentation for Janus federated authentication service.

## 🎯 Template Repository

**Using this as a template?** Start here:
- **[../TEMPLATE_SETUP.md](../TEMPLATE_SETUP.md)** - Complete template setup guide
- **[PACKAGE_RENAMING.md](./PACKAGE_RENAMING.md)** - Detailed package renaming instructions

## 📑 Documentation Files

### [SECURITY.md](./SECURITY.md) - Complete Security Guide
**Covers everything security-related:**
- Quick setup & production checklist
- Credential management & environment variables
- Stateless REST API authentication (JWT Bearer tokens)
- Database security & connection pooling
- SSL/TLS configuration
- Password rotation & incident response
- Secrets management integration (AWS, Vault, Kubernetes)

### [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) - API Design Patterns  
**Building APIs with Swagger/OpenAPI:**
- Interface pattern for clean controllers
- Swagger annotation reference
- Best practices for API documentation
- Creating new API endpoints
- Testing with Swagger UI

### [API_VERSIONING.md](./API_VERSIONING.md) - Versioning Strategy
**Package-based API versioning:**
- How versioning works (v1, v2, etc.)
- Creating new API versions
- Deprecation strategies
- Examples and best practices
- Migration guides

## 🚀 Quick Start

### New Users
1. Read the [Main README](../README.md) - Project overview
2. Follow [SECURITY.md](./SECURITY.md) quick setup
3. Explore API via Swagger UI at http://localhost:9090/swagger-ui.html

### Developers
1. Understand [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) - Interface pattern
2. Learn [API_VERSIONING.md](./API_VERSIONING.md) - Versioning strategy  
3. Review [SECURITY.md](./SECURITY.md) - Security best practices

### System Administrators
1. Configure [SECURITY.md](./SECURITY.md) - Production setup
2. Review credential management section
3. Set up SSL/TLS for all connections

## 🔍 Find What You Need

| I want to... | Go to... |
|--------------|----------|
| Secure my installation | [SECURITY.md](./SECURITY.md) |
| Use Bearer token authentication | [SECURITY.md - Stateless REST API](./SECURITY.md#1-stateless-rest-api) |
| Rotate credentials | [SECURITY.md - Common Tasks](./SECURITY.md#-common-security-tasks) |
| Add API documentation | [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) |
| Create API v2 | [API_VERSIONING.md](./API_VERSIONING.md#adding-a-new-version-v2) |
| Understand dev vs prod profiles | [Main README - Environment Profiles](../README.md#-environment-profiles) |

## 📚 External Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [OpenAPI Specification](https://spec.openapis.org/oas/v3.1.0)
- [JWT Best Practices](https://datatracker.ietf.org/doc/html/rfc8725)

## 🆘 Getting Help

1. Check this documentation
2. Review the [Main README](../README.md)
3. Check `.env.example` for configuration options
4. Open a GitHub issue

---

**Janus Documentation** - Your guide to federated authentication 🚪✨
