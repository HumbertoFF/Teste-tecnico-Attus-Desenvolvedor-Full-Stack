# --- Etapa 1: build ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Baixa dependências antes de copiar o código (aproveita cache do Docker)
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# --- Etapa 2: runtime ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuário não-root por segurança
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Cria o diretório de uploads e dá ownership ao appuser ANTES de trocar de usuário
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app/uploads

USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
