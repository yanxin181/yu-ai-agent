# Yu AI Agent 前端

这是一个独立的 Vue 3 + Vite 前端，提供主页、AI 恋爱大师和 AI 超级智能体两个聊天入口。

## 启动

```bash
cd frontend
npm install
npm run dev
```

默认访问 `http://localhost:5173`。

## API 地址

默认后端地址为 `http://localhost:8123/api`，可以通过环境变量覆盖：

```bash
VITE_API_BASE_URL=http://localhost:8123/api npm run dev
```

聊天接口：

- `GET /ai/love_app/chat/sse?message={message}&chatId={chatId}`
- `GET /ai/manus/chat?message={message}`

前端会为每次进入聊天页生成 UUID 会话 id，并使用浏览器流式读取 SSE 内容，实时追加到 AI 消息气泡。
