# API 密钥环境变量化设计

## 意图

让项目可以安全上传到 Git：运行时凭据由环境变量提供，仓库只保留变量名、非敏感配置和配置示例。

## 目标

- 保留现有 `DASHSCOPE_API_KEY` 环境变量配置。
- 将搜索服务、高德地图 MCP 和 PostgreSQL 凭据移出版本控制。
- 提供根目录 `.env` 本地配置文件、可上传的 `.env.example` 和启动配置说明。
- 让本地配置文件（`.env`、`.env.*`）不会被 Git 追踪。

## 非目标与约束

- 不改变模型、数据库地址、接口路径、前端行为或 MCP 服务逻辑。
- 不把真实凭据写入示例文件、提交或命令输出。
- `.env` 由 Spring Boot 通过 `spring.config.import` 自动读取；MCP JSON 只保留环境变量占位符。
- 当前工作目录没有 `.git`，本次只修改工作区文件，不负责提交或推送。

## 方案

考虑过只替换 API key、集中新增 Java 配置类、以及直接使用 Spring 配置占位符三种方式。只替换 API key 会继续暴露数据库密码；新增配置类会扩大改动范围；本次采用现有 Spring 配置中的环境变量占位符，并在 MCP JSON 中使用同名环境变量占位符，配合示例文件和忽略规则完成迁移。

变量约定如下：

- `DASHSCOPE_API_KEY`
- `SEARCH_API_KEY`
- `AMAP_MAPS_API_KEY`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## 实现与验证

- 修改 `src/main/resources/application.yml` 和 `src/main/resources/mcp-servers.json`。
- 新增根目录 `.env`（本地真实配置）和 `.env.example`（可上传模板），并更新项目启动说明。
- 更新 `.gitignore`，忽略真实环境文件但保留 `.env.example`。
- 使用敏感值扫描确认源码配置不再包含真实凭据，并运行 Maven 测试与前端构建。
