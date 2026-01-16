#!/bin/bash

set -e

echo "🚪 Starting Janus - Federated Authentication Service"
echo "=================================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker Desktop and try again."
    exit 1
fi

echo "✅ Docker is running"

# Start infrastructure services
echo "🐘 Starting PostgreSQL and Keycloak..."
docker-compose up -d

echo "⏳ Waiting for services to be healthy..."
echo "   This may take up to 60 seconds for Keycloak to start..."

# Wait for PostgreSQL
until docker-compose exec -T postgres pg_isready -U janus > /dev/null 2>&1; do
    echo "   Waiting for PostgreSQL..."
    sleep 2
done
echo "✅ PostgreSQL is ready"

# Wait for Keycloak
echo "   Waiting for Keycloak (this takes longer)..."
sleep 30

echo ""
echo "🎉 Infrastructure services are running!"
echo ""
echo "Next steps:"
echo "1. Configure Keycloak:"
echo "   - Access: http://localhost:8080"
echo "   - Login: admin / admin"
echo "   - Create realm: 'janus'"
echo "   - Create client: 'janus-client'"
echo ""
echo "2. Update application.yml with your Keycloak client secret"
echo ""
echo "3. Start the application:"
echo "   mvn spring-boot:run -Dspring-boot.run.profiles=dev"
echo ""
echo "📚 See README.md for detailed setup instructions"
