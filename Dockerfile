FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Testes unitários rodam no "package". Os de integração (*IT, Testcontainers) exigem Docker e rodam no CI
# com "mvn verify".
RUN mvn -B package
FROM eclipse-temurin:21.0.13_9-jre-alpine
# Correções de segurança do sistema publicadas depois da imagem base.
RUN apk upgrade --no-cache
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
USER 10001
EXPOSE 8080
# Heap proporcional à memória do container (o padrão da JVM usa só 25%); sem memória, encerra para a
# plataforma reiniciar em vez de seguir degradada.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["java","-Duser.timezone=UTC","-jar","app.jar"]
