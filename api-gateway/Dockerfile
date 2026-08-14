# Build depuis la racine du repo :
#   docker build -f api-gateway/Dockerfile -t findme/api-gateway .
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY pom.xml .
COPY auth-service/pom.xml auth-service/pom.xml
COPY address-service/pom.xml address-service/pom.xml
COPY admin-service/pom.xml admin-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY .mvn .mvn
COPY mvnw .

RUN --mount=type=cache,target=/root/.m2 chmod +x mvnw && ./mvnw -q -pl api-gateway -am dependency:go-offline

COPY api-gateway api-gateway
RUN --mount=type=cache,target=/root/.m2 ./mvnw -q -pl api-gateway -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/api-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]