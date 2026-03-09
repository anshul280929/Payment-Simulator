FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY common-lib/pom.xml /common-lib/pom.xml
COPY common-lib/src /common-lib/src
WORKDIR /common-lib
RUN apt-get update && apt-get install -y maven && mvn clean install -DskipTests

WORKDIR /app
COPY payment-gateway-service/pom.xml .
COPY payment-gateway-service/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
