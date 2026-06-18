# Daily Hot Brief Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a runnable Spring Boot 3 + Vue 3 daily hot-list brief app that fetches real public hot sources and displays them in the approved calm morning-brief UI.

**Architecture:** Spring Boot serves JSON APIs and static Vue assets from one Maven project. Each hot source implements a small `HotSource` interface so source failures are isolated and future sources can be added without changing controllers.

**Tech Stack:** Java 21, Spring Boot 3, Maven, Lombok, Jsoup, Jackson, Vue 3 CDN, plain CSS.

---

## File Structure

- `pom.xml`: Maven dependencies and Java 21 configuration.
- `src/main/java/com/example/dailyhot/DailyHotApplication.java`: Spring Boot entry point.
- `src/main/java/com/example/dailyhot/model/*.java`: Lombok DTOs for API responses.
- `src/main/java/com/example/dailyhot/service/HotSource.java`: source abstraction.
- `src/main/java/com/example/dailyhot/service/HotAggregationService.java`: invokes all sources and filters by source id.
- `src/main/java/com/example/dailyhot/source/*.java`: Baidu, GitHub, and Bilibili fetchers.
- `src/main/java/com/example/dailyhot/controller/*.java`: page, hot API, and reserved report endpoints.
- `src/main/resources/application.yml`: app name and server settings.
- `src/main/resources/static/index.html`: Vue host page.
- `src/main/resources/static/app.js`: Vue app and API interaction.
- `src/main/resources/static/styles.css`: approved visual style.
- `docs/FEATURE_LOG.md`: implementation and push record.
- `src/test/java/com/example/dailyhot/service/HotAggregationServiceTest.java`: unit tests for aggregation behavior.

## Task 1: Git and Project Skeleton

**Files:**
- Create: `pom.xml`
- Create: `src/main/java/com/example/dailyhot/DailyHotApplication.java`
- Create: `src/main/resources/application.yml`
- Create: `docs/FEATURE_LOG.md`

- [ ] **Step 1: Connect Git repository**

Run:

```bash
git init
git branch -M main
git remote add origin https://github.com/AAKeer/-_-.git
```

Expected: local repository exists and `origin` points to the provided GitHub URL.

- [ ] **Step 2: Create Maven project descriptor**

Create `pom.xml` with Spring Boot web, Lombok, Jsoup, and test dependencies.

- [ ] **Step 3: Create app entry point**

Create `DailyHotApplication.java` with `SpringApplication.run(DailyHotApplication.class, args)`.

- [ ] **Step 4: Create application config**

Create `application.yml` with `spring.application.name: daily-hot-brief` and `server.port: 8080`.

- [ ] **Step 5: Record skeleton feature**

Append to `docs/FEATURE_LOG.md`:

```markdown
## 2026-06-18 - Project skeleton

- Created Spring Boot 3 Maven project.
- Added Java 21, Lombok, Jsoup, and test dependencies.
- Added application entry point and default port 8080.
- Verification: pending until dependencies are downloaded.
```

- [ ] **Step 6: Commit and push skeleton**

Run:

```bash
git add pom.xml src/main/java/com/example/dailyhot/DailyHotApplication.java src/main/resources/application.yml docs/FEATURE_LOG.md docs/superpowers docs/superpowers/plans docs/superpowers/specs ui-preview.html
git commit -m "chore: scaffold daily hot brief project"
git push -u origin main
```

Expected: first feature milestone is pushed.

## Task 2: Core Models and Aggregation

**Files:**
- Create: `src/main/java/com/example/dailyhot/model/HotItem.java`
- Create: `src/main/java/com/example/dailyhot/model/HotSourceResult.java`
- Create: `src/main/java/com/example/dailyhot/model/DailyHotResponse.java`
- Create: `src/main/java/com/example/dailyhot/service/HotSource.java`
- Create: `src/main/java/com/example/dailyhot/service/HotAggregationService.java`
- Create: `src/test/java/com/example/dailyhot/service/HotAggregationServiceTest.java`
- Modify: `docs/FEATURE_LOG.md`

- [ ] **Step 1: Write aggregation tests**

Create tests that verify all sources are included, a single source can be selected, and unknown sources return empty.

- [ ] **Step 2: Implement Lombok DTOs**

Create DTOs with `@Data`, `@Builder`, `@NoArgsConstructor`, and `@AllArgsConstructor`.

- [ ] **Step 3: Implement `HotSource` interface**

Define `source()`, `displayName()`, and `fetch()`.

- [ ] **Step 4: Implement aggregation service**

Inject `List<HotSource>`, call each source, and catch source exceptions into failed `HotSourceResult`.

- [ ] **Step 5: Run tests**

Run:

```bash
mvn test
```

Expected: aggregation tests pass.

- [ ] **Step 6: Record and push**

Append to `docs/FEATURE_LOG.md`:

```markdown
## 2026-06-18 - Core aggregation

- Added unified hot-list DTOs.
- Added HotSource abstraction and aggregation service.
- Added tests for all-source and single-source aggregation.
- Verification: `mvn test`.
```

Commit and push:

```bash
git add src/main/java/com/example/dailyhot/model src/main/java/com/example/dailyhot/service src/test/java/com/example/dailyhot/service docs/FEATURE_LOG.md
git commit -m "feat: add hot source aggregation core"
git push
```

