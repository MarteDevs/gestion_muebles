FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests=true clean package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/app.jar
EXPOSE 8080
ENV JAVA_OPTS=
CMD ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
