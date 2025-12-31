FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace/app

# Copy gradle wrapper and config files
COPY gradle gradle
COPY build.gradle settings.gradle gradlew ./
COPY src src

# Make gradlew executable and build the application (skip tests for faster build)
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

# Stage 2: Create the minimal runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
