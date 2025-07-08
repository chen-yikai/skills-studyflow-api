# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy gradle files for dependency caching
COPY gradle/ gradle/
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./

# Make gradlew executable
RUN chmod +x gradlew

# Create a dummy source structure and build to cache dependencies
RUN mkdir -p src/main/kotlin src/main/resources src/test/kotlin
RUN echo 'fun main() {}' > src/main/kotlin/Main.kt
RUN ./gradlew build --no-daemon -x test || true
RUN rm -rf src/

# Copy actual source code
COPY src/ src/

# Build the application
RUN ./gradlew build --no-daemon -x test

# Create directories for uploaded files and database
RUN mkdir -p /app/records /app/data

# Expose port 8080
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=docker

# Run the application
CMD ["java", "-jar", "build/libs/skills-studyflow-api-0.0.1-SNAPSHOT.jar"]
