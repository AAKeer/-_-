const { createApp } = Vue;

createApp({
  data() {
    return {
      loading: false,
      pageError: "",
      date: "",
      fetchedAt: "",
      sources: [],
      skeletonSources: ["a", "b", "c"]
    };
  },
  computed: {
    displayDate() {
      if (!this.date) {
        return new Intl.DateTimeFormat("zh-CN", {
          year: "numeric",
          month: "long",
          day: "numeric",
          weekday: "long"
        }).format(new Date());
      }
      const parsed = new Date(`${this.date}T00:00:00`);
      return new Intl.DateTimeFormat("zh-CN", {
        year: "numeric",
        month: "long",
        day: "numeric",
        weekday: "long"
      }).format(parsed);
    },
    fetchedTime() {
      if (!this.fetchedAt) {
        return "--:--";
      }
      return new Intl.DateTimeFormat("zh-CN", {
        hour: "2-digit",
        minute: "2-digit",
        hour12: false
      }).format(new Date(this.fetchedAt));
    },
    sourceCount() {
      return this.sources.length;
    },
    itemCount() {
      return this.sources.reduce((total, source) => total + ((source.items && source.items.length) || 0), 0);
    }
  },
  mounted() {
    this.fetchDaily();
  },
  methods: {
    async fetchDaily() {
      this.loading = true;
      this.pageError = "";

      try {
        const response = await fetch("/api/hot/daily", {
          headers: {
            "Accept": "application/json"
          }
        });

        if (!response.ok) {
          throw new Error(`请求失败：HTTP ${response.status}`);
        }

        const payload = await response.json();
        this.date = payload.date || "";
        this.fetchedAt = payload.fetchedAt || "";
        this.sources = Array.isArray(payload.sources) ? payload.sources : [];
      } catch (error) {
        this.pageError = error.message || "暂时无法获取热榜数据。";
      } finally {
        this.loading = false;
      }
    },
    sourceMark(source) {
      const marks = {
        baidu: "百",
        bilibili: "B",
        github: "Git"
      };
      return marks[source.source] || (source.displayName || source.source || "?").slice(0, 1);
    },
    sourceDescription(source) {
      const descriptions = {
        baidu: "实时热搜指数",
        bilibili: "公开视频热度",
        github: "Trending today"
      };
      if (!source.success) {
        return "温和失败态";
      }
      return descriptions[source.source] || "公开热榜来源";
    },
    sourceTime(source) {
      const firstItem = source.items && source.items.length ? source.items[0] : null;
      if (!firstItem || !firstItem.fetchedAt) {
        return source.success ? "刚刚" : "稍后";
      }
      return new Intl.DateTimeFormat("zh-CN", {
        hour: "2-digit",
        minute: "2-digit",
        hour12: false
      }).format(new Date(firstItem.fetchedAt));
    }
  }
}).mount("#app");
