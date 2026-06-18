const platforms = {
  baidu: {
    title: "百度",
    subtitle: "查看大众舆论与社会热点，适合作为每日信息入口。",
    mark: "百",
    defaultBoard: "baidu-hot",
    boards: [
      {
        id: "baidu-hot",
        title: "百度热搜",
        description: "实时热搜指数",
        mark: "百",
        endpoint: "/api/hot/source/baidu"
      }
    ]
  },
  bilibili: {
    title: "Bilibili",
    subtitle: "浏览 B 站不同分区的真实排行榜，捕捉内容社区趋势。",
    mark: "B",
    defaultBoard: "bilibili-all",
    boards: [
      {
        id: "bilibili-all",
        title: "总榜",
        description: "Bilibili 全站排行榜",
        mark: "B",
        endpoint: "/api/hot/source/bilibili?category=all"
      },
      {
        id: "bilibili-technology",
        title: "科技",
        description: "Bilibili 科技分区排行榜",
        mark: "科",
        endpoint: "/api/hot/source/bilibili?category=technology"
      },
      {
        id: "bilibili-game",
        title: "游戏",
        description: "Bilibili 游戏分区排行榜",
        mark: "游",
        endpoint: "/api/hot/source/bilibili?category=game"
      },
      {
        id: "bilibili-food",
        title: "美食",
        description: "Bilibili 美食分区排行榜",
        mark: "食",
        endpoint: "/api/hot/source/bilibili?category=food"
      }
    ]
  },
  github: {
    title: "GitHub",
    subtitle: "查看开源项目趋势，并预留 Skills 榜单入口。",
    mark: "Git",
    defaultBoard: "github-daily",
    boards: [
      {
        id: "github-daily",
        title: "GitHub 每日热榜",
        description: "Trending daily",
        mark: "Git",
        endpoint: "/api/hot/source/github?since=daily"
      },
      {
        id: "github-weekly",
        title: "GitHub 每周热榜",
        description: "Trending weekly",
        mark: "Git",
        endpoint: "/api/hot/source/github?since=weekly"
      },
      {
        id: "skills-all",
        title: "Skills 全时间榜单",
        description: "Skills 全时间安装榜",
        mark: "S",
        endpoint: "/api/hot/source/skills?period=all"
      },
      {
        id: "skills-daily",
        title: "Skills 每日榜单",
        description: "Skills 24h 趋势榜",
        mark: "S",
        endpoint: "/api/hot/source/skills?period=daily"
      },
      {
        id: "skills-hot",
        title: "Skills 当前热门",
        description: "Skills 当前热度榜",
        mark: "S",
        endpoint: "/api/hot/source/skills?period=hot"
      }
    ]
  },
  ai: {
    title: "AI 行业",
    subtitle: "后续可接入 AI 行业资讯、模型发布和产品趋势。",
    mark: "AI",
    defaultBoard: "ai-placeholder",
    boards: [
      {
        id: "ai-placeholder",
        title: "AI 行业",
        description: "预留入口",
        mark: "AI",
        placeholder: true
      }
    ]
  },
  "game-community": {
    title: "游戏社区",
    subtitle: "收录 Steam 热销榜、Steam 热玩榜等游戏趋势。",
    mark: "游",
    defaultBoard: "steam-topsellers",
    boards: [
      {
        id: "steam-topsellers",
        title: "Steam 热销榜",
        description: "Steam 官方热销趋势",
        mark: "热",
        endpoint: "/api/hot/source/steam?type=topsellers"
      },
      {
        id: "steam-mostplayed",
        title: "Steam 热玩榜",
        description: "Steam 当前在线趋势",
        mark: "玩",
        endpoint: "/api/hot/source/steam?type=mostplayed"
      }
    ]
  }
};

const state = {
  currentPlatform: null,
  currentBoard: null,
  cache: new Map(),
  loading: false
};

const elements = {
  pageTitle: document.querySelector("#pageTitle"),
  pageSubtitle: document.querySelector("#pageSubtitle"),
  currentDate: document.querySelector("#currentDate"),
  homeTime: document.querySelector("#homeTime"),
  refreshButton: document.querySelector("#refreshButton"),
  refreshText: document.querySelector("#refreshText"),
  homeView: document.querySelector("#homeView"),
  detailView: document.querySelector("#detailView"),
  backButton: document.querySelector("#backButton"),
  platformTitle: document.querySelector("#platformTitle"),
  platformIntro: document.querySelector("#platformIntro"),
  boardNav: document.querySelector("#boardNav"),
  boardMark: document.querySelector("#boardMark"),
  boardTitle: document.querySelector("#boardTitle"),
  boardDesc: document.querySelector("#boardDesc"),
  boardTime: document.querySelector("#boardTime"),
  boardContent: document.querySelector("#boardContent")
};

function init() {
  updateDate();
  window.setInterval(updateDate, 1000);
  bindEvents();
  showHome();
}

function bindEvents() {
  document.querySelectorAll("[data-platform]").forEach((button) => {
    button.addEventListener("click", () => openPlatform(button.dataset.platform));
  });

  elements.backButton.addEventListener("click", showHome);
  elements.refreshButton.addEventListener("click", () => {
    if (state.currentPlatform && state.currentBoard) {
      loadBoard(state.currentBoard, { force: true });
    }
  });
}

