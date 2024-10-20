# Stage 1: Build the application using JDK to extract layers
FROM eclipse-temurin:17-jre as builder
WORKDIR application

# Copy the built JAR into the workspace
COPY ./build/libs/*.jar app.jar

# Extract the JAR layers using layertools (requires JDK)
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 2: Create the final Docker image
FROM eclipse-temurin:17-jre
WORKDIR application

# Copy the extracted layers from the builder stage
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./

EXPOSE 8080

# Use Spring Boot's JarLauncher without explicitly setting the classpath
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]