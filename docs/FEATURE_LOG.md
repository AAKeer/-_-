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

## 2026-06-18 - Vue frontend

- Added Vue 3 homepage.
- Added refresh interaction and loading state.
- Added approved calm morning-brief visual style.
- Verification: `mvn test` passed, 3 tests.

## 2026-06-18 - Runtime verification

- Started the app with `mvn spring-boot:run` on port 8080.
- Verified `GET /` returned HTTP 200.
- Verified `GET /app.js` returned HTTP 200.
- Verified `GET /styles.css` returned HTTP 200.
- Verified `GET /api/hot/daily` returned HTTP 200 with `application/json;charset=UTF-8`.
- Current source results: `baidu` success with 20 items, `bilibili` success with 20 items, `github` success with 16 items.
- Added explicit UTF-8 charset to JSON API responses.

## 2026-06-18 - Parameterized hot source APIs

- Added query parameter passthrough for `GET /api/hot/source/{source}`.
- Added Bilibili category support for `all`, `technology`, `game`, and `food` using the ranking v2 API.
- Added GitHub `since=daily|weekly` support.
- Added Skills placeholder source returning `success=false` and `暂未接入真实数据源`.
- Fixed source/service user-facing Chinese messages that were previously mojibake in source files.
- Verification: `mvn test` passed, 4 tests.
