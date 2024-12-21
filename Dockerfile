FROM azul/zulu-openjdk-alpine:23-jre
WORKDIR /app

COPY build/libs/app.jar app.jar
COPY build/agent/opentelemetry-javaagent.jar opentelemetry-javaagent.jar

EXPOSE 8000
ENTRYPOINT java -jar -javaagent:opentelemetry-javaagent.jar app.jar
