#!/usr/bin/env fish

# Colors for output
set -l RED \e\[0\;31m
set -l GREEN \e\[0\;32m
set -l YELLOW \e\[1\;33m
set -l BLUE \e\[0\;34m
set -l NC \e\[0m

echo "$BLUE========================================$NC"
echo "$BLUE  Template Initialization Script$NC"
echo "$BLUE  Federated Authentication Service$NC"
echo "$BLUE========================================$NC"
echo ""

# Function to validate package name
function validate_package_name
    if not string match -qr '^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)*$' $argv[1]
        echo "$RED""Error: Invalid package name. Use lowercase letters, numbers, underscores, and dots.$NC"
        echo "$RED""Example: com.example.myapp$NC"
        return 1
    end
    return 0
end

# Function to validate project name
function validate_project_name
    if not string match -qr '^[a-z][a-z0-9-]*$' $argv[1]
        echo "$RED""Error: Invalid project name. Use lowercase letters, numbers, and hyphens.$NC"
        echo "$RED""Example: my-auth-service$NC"
        return 1
    end
    return 0
end

# Function to validate port
function validate_port
    if not string match -qr '^[0-9]+$' $argv[1]; or test $argv[1] -lt 1024; or test $argv[1] -gt 65535
        echo "$RED""Error: Port must be a number between 1024 and 65535.$NC"
        return 1
    end
    return 0
end

# Check if running in non-interactive mode
if test "$argv[1]" = "--non-interactive"
    set COMPANY_NAME (test -n "$argv[2]"; and echo $argv[2]; or echo "example")
    set PROJECT_NAME (test -n "$argv[3]"; and echo $argv[3]; or echo "auth-service")
    set PACKAGE_NAME (test -n "$argv[4]"; and echo $argv[4]; or echo "com.example.authservice")
    set DB_NAME (test -n "$argv[5]"; and echo $argv[5]; or echo "authdb")
    set SERVER_PORT (test -n "$argv[6]"; and echo $argv[6]; or echo "9090")
else
    # Interactive prompts
    echo "$YELLOW""This script will customize the template with your project details.$NC"
    echo ""
    
    # Company/Organization Name
    read -P "Enter your company/organization name (e.g., example): " COMPANY_NAME
    set COMPANY_NAME (test -n "$COMPANY_NAME"; and echo $COMPANY_NAME; or echo "example")
    
    # Project Name
    while true
        read -P "Enter your project name (e.g., my-auth-service): " PROJECT_NAME
        set PROJECT_NAME (test -n "$PROJECT_NAME"; and echo $PROJECT_NAME; or echo "auth-service")
        if validate_project_name $PROJECT_NAME
            break
        end
    end
    
    # Package Name
    while true
        read -P "Enter your Java package name (e.g., com.example.authservice): " PACKAGE_NAME
        set PACKAGE_NAME (test -n "$PACKAGE_NAME"; and echo $PACKAGE_NAME; or echo "com.example.authservice")
        if validate_package_name $PACKAGE_NAME
            break
        end
    end
    
    # Database Name
    read -P "Enter your database name (e.g., authdb): " DB_NAME
    set DB_NAME (test -n "$DB_NAME"; and echo $DB_NAME; or echo "authdb")
    
    # Server Port
    while true
        read -P "Enter your server port (default: 9090): " SERVER_PORT
        set SERVER_PORT (test -n "$SERVER_PORT"; and echo $SERVER_PORT; or echo "9090")
        if validate_port $SERVER_PORT
            break
        end
    end
    
    echo ""
    echo "$YELLOW""Review your configuration:$NC"
    echo "  Company Name:    $COMPANY_NAME"
    echo "  Project Name:    $PROJECT_NAME"
    echo "  Package Name:    $PACKAGE_NAME"
    echo "  Database Name:   $DB_NAME"
    echo "  Server Port:     $SERVER_PORT"
    echo ""
    read -P "Continue with these values? (y/n): " CONFIRM
    
    if not string match -qr '^[Yy]$' $CONFIRM
        echo "$RED""Aborted.$NC"
        exit 1
    end
end

echo ""
echo "$GREEN""Starting template customization...$NC"
echo ""

# Convert package name to directory path
set PACKAGE_PATH (string replace -a '.' '/' $PACKAGE_NAME)
set OLD_PACKAGE_PATH "com/dotbrains/janus"

# Backup original files
echo "$BLUE""[1/8]$NC Creating backup..."
set BACKUP_DIR ".template-backup-"(date +%Y%m%d-%H%M%S)
mkdir -p $BACKUP_DIR
cp -r src pom.xml docker-compose.yml $BACKUP_DIR/ 2>/dev/null; or true

# Update Java package structure
echo "$BLUE""[2/8]$NC Renaming Java packages..."
mkdir -p "src/main/java/$PACKAGE_PATH"
cp -r src/main/java/$OLD_PACKAGE_PATH/* "src/main/java/$PACKAGE_PATH/" 2>/dev/null; or true
rm -rf src/main/java/com

# Update package declarations in Java files
echo "$BLUE""[3/8]$NC Updating package declarations..."
find src/main/java -type f -name "*.java" -exec sed -i.bak "s/package com\.dotbrains\.janus/package $PACKAGE_NAME/g" {} \;
find src/main/java -type f -name "*.java" -exec sed -i.bak "s/import com\.dotbrains\.janus/import $PACKAGE_NAME/g" {} \;
find src/main/java -type f -name "*.java.bak" -delete

# Update pom.xml
echo "$BLUE""[4/8]$NC Updating pom.xml..."
sed -i.bak "s/<groupId>com\.dotbrains<\/groupId>/<groupId>$COMPANY_NAME<\/groupId>/g" pom.xml
sed -i.bak "s/<artifactId>janus<\/artifactId>/<artifactId>$PROJECT_NAME<\/artifactId>/g" pom.xml
set PROJECT_NAME_TITLE (string replace -a '-' ' ' $PROJECT_NAME | string replace -r '\w+' (string replace -r '(.)(.*)'  -- (string upper '$1') (string lower '$2')))
sed -i.bak "s/<name>Janus<\/name>/<name>$PROJECT_NAME_TITLE<\/name>/g" pom.xml
rm pom.xml.bak

# Update application.yml
echo "$BLUE""[5/8]$NC Updating application.yml..."
find src/main/resources -name "application*.yml" -exec sed -i.bak "s/name: janus/name: $PROJECT_NAME/g" {} \;
find src/main/resources -name "application*.yml" -exec sed -i.bak "s/janus/$DB_NAME/g" {} \;
find src/main/resources -name "application*.yml" -exec sed -i.bak "s/com\.dotbrains\.janus/$PACKAGE_NAME/g" {} \;
find src/main/resources -name "application*.yml" -exec sed -i.bak "s/port: 9090/port: $SERVER_PORT/g" {} \;
set POOL_NAME (string replace -r '^(.)' -- (string upper '$1') $PROJECT_NAME)"HikariPool"
find src/main/resources -name "application*.yml" -exec sed -i.bak "s/pool-name: JanusHikariPool/pool-name: $POOL_NAME/g" {} \;
find src/main/resources -name "*.bak" -delete

# Update docker-compose.yml
echo "$BLUE""[6/8]$NC Updating docker-compose.yml..."
sed -i.bak "s/janus-postgres/$PROJECT_NAME-postgres/g" docker-compose.yml
sed -i.bak "s/janus-keycloak/$PROJECT_NAME-keycloak/g" docker-compose.yml
sed -i.bak "s/janus-network/$PROJECT_NAME-network/g" docker-compose.yml
sed -i.bak "s/POSTGRES_DB: janus/POSTGRES_DB: $DB_NAME/g" docker-compose.yml
sed -i.bak "s/POSTGRES_USER: janus/POSTGRES_USER: $DB_NAME/g" docker-compose.yml
sed -i.bak "s/jdbc:postgresql:\/\/localhost:5432\/janus/jdbc:postgresql:\/\/localhost:5432\/$DB_NAME/g" docker-compose.yml
sed -i.bak "s/pg_isready -U janus/pg_isready -U $DB_NAME/g" docker-compose.yml
rm docker-compose.yml.bak

# Update .env.example
echo "$BLUE""[7/8]$NC Updating .env.example..."
sed -i.bak "s/janus/$DB_NAME/g" .env.example
sed -i.bak "s/9090/$SERVER_PORT/g" .env.example
rm .env.example.bak

# Update SQL files
echo "$BLUE""[8/8]$NC Updating SQL files..."
find src/main/resources -name "*.sql" -exec sed -i.bak "s/@dotbrains\.com/@$COMPANY_NAME.com/g" {} \;
find src/main/resources -name "*.bak" -delete

echo ""
echo "$GREEN""✓ Template customization complete!$NC"
echo ""
echo "$YELLOW""Next steps:$NC"
echo "  1. Review the changes in your editor"
echo "  2. Update README.md with your project details"
echo "  3. Configure Keycloak (see TEMPLATE_SETUP.md)"
echo "  4. Update .env.example with your credentials"
echo "  5. Run: mvn clean package"
echo "  6. Start infrastructure: docker-compose up -d"
echo ""
echo "$BLUE""Backup created at: $BACKUP_DIR$NC"
echo "$YELLOW""You can safely delete the backup once you verify everything works.$NC"
echo ""
echo "$GREEN""Happy coding! 🚀$NC"
