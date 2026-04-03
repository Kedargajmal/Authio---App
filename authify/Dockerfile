# Step 1: Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy project files
COPY . .

# Build the jar
RUN mvn clean package -DskipTests

# Step 2: Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy built jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]