# AI 双应用前端设计

## 意图

为现有 Spring Boot AI 后端提供一个可直接启动的 Vue3 前端，用户可以从主页进入 AI 恋爱大师或 AI 超级智能体，并在聊天室中看到流式回答。

## 目标

- 提供主页应用切换入口。
- 两个聊天页保持一致的聊天体验：用户消息右侧、AI 消息左侧、底部输入框。
- 进入聊天页自动生成 UUID chatId，并在调用恋爱大师接口时携带它。
- 通过流式读取实时呈现 `TEXT_EVENT_STREAM` 内容，支持停止生成和错误重试。
- 使用 Axios 作为请求库，封装 API 基地址 `http://localhost:8123/api`。
- 支持窄屏设备，保证输入框、按钮和聊天内容可操作。

## 非目标

- 不修改 Spring Boot 接口协议。
- 不实现登录、消息持久化、Markdown 富文本渲染或多用户会话列表。
- 不把前端打包进后端 jar；前端保持独立项目，可按需部署到静态服务器。

## 约束

- 技术栈：Vue 3、Vite、Axios。
- 接口：`GET /ai/love_app/chat/sse?message=&chatId=` 和 `GET /ai/manus/chat?message=`。
- 两个接口都产生 SSE；前端按 `data:` 事件和裸文本分片兼容解析。
- Manus 当前后端会发送一条完整结果，但前端仍按流式方式消费。

## 方案

1. 原生 EventSource：代码量少，但无法方便地发送 `chatId` 之外的自定义参数，也不便于取消和统一错误处理。
2. Axios 直接设置 `responseType: stream`：浏览器端 Axios 适配不一致，难以稳定获得 `ReadableStream`。
3. Axios 负责请求配置，浏览器 `fetch` 读取 `ReadableStream`：能保留 Axios 请求库和统一配置，同时可靠处理分片、AbortController 和 SSE 结束事件。采用此方案。

## 交互设计

- 主页用两个高对比应用卡片呈现能力说明和进入按钮。
- 聊天页顶部显示应用名称、简短说明、当前会话 id 和返回主页按钮。
- 欢迎消息由前端放入消息列表；发送后立即显示用户消息，再追加一个空的 AI 消息并逐片填充。
- 生成期间显示“停止生成”；停止后保留已收到内容，发送按钮在输入为空时禁用。
- 请求错误在 AI 气泡中明确提示，并提供重试动作。
- 聊天滚动区域自动滚到底部，尊重 `prefers-reduced-motion`。

## 架构

- `src/App.vue` 根据 `vue-router` 路由承载页面。
- `src/views/HomeView.vue`、`LoveChatView.vue`、`ManusChatView.vue` 分离页面职责。
- `src/components/ChatShell.vue` 负责共享聊天布局和交互。
- `src/composables/useSseChat.js` 负责 UUID、消息状态、请求取消、SSE 解析。
- `src/services/api.js` 创建 Axios 实例并提供请求配置；流读取仍使用 `fetch` 以兼容浏览器。
- `src/styles.css` 提供设计 token、响应式布局和无障碍焦点样式。

## 验收标准

- `npm run build` 在 `frontend/` 中成功退出。
- 主页可通过两个入口进入对应聊天页。
- 两个聊天页的 URL 和接口路径不同，恋爱大师请求携带 chatId。
- 在后端返回多段 SSE 数据时，AI 气泡内容会连续追加并在完成后恢复发送状态。
- 停止按钮可以中断请求，刷新页面或再次进入会生成新的 chatId。
