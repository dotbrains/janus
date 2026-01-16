# Package Renaming Guide

This guide provides detailed instructions for renaming the Java package structure from `com.dotbrains.janus` to your own package name.

## Quick Start

**Recommended**: Use the initialization script which handles this automatically:

```bash
./scripts/init-template.sh
```

For manual renaming, follow the instructions below.

## Manual Package Renaming

### Method 1: Using IntelliJ IDEA (Recommended)

IntelliJ IDEA provides powerful refactoring tools that handle package renaming safely.

1. **Open the project** in IntelliJ IDEA
2. **Navigate** to `src/main/java/com/dotbrains/janus` in the Project view
3. **Right-click** on the `janus` package
4. **Select** "Refactor" → "Rename" (or press Shift+F6)
5. **Enter** your new package name (e.g., `authservice`)
6. **Click** "Refactor"
7. IntelliJ will show you a preview of all changes
8. **Review** the changes and click "Do Refactor"

#### For Multiple Levels

If you want to change `com.dotbrains` to `com.example`:

1. Right-click on `dotbrains` package
2. Select "Refactor" → "Rename"
3. Enter `example`
4. Repeat the process for the final package name

### Method 2: Using VS Code

VS Code with Java extensions can also handle refactoring:

1. **Install** the "Java Extension Pack" if not already installed
2. **Open** the project folder in VS Code
3. **Navigate** to any Java file
4. **Right-click** on the package declaration at the top
5. **Select** "Rename Symbol" (or press F2)
6. **Enter** your new package name
7. VS Code will update all references

### Method 3: Using Eclipse

1. **Open** the project in Eclipse
2. **Navigate** to the package in Package Explorer
3. **Right-click** on `com.dotbrains.janus`
4. **Select** "Refactor" → "Rename"
5. **Enter** your new package name
6. **Check** "Update references"
7. **Click** "OK"

### Method 4: Command Line (Advanced)

Use this method if you prefer command-line tools or don't have an IDE.

#### Prerequisites
- Unix-like system (macOS, Linux, WSL)
- `sed` command available

#### Steps

```bash
# 1. Define your new package name
NEW_PACKAGE="com.example.authservice"
OLD_PACKAGE="com.dotbrains.janus"

# 2. Create new package directory structure
NEW_PATH=$(echo "$NEW_PACKAGE" | sed 's/\./\//g')
mkdir -p "src/main/java/$NEW_PATH"

# 3. Copy files to new location
cp -r src/main/java/com/dotbrains/janus/* "src/main/java/$NEW_PATH/"

# 4. Update package declarations in Java files
find src/main/java -type f -name "*.java" -exec sed -i.bak \
  "s/package $OLD_PACKAGE/package $NEW_PACKAGE/g" {} \;

# 5. Update import statements
find src/main/java -type f -name "*.java" -exec sed -i.bak \
  "s/import $OLD_PACKAGE/import $NEW_PACKAGE/g" {} \;

# 6. Clean up backup files
find src/main/java -name "*.bak" -delete

# 7. Remove old package directory
rm -rf src/main/java/com/dotbrains

# 8. Update application.yml logging configuration
find src/main/resources -name "application*.yml" -exec sed -i.bak \
  "s/$OLD_PACKAGE/$NEW_PACKAGE/g" {} \;

# 9. Clean up backup files
find src/main/resources -name "*.bak" -delete
```

**Note**: On macOS, use `sed -i '' ` instead of `sed -i.bak` for in-place editing.

## Additional Files to Update

After renaming packages, update these files manually:

### 1. pom.xml

Update the Maven coordinates:

```xml
<groupId>com.example</groupId>
<artifactId>my-auth-service</artifactId>
<name>My Auth Service</name>
<description>My federated authentication service</description>
```

### 2. application.yml

Update logging configuration:

```yaml
logging:
  level:
    com.example.authservice: DEBUG  # Update this line
```

### 3. docker-compose.yml

Update service names and database names:

```yaml
services:
  postgres:
    container_name: my-auth-service-postgres  # Update
    environment:
      POSTGRES_DB: my_auth_db  # Update
      POSTGRES_USER: my_auth_user  # Update
```

### 4. .env.example

Update database and service names:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/my_auth_db
DATABASE_USERNAME=my_auth_user
```

### 5. Documentation

Update references in:
- README.md
- docs/*.md files
- CODEOWNERS

## Verification

After renaming, verify everything works:

### 1. Compile the Project

```bash
mvn clean compile
```

If compilation succeeds, package declarations are correct.

### 2. Run Tests

```bash
mvn test
```

Ensure all tests pass.

### 3. Check for Missed References

```bash
# Search for old package name
grep -r "com.dotbrains.janus" src/
```

Should return no results (or only in comments).

### 4. Run the Application

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Verify the application starts without errors.

### 5. Test API Endpoints

```bash
# Health check
curl http://localhost:9090/api/v1/auth/health
```

## Common Issues and Solutions

### Issue: Compilation Errors After Renaming

**Solution**: Make sure you updated all three places:
1. Package declarations (`package com.example.authservice;`)
2. Import statements (`import com.example.authservice.user.User;`)
3. Directory structure (`src/main/java/com/example/authservice/`)

### Issue: "Cannot find symbol" Errors

**Solution**: Your IDE might have cached the old package structure.
- IntelliJ: File → Invalidate Caches → Invalidate and Restart
- Eclipse: Project → Clean
- VS Code: Reload Window (Ctrl+Shift+P → "Reload Window")

### Issue: Application Won't Start

**Solution**: Check application.yml for hardcoded package names:
```bash
grep -r "com.dotbrains.janus" src/main/resources/
```

### Issue: Tests Fail After Renaming

**Solution**: Test files need the same package renaming:
```bash
find src/test/java -name "*.java" -exec sed -i '' \
  's/com.dotbrains.janus/com.example.authservice/g' {} \;
```

## Package Naming Conventions

Follow Java package naming conventions:

- **All lowercase**: `com.example.authservice` ✓ (not `com.Example.AuthService` ✗)
- **Reverse domain**: `com.yourcompany.projectname` ✓
- **No hyphens**: Use underscores or camelCase ✓
- **Descriptive**: Use meaningful names ✓

### Good Examples
- `com.acme.authentication`
- `com.mycompany.auth.service`
- `io.github.username.authservice`

### Bad Examples
- `com.company.Auth-Service` (hyphens)
- `Com.Company.AuthService` (capitals)
- `com.a.b` (too short/unclear)

## Automation Script

The template includes automation scripts that handle all of this:

**Bash:**
```bash
./scripts/init-template.sh
```

**Fish:**
```fish
./scripts/init-template.fish
```

**Non-Interactive Mode:**
```bash
./scripts/init-template.sh --non-interactive \
  mycompany \
  my-auth-service \
  com.mycompany.authservice \
  authdb \
  9090
```

## Need Help?

If you encounter issues:

1. Check this guide thoroughly
2. Review the [TEMPLATE_SETUP.md](../TEMPLATE_SETUP.md) guide
3. Search existing GitHub issues
4. Create a new issue with:
   - Your operating system
   - IDE (if using one)
   - Complete error messages
   - Steps you've already tried

---

**Pro Tip**: Always commit your changes before doing major refactoring. This way you can easily revert if something goes wrong!
