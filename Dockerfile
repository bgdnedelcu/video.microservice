FROM openjdk:19
WORKDIR /usr/app/
COPY video*.jar app.jar
CMD ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]