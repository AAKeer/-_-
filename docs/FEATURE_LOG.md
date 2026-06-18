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

## 2026-06-18 - Platform navigation frontend

- Reworked the existing frontend into a platform navigation home page plus platform detail pages.
- Removed Vue usage and kept the implementation to plain HTML, CSS, and JavaScript.
- Kept the approved calm morning-brief visual style, colors, card texture, spacing, and typography.
- Added detail-page board navigation for Baidu, Bilibili, GitHub, AI industry placeholder, and game community placeholder.
- Added in-memory frontend cache for loaded boards and refresh for the current board.
- Added loading, error, empty, placeholder, and success states for the right-side board panel.
- Verification: `mvn test` passed, 4 tests.
- Runtime verification on port 8080: home/app.js/styles.css returned 200; baidu 20 items; bilibili all/technology/game/food each 20 items; github daily 16 items; github weekly 20 items; skills all/daily returned `success=false` with 0 items.

## 2026-06-18 - Steam game community and scheduled task

- Added Steam game community support with `topsellers` and `mostplayed` board types.
- Steam top sellers use the official Steam Store `search/?filter=topsellers` page and parse real result rows.
- Steam most played uses the official Steam Charts API `ISteamChartsService/GetGamesByConcurrentPlayers/v1/` and Steam Store `appdetails` for game names.
- Added homepage game community entry text for Steam hot boards.
- Added browser-local date/time display with one-second updates.
- Enabled Spring scheduling with `@EnableScheduling`.
- Added `HotListUpdateTask` using `@Scheduled(fixedRate = 43_200_000, initialDelay = 10_000)` for a lightweight 12-hour verification task.
- Scheduled task logs start, Steam top sellers verification result, and GitHub hot list verification result. It does not cache, persist, or enqueue data.
- Verification: `mvn test` passed, 4 tests.
- Runtime verification on port 8080: home returned 200; `steam?type=topsellers` returned 20 real items; `steam?type=mostplayed` returned 20 real items; scheduled task emitted its first log after startup.
