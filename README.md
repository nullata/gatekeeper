# Gatekeeper Proxy Server

## Overview
Gatekeeper is a proxy server application that ensures secure and controlled access to backend services. It enforces rate limiting, API key validation, and environment validation against a database before forwarding requests to a target URL. If a request key cannot be validated, Gatekeeper will return a `401 Unauthorized` response.

## Features
- **Rate Limiting**: Controls the rate of incoming requests, either globally or per individual request key.
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
- `DB_TYPE`: Database type (e.g., `mariadb`, `mysql`, `postgres`, `db2`, `mssql`, `sqlite`).
- `DB_HOST`: Database host.
- `DB_PORT`: Database port.
- `DB_NAME`: Database name.
- `DB_USERNAME`: Database username.
- `DB_PASSWORD`: Database password.
- `TABLE_NAME`: Database table name containing API keys.
- `COLUMN_NAME`: Column name in the table containing API keys.

### Database
This application supports multiple databases including MariaDB, MySQL, PostgreSQL, DB2, MSSQL, and SQLite. The provided `ddl-gatekeeper-example.sql` file provides an example of the required baseline database structure against which user key tokens will be validated.

### Additional Configurations

- `RATE_LIMIT_ENABLED`: Optional. Determines whether the rate limiter is enabled. String: `"true"`|`"false"`. Default: `"false"`.
- `RATE_LIMIT_RATE`: Optional. Rate limit threshold. Int value. Default: `100`.
- `RATE_LIMIT_TIMEOUT`: Optional. Rate limit timeout period (seconds). Int value. Default: `10`.
- `RATE_LIMIT_MODE`: Optional. The mode at which rate limiting works - supports `global` and `individual` values. 
    - `global` - Default value. Establishes a global rate for all keys, based on `RATE_LIMIT_RATE` and `RATE_LIMIT_MODE`.
    - `individual` - Establishes a rate limit per individual key, based on `RATE_LIMIT_RATE` and `RATE_LIMIT_MODE`.

- `ENABLE_CACHING`: Optional. Determines whether the caching feature is enabled, where user key tokens are stored in a memory cache once validated. String: `"true"`|`"false"`. Default: `true`.
- `CACHE_MAX_SIZE`: Optional. The maximum number of keys that can be stored in cache at once. Only taken into account if caching is enabled. Int value. Default: `100`.
- `CACHE_MAX_DURATION_M`: Optional. The maximum duration (in minutes) that individual keys are stored in the cache. Only taken into account if caching is enabled. Int value. Default: `10`.

#### Volumes
You can attach a volume for application logs which, in the container, are stored in `/var/log/gatekeeper`. Refer to `entrypoint.sh`.

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

## TODO
Maybe tests someday. Maybe.

🖖
