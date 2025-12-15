# ---------- build stage ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Копируем gradle wrapper и конфиги
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts ./
COPY settings.gradle.kts ./

# Чиним права и окончания строк
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Прогрев зависимостей
RUN ./gradlew --version
RUN ./gradlew dependencies --no-daemon

# Копируем исходники
COPY src src

# Собираем fat jar
RUN ./gradlew buildFatJar --no-daemon

# ---------- runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Копируем jar и wait-for-it
COPY --from=build /app/build/libs/*-all.jar app.jar
COPY wait-for-it.sh wait-for-it.sh
RUN chmod +x wait-for-it.sh

EXPOSE 8080

# Ждём готовности базы перед запуском сервера
CMD ["./wait-for-it.sh", "db:5432", "--", "java", "-jar", "app.jar"]
