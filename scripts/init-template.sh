#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Template Initialization Script${NC}"
echo -e "${BLUE}  Federated Authentication Service${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to validate package name
validate_package_name() {
    if [[ ! $1 =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)*$ ]]; then
        echo -e "${RED}Error: Invalid package name. Use lowercase letters, numbers, underscores, and dots.${NC}"
        echo -e "${RED}Example: com.example.myapp${NC}"
        return 1
    fi
    return 0
}

# Function to validate project name
validate_project_name() {
    if [[ ! $1 =~ ^[a-z][a-z0-9-]*$ ]]; then
        echo -e "${RED}Error: Invalid project name. Use lowercase letters, numbers, and hyphens.${NC}"
        echo -e "${RED}Example: my-auth-service${NC}"
        return 1
    fi
    return 0
}

# Function to validate port
validate_port() {
    if [[ ! $1 =~ ^[0-9]+$ ]] || [ $1 -lt 1024 ] || [ $1 -gt 65535 ]; then
        echo -e "${RED}Error: Port must be a number between 1024 and 65535.${NC}"
        return 1
    fi
    return 0
}

# Check if running in non-interactive mode
if [ "$1" == "--non-interactive" ]; then
    COMPANY_NAME=${2:-"example"}
    PROJECT_NAME=${3:-"auth-service"}
    PACKAGE_NAME=${4:-"com.example.authservice"}
    DB_NAME=${5:-"authdb"}
    SERVER_PORT=${6:-"9090"}
else
    # Interactive prompts
    echo -e "${YELLOW}This script will customize the template with your project details.${NC}"
    echo ""
    
    # Company/Organization Name
    read -p "Enter your company/organization name (e.g., example): " COMPANY_NAME
    COMPANY_NAME=${COMPANY_NAME:-example}
    
    # Project Name
    while true; do
        read -p "Enter your project name (e.g., my-auth-service): " PROJECT_NAME
        PROJECT_NAME=${PROJECT_NAME:-auth-service}
        if validate_project_name "$PROJECT_NAME"; then
            break
        fi
    done
    
    # Package Name
    while true; do
        read -p "Enter your Java package name (e.g., com.example.authservice): " PACKAGE_NAME
        PACKAGE_NAME=${PACKAGE_NAME:-com.example.authservice}
        if validate_package_name "$PACKAGE_NAME"; then
            break
        fi
    done
    
    # Database Name
    read -p "Enter your database name (e.g., authdb): " DB_NAME
    DB_NAME=${DB_NAME:-authdb}
    
    # Server Port
    while true; do
        read -p "Enter your server port (default: 9090): " SERVER_PORT
        SERVER_PORT=${SERVER_PORT:-9090}
        if validate_port "$SERVER_PORT"; then
            break
        fi
    done
    
    echo ""
    echo -e "${YELLOW}Review your configuration:${NC}"
    echo "  Company Name:    $COMPANY_NAME"
    echo "  Project Name:    $PROJECT_NAME"
    echo "  Package Name:    $PACKAGE_NAME"
    echo "  Database Name:   $DB_NAME"
    echo "  Server Port:     $SERVER_PORT"
    echo ""
    read -p "Continue with these values? (y/n): " CONFIRM
    
    if [[ ! $CONFIRM =~ ^[Yy]$ ]]; then
        echo -e "${RED}Aborted.${NC}"
        exit 1
    fi
fi

echo ""
echo -e "${GREEN}Starting template customization...${NC}"
echo ""

# Convert package name to directory path
PACKAGE_PATH=$(echo "$PACKAGE_NAME" | sed 's/\./\//g')
OLD_PACKAGE_PATH="com/dotbrains/janus"

# Backup original files
echo -e "${BLUE}[1/8]${NC} Creating backup..."
BACKUP_DIR=".template-backup-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$BACKUP_DIR"
cp -r src pom.xml docker-compose.yml "$BACKUP_DIR/" 2>/dev/null || true

