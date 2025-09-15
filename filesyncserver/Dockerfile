# Use a specific Java version for reproducibility
FROM eclipse-temurin:21-jre-alpine

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Set working directory
WORKDIR /app

# Copy the built JAR file
COPY target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Set environment variables with defaults
ENV SPRING_PROFILES_ACTIVE=production
ENV SERVER_PORT=8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]