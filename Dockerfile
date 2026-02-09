# ========================================
# Stage 1: Build
# ========================================
FROM gradle:8.11.1-jdk21 AS builder

WORKDIR /home/gradle/project

# 빌드 파일 복사 및 의존성 캐시
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

# 소스 코드 복사 및 빌드 (테스트 스킵)
COPY src src
RUN gradle build -x test --no-daemon

# ========================================
# Stage 2: Runtime
# ========================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# JAR 파일 복사 (⚠️ plain.jar 제외)
ARG JAR_FILE=build/libs/*.jar
COPY --from=builder /home/gradle/project/${JAR_FILE} app.jar

# 포트 노출
EXPOSE 8080

# Health check (wget 사용 - jre 이미지에 포함)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --spider -q http://localhost:8080/api/actuator/health || exit 1

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
