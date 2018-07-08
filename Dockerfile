FROM maven:3.5.4-jdk-10-slim as jar

WORKDIR /opt/app

COPY pom.xml .

RUN mvn install

COPY . .

RUN mvn clean compile package


FROM openjdk:10-jdk-slim

COPY --from=jar /opt/app/target/sniproxy-1.0-SNAPSHOT-jar-with-dependencies.jar /opt/app/sniproxy.jar

CMD ["java", "-jar", "/opt/app/sniproxy.jar"]