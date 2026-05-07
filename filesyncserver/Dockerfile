### Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

# Copy Maven wrapper + pom first for better layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN ./mvnw -DskipTests dependency:go-offline

COPY src/ src/

RUN ./mvnw -DskipTests package \
	&& JAR_FILE=$(ls -1 target/*.jar | grep -v '\\.original\\.jar$' | head -n 1) \
	&& test -n "$JAR_FILE" \
	&& cp "$JAR_FILE" /workspace/app.jar

### Runtime stage
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

COPY --from=builder /workspace/app.jar /app/app.jar

EXPOSE 8080

ENV SERVER_PORT=8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]