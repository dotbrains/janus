# Multi-stage build for Spring Boot application

# Stage 1: Build stage (already built by workflow, so we just copy the artifact)
FROM eclipse-temurin:21-jre-alpine AS runtime

# Set working directory
WORKDIR /app

# Create non-root user for running the application
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the built JAR file from target directory
COPY target/*.jar app.jar

# Change ownership to non-root user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose default Spring Boot port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
