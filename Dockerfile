# -------- Stage 1: Build --------
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /build

# Copy pom and fetch dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the app (skip tests if you want faster builds)
RUN mvn clean package -DskipTests


# -------- Stage 2: Runtime --------
FROM eclipse-temurin:21-jdk

WORKDIR /gatekeeper

# Copy built JAR from stage 1
COPY --from=build /build/target/*.jar app.jar

# Copy and set up entrypoint
COPY entrypoint.sh ./entrypoint.sh
RUN chmod +x ./entrypoint.sh

# Create required folders
RUN mkdir -p /var/log/gatekeeper /var/log/gatekeeper/heapdump

# Expose port
EXPOSE 8080

# Healthcheck (optional)
# HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
#   CMD curl -f http://localhost:8080/actuator/health || exit 1

# Start the app
ENTRYPOINT ["./entrypoint.sh"]
