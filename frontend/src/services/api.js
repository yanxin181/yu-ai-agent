import axios from 'axios'

const api = axios.create({
  // Vite 开发环境走同源代理，避免浏览器因旧后端实例未加载 CORS 配置而拦截 SSE。
  baseURL: import.meta.env.VITE_API_BASE_URL || (import.meta.env.DEV ? '/api' : 'http://localhost:8123/api'),
  timeout: 180000,
  headers: {
    Accept: 'text/event-stream',
  },
})

export default api
