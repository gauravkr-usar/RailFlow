# ==========================================
# RailFlow Backend - Multi-stage Production Dockerfile
# Stage 1: Build JAR using Maven
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy dependency definition to leverage Docker caching
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and package application
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Minimal Production JRE Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root system user for security
RUN addgroup -S railflow && adduser -S railflow -G railflow

# Copy compiled jar from builder stage
COPY --from=builder /build/target/railflow-*.jar app.jar

# Set file ownership
RUN chown -R railflow:railflow /app
USER railflow

# Environment flags for optimal container memory usage
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
