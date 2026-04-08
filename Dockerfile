# =============================================
# 1단계: Gradle 빌드
# =============================================
FROM --platform=linux/amd64 gradle:8.5-jdk17-alpine AS build
WORKDIR /app

# 의존성 캐시를 위해 gradle 파일 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

# 소스 복사 후 JAR 빌드 (테스트 제외)
COPY src ./src
RUN gradle bootJar --no-daemon -x test

# =============================================
# 2단계: 실행 이미지 (경량)
# =============================================
FROM --platform=linux/amd64 amazoncorretto:17-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]