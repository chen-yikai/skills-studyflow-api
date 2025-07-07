# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy gradle files for dependency caching
COPY gradle/ gradle/
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts build.gradle.kts ./

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (this layer will be cached unless build files change)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew build --no-daemon -x test

# Create directory for uploaded files
RUN mkdir -p /app/records

# Expose port 8080
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=docker

# Run the application
CMD ["java", "-jar", "build/libs/skills-studyflow-api-0.0.1-SNAPSHOT.jar"]