function updateDate() {
  const now = new Date();
  const formattedDate = new Intl.DateTimeFormat("zh-CN", {
    year: "numeric",
    month: "long",
    day: "numeric",
    weekday: "long"
  }).format(now);
  const formattedTime = `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;

  elements.currentDate.textContent = `${formattedDate} ${formattedTime}`;
  elements.homeTime.textContent = formattedTime;
}

function pad(value) {
  return String(value).padStart(2, "0");
}

function showHome() {
  state.currentPlatform = null;
  state.currentBoard = null;
  elements.pageTitle.textContent = "每日热榜早报";
  elements.pageSubtitle.textContent = "汇聚今日值得关注的信息。少一点噪音，多一点清晰，把分散的热榜整理成一份适合早晨阅读的简报。";
  elements.homeView.classList.remove("hidden");
  elements.detailView.classList.add("hidden");
  elements.refreshButton.classList.add("hidden");
}

function openPlatform(platformId) {
  const platform = platforms[platformId];
  if (!platform) {
    return;
  }

  state.currentPlatform = platformId;
  elements.pageTitle.textContent = platform.title;
  elements.pageSubtitle.textContent = platform.subtitle;
  elements.platformTitle.textContent = platform.title;
  elements.platformIntro.textContent = platform.subtitle;
  elements.homeView.classList.add("hidden");
  elements.detailView.classList.remove("hidden");
  elements.refreshButton.classList.remove("hidden");
  renderBoardNav(platform);
  loadBoard(platform.defaultBoard);
}

function renderBoardNav(platform) {
  elements.boardNav.innerHTML = "";

  platform.boards.forEach((board) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "board-nav-item";
    button.dataset.board = board.id;
    button.innerHTML = `
      <span class="board-nav-title">${escapeHtml(board.title)}</span>
      <span class="board-nav-desc">${escapeHtml(board.description)}</span>
    `;
    button.addEventListener("click", () => loadBoard(board.id));
    elements.boardNav.appendChild(button);
  });
}

async function loadBoard(boardId, options = {}) {
  const platform = platforms[state.currentPlatform];
  const board = platform.boards.find((item) => item.id === boardId);
  if (!board) {
    return;
  }

  state.currentBoard = boardId;
  updateBoardHeader(board);
  updateSelectedBoard(boardId);

  if (board.placeholder) {
    renderState("暂未接入真实数据源");
    return;
  }

  const cacheKey = board.endpoint;
  if (!options.force && state.cache.has(cacheKey)) {
    renderResult(state.cache.get(cacheKey));
    return;
  }

  setLoading(true);
  renderLoading();

  try {
    const response = await fetch(board.endpoint, {
      headers: {
        Accept: "application/json"
      }
    });

    if (!response.ok) {
      throw new Error(`请求失败：HTTP ${response.status}`);
    }

    const payload = await response.json();
    state.cache.set(cacheKey, payload);
    renderResult(payload);
  } catch (error) {
    renderState(error.message || "当前榜单请求失败");
  } finally {
    setLoading(false);
  }
}

function updateBoardHeader(board) {
  elements.boardMark.textContent = board.mark;
  elements.boardTitle.textContent = board.title;
  elements.boardDesc.textContent = board.description;
  elements.boardTime.textContent = "请求中";
}

function updateSelectedBoard(boardId) {
  document.querySelectorAll(".board-nav-item").forEach((button) => {
    button.classList.toggle("active", button.dataset.board === boardId);
  });
}

function setLoading(isLoading) {
  state.loading = isLoading;
  elements.refreshButton.disabled = isLoading;
  elements.refreshButton.classList.toggle("is-loading", isLoading);
  elements.refreshText.textContent = isLoading ? "刷新中" : "刷新当前榜单";
}

function renderLoading() {
  elements.boardContent.innerHTML = `
    <div class="loading-state" aria-label="加载中">
      <div class="skeleton"></div>
      <div class="skeleton short"></div>
      <div class="skeleton"></div>
      <div class="skeleton short"></div>
    </div>
  `;
}

function renderResult(payload) {
  const items = Array.isArray(payload.items) ? payload.items : [];
  const firstItem = items[0];
  elements.boardTime.textContent = firstItem && firstItem.fetchedAt ? formatTime(firstItem.fetchedAt) : "刚刚";

  if (!payload.success) {
    renderState(payload.errorMessage || "暂未接入真实数据源");
    return;
  }

  if (items.length === 0) {
    renderState("当前榜单暂无数据");
    return;
  }

  elements.boardContent.innerHTML = `
    <ol class="hot-list">
      ${items.map(renderItem).join("")}
    </ol>
  `;
}

function renderItem(item) {
  const title = item.title || "未命名条目";
  const url = item.url || "#";
  const rank = item.rank || "-";
  const heat = item.heat || "";
  const source = item.source || "";

  return `
    <li class="hot-item">
      <div class="rank">${escapeHtml(String(rank))}</div>
      <div>
        <a class="item-title" href="${escapeAttribute(url)}" target="_blank" rel="noopener noreferrer">${escapeHtml(title)}</a>
        <div class="item-meta">
          ${heat ? `<span class="heat">${escapeHtml(String(heat))}</span><span class="divider"></span>` : ""}
          <span>${escapeHtml(source)}</span>
        </div>
      </div>
    </li>
  `;
}

function renderState(message) {
  elements.boardTime.textContent = "稍后";
  elements.boardContent.innerHTML = `<div class="soft-state">${escapeHtml(message)}</div>`;
}

function formatTime(value) {
  return new Intl.DateTimeFormat("zh-CN", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false
  }).format(new Date(value));
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function escapeAttribute(value) {
  return escapeHtml(value).replaceAll("`", "&#096;");
}

init();
