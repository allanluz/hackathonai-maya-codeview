# Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="Sinqia - Sistema MAYA"
LABEL version="1.0.0"
LABEL description="MAYA Code Review System Backend"

# Create app user
RUN addgroup -g 1001 -S maya && \
    adduser -u 1001 -S maya -G maya

# Install required packages
RUN apk add --no-cache curl

# Create necessary directories
RUN mkdir -p /var/log/maya && \
    chown -R maya:maya /var/log/maya

# Set working directory
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/maya-code-review-*.jar app.jar

# Change ownership
RUN chown -R maya:maya /app

# Switch to maya user
USER maya

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=docker

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
