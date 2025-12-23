# Build stage
FROM eclipse-temurin:24-jdk-alpine AS build
WORKDIR /app

# Install Gradle
RUN apk add --no-cache gradle libgcc

# Copy gradle files
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Copy source code
COPY src ./src

# Build application
RUN gradle build --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app

# Install runtime deps (required by netty-quiche)
RUN apk add --no-cache libgcc

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]