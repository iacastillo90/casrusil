# Etapa 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
# Copiar wrappers y configuración
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .
# Otorgar permisos al wrapper y descargar dependencias
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# Copiar el código fuente y compilar
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Exponer el puerto
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]
