FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY src/backend/pom.xml .
RUN mvn dependency:go-offline
COPY src/backend/src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/idp-backend-0.1.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
