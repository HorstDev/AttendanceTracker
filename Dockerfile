FROM openjdk:21-jdk
ARG JAR_FILE=target/*.jar
COPY ./target/attendancetracker-0.0.1-SNAPSHOT.jar app.jar
# Копируем локальный cacerts
COPY ./config/cacerts "$JAVA_HOME/lib/security/cacerts"
ENTRYPOINT ["java", "-jar", "/app.jar"]