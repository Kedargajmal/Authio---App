# Step 1: Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the entire repository into the container
COPY . .

# Move into the nested project folder and build the JAR
# Note: Ensure "authify" matches your folder name case exactly
RUN cd authify && mvn clean package -DskipTests

# Step 2: Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the JAR from the build stage's nested target folder
COPY --from=build /app/authify/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]