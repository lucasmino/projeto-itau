# Build stage (se você compila dentro do container)
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -e -DskipTests package

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /opt/app
# timezone opcional
ENV TZ=America/Sao_Paulo
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]


