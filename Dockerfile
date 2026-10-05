# ---------- build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp clean package -Ddependency-check.skip=true \
 && cp "$(ls -S target/*.jar | head -1)" /src/app.jar   # largest jar = the fat/shaded jar

# ---------- runtime ----------
FROM eclipse-temurin:21-jre
RUN groupadd -r app && useradd -r -g app app \
 && mkdir -p /app /certs && chown app:app /app /certs
# AWS RDS CA bundle (Postgres connections use sslmode=require + this trust store)
ADD --chmod=644 https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem /certs/rds-global-bundle.pem
COPY --from=build /src/app.jar /app/app.jar
COPY --chmod=755 hearth-entrypoint.sh /usr/local/bin/hearth-entrypoint.sh
USER app
WORKDIR /app
EXPOSE 8443
ENTRYPOINT ["/usr/local/bin/hearth-entrypoint.sh"]
CMD ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
