# ========================================
# Stage 1: Build
# ========================================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Gradle wrapper 및 빌드 파일 복사
COPY gradlew build.gradle settings.gradle ./
COPY gradle gradle

# Gradle 실행 권한 부여 및 의존성 캐시
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드 (테스트 스킵)
COPY src src
RUN ./gradlew build -x test --no-daemon

# ========================================
# Stage 2: Runtime
# ========================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# JAR 파일 복사 (⚠️ plain.jar 제외)
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar

# 포트 노출
EXPOSE 8080

# Health check (wget 사용 - jre 이미지에 포함)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --spider -q http://localhost:8080/api/actuator/health || exit 1

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