## Task 3: Real Hot Source Fetchers

**Files:**
- Create: `src/main/java/com/example/dailyhot/source/BaiduHotSource.java`
- Create: `src/main/java/com/example/dailyhot/source/GithubTrendingSource.java`
- Create: `src/main/java/com/example/dailyhot/source/BilibiliHotSource.java`
- Modify: `docs/FEATURE_LOG.md`

- [ ] **Step 1: Implement Baidu source**

Fetch `https://top.baidu.com/board?tab=realtime` with a browser-like User-Agent and parse ranking items with Jsoup. Extract title, URL, rank, heat, source, and fetchedAt.

- [ ] **Step 2: Implement GitHub Trending source**

Fetch `https://github.com/trending` with a browser-like User-Agent and parse repository articles. Extract repository name as title, GitHub URL, rank, stars-today text as heat when present, source, and fetchedAt.

- [ ] **Step 3: Implement Bilibili source**

Fetch `https://api.bilibili.com/x/web-interface/popular?ps=20&pn=1` with a browser-like User-Agent and parse JSON response. Extract title, video URL, rank, and stat view count as heat when present.

- [ ] **Step 4: Run tests**

Run:

```bash
mvn test
```

Expected: project compiles and tests pass.

- [ ] **Step 5: Record and push**

Append to `docs/FEATURE_LOG.md`:

```markdown
## 2026-06-18 - Real source fetchers

- Added Baidu hot search HTML fetcher.
- Added GitHub Trending HTML fetcher.
- Added Bilibili popular JSON fetcher.
- Verification: `mvn test`.
```

Commit and push:

```bash
git add src/main/java/com/example/dailyhot/source docs/FEATURE_LOG.md
git commit -m "feat: add real hot source fetchers"
git push
```

## Task 4: API Controllers

**Files:**
- Create: `src/main/java/com/example/dailyhot/controller/HotController.java`
- Create: `src/main/java/com/example/dailyhot/controller/PageController.java`
- Create: `src/main/java/com/example/dailyhot/controller/ReportController.java`
- Modify: `docs/FEATURE_LOG.md`

- [ ] **Step 1: Implement hot API**

Create `GET /api/hot/daily` and `GET /api/hot/source/{source}`. Unknown sources return 404.

- [ ] **Step 2: Implement page route**

Create `GET /` that forwards to `/index.html`.

- [ ] **Step 3: Implement reserved summary route**

Create `GET /api/report/summary` returning HTTP 501 with a clear reserved message.

- [ ] **Step 4: Run tests**

Run:

```bash
mvn test
```

Expected: project compiles and tests pass.

- [ ] **Step 5: Record and push**

Append to `docs/FEATURE_LOG.md`:

```markdown
## 2026-06-18 - HTTP APIs

- Added daily aggregation API.
- Added source-specific API.
- Added homepage forward route.
- Added reserved AI summary endpoint.
- Verification: `mvn test`.
```

Commit and push:

```bash
git add src/main/java/com/example/dailyhot/controller docs/FEATURE_LOG.md
git commit -m "feat: expose daily hot APIs"
git push
```

## Task 5: Vue Frontend

**Files:**
- Create: `src/main/resources/static/index.html`
- Create: `src/main/resources/static/app.js`
- Create: `src/main/resources/static/styles.css`
- Modify: `docs/FEATURE_LOG.md`

- [ ] **Step 1: Create Vue host HTML**

Use Vue 3 CDN and mount `#app`.

- [ ] **Step 2: Implement Vue app**

Fetch `/api/hot/daily` on load and when clicking refresh. Render sources, successful items, loading skeletons, and per-source errors.

- [ ] **Step 3: Port approved visual style**

Move the approved `ui-preview.html` styling into `styles.css`, adapted to dynamic data.

- [ ] **Step 4: Run tests**

Run:

```bash
mvn test
```

Expected: project compiles and tests pass.

- [ ] **Step 5: Record and push**

Append to `docs/FEATURE_LOG.md`:

```markdown
## 2026-06-18 - Vue frontend

- Added Vue 3 homepage.
- Added refresh interaction and loading state.
- Added approved calm morning-brief visual style.
- Verification: `mvn test`.
```

Commit and push:

```bash
git add src/main/resources/static docs/FEATURE_LOG.md
git commit -m "feat: add daily hot brief frontend"
git push
```

## Task 6: Runtime Verification

**Files:**
- Modify: `docs/FEATURE_LOG.md`

- [ ] **Step 1: Start app**

Run:

```bash
mvn spring-boot:run
```

Expected: Spring Boot starts on port 8080.

- [ ] **Step 2: Verify API**

Open:

```text
http://localhost:8080/api/hot/daily
```

Expected: JSON contains `date`, `fetchedAt`, and `sources`.

- [ ] **Step 3: Verify page**

Open:

```text
http://localhost:8080
```

Expected: homepage loads, refresh works, and source cards render.

- [ ] **Step 4: Record and push**

Append actual verification results to `docs/FEATURE_LOG.md`, including which sources succeeded or failed in the current environment.

Commit and push:

```bash
git add docs/FEATURE_LOG.md
git commit -m "docs: record runtime verification"
git push
```

