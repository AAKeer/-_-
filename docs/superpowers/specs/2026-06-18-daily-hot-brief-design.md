# 每日热榜早报 V1 设计方案

## 目标

「每日热榜早报」是一个轻量级 Web 应用，用来聚合公开可访问的每日热榜数据，并以清晨信息简报的方式展示。V1 的重点是可运行、真实数据、结构清晰、页面好读。

项目不是后台管理系统，也不是商业级复杂平台。它优先解决三件事：

- 从真实公开来源获取热榜数据。
- 通过统一接口返回聚合结果。
- 用淡雅、简洁、适合日常阅读的页面展示热榜。

## 技术栈

后端：

- Java 21
- Spring Boot 3
- Maven
- Lombok
- Jsoup
- Java HttpClient 或 Spring RestClient

前端：

- Vue 3
- 原生 CSS
- 不引入复杂 UI 组件库
- 静态资源由 Spring Boot 直接托管

数据库：

- V1 不使用数据库
- 不存历史
- 不做归档

AI：

- V1 不接入真实大模型
- 预留 `/api/report/summary`
- 后续可扩展 Spring AI 或 Spring AI Alibaba

## V1 数据源

首批推荐启用：

1. 百度热搜
   - 来源：`https://top.baidu.com/board?tab=realtime`
   - 获取方式：HTML 解析
   - 优点：公开页面可访问，榜单信息包含标题、链接、排名、热搜指数
   - 风险：页面结构变化会影响解析

2. GitHub Trending
   - 来源：`https://github.com/trending`
   - 获取方式：HTML 解析
   - 优点：公开页面稳定，适合补充开发者热点
   - 风险：GitHub 没有官方 Trending API，只能解析页面

3. Bilibili 热门
   - 页面来源：`https://www.bilibili.com/v/popular/all`
   - 优先尝试接口：`https://api.bilibili.com/x/web-interface/popular?ps=20&pn=1`
   - 获取方式：优先 JSON，失败则该来源返回失败状态
   - 风险：可能受请求头、风控、访问环境影响

V1 暂不启用：

- 微博热搜：访问容易进入微博访客系统，稳定获取通常需要 Cookie 或反爬处理。
- 知乎热榜：页面和接口受请求头、Cookie、风控影响较大，先预留扩展位置，不作为 V1 主路径。

任何来源不可用时，不使用假数据补齐。该来源返回 `success=false` 和明确错误信息，其他来源继续展示。

## 功能边界

V1 实现：

- 首页展示每日热榜早报。
- 刷新按钮请求 `/api/hot/daily`。
- 按来源分区展示热榜。
- 每条热榜展示排名、标题、来源、热度值。
- 标题可点击跳转原链接。
- 加载中状态。
- 温和错误提示。
- 某个数据源失败不影响其他来源。
- 移动端单列展示。

V1 不实现：

- 登录系统
- 权限系统
- 数据库
- 历史存储
- 定时任务
- 消息推送
- 复杂爬虫框架
- 代理池
- 全量 mock 数据
- 微服务拆分
- 真实 AI 总结

## 接口设计

### GET /

返回前端首页。

### GET /api/hot/daily

返回所有启用来源的今日热榜。

响应示例：

```json
{
  "date": "2026-06-18",
  "fetchedAt": "2026-06-18T20:18:00+08:00",
  "sources": [
    {
      "source": "baidu",
      "displayName": "百度热搜",
      "success": true,
      "items": [
        {
          "title": "示例标题",
          "url": "https://www.baidu.com/s?wd=example",
          "source": "baidu",
          "rank": 1,
          "heat": "7904504",
          "fetchedAt": "2026-06-18T20:18:00+08:00"
        }
      ],
      "errorMessage": null
    }
  ]
}
```

### GET /api/hot/source/{source}

返回指定来源热榜。

支持的 `source`：

- `baidu`
- `github`
- `bilibili`

未知来源返回 404。

### GET /api/report/summary

V1 预留接口。返回 501 或明确提示：AI summary is reserved for future versions.

## 数据结构

`HotItem`

- `title: String`
- `url: String`
- `source: String`
- `rank: Integer`
- `heat: String`
- `fetchedAt: OffsetDateTime`

`HotSourceResult`

- `source: String`
- `displayName: String`
- `success: boolean`
- `items: List<HotItem>`
- `errorMessage: String`

`DailyHotResponse`

- `date: LocalDate`
- `fetchedAt: OffsetDateTime`
- `sources: List<HotSourceResult>`

`HotSource`

- `String source()`
- `String displayName()`
- `HotSourceResult fetch()`

DTO 使用 Lombok 简化样板代码，例如 `@Data`、`@Builder`、`@NoArgsConstructor`、`@AllArgsConstructor`。

## 后端架构

目录结构：

```text
src/main/java/com/example/dailyhot/
  DailyHotApplication.java

  controller/
    PageController.java
    HotController.java
    ReportController.java

  model/
    HotItem.java
    HotSourceResult.java
    DailyHotResponse.java

  service/
    HotAggregationService.java
    HotSource.java

  source/
    BaiduHotSource.java
    GithubTrendingSource.java
    BilibiliHotSource.java

  config/
    HttpClientConfig.java
```

聚合流程：

1. `HotController` 接收请求。
2. `HotAggregationService` 遍历所有 `HotSource` 实现。
3. 每个来源独立请求和解析。
4. 单个来源异常被捕获并转换为失败结果。
5. 返回统一 `DailyHotResponse`。

## 前端设计

视觉方向已通过 `ui-preview.html` 确认：

- 清晨信息简报气质
- 低饱和米白、雾蓝、墨绿、暖灰配色
- 卡片接近白色
- 阴影极轻
- 信息层级清晰
- 多来源卡片网格
- 移动端单列
- 错误态克制
- 加载态为柔和 skeleton

正式前端使用 Vue 3：

- `main.js` 挂载 Vue 应用
- `index.html` 提供根节点
- `styles.css` 保存视觉样式
- Vue 负责请求 `/api/hot/daily`、渲染来源卡片、处理刷新 loading 和错误状态

静态资源目录：

```text
src/main/resources/static/
  index.html
  app.js
  styles.css
```

V1 可直接通过 CDN 引入 Vue 3，避免引入 Node 构建链，让项目保持 `mvn spring-boot:run` 即可启动。

## 错误处理

- HTTP 请求设置超时。
- 每个来源独立 try/catch。
- 来源失败时 `success=false`，`items=[]`。
- 错误信息面向用户保持温和，面向日志保留具体异常。
- 聚合接口只要有一个来源成功，就返回 200。
- 全部来源失败时仍返回 200，但每个来源都带失败状态。

## 验证方式

启动：

```bash
mvn spring-boot:run
```

浏览器访问：

```text
http://localhost:8080
```

接口验证：

```text
GET http://localhost:8080/api/hot/daily
GET http://localhost:8080/api/hot/source/baidu
GET http://localhost:8080/api/hot/source/github
GET http://localhost:8080/api/hot/source/bilibili
```

验收标准：

- 首页能打开。
- 页面风格与已确认预览一致。
- 刷新按钮能重新请求接口。
- 至少 2 个真实来源在当前网络环境下能返回数据。
- 单个来源失败不会影响其他来源展示。
- 接口不返回全量 mock 数据。
- 项目可通过 `mvn spring-boot:run` 启动。

