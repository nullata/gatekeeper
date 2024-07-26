# Gatekeeper Proxy Server

## Overview
Gatekeeper is a proxy server application that ensures secure and controlled access to backend services. It enforces rate limiting, API key validation, and environment validation before forwarding requests to a target URL.

## Features
- **Rate Limiting**: Controls the rate of incoming requests.
- **Caching**: Allows for validated values to be cached in app memory. On by default.
- **API Key Validation**: Ensures only requests with valid API keys are processed.
- **Environment Validation**: Verifies required environment variables are set before starting the application.

## Prerequisites
- Docker
- Docker Compose
- Maven

## Installation

1. Clone the repository:
    ```bash
    git clone https://github.com/nullata/gatekeeper.git
    cd gatekeeper
    ```

2. Build and deploy the application:
    ```bash
    ./build-install.sh
    ```

## Configuration
Configure the following environment variables before starting the application:

- `PROXY_TARGET_URL`: The URL to which requests will be forwarded.
- `DB_HOST`: Database host.
- `DB_PORT`: Database port.
- `DB_NAME`: Database name.
- `DB_USERNAME`: Database username.
- `DB_PASSWORD`: Database password.

### Database
This application supports a MariaDB database. The provided `ddl-gatekeeper-example.sql` file, the `ApiTokens` entity class and the `KeyFetcherService` serve as placeholders for developers to extend upon. These can be customized to suit specific application requirements.

### Additional configurations

- `RATE_LIMIT_ENABLED`: Optional. Determines whether the rate limiter is enabled. String: `"true"`|`"false"`. Default: `"false"`
- `RATE_LIMIT_RATE`: Rate limit threshold. Int value.
- `RATE_LIMIT_TIMEOUT`: Rate limit timeout period (seconds). Int value.

#### Volumes
You can attach a volume for application logs which in the container are stored in `/var/logs/gatekeeper`. Refer to `entrypoint.sh`


## Usage
Once the application has been built, start the application using Docker Compose:
```bash
docker-compose up
```

The application will be available at the configured port and will start proxying requests to the target URL after validating the environment and rate limits.

## HTTP Header for Validation

Remote clients need to submit the HTTP header `x-gate-key` for validation. This header contains the API key that the server will validate.

## Example Request with `x-gate-key` Header

```http
GET /some-protected-resource HTTP/1.1
Host: api.example.com
x-gate-key: abc123xyz456
```

### Breakdown
- `GET /some-protected-resource HTTP/1.1`: The HTTP method and resource path the client is requesting.
- `Host: api.example.com`: The host header specifying the server.
- `x-gate-key: abc123xyz456`: The custom header containing the API key `abc123xyz456` for validation.

In this example, the API key `abc123xyz456` is sent in the `x-gate-key` header. The server will use this key to validate the client's request based on the validation process described earlier. If the key is valid, the server will process the request; otherwise, it will return an `UNAUTHORIZED` response.

## Functionality
### Validation Process in the Application

1. **Header Extraction**: In the `ProxyHandler` class, the `x-gate-key` header is extracted from the incoming request:
   ```java
   String apiKeyHeader = exchange.getRequest().getHeaders().getFirst("x-gate-key");
   ```

2. **API Key Validation**: The extracted API key is then validated using the `KeyFetcherService`:
   ```java
   if (apiKeyHeader == null || !keyFetcherService.apiKeyValidator(apiKeyHeader)) {
       exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
       return exchange.getResponse().setComplete();
   }
   ```

3. **KeyFetcherService Logic**: In `KeyFetcherService`, the method `apiKeyValidator` checks the validity of the API key:
   ```java
   public boolean apiKeyValidator(String requestKey) {
       if (isValidationComplete) {
           Optional<ApiTokens> token = apiTokensRepository.findByUserTokens(requestKey);
           return token.isPresent();
       }
       return false;
   }
   ```

4. **Database Check**: The `ApiTokensRepository` is used to look up the API key in the database:
   ```java
   Optional<ApiTokens> findByUserTokens(String userTokens);
   ```

5. **Entity Class**: The `ApiTokens` entity class represents the database table where API tokens are stored:
   ```java
   @Entity
   @Table(name = "api_tokens")
   public class ApiTokens implements Serializable {
       // ...
   }
   ```
### Summary
- Remote clients must submit the `x-gate-key` HTTP header with their API key.
- The `ProxyHandler` extracts and validates the API key using the `KeyFetcherService`.
- The `KeyFetcherService` checks the API key against the `ApiTokensRepository`.
- If the key is valid and found in the database, the request is processed; otherwise, it is rejected with an `UNAUTHORIZED` status.

## TODO
Maybe tests someday. Maybe.

🖖
