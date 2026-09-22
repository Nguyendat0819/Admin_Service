# ============================================================
#  Multi-stage Dockerfile cho Spring Boot 4.x (Java 17)
#  Build:   docker build -t java_template:latest .
#  Run:     docker run -p 8080:8080 --env-file .env java_template:latest
# ============================================================

# ---------- Stage 1: Build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Cache layer: chỉ copy pom trước để tận dụng Docker layer cache
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

# Copy source và build (bỏ qua test để build nhanh; nếu cần test thì bỏ "-DskipTests")
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Tạo user non-root để chạy app (bảo mật)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar đã build từ stage trước
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

# Healthcheck dùng actuator (đã có sẵn trong image)
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
