# ---- build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first (caching optimization)
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn -DskipTests clean package

# ---- runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copy with explicit name to avoid wildcard issues
COPY --from=build /app/target/*.jar app.jar
EXPOSE 5000
# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:5000/msp/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]