# Build stage (uses official Maven image with JDK)
FROM maven:3.9.9-amazoncorretto-21 AS build
WORKDIR /app

# Copy project files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src ./src

# Build the application
RUN mvn -B -DskipTests package

# Final stage (runtime)
FROM eclipse-temurin:21-jdk AS runtime
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
