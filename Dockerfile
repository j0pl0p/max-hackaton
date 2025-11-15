FROM gradle:8.5-jdk17 AS build

WORKDIR /app

COPY . .

RUN gradle :app:build --no-daemon -x test

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
