FROM ubuntu:18.04

RUN apt-get update --fix-missing
RUN apt-get upgrade -y
RUN apt-get install openjdk-11-jdk -y

COPY .mvn .mvn
COPY mvnw mvnw
COPY src src
COPY pom.xml pom.xml
RUN ./mvnw package
RUN mv target/server-0.0.1-SNAPSHOT.jar server.jar
RUN rm -rf target src .mvn mnvw pom.xml

EXPOSE 8080

CMD java -jar server.jar $ID