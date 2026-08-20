# ─────────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

# Copy parent pom first to resolve the module list
COPY pom.xml .

# Copy each module's pom so Maven can download dependencies without source
COPY gwent-engine/pom.xml gwent-engine/
COPY gwent-api/pom.xml    gwent-api/

# Download all dependencies (cached layer — only re-runs when a pom changes)
RUN mvn dependency:go-offline -B --no-transfer-progress

# Copy source
COPY gwent-engine/src gwent-engine/src
COPY gwent-api/src    gwent-api/src

# Build — tests are skipped here; they run in CI before the image is built
RUN mvn package -DskipTests -B --no-transfer-progress

# ─────────────────────────────────────────────
# Stage 2: Runtime
# ─────────────────────────────────────────────
FROM eclipse-temurin:25-jre-jammy

WORKDIR /app

COPY --from=build /app/gwent-api/target/gwent-api-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
