# Gout Together Backend

This project is for learning Java, Spring Boot
from [Java Backend Developer Boot Camp \[2024\]](https://www.youtube.com/playlist?list=PLm3A9eDaMzukMQtdDoeOR-HbFN35vieQY)
BY [TP Coder](https://www.youtube.com/@tpcoder)

Course Code Example: <https://github.com/marttp/20240629-GoutTogether>

## Related commands

### Build Jar and Get the OpenTelemetry Agent

```bash
./gradlew clean build
```

### Generate RSA Keypair

```shell
openssl genrsa -out private_key.pem 4096
openssl rsa -pubout -in private_key.pem -out public_key.pem
openssl pkcs8 -topk8 -in private_key.pem -inform pem -out private_key_pkcs8.pem -outform pem -nocrypt
```

### To Assign Public/Private Key for Spring Security Config

```shell
base64 -i src/main/resources/private_key_pkcs8.pem # Then put the Base64 text to the `OAUTH_PRIVATE_KEY_BASE64` env var
base64 -i src/main/resources/public_key.pem # Then put the Base64 text to the `OAUTH_PUBLIC_KEY_BASE64` env var
```

### Start the application

#### In development mode with OpenTelemetry Java Agent

As we already declared the required environment variables in the `.env` file, we can start application with the following
command.

```bash
./gradlew bootRun
```

Since this is for learning purpose, the environment variables in the `.env` file are:

```env
JAVA_TOOL_OPTIONS="-javaagent:build/agent/opentelemetry-javaagent.jar"
OTEL_SERVICE_NAME=gout
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
OTEL_EXPORTER_OTLP_PROTOCOL=grpc
OTEL_RESOURCE_ATTRIBUTES="service.name=gout,service.instance.id=gout,env=dev"
# Logs are disabled by default
OTEL_LOGS_EXPORTER=otlp
OTEL_METRIC_EXPORT_INTERVAL=500
OTEL_BSP_SCHEDULE_DELAY=500

```

#### In Build mode with OpenTelemetry Java Agent

```bash
java -javaagent:build/agent/opentelemetry-javaagent.jar -jar build/libs/app.jar
```

### Visit to Grafana on local machine

- <http://localhost:3000>

## My Summary

### Things I learned from attending the course

- OpenTelemetry Java Agent
- Spring Security
- Unit Testing with JUnit 5 and Mockito
- Config Authorization with OAuth2ResourceServer
- Integration Testing

### Things I did different

- Using the Gradle Kotlin instead of the Gradle Groovy
- PasswordEncoder, using Argon2PasswordEncoder instead of BCryptPasswordEncoder
- Add more assertion, assert the error message
- Use Virtual Thread for concurrency fetching independently data from database
