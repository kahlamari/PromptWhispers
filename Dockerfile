FROM --platform=linux/amd64 openjdk:21

EXPOSE 8080

ADD backend/target/promptwhispers-backend-0.0.1-SNAPSHOT.jar backend.jar

CMD ["sh", "-c", "java -jar /backend.jar"]