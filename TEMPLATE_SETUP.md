# Template Setup Guide

Welcome! This guide will help you set up your own federated authentication service using this template.

## 🚀 Quick Start

### Option 1: Automated Setup (Recommended)

Use the initialization script to automatically customize the template:

**Using Bash:**
```bash
./scripts/init-template.sh
```

**Using Fish Shell:**
```fish
./scripts/init-template.fish
```

The script will prompt you for:
- Company/organization name (e.g., `example`)
- Project name (e.g., `my-auth-service`)
- Java package name (e.g., `com.example.authservice`)
- Database name (e.g., `authdb`)
- Server port (default: `9090`)

**Non-Interactive Mode:**
```bash
./scripts/init-template.sh --non-interactive company-name project-name com.company.project dbname 9090
```

### Option 2: Manual Setup

If you prefer to customize manually, see the [Manual Setup](#manual-setup) section below.

## 📋 Prerequisites

Before you begin, ensure you have:

- ✅ **Java 21+** - [Download](https://adoptium.net/)
- ✅ **Maven 3.8+** - [Installation Guide](https://maven.apache.org/install.html)
- ✅ **Docker & Docker Compose** - [Get Docker](https://docs.docker.com/get-docker/)
- ✅ **Git** - [Download](https://git-scm.com/downloads)

### Verify Prerequisites

```bash
java -version    # Should show Java 21 or higher
mvn -version     # Should show Maven 3.8 or higher
docker --version # Should show Docker 20.10 or higher
```

## 🔧 Post-Script Configuration

After running the initialization script, complete these steps:

### 1. Configure Keycloak

Start the infrastructure services:

```bash
docker-compose up -d
```

Wait for services to be healthy (~60 seconds for Keycloak), then:

1. **Access Keycloak Admin Console**: http://localhost:8080
2. **Login** with default credentials: `admin` / `admin`
3. **Create a Realm**:
   - Click "Create Realm"
   - Name it with your project name (e.g., `my-auth-service`)
   - Click "Create"

4. **Create a Client**:
   - Go to "Clients" → "Create client"
   - Client ID: `{your-project-name}-client` (e.g., `my-auth-service-client`)
   - Client Protocol: `openid-connect`
   - Click "Next"
   - Enable "Client authentication"
   - Click "Save"

5. **Configure Client Settings**:
   - Go to "Settings" tab
   - Valid Redirect URIs: `http://localhost:{your-port}/*`
   - Web Origins: `http://localhost:{your-port}`
   - Click "Save"

6. **Get Client Secret**:
   - Go to "Credentials" tab
   - Copy the "Client Secret"

7. **Create Test Users** (optional):
   - Go to "Users" → "Add user"
   - Create users matching your database seed data

### 2. Update Configuration Files

**Update `src/main/resources/application.yml`:**

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-secret: YOUR_CLIENT_SECRET_HERE  # From Keycloak
```

**Or use environment variables:**

```bash
export KEYCLOAK_CLIENT_SECRET=your-client-secret-here
```

**Update `.env.example` (or create `.env`):**

```bash
# Copy from .env.example
cp .env.example .env

# Edit with your values
vim .env
```

Update these critical values:
- `KEYCLOAK_CLIENT_SECRET` - From Keycloak Credentials tab
- `DATABASE_PASSWORD` - Change from default for production
- `KEYCLOAK_ISSUER_URI` - Update realm name if changed
- `ALLOWED_ORIGINS` - Add your frontend URLs

### 3. Update Documentation

Customize these files with your project details:

- **README.md** - Update project name, description, and specific instructions
- **docs/** - Update any references to generic names with your specifics
- **.env.example** - Add any custom environment variables

### 4. Build and Test

```bash
# Build the application
mvn clean package

# Run with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or run with production profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

The application will start on your configured port (default: http://localhost:9090)

### 5. Verify Setup

**Health Check:**
```bash
curl http://localhost:{your-port}/api/v1/auth/health
```

**Get Access Token:**
```bash
curl -X POST http://localhost:8080/realms/{your-realm}/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id={your-client-id}" \
  -d "client_secret={your-client-secret}" \
  -d "username=test-user" \
  -d "password=test-password"
```

**Test Protected Endpoint:**
```bash
curl http://localhost:{your-port}/api/v1/auth/user \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## 📝 Manual Setup

If you prefer to customize manually:

### 1. Rename Java Packages

**IDE Method (IntelliJ IDEA):**
1. Right-click on `com.dotbrains.janus` package
2. Select "Refactor" → "Rename"
3. Enter your new package name (e.g., `com.example.authservice`)
4. Click "Refactor"

**Command Line Method:**
```bash
# Create new package structure
mkdir -p src/main/java/com/example/authservice

# Copy files
cp -r src/main/java/com/dotbrains/janus/* src/main/java/com/example/authservice/

# Update package declarations
find src/main/java -name "*.java" -exec sed -i '' 's/com.dotbrains.janus/com.example.authservice/g' {} +

# Remove old package
rm -rf src/main/java/com/dotbrains
```

### 2. Update pom.xml

```xml
<groupId>com.example</groupId>
<artifactId>my-auth-service</artifactId>
<name>My Auth Service</name>
<description>Your description here</description>
```

### 3. Update Configuration Files

**application.yml:**
- Change `spring.application.name`
- Update database names
- Update package names in logging configuration
- Update HikariCP pool name

**docker-compose.yml:**
- Rename service containers
- Update database names
- Update network names

**.env.example:**
- Update all references to project-specific names

### 4. Update SQL Files

Update `src/main/resources/data.sql` with your own seed data:
- Email domains
- User information
- Roles

## 🔒 Security Checklist

Before deploying to production:

- [ ] Change all default passwords in `.env`
- [ ] Generate strong `KEYCLOAK_CLIENT_SECRET`
- [ ] Update `ALLOWED_ORIGINS` with your actual frontend URLs
- [ ] Enable SSL/TLS in production profile
- [ ] Review and update `docs/SECURITY.md`
- [ ] Set up database backups
- [ ] Configure proper firewall rules
- [ ] Enable monitoring and logging
- [ ] Review all TODOs in code

## 🎨 Customization Tips

### Adding New Endpoints

Create new API versions in packages:
- `api/v2/YourAPI.java` - API interface
- `your-feature/v2/YourController.java` - Implementation

See `docs/API_VERSIONING.md` for details.

### Database Schema Changes

1. Modify `src/main/resources/schema.sql`
2. Update entity classes in `user/` package
3. Test with: `mvn clean package && mvn spring-boot:run`

### Custom Token Claims

Edit `token/CustomClaimsMapper.java` to add your own claims using SpEL expressions.

## 🐛 Troubleshooting

### Script Fails with "Permission Denied"

```bash
chmod +x scripts/init-template.sh
chmod +x scripts/init-template.fish
```

### Keycloak Not Starting

```bash
# Check logs
docker-compose logs keycloak

# Increase wait time in start.sh (line 32)
sleep 60  # instead of 30
```

### Build Failures After Renaming

```bash
# Clean and rebuild
mvn clean
rm -rf target/
mvn package
```

### Database Connection Issues

- Verify PostgreSQL is running: `docker-compose ps`
- Check credentials in `application.yml` match `docker-compose.yml`
- Ensure database exists: `docker-compose exec postgres psql -U {dbuser} -l`

## 📚 Next Steps

1. **Read the Documentation**:
   - `README.md` - Overview and features
   - `docs/SECURITY.md` - Security best practices
   - `docs/API_DOCUMENTATION.md` - API details
   - `docs/API_VERSIONING.md` - Versioning strategy

2. **Customize Your Service**:
   - Add custom endpoints
   - Modify token claims
   - Extend user model
   - Add business logic

3. **Deploy to Production**:
   - Set up CI/CD pipeline (see `.github/workflows/`)
   - Configure production environment
   - Set up monitoring and alerts
   - Review security checklist

## 🤝 Getting Help

- **Issues**: Check existing GitHub issues or create a new one
- **Documentation**: See the `docs/` directory for comprehensive guides
- **Community**: Contribute improvements back to the template!

## ✨ Template Features

This template includes:

- ✅ Spring Boot 4.0.1 with Spring Security 6.x
- ✅ Keycloak integration (OAuth2/OIDC)
- ✅ JWT token enhancement
- ✅ PostgreSQL with Hibernate
- ✅ API versioning support
- ✅ Swagger/OpenAPI documentation
- ✅ Docker Compose setup
- ✅ Multiple environment profiles (dev/prod)
- ✅ Comprehensive security configuration
- ✅ Vertical slice architecture

---

**Happy Building! 🚀**

If you found this template helpful, please give it a star ⭐ on GitHub!
