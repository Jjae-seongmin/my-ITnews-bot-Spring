# 1단계: Gradle로 빌드
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY src .
RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test

# 2단계: 빌드 결과물만 가지고 실제 실행 이미지 생성
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Render가 지정하는 포트를 사용하도록 설정
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]