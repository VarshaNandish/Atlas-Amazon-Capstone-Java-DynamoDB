# build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# runtime stage (smaller)
FROM eclipse-temurin:17-jre
WORKDIR /app
# copy jar from build stage (adjust name if different)
COPY --from=build /app/target/atlas-capstone-1.0.0.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
