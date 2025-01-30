# Gout Together Backend

This project is for learning Java, Spring Boot
from [Java Backend Developer Boot Camp \[2024\]](https://www.youtube.com/playlist?list=PLm3A9eDaMzukMQtdDoeOR-HbFN35vieQY)
BY [TP Coder](https://www.youtube.com/@tpcoder)

Course Code Example: <https://github.com/marttp/20240629-GoutTogether>

## API Usage

Download Postman Collection from the [Gout-Together.postman_collection.json](https://raw.githubusercontent.com/fResult/Gout-Together/refs/heads/main/Gout-Together.postman_collection.json) file, then import to your [Postman](https://www.postman.com/downloads).

```bash
# Download the Postman Collection to current (working) directory
curl -O https://raw.githubusercontent.com/fResult/Gout-Together/refs/heads/main/Gout-Together.postman_collection.json

# Download the Postman Collection to your specific directory
curl -o /path/to/your/target/directory/Gout-Together.postman_collection.json https://raw.githubusercontent.com/fResult/Gout-Together/refs/heads/main/Gout-Together.postman_collection.json
```

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

#### Assign Public/Private Key for Spring Security Config

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
- Spring Security implementation
- Unit Testing with JUnit 5 and Mockito
- Configuring authorization with OAuth2ResourceServer
- Integration Testing
- Pessimistic Locking for data consistency
- Implementing idempotency keys to prevent duplicate transaction processing

### Things I did different and learned further

- Used the Gradle Kotlin instead of the Gradle Groovy
- PasswordEncoder: using Argon2PasswordEncoder instead of BCryptPasswordEncoder
- Added more assertion, including asserting error message in Unit Tests
- Resolved circular dependency using `@Lazy` annotation
- Implemented [Virtual Thread](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html) and `CompletableFuture` for concurrent database operations
    - See: [WalletServiceImpl.getConsumerAndTourCompanyWallets()](https://github.com/fResult/Gout-Together/blob/f4ef3ffebc4bee2ad9919b78996cfa1c659e484f/src/main/java/dev/fResult/goutTogether/wallets/services/WalletServiceImpl.java#L202-L224)
- Handled complex exception propagation in multi-threaded scenarios
    - Implemented in [ErrorHelper.throwMatchedException()](https://github.com/fResult/Gout-Together/blob/f4ef3ffebc4bee2ad9919b78996cfa1c659e484f/src/main/java/dev/fResult/goutTogether/common/helpers/ErrorHelper.java#L24-L38)
    - Applied in [service layer](https://github.com/fResult/Gout-Together/blob/f4ef3ffebc4bee2ad9919b78996cfa1c659e484f/src/main/java/dev/fResult/goutTogether/wallets/services/WalletServiceImpl.java#L225-L227)
- Created custom application properties
    - Implemented in: [MyApplicationProperties.java](https://github.com/fResult/Gout-Together/blob/f4ef3ffebc4bee2ad9919b78996cfa1c659e484f/src/main/java/dev/fResult/goutTogether/common/configs/MyApplicationProperties.java)
    - Applied in [SecurityConfig.java](https://github.com/fResult/Gout-Together/blob/34161612b0706213de18d278ebc5c15681c4b324/src/main/java/dev/fResult/goutTogether/common/configs/SecurityConfig.java#L52-L56)

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
