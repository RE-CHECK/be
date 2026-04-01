# Build stage (소스코드를 컴파일하고 JAR을 만드는 단계)
FROM eclipse-temurin:21-jdk AS build

# 작업 위치
WORKDIR /app

# Gradle Wrapper 및 의존성 파일 먼저 복사 (캐싱 레이어 사용)
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

# 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성 다운로드 (build.gradle 변경 없으면 캐시 레이어 사용)
RUN ./gradlew dependencies --no-daemon

# 소스코드 복사 (변경 시 여기부터 재실행)
COPY src src

# 프로젝트 빌드 (빌드는 일회성이므로 메모리 점유를 발생시키는 gradle 데몬 미사용)
RUN ./gradlew bootJar -x test --no-daemon

# Run stage (빌드된 JAR을 실행만 하는 단계)
FROM eclipse-temurin:21-jre

# 런타임 컨테이너 작업 위치
WORKDIR /app

# 빌드 스테이지에서 생성된 JAR파일만 런타임 이미지로 가져옴
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 문서화
EXPOSE 8080

# 컨테이너 시작 시 실행할 명령어 (Spring Boot 어플리케이션 실행)
ENTRYPOINT ["java", "-jar", "app.jar"]