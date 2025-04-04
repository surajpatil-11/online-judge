#FROM ubuntu:latest

FROM maven:3.9-amazoncorretto-17

#RUN mkdir /app

WORKDIR /app

# Copy the pom.xml and source code
COPY ./pom.xml /app/pom.xml
COPY ./src /app/src/

# RUN cd app
# Use sed to ensure Unix line endings and set permissions
RUN sed -i 's/\r$//' /app/pom.xml && \
    chmod 644 /app/pom.xml

# Build the application
RUN mvn clean package -DskipTests

CMD ["java", "-jar", "target/execution-service-0.0.1-SNAPSHOT.jar"]