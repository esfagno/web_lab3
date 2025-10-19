FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM quay.io/wildfly/wildfly:31.0.0.Final-jdk17

RUN curl -fL https://jdbc.postgresql.org/download/postgresql-42.7.3.jar -o /opt/jboss/wildfly/standalone/deployments/postgresql-42.7.3.jar

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --commands="\
    embed-server --std-out=echo,\
    data-source add --name=JSFLabDS \
      --jndi-name=java:jboss/datasources/JSFLabDS \
      --driver-name=postgresql-42.7.3.jar \
      --connection-url=jdbc:postgresql://db:5432/jsf_lab \
      --user-name=jsf_user \
      --password=jsf_pass,\
    stop-embedded-server"

COPY --from=builder /app/target/*.war /opt/jboss/wildfly/standalone/deployments/jsf-lab.war

EXPOSE 8080
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]