# Update Java package structure
echo -e "${BLUE}[2/8]${NC} Renaming Java packages..."
mkdir -p "src/main/java/$PACKAGE_PATH"
cp -r src/main/java/$OLD_PACKAGE_PATH/* "src/main/java/$PACKAGE_PATH/" 2>/dev/null || true
rm -rf src/main/java/com

# Update package declarations in Java files
echo -e "${BLUE}[3/8]${NC} Updating package declarations..."
find src/main/java -type f -name "*.java" -exec sed -i.bak "s/package com\.dotbrains\.janus/package $PACKAGE_NAME/g" {} \;
find src/main/java -type f -name "*.java" -exec sed -i.bak "s/import com\.dotbrains\.janus/import $PACKAGE_NAME/g" {} \;
find src/main/java -type f -name "*.java.bak" -delete

# Update pom.xml
echo -e "${BLUE}[4/8]${NC} Updating pom.xml..."
sed -i.bak "s/<groupId>com\.dotbrains<\/groupId>/<groupId>$COMPANY_NAME<\/groupId>/g" pom.xml
sed -i.bak "s/<artifactId>janus<\/artifactId>/<artifactId>$PROJECT_NAME<\/artifactId>/g" pom.xml
sed -i.bak "s/<name>Janus<\/name>/<name>$(echo $PROJECT_NAME | sed 's/-/ /g' | awk '{for(i=1;i<=NF;i++) $i=toupper(substr($i,1,1)) tolower(substr($i,2));}1')<\/name>/g" pom.xml
rm pom.xml.bak

# Update application.yml
echo -e "${BLUE}[5/8]${NC} Updating application.yml..."
find src/main/resources -name "application*.yml" -exec sed -i.bak "s/name: janus/name: $PROJECT_NAME/g" {} \;
find src/main/resources -name "application*.yml" -exec sed -i.bak "s/janus/$DB_NAME/g" {} \;
find src/main/resources -name "application*.yml" -exec sed -i.bak "s/com\.dotbrains\.janus/$PACKAGE_NAME/g" {} \;
find src/main/resources -name "application*.yml" -exec sed -i.bak "s/port: 9090/port: $SERVER_PORT/g" {} \;
find src/main/resources -name "application*.yml" -exec sed -i.bak "s/pool-name: JanusHikariPool/pool-name: ${PROJECT_NAME^}HikariPool/g" {} \;
find src/main/resources -name "*.bak" -delete

# Update docker-compose.yml
echo -e "${BLUE}[6/8]${NC} Updating docker-compose.yml..."
sed -i.bak "s/janus-postgres/${PROJECT_NAME}-postgres/g" docker-compose.yml
sed -i.bak "s/janus-keycloak/${PROJECT_NAME}-keycloak/g" docker-compose.yml
sed -i.bak "s/janus-network/${PROJECT_NAME}-network/g" docker-compose.yml
sed -i.bak "s/POSTGRES_DB: janus/POSTGRES_DB: $DB_NAME/g" docker-compose.yml
sed -i.bak "s/POSTGRES_USER: janus/POSTGRES_USER: $DB_NAME/g" docker-compose.yml
sed -i.bak "s/jdbc:postgresql:\/\/localhost:5432\/janus/jdbc:postgresql:\/\/localhost:5432\/$DB_NAME/g" docker-compose.yml
sed -i.bak "s/pg_isready -U janus/pg_isready -U $DB_NAME/g" docker-compose.yml
rm docker-compose.yml.bak

# Update .env.example
echo -e "${BLUE}[7/8]${NC} Updating .env.example..."
sed -i.bak "s/janus/$DB_NAME/g" .env.example
sed -i.bak "s/9090/$SERVER_PORT/g" .env.example
rm .env.example.bak

# Update SQL files
echo -e "${BLUE}[8/8]${NC} Updating SQL files..."
find src/main/resources -name "*.sql" -exec sed -i.bak "s/@dotbrains\.com/@$COMPANY_NAME.com/g" {} \;
find src/main/resources -name "*.bak" -delete

echo ""
echo -e "${GREEN}✓ Template customization complete!${NC}"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo "  1. Review the changes in your editor"
echo "  2. Update README.md with your project details"
echo "  3. Configure Keycloak (see TEMPLATE_SETUP.md)"
echo "  4. Update .env.example with your credentials"
echo "  5. Run: mvn clean package"
echo "  6. Start infrastructure: docker-compose up -d"
echo ""
echo -e "${BLUE}Backup created at: $BACKUP_DIR${NC}"
echo -e "${YELLOW}You can safely delete the backup once you verify everything works.${NC}"
echo ""
echo -e "${GREEN}Happy coding! 🚀${NC}"
