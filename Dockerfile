# ---------- Build stage ----------
FROM gradle:8.8-jdk23 AS build
WORKDIR /app
COPY . .
RUN gradle --no-daemon clean bootJar

# ---------- Runtime stage ----------
FROM eclipse-temurin:23-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
