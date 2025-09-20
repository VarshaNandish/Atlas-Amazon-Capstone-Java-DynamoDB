# Stage 1: Build the jar with Maven
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# copy pom and source to leverage layer caching
COPY pom.xml ./
COPY src ./src

# build jar (assembly creates jar-with-dependencies)
RUN mvn -B -DskipTests package

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# copy the assembled jar from the build stage
# pattern matches the jar-with-dependencies produced by your assembly plugin
COPY --from=build /app/target/*-jar-with-dependencies.jar app.jar

# If your app needs environment vars or ports, expose here (console app needs none)
# EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
