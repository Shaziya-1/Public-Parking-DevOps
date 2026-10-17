# =============================================================================
# Dockerfile - Public Parking Availability Dashboard
# Multi-stage build: build with Maven, run with JRE only
# Student: Sajiya Shaikh | Roll: 24102A2005
# =============================================================================

# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

LABEL stage="builder"

WORKDIR /build

# Copy Maven wrapper and POM first (for layer caching)
COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd pom.xml ./

# Fix potential Windows CRLF line endings on checkout and make mvnw executable
RUN sed -i 's/\r$//' ./mvnw && chmod +x ./mvnw

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B -q

# Copy source code
COPY src/ ./src/

# Build the application JAR (skip tests in image build; tests are run in CI pipeline)
RUN ./mvnw package -DskipTests -B -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="Sajiya Shaikh <sajiya@example.com>"
LABEL description="Public Parking Availability Dashboard - Academic Project"
LABEL version="1.0.0"

# Create a non-root user for security
RUN addgroup -S parking && adduser -S parking -G parking

# Create application directories
RUN mkdir -p /app/data /app/logs && \
    chown -R parking:parking /app

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /build/target/public-parking-dashboard-*.jar app.jar

# Adjust permissions
RUN chown parking:parking app.jar

# Run as non-root user
USER parking

# Expose the application port
EXPOSE 8080

# Data persistence volume
VOLUME ["/app/data"]

# JVM options for container-friendly memory management
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport"
ENV APP_PORT=8080
ENV LOW_AVAILABILITY_THRESHOLD=85
ENV STALE_THRESHOLD_MINUTES=60

# Health check using the Spring Actuator endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=$APP_PORT"]
