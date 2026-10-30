# API 密钥环境变量化 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove runtime credentials from tracked source configuration and document the environment variables required to run the backend and MCP integrations.

**Architecture:** Keep Spring Boot's existing property structure and replace sensitive scalar values with `${ENV_VAR}` placeholders. Import a root `.env` file through Spring Boot for local startup, keep the MCP JSON shape unchanged while resolving the Amap key from `AMAP_MAPS_API_KEY`, and provide a safe example file.

**Tech Stack:** Spring Boot 3.5.8, Maven, YAML, JSON, Vue 3/Vite, PowerShell verification.

**Spec:** `docs/superpowers/specs/2026-10-28-api-key-environment-variables-design.md`

## Global Constraints

- Never write real credentials into the repository or command output.
- Store local real values only in the ignored root `.env`; `.env.example` contains no real values.
- Preserve model, database, API, frontend, and MCP behavior apart from credential sourcing.
- Keep `.env.example` tracked while ignoring `.env` and `.env.*` files.
- The current workspace has no `.git` directory; do not attempt commit or push operations.

## Review Focus

- A missing required backend credential should be visible as a configuration failure rather than silently using a checked-in fallback; verify all placeholders have no defaults.
- The MCP config must carry the same `AMAP_MAPS_API_KEY` placeholder and no literal key; scan the JSON after editing.
- `.env.example` must list every required variable without secret values; compare it against the placeholders in application and MCP config.
- Existing frontend build behavior must remain intact; run the existing Vite build.
- Existing backend tests must still compile and run; run the Maven test suite.

### Task 1: Replace tracked credentials and add local configuration template

**Files:**
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/resources/mcp-servers.json`
- Modify: `.gitignore`
- Create: `.env.example`
- Create: `.env`
- Create: `README.md`

**Interfaces:**
- Consumes: the current Spring property names and MCP server JSON structure.
- Produces: placeholders for `DASHSCOPE_API_KEY`, `SEARCH_API_KEY`, `AMAP_MAPS_API_KEY`, `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`; a safe template and setup instructions.

- [ ] **Step 1: Import local configuration and replace hard-coded scalar values**

  In `application.yml`, add `spring.config.import: optional:file:.env[.properties]`, use `${SPRING_DATASOURCE_URL}`, `${SPRING_DATASOURCE_USERNAME}`, `${SPRING_DATASOURCE_PASSWORD}`, and `${SEARCH_API_KEY}` while retaining `${DASHSCOPE_API_KEY}`. In `mcp-servers.json`, replace the Amap literal with `${AMAP_MAPS_API_KEY}` and preserve the server command and arguments.

- [ ] **Step 2: Add the local file, safe template, and ignore rules**

  Create `.env` with the existing local database/search/Amap values moved out of source and a blank `DASHSCOPE_API_KEY` entry for the user to fill if it is not already set in the process environment. Create `.env.example` with blank values for secrets, a non-secret PostgreSQL URL placeholder, the six backend variables, `MCP_CLIENT_ENABLED=false`, and the existing frontend `VITE_API_BASE_URL` example. Add `.env` and `.env.*` to `.gitignore` while explicitly retaining `.env.example`.

- [ ] **Step 3: Document injection points**

  Create root `README.md` with a short configuration section that points to `.env.example`, explains that Spring Boot imports the local `.env` file, and includes the PowerShell and POSIX shell startup forms without real values.

- [ ] **Step 4: Verify the edited configuration is structurally safe**

  Run a repository scan that searches source configuration for known literal credential patterns and confirms the six variable names are present. Expected result: no known literal keys or database password remain under `src/main` or `mcp-servers.json`; each required variable appears in `.env.example`.

### Task 2: Run regression checks

**Files:**
- Test: existing Maven tests under `src/test/java`
- Test: existing frontend build under `frontend`

**Interfaces:**
- Consumes: the configuration changes from Task 1.
- Produces: passing backend tests and frontend production bundle.

- [ ] **Step 1: Run backend tests**

  Run `.\mvnw.cmd test -DskipTests=false` from the repository root. Expected: Maven completes successfully; tests that require external services may be reported separately if the environment does not provide their dependencies.

- [ ] **Step 2: Run the frontend production build**

  Run `npm run build` from `frontend`. Expected: Vite completes successfully and emits the existing `frontend/dist` bundle.

- [ ] **Step 3: Re-run the sensitive-value scan**

  Run the same scan from Task 1 after tests/builds. Expected: generated artifacts do not reintroduce credentials into tracked source files.
