# Yu AI Agent

## 本地配置

DASHSCOPE_API_KEY 从系统环境变量读取，无需写入 `.env`。其他密钥和数据库凭据放在项目根目录的 `.env` 文件中。Spring Boot 会通过 `application.yml` 中的 `spring.config.import` 自动读取它；`.env` 已被 `.gitignore` 忽略，不要上传。可以复制 `.env.example` 作为配置模板。

系统环境变量：

- `DASHSCOPE_API_KEY`：阿里云模型服务密钥

修改系统环境变量后，重新打开终端或重启 IntelliJ IDEA，再启动后端，让新进程读取更新后的值。

在 `.env` 中填写的变量：

- `SEARCH_API_KEY`：搜索服务密钥
- `AMAP_MAPS_API_KEY`：高德地图 MCP 密钥
- `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`：PostgreSQL 连接配置

在 Windows PowerShell 中启动后端：

```powershell
.\mvnw.cmd spring-boot:run
```

在 Linux/macOS 中启动后端：

```bash
./mvnw spring-boot:run
```

如果运行环境不会自动读取 `.env`，请在启动进程前以同名系统环境变量注入这些值。MCP 配置文件只保留 `${AMAP_MAPS_API_KEY}` 占位符，MCP 宿主也需要读取同名环境变量。

前端仍可在 `frontend` 目录执行 `npm run dev`，并通过 `VITE_API_BASE_URL` 覆盖后端地址。
