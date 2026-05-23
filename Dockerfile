FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Railway automatically sets the PORT environment variable
ENV PORT=8080
EXPOSE ${PORT}

# Explicitly pass the port to the Spring Boot application
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]
