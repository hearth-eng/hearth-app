# ---------- build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
# This project builds a thin jar (maven-jar-plugin, Class-Path: lib/) plus its
# dependencies copied to target/lib/ by maven-dependency-plugin's copy-dependencies
# goal - there is no shaded/fat jar. Both the jar and lib/ must ship together,
# keeping the same relative layout the manifest's Class-Path expects.
RUN mvn -B -ntp clean package -Ddependency-check.skip=true \
 && cp target/hearth-app-*.jar /src/app.jar \
 && cp -r target/lib /src/lib

# ---------- runtime ----------
FROM eclipse-temurin:21-jre
RUN groupadd -r app && useradd -r -g app app \
 && mkdir -p /app /certs && chown app:app /app /certs
# AWS RDS CA bundle (Postgres connections use sslmode=require + this trust store)
ADD --chmod=644 https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem /certs/rds-global-bundle.pem
COPY --from=build /src/app.jar /app/app.jar
COPY --from=build /src/lib /app/lib
COPY --chmod=755 hearth-entrypoint.sh /usr/local/bin/hearth-entrypoint.sh
USER app
WORKDIR /app
EXPOSE 8443
ENTRYPOINT ["/usr/local/bin/hearth-entrypoint.sh"]
CMD ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
