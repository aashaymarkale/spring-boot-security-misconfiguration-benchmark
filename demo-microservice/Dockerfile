FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/demo-microservice-1.0.0.jar app.jar
# The deployed container always runs the prod profile.
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
