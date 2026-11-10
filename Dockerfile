FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21.0.5_11-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build --chown=app:app /build/target/ms-inventario-0.0.1-SNAPSHOT.jar app.jar
USER app
EXPOSE 8200
ENTRYPOINT ["java", "-jar", "app.jar"]
