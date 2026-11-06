# syntax=docker/dockerfile:1

# Stage 1: build. As camadas estão ordenadas da que muda MENOS para a que muda MAIS, para o Docker
# reaproveitar cache: mudar o código em src/ não baixa as dependências de novo.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1) Dependências: só refaz se o pom.xml mudar. O cache do ~/.m2 também persiste entre builds
#    (mesmo quando o pom muda, só o que for novo é baixado).
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

# 2) Código: muda quase sempre, mas aqui só compila.
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o JAR compilado do stage anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta
EXPOSE 8080

# UTC explícito: instantes são gravados em UTC; o "dia" do negócio vem de America/Sao_Paulo no código.
ENTRYPOINT ["java", "-Duser.timezone=UTC", "-jar", "app.jar"]
