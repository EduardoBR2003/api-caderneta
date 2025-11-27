# Stage 1: Build da aplicação
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copia arquivos do Maven
COPY pom.xml .
COPY src ./src

# Instala Maven e compila o projeto
RUN apk add --no-cache maven
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o JAR compilado do stage anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta
EXPOSE 8080

# Comando para executar
ENTRYPOINT ["java", "-jar", "app.jar"]