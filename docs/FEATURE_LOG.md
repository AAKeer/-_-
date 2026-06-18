# Feature Log

## 2026-06-18 - Project skeleton

- Created Spring Boot 3 Maven project.
- Added Java 21, Lombok, Jsoup, and test dependencies.
- Added application entry point and default port 8080.
- Verification: pending until dependencies are downloaded.

## 2026-06-18 - Core aggregation

- Added unified hot-list DTOs.
- Added HotSource abstraction and aggregation service.
- Added tests for all-source and single-source aggregation.
- Verification: `mvn test` passed, 3 tests.

## 2026-06-18 - Real source fetchers

- Added Baidu hot search HTML fetcher.
- Added GitHub Trending HTML fetcher.
- Added Bilibili popular JSON fetcher.
- Verification: `mvn test` passed, 3 tests.

## 2026-06-18 - HTTP APIs

- Added daily aggregation API.
- Added source-specific API with 404 for unknown sources.
- Added homepage forward route.
- Added reserved AI summary endpoint.
- Verification: `mvn test` passed, 3 tests.
