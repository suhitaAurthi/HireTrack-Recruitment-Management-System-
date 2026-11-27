# ==============================================================================
# Multi-Stage Dockerfile for HireTrack (Spring Boot 3.3 + Java 21)
# Stage 1: Build & Package JAR with Maven
# Stage 2: Minimal, secure JRE Runtime (~180MB)
# ==============================================================================

# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy Maven wrapper and POM first to leverage Docker layer caching for dependencies
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Download dependencies offline (cached layer unless pom.xml changes)
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B || true

# Copy application source code
COPY src src

# Package production executable JAR without running tests (tests already passed in CI)
RUN ./mvnw clean package -DskipTests -B

# Extract fat JAR layers for optimal runtime caching
RUN java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# Stage 2: Lightweight runtime stage
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Add non-root system group and user for security compliance
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create directory for persistent CV resume uploads and set ownership
RUN mkdir -p /app/uploads/resumes && chown -R appuser:appgroup /app/uploads

# Copy layered Spring Boot application files from builder stage
COPY --from=builder --chown=appuser:appgroup /workspace/target/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/target/extracted/application/ ./

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "org.springframework.boot.loader.launch.JarLauncher"]
