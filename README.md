# Emergency API (Java Backend Tech Test)

Spring Boot REST API that integrates with what3words for emergency reporting workflows.

## Tech Stack

- Java 25
- Spring Boot 3
- Gradle
- JUnit 5 + MockMvc + Mockito

## Setup

1. Ensure Java 25+ is available (or let Gradle auto-provision via toolchains).
2. Run the app:
   ```bash
   ./gradlew bootRun
   ```

The build uses a Java 25 toolchain and compiles bytecode at Java 21 level for dependency compatibility.

Configuration is environment-first via `src/main/resources/application.properties` placeholders:

- `what3words.api.key=${WHAT3WORDS_API_KEY:}`
- `what3words.api.base-url=${WHAT3WORDS_API_BASE_URL:https://api.what3words.com/v3}`
- `server.port=${SERVER_PORT:8080}`

Set these with environment variables before running locally if needed:

```bash
export WHAT3WORDS_API_KEY=your_real_key
export WHAT3WORDS_API_BASE_URL=https://api.what3words.com/v3
export SERVER_PORT=8080
```

## Endpoints

### 1) Coordinate to 3wa

`GET /emergencyapi/coord-to-3wa?lat=X&lng=Y`

Example:

```bash
curl "http://localhost:8080/emergencyapi/coord-to-3wa?lat=51.508341&lng=-0.125499"
```

Response:

```json
{
  "3wa": "daring.lion.race"
}
```

Error (invalid/missing coordinates):

```json
{
  "message": "Coordinates supplied do not convert to a 3wa"
}
```

### 2) 3wa to coordinate

`GET /emergencyapi/3wa-to-coord?3wa=X`

Example:

```bash
curl "http://localhost:8080/emergencyapi/3wa-to-coord?3wa=daring.lion.race"
```

Response:

```json
{
  "lat": 51.508341,
  "lng": -0.125499
}
```

Error (invalid format):

```json
{
  "message": "3wa address supplied has invalid format"
}
```

Error (not recognized / out of UK, with UK suggestions):

```json
{
  "message": "3wa not recognised: filled.count.snap",
  "suggestions": [
    {
      "country": "GB",
      "nearestPlace": "Bishops Cleeve, Gloucestershire",
      "words": "filled.count.snaps"
    },
    {
      "country": "GB",
      "nearestPlace": "Bayswater, London",
      "words": "filled.count.soap"
    },
    {
      "country": "GB",
      "nearestPlace": "Wednesfield, West Midlands",
      "words": "filled.count.slap"
    }
  ]
}
```

### 3) Language convert

`GET /emergencyapi/language-convert?3wa=X&target_language=Y`

Example:

```bash
curl "http://localhost:8080/emergencyapi/language-convert?3wa=daring.lion.race&target_language=cy"
```

Response:

```json
{
  "3wa": "sychach.parciau.lwmpyn"
}
```

Validation behavior follows the same UK and format constraints as `/3wa-to-coord`.

## Tests

Run tests:

```bash
./gradlew test
```

Coverage includes successful and error scenarios for all three endpoints plus service-level validation logic.

## Docker

1. Create local environment file:

```bash
cp .env.example .env
```

2. Set `WHAT3WORDS_API_KEY` in `.env`.
3. Start with Docker Compose:

```bash
docker compose up -d --build
```

Or use Make targets:

```bash
make up
make logs
make down
```
