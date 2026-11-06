FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -Dtest=!StartupTests package
FROM eclipse-temurin:21.0.10_7-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java","-Duser.timezone=UTC","-jar","app.jar"]
