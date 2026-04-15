FROM ubuntu:latest

WORKDIR /app

COPY . .

RUN apt update 
RUN apt install -y openjdk-17-jdk maven 

RUN mvn clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/app.jar"]