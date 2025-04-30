# Build stage
FROM eclipse-temurin:17-jdk-alpine as builder

WORKDIR /app
COPY target/*.jar app.jar

# Run stage using slim runtime
FROM eclipse-temurin:17-jdk-alpine

# Create log directory and set work dir
WORKDIR /app
RUN mkdir /logs && chmod -R 777 /logs && touch /logs/.keep
VOLUME /logs

COPY --from=builder /app/app.jar .

ENTRYPOINT ["java", "-jar", "/app/app.jar"]