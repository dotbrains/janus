# Automated Setup Documentation

This document explains the automated configuration files that enable zero-config local development for Janus.

## 📦 Configuration Files

### 1. `docker-compose.yml`
Orchestrates PostgreSQL and Keycloak services with automatic initialization.

**Key Features:**
- Automatic database creation via initialization script
- Keycloak realm import on startup
- Health checks for service readiness
- Proper service dependencies

### 2. `init-db.sh`
PostgreSQL initialization script that runs on first startup.

**What it does:**
- Creates the `keycloak` database automatically
- Grants proper permissions to the janus user
- Only runs when the database volume is empty (first startup)

**Location:** Mounted to `/docker-entrypoint-initdb.d/init-db.sh` in the PostgreSQL container

### 3. `keycloak-realm.json`
Complete Keycloak realm configuration with pre-configured settings.

**Includes:**
- Realm: `janus`
- Client: `janus-client` (secret: `change-me`)
- Roles: USER, ADMIN, DEVELOPER, PRODUCT_MANAGER, DESIGNER, DEVOPS, SECURITY, SENIOR, SUPER_ADMIN
- Protocol Mappers: username, email, roles
- Test Users:
  - `admin` / `admin` (roles: USER, ADMIN, SUPER_ADMIN)
  - `john.doe` / `password` (roles: USER, DEVELOPER, SENIOR)
  - `jane.smith` / `password` (roles: USER, PRODUCT_MANAGER)

**Location:** Mounted to `/opt/keycloak/data/import/janus-realm.json` in the Keycloak container

### 4. `schema.sql` & `data.sql`
Database initialization scripts for the Janus application database.

**Custom SQL Separator:** Uses `^` as the statement separator instead of `;` to support PostgreSQL functions with dollar-quoted strings (`$$`).

**Configuration in `application.yml`:**
```yaml
spring:
  sql:
    init:
      separator: ^
      comment-prefixes:
        - --
```

## 🚀 How It Works

### First Startup Flow

1. **Docker Compose starts PostgreSQL**
   - Creates `janus` database (default POSTGRES_DB)
   - Runs `init-db.sh` to create `keycloak` database
   - Database is ready with both databases created

2. **Docker Compose starts Keycloak**
   - Waits for PostgreSQL to be healthy
   - Connects to the `keycloak` database
   - Imports `keycloak-realm.json` via `--import-realm` flag
   - Realm `janus` is created with all configurations

3. **Spring Boot application starts**
   - Connects to `janus` database
   - Runs `schema.sql` to create tables, functions, triggers
   - Runs `data.sql` to seed sample users
   - Application is ready to authenticate users

### Subsequent Startups

- PostgreSQL: `init-db.sh` does **not** run (database already initialized)
- Keycloak: Realm import is skipped if realm already exists (IGNORE_EXISTING strategy)
- Spring Boot: SQL initialization mode is `always`, but uses Flyway/Liquibase conventions

## 🔒 Security Considerations

### Development (Default)
- Client secret: `change-me`
- Test user passwords: `password` or `admin`
- SSL disabled for database connections
- Keycloak in development mode

### Production (Required Changes)

**1. Update Keycloak Client Secret:**
```bash
# In Keycloak Admin Console:
# Realms > janus > Clients > janus-client > Credentials > Regenerate Secret

# Then update application.yml:
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-secret: ${KEYCLOAK_CLIENT_SECRET}
```

**2. Change Default Passwords:**
- Remove or change test user passwords in Keycloak
- Use strong passwords for admin accounts

**3. Enable SSL/TLS:**
- Configure PostgreSQL with SSL certificates
- Use HTTPS for Keycloak (behind a reverse proxy)
- Update all URIs to use https://

**4. Remove `init-db.sh` from production:**
- Create databases via infrastructure-as-code
- Use proper database migration tools

**5. Update `keycloak-realm.json` or don't use it:**
- Remove test users
- Change client secret before import
- Or configure Keycloak manually in production

## 🔄 Resetting Your Environment

To completely reset your local environment:

```bash
# Stop and remove containers, volumes, and networks
docker-compose down -v

# Start fresh (will re-run all initialization)
docker-compose up -d

# Wait for services to be healthy
docker-compose ps
```

This will:
- Delete all data in PostgreSQL
- Delete all Keycloak configurations
- Re-run `init-db.sh`
- Re-import `keycloak-realm.json`
- Re-seed the database on next application start

## 📝 Customizing Configuration

### Adding New Users to Keycloak

Edit `keycloak-realm.json` and add to the `users` array:

```json
{
  "username": "new.user",
  "enabled": true,
  "emailVerified": true,
  "email": "new.user@example.com",
  "firstName": "New",
  "lastName": "User",
  "credentials": [
    {
      "type": "password",
      "value": "password",
      "temporary": false
    }
  ],
  "realmRoles": ["USER", "DEVELOPER"]
}
```

Then restart Keycloak: `docker-compose restart keycloak`

### Adding New Roles

Edit `keycloak-realm.json` and add to the `roles.realm` array:

```json
{
  "name": "NEW_ROLE",
  "description": "Description of new role"
}
```

### Changing Database Names

If you need different database names:

1. Edit `docker-compose.yml`:
   ```yaml
   environment:
     POSTGRES_DB: your_app_db_name
   ```

2. Edit `init-db.sh`:
   ```bash
   CREATE DATABASE your_keycloak_db_name;
   ```

3. Update connection strings in both Keycloak and Spring Boot configs

## 🐛 Troubleshooting

### Keycloak Not Starting
```bash
# Check logs
docker logs janus-keycloak

# Common issue: Database doesn't exist
# Solution: Ensure init-db.sh ran
docker exec janus-postgres psql -U janus -l
```

### Realm Not Imported
```bash
# Check if realm file is mounted
docker exec janus-keycloak ls -la /opt/keycloak/data/import/

# Check import logs
docker logs janus-keycloak | grep -i import
```

### SQL Script Errors
```bash
# Verify separator configuration
# Check application.yml for:
spring:
  sql:
    init:
      separator: ^
```

## 📚 Related Documentation

- [README.md](../README.md) - Main project documentation
- [docker-compose.yml](../docker-compose.yml) - Service orchestration
- [keycloak-realm.json](../keycloak-realm.json) - Realm configuration
- [init-db.sh](../init-db.sh) - Database initialization
- [schema.sql](../src/main/resources/schema.sql) - Database schema
- [data.sql](../src/main/resources/data.sql) - Sample data
