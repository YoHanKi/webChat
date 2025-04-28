# 1) Build stage: Gradle으로 fat-jar 생성
FROM gradle:7.5.1-jdk17 AS builder
WORKDIR /home/gradle/project
# 멀티모듈 루트 빌드를 위해 settings.gradle, build.gradle, gradle wrapper, api 디렉터리 복사
COPY settings.gradle settings.gradle
COPY build.gradle build.gradle
COPY gradlew gradlew
COPY gradle gradle
COPY api api
RUN chmod +x gradlew \
    && ./gradlew :api:bootJar --no-daemon

# 2) Runtime stage: 가벼운 JRE 기반 이미지
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
EXPOSE 8080
# builder에서 생성된 jar 복사
COPY --from=builder /home/gradle/project/api/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]