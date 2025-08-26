# ---------- Build stage (JDK 23 + Gradle Wrapper) ----------
FROM eclipse-temurin:23-jdk AS build
WORKDIR /app

# 캐시 적중을 위해 설정/래퍼 먼저 복사
COPY gradlew gradlew.bat settings.gradle build.gradle gradle/ ./
RUN chmod +x gradlew

# wrapper 파일을 명시적으로 복사 (dockerignore 우회)
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.jar
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.properties

# 소스 복사
COPY src ./src

# 빌드
RUN ./gradlew --no-daemon clean bootJar

# ---------- Runtime stage (JRE 23) ----------
FROM eclipse-temurin:23-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
