.PHONY: help start stop restart logs clean build run test docker-up docker-down

help:
	@echo "Janus - Federated Authentication Service"
	@echo "========================================"
	@echo ""
	@echo "Available commands:"
	@echo "  make start       - Start infrastructure (PostgreSQL, Keycloak)"
	@echo "  make stop        - Stop infrastructure services"
	@echo "  make restart     - Restart infrastructure services"
	@echo "  make logs        - View infrastructure logs"
	@echo "  make clean       - Clean Maven build artifacts"
	@echo "  make build       - Build the application"
	@echo "  make run         - Run the application with dev profile"
	@echo "  make test        - Run tests"
	@echo "  make docker-up   - Start infrastructure (same as start)"
	@echo "  make docker-down - Stop and remove infrastructure"

start: docker-up

docker-up:
	@echo "🚀 Starting infrastructure services..."
	docker-compose up -d
	@echo "✅ Services started. Waiting for health checks..."
	@echo "   PostgreSQL: localhost:5432"
	@echo "   Keycloak: http://localhost:8080"

stop:
	@echo "🛑 Stopping infrastructure services..."
	docker-compose stop

docker-down:
	@echo "🧹 Stopping and removing infrastructure services..."
	docker-compose down

restart:
	@echo "🔄 Restarting infrastructure services..."
	docker-compose restart

logs:
	docker-compose logs -f

clean:
	@echo "🧹 Cleaning build artifacts..."
	mvn clean

build:
	@echo "🔨 Building application..."
	mvn clean package -DskipTests

run:
	@echo "🚀 Running Janus application..."
	mvn spring-boot:run -Dspring-boot.run.profiles=dev

test:
	@echo "🧪 Running tests..."
	mvn test
