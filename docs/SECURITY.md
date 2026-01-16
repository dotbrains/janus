# Security Guide

Complete security documentation for Janus federated authentication service.

## 🚀 Quick Start

### Minimum Security Setup

```bash
# 1. Copy environment template
cp .env.example .env

# 2. Set required secrets (CHANGE THESE!)
export DATABASE_PASSWORD=your-secure-password
export KEYCLOAK_CLIENT_SECRET=your-keycloak-client-secret
```

### Production Checklist

- [ ] Change all default passwords
- [ ] Use environment variables for secrets
- [ ] Enable SSL/TLS for database
- [ ] Enable HTTPS for application
- [ ] Configure proper CORS origins
- [ ] Rotate credentials regularly
- [ ] Enable audit logging

## 🔐 Credential Management

### Environment Variables

All sensitive credentials are externalized using `@Value` annotations. **Never hardcode credentials.**

| Variable | Description | Required |
|----------|-------------|----------|
| `DATABASE_URL` | PostgreSQL connection URL | Yes |
| `DATABASE_USERNAME` | Database username | Yes |
| `DATABASE_PASSWORD` | Database password | Yes |
| `KEYCLOAK_CLIENT_SECRET` | OAuth2 client secret | Yes |

### Configuration Pattern

```java
@Value("${DATABASE_PASSWORD:default-dev-value}")
private String databasePassword;
```

### Setup Methods

#### Production (Recommended)
```bash
export DATABASE_PASSWORD="secure-password"
export KEYCLOAK_CLIENT_SECRET="client-secret"
```

#### Development
```bash
cp .env.example .env
nano .env  # Edit with your values
```

## 🛡️ Security Features

### 1. Stateless REST API

Janus is a **fully stateless** REST service:

- ✅ No server-side sessions (`SessionCreationPolicy.STATELESS`)
- ✅ JWT Bearer token authentication
- ✅ No cookies (no JSESSIONID)
- ✅ Truly RESTful and horizontally scalable

**Authentication:**
```bash
# Get token from Keycloak
curl -X POST http://keycloak:8080/realms/janus/protocol/openid-connect/token \
  -d "grant_type=password" \
  -d "client_id=janus-client" \
  -d "client_secret=YOUR_SECRET" \
  -d "username=user" \
  -d "password=pass"

# Use Bearer token
curl http://localhost:9090/api/v1/auth/user \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 2. Database Security

#### Secure Logging
- Passwords are **never logged**
- Usernames masked in logs (e.g., `j***s`)
- URLs with passwords are sanitized

#### Connection Pool Security
- Prepared statement caching enabled
- Connection validation on borrow
- Leak detection enabled (60 seconds in prod)

#### Production Settings
```yaml
spring:
  datasource:
    url: jdbc:postgresql://db.example.com:5432/janus?ssl=true&sslmode=require
    hikari:
      leak-detection-threshold: 60000
      connection-test-query: SELECT 1
```

### 3. JWT Security

- ✅ Signature verification via Keycloak
- ✅ Token expiration enforcement
- ✅ Issuer validation
- ✅ Audience validation

### 4. CORS Configuration

```yaml
janus:
  cors:
    allowed-origins: https://app.example.com
    allowed-methods: GET,POST,PUT,DELETE
    allow-credentials: true
```

**Production**: Only whitelist specific domains!

## 🔒 SSL/TLS Configuration

### Application HTTPS

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

### PostgreSQL SSL

```yaml
spring:
  datasource:
    url: jdbc:postgresql://db:5432/janus?ssl=true&sslmode=require
```

## 🚨 Default Passwords

| Service | Default | **CHANGE IN PRODUCTION!** |
|---------|---------|---------------------------|
| Database | `janus123` | ✅ Required |
| Keycloak Admin | `admin` | ✅ Required |
| Keycloak Client | `change-me` | ✅ Required |

## 🔧 Common Security Tasks

### Rotate Database Password

```bash
# 1. Update .env
DATABASE_PASSWORD=new-secure-password

# 2. Update database
psql -U postgres -c "ALTER USER janus WITH PASSWORD 'new-secure-password';"

# 3. Restart application
```

### Rotate Keycloak Secret

```bash
# 1. Regenerate secret in Keycloak Admin Console
# 2. Update environment
export KEYCLOAK_CLIENT_SECRET=new-secret

# 3. Restart application
```

### Verify No Default Passwords

```bash
# Check for insecure defaults
grep -E "(janus123|change-me|admin)" .env
```

## 📊 Security Monitoring

### Check Logs

```bash
# Authentication failures
grep "Authentication error" logs/spring.log

# Access denied
grep "Access denied" logs/spring.log

# Database issues
grep "HikariPool" logs/spring.log
```

### What's NOT Logged

- ❌ Passwords (never logged)
- ❌ Client secrets
- ❌ API keys
- ✅ Masked usernames (first & last char only)
- ✅ Sanitized URLs

## 🆘 Security Incident Response

If credentials are compromised:

1. **IMMEDIATELY** rotate all credentials
2. Check logs for unauthorized access:
   ```bash
   grep "Authentication" logs/spring.log | grep -v "successful"
   ```
3. Update all environment variables
4. Restart application
5. Document incident
6. Review access patterns

## 🔍 Security Audit

Regular audits should include:

```bash
# Dependency vulnerability scan
mvn dependency-check:check

# Check for exposed secrets
git log -p | grep -E "(password|secret|key)"

# Verify SSL/TLS
openssl s_client -connect localhost:9090

# Check CORS configuration
curl -H "Origin: http://evil.com" http://localhost:9090/api/v1/auth/health
```

## 🏗️ Secrets Management Integration

### AWS Secrets Manager

```java
@Bean
public String databasePassword() {
    SecretsManagerClient client = SecretsManagerClient.create();
    GetSecretValueRequest request = GetSecretValueRequest.builder()
        .secretId("janus/database/password")
        .build();
    return client.getSecretValue(request).secretString();
}
```

### HashiCorp Vault

```yaml
spring:
  cloud:
    vault:
      host: vault.example.com
      port: 8200
      scheme: https
      authentication: TOKEN
      token: ${VAULT_TOKEN}
```

### Kubernetes Secrets

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: janus-secrets
type: Opaque
data:
  database-password: <base64-encoded>
  keycloak-client-secret: <base64-encoded>
```

## 📚 Additional Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security](https://spring.io/projects/spring-security)
- [OAuth 2.0 Security](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-security-topics)
- [JWT Best Practices](https://datatracker.ietf.org/doc/html/rfc8725)
- [PostgreSQL Security](https://www.postgresql.org/docs/current/security.html)
- [Keycloak Documentation](https://www.keycloak.org/documentation)

## 📧 Reporting Security Issues

**Email**: security@dotbrains.com  
**Do NOT** create public GitHub issues for vulnerabilities.

---

**Security is an ongoing process** - Regular reviews and updates are essential!
