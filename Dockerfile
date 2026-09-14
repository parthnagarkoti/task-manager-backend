# ── Build stage ─────────────────────────────────────────────
# Uses a full JDK (needed to compile) — discarded after the build.
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy only what's needed to resolve dependencies first, so Docker
# can cache this layer and skip re-downloading on source-only changes.
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ── Runtime stage ───────────────────────────────────────────
# JRE only (no compiler, no build tools) — this is what actually ships.
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
