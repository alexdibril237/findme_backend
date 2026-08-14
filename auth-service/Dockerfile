# Build depuis la racine du repo :
#   docker build -f auth-service/Dockerfile -t findme/auth-service .
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY pom.xml .
COPY auth-service/pom.xml auth-service/pom.xml
COPY address-service/pom.xml address-service/pom.xml
COPY admin-service/pom.xml admin-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY .mvn .mvn
COPY mvnw .

RUN --mount=type=cache,target=/root/.m2/repository chmod +x mvnw && ./mvnw -q -pl auth-service -am dependency:go-offline

COPY auth-service auth-service
RUN --mount=type=cache,target=/root/.m2/repository ./mvnw -q -pl auth-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/auth-service/target/auth-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]