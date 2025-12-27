# ========================================
# Stage 1: Build
# ========================================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Gradle wrapper 및 빌드 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사
COPY src src

# Gradle 실행 권한 부여
RUN chmod +x gradlew

# 빌드 실행 (테스트 스킵)
RUN ./gradlew build -x test --no-daemon

# ========================================
# Stage 2: Runtime
# ========================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
