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

### Environment Variables

#### Add Environment File

```bash
cp .envTemplate .env
```

#### Generate RSA Keypair

```shell
openssl genrsa -out src/main/resources/private_key.pem 4096
openssl rsa -pubout -in src/main/resources/private_key.pem -out src/main/resources/public_key.pem
openssl pkcs8 -topk8 -in src/main/resources/private_key.pem -inform pem -out src/main/resources/private_key_pkcs8.pem -outform pem -nocrypt
```

#### To Assign Public/Private Key for Spring Security Config

```shell
base64 -i src/main/resources/private_key_pkcs8.pem # Then put the Base64 text to the `OAUTH_PRIVATE_KEY_BASE64` env var
base64 -i src/main/resources/public_key.pem # Then put the Base64 text to the `OAUTH_PUBLIC_KEY_BASE64` env var
```

### Start the application

#### In development mode with OpenTelemetry Java Agent

As we already declared the required environment variables in the `.env` file, we can start application with the
following
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
- Pessimistic Locking to handle data consistency

### Things I did different and learned further

- Using the Gradle Kotlin instead of the Gradle Groovy
- PasswordEncoder, using Argon2PasswordEncoder instead of BCryptPasswordEncoder
- Adding more assertion, assert the error message
- Solving Circular Dependency by using `@Lazy` annotation
- Using [Virtual Thread](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html) and Future for concurrency fetching independent data from database
- Rethrowing exception cause by multiple threads could be tricky, need to be careful
  - See:
    - [ErrorHelper.throwMatchedException()](https://github.com/fResult/Gout-Together/blob/5a70c5e884b0ed8575fadb9c5280662966581a0d/src/main/java/dev/fResult/goutTogether/common/helpers/ErrorHelper.java#L20-L34)
    - [Utilized in the service](https://github.com/fResult/Gout-Together/blob/5a70c5e884b0ed8575fadb9c5280662966581a0d/src/main/java/dev/fResult/goutTogether/wallets/services/WalletServiceImpl.java#L225-L227)
- Create a custom application properties
  - See: [MyApplicationProperties.java](https://github.com/fResult/Gout-Together/blob/e6f5113f95b57f6c1c51a8113b2ce53a343b35b1/src/main/java/dev/fResult/goutTogether/common/configs/MyApplicationProperties.java)

### Test Coverage
![Image to display percentage of code coverage](https://github.com/user-attachments/assets/ef5c0b2d-3fd4-4af7-804d-7819b5c4b1b8)

<footer>
  <div align=center>
    <br><br>.<br><br>.<br><br>.<br><br>.<br><br>.<br><br>.<br><br>.<br><br>.<br><br>.<br><br>.<br><br>.<br><br>.<br><br>.<br><br>.<br><br>
  </div>

  <p align=center>
    [This Space Intentionally Left Blank]
  </p>

  <p align=center>
    The bottom of every page is padded so readers can maintain a consistent eyeline.
  </p>
</footer>
