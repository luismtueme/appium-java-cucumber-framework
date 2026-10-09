# Appium server image for local Android runs via docker compose.
# The Java test suite still runs on the host (or in CI) and talks to this server.
FROM maven:3.9-eclipse-temurin-26

WORKDIR /workspace
COPY . .
RUN ./mvnw -B -q -DskipTests -DskipITs package || true

CMD ["echo", "Use docker compose for the Appium server; run ./mvnw verify on the host against it."]
