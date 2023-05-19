FROM openjdk:19
WORKDIR /usr/app/
COPY user*.jar app.jar
CMD ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]