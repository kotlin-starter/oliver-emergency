# ---------- Build stage ----------
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

# Cache Gradle wrapper and dependencies first
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

# Prepare wrapper permissions
RUN chmod +x gradlew

# Copy source
COPY src src

# Build distribution (creates build/install/emergency)
RUN ./gradlew --no-daemon clean installDist

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

ENV PORT=8080 \
    JAVA_OPTS="-Xms256m -Xmx512m" \
    DEVELOPMENT=false \
    RELOAD=false

WORKDIR /app

# Copy the installDist output
COPY --from=builder /workspace/build/install/emergency /app

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --retries=12 CMD curl -f http://127.0.0.1:${PORT}/health || exit 1

ENTRYPOINT ["/app/bin/emergency"]


