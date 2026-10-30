# AI 双应用前端 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 在 `frontend/` 新建一个可启动的 Vue3 + Axios AI 聊天前端，覆盖主页、恋爱大师和超级智能体两个 SSE 聊天页。

**Architecture:** 独立 Vite 项目；页面层由 Vue Router 管理，共享 `ChatShell` 展示结构；`useSseChat` 用 `fetch` 读取 SSE，Axios 实例统一基础 URL 与默认配置。

**Tech Stack:** Vue 3、Vite、Vue Router、Axios、原生 Fetch ReadableStream、CSS。

**Spec:** `docs/superpowers/specs/2026-10-27-ai-chat-frontend-design.md`

## Global Constraints

- API base URL 固定为 `http://localhost:8123/api`，支持 `VITE_API_BASE_URL` 覆盖。
- 恋爱大师接口为 `/ai/love_app/chat/sse`，携带 `message` 与 `chatId`。
- 超级智能体接口为 `/ai/manus/chat`，携带 `message`。
- 前端使用 Axios 请求库；SSE 用浏览器流读取保证兼容性。
- 页面必须支持移动端、键盘操作、可见焦点和 reduced-motion。

## Review Focus

- SSE 数据既可能带 `data:` 前缀，也可能是裸文本，解析器必须都能追加。
- JSON 转义、跨 chunk 的半截 UTF-8 文本必须由 `TextDecoder` 流式解码。
- 停止生成后 AbortError 不得被误显示为请求失败。
- 空消息和重复提交必须被阻止。
- 后端不可用时，页面必须回到可输入状态并提供可重试反馈。

### Task 1: Scaffold and API layer

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/index.html`
- Create: `frontend/src/main.js`
- Create: `frontend/src/router.js`
- Create: `frontend/src/services/api.js`

- [ ] 创建 Vite 项目元数据、Vue 入口、路由和 Axios 实例。
- [ ] 配置 `VITE_API_BASE_URL` 默认值和根路由。
- [ ] 运行 `npm install`，确认依赖可解析。

### Task 2: SSE composable and shared chat shell

**Files:**
- Create: `frontend/src/composables/useSseChat.js`
- Create: `frontend/src/components/ChatShell.vue`
- Create: `frontend/src/components/AppIcon.vue`

- [ ] 实现 UUID chatId、消息状态、AbortController、发送/停止/重试流程。
- [ ] 实现 SSE 行解析、`data:` 事件、`[DONE]` 和裸文本兼容。
- [ ] 实现共享聊天布局、输入区、发送状态、错误重试和自动滚动。

### Task 3: Pages and styling

**Files:**
- Create: `frontend/src/App.vue`
- Create: `frontend/src/views/HomeView.vue`
- Create: `frontend/src/views/LoveChatView.vue`
- Create: `frontend/src/views/ManusChatView.vue`
- Create: `frontend/src/styles.css`

- [ ] 实现主页应用卡片与路由入口。
- [ ] 用配置分别挂载两个聊天页，接入对应接口和欢迎语。
- [ ] 实现设计 token、响应式布局、滚动区域、焦点态和 reduced-motion。

### Task 4: Build verification

**Files:**
- Verify: `frontend/dist/`

- [ ] 运行 `npm run build`，确认 Vite 生产构建退出码为 0。
- [ ] 检查构建产物包含主页、聊天页面和静态资源，无未解析导入。
