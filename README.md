<p align="center">
  <h1 align="center">翻斗花园物业管理系统</h1>
  <p align="center">Spring Boot 4 + Vue 3 前后端分离社区物业管理平台，集成 DeepSeek AI Agent</p>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/JDK-21-orange.svg" alt="JDK 21">
  <img src="https://img.shields.io/badge/Spring_Boot-4.0-brightgreen.svg" alt="Spring Boot 4.0">
  <img src="https://img.shields.io/badge/Spring_AI-2.0-blue.svg" alt="Spring AI 2.0">
  <img src="https://img.shields.io/badge/Vue-3.5-4FC08D.svg?logo=vuedotjs" alt="Vue 3.5">
  <img src="https://img.shields.io/badge/Vite-8-646CFF.svg?logo=vite" alt="Vite 8">
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1.svg?logo=mysql" alt="MySQL 8.0">
  <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="License">
</p>

## 功能模块

| 模块 | 入口 | 说明 |
|------|------|------|
| AI 助手 | 右下角浮动按钮 (全局) | 自然语言驱动的页面跳转、数据查询、操作确认 (DeepSeek V4) |
| 用户管理 | 用户列表 / 权限管理 | 注册登录、账户初始化、角色权限 (RBAC)、头像上传、状态管理 |
| 财务管理 | 财务面板 / 账单管理 / 账单类型 | 账单创建编辑、明细管理、缴费支付、状态流转、Excel 导出、个人汇总 |
| 资产管理 | 资产面板 / 房产 / 停车位 / 建筑物 | 资产台账、面积/楼层/位置管理、到期追踪、批量导出 |
| 安防管理 | 安全面板 / 访客 / 车辆 / 通行记录 / 门岗 | 访客登记审批、车辆出入场、通行历史查询、门岗配置 |
| 设施管理 | 设施面板 / 设施信息 / 设施类型 | 公共设施台账、检修追踪、临期预警、责任人指派 |
| 工单管理 | 工单面板 / 工单列表 / 工单类型 | 投诉/报修/建议/退款/账户类工单全生命周期 |

> 系统内置 6 种角色 (管理员/经理/员工/安保/业主/租户)，前端路由与后端 API 均按角色过滤。AI 助手覆盖全部 6 个业务模块，支持 30+ 种操作指令。

## 技术栈

**后端**

| 组件 | 选型 |
|------|------|
| 框架 | Spring Boot 4.0.6 (Java 21) |
| 安全 | Spring Security + JWT (jjwt 0.13) + AOP 声明式权限 |
| AI | Spring AI 2.0 + DeepSeek V4 (SSE 流式对话) |
| ORM | MyBatis + PageHelper 分页 |
| 数据库 | MySQL 8.0 / Druid 连接池 |
| 存储 | MinIO (头像等文件) |
| 工具 | Lombok / FastJSON2 / Apache POI / Hutool |

**前端**

| 组件 | 选型 |
|------|------|
| 框架 | Vue 3 (Composition API) + Vite 8 |
| UI | Element Plus |
| 状态 | Pinia |
| 图表 | ECharts / vue-echarts |
| HTTP | Axios (请求缓存 + 写入失效) + SSE (AI 流式对话) |

## 快速开始

### 环境要求

- JDK 21 / Maven 3.6+ / MySQL 8.0+ / Node.js 18+
- (可选) DeepSeek API Key — AI 助手功能需要

### 1. 初始化数据库

```sql
CREATE DATABASE cms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

```bash
mysql -u root -p cms < cms.sql
```

### 2. 配置环境变量

在 `src/main/resources/` 下创建 `env.yaml`：

```yaml
datasource:
  url: jdbc:mysql://localhost:3306/cms?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
  username: root
  password: your_password

jwt:
  secret: your-jwt-secret-at-least-256-bits
  expiration: 86400000

# 可选：AI 助手 (DeepSeek)
deepseek:
  base-url: https://api.deepseek.com
  api-key: your-deepseek-api-key
  chat:
    model: deepseek-v4-flash
```

### 3. 启动后端

```bash
./mvnw spring-boot:run
# 后端运行在 http://localhost:8080
```

### 4. 启动前端

```bash
cd cms_webui
npm install
npm run dev
# 前端运行在 http://localhost:5173，自动代理 /api 到后端
```

### 5. 登录

浏览器打开 `http://localhost:5173`，使用内置测试账号登录（默认密码 `123456`）：

| 账号 | 角色 |
|------|------|
| `admin` | 系统管理员 |
| `manager` | 物业经理 |
| `employee` | 物业员工 |
| `user` | 业主 |

## 项目结构

```
CommunityManageSystem
├── src/main/java/edu/tut/cms/
│   ├── common/               # 配置/Security/AOP/异常/过滤器/工具 (含 Agent 配置)
│   ├── pojo/{dto,entity,vo}/ # 数据传输/实体/视图对象 (含 Agent DTO/VO)
│   └── server/{controller,service,mapper}/
│       ├── controller/       # REST 控制器 (含 AgentController SSE)
│       └── service/impl/     # 服务实现 (含 AgentServiceImpl 980行)
├── src/main/resources/
│   ├── application.yaml      # 应用配置 (含 Spring AI DeepSeek)
│   ├── env.yaml              # 敏感配置（自建，已 gitignore）
│   └── mapper/               # MyBatis XML
├── cms_webui/src/
│   ├── api/      # 接口封装 (含 agent.js SSE 流式)
│   ├── views/    # 页面（按模块分目录，10个列表页集成 Agent 控制）
│   ├── components/ / layout/ / stores/ / router/
│   └── utils/    # 请求封装、校验器、Agent 页面控制
├── cms.sql                   # 建库脚本 + 种子数据
└── pom.xml
```

> 详细架构说明见 [INDEX.md](INDEX.md)，模块文件清单见 [MODLE.md](MODLE.md)。

## API 约定

- 前缀 `/api`，认证头 `Authorization: Bearer <token>`
- 统一响应：`{ code: 200, message: "success", data: ... }`
- 分页响应：`{ total, pageNum, pageSize, pages, records }`
- AI 对话：`POST /api/agent/chat` (SSE, `text/event-stream`)，`POST /api/agent/execute` (JSON)
- 错误：`401` 未认证、`403` 权限不足/冻结

## 角色权限

| 角色 | 权限等级 | 可见范围 |
|------|:--------:|----------|
| 系统管理员 | 3 · 系统 | 全部 (含 AI 全部操作) |
| 物业经理 | 2 · 管理 | 全部管理功能 (AI 受限) |
| 物业员工 | 1 · 操作 | 设施、资产、工单 (AI 受限) |
| 安保人员 | 1 · 操作 | 访客、车辆、门岗 (AI 受限) |
| 业主 / 租户 | 0 · 查看 | 个人资产、账单、工单 (AI 仅查询) |

后端 `@RequireRole({0,1,3})` 注解声明接口权限，AOP 切面拦截校验。AI Agent 内独立实现二级权限校验 (`assertAllowed`)。

## 部署

```bash
# 后端打包
mvn clean package -DskipTests
java -jar target/CommunityManageSystem-*.jar

# 前端构建
cd cms_webui && npm run build
# dist/ 部署到 Nginx，需配置 /api 反向代理
```

## 常见问题

<details>
<summary>后端启动报 DataSource 错误</summary>
检查 <code>env.yaml</code> 是否存在且数据库连接信息正确，MySQL 已启动且 <code>cms</code> 库已创建。
</details>

<details>
<summary>AI 助手无法使用 / 回复"AI服务暂时不可用"</summary>
检查 <code>env.yaml</code> 中的 DeepSeek 配置是否正确，API Key 是否有效。即使未配置 DeepSeek，本地关键词匹配仍可处理部分指令。
</details>

<details>
<summary>npm install 失败或缓慢</summary>
<code>npm install --registry=https://registry.npmmirror.com</code>
</details>

<details>
<summary>前端请求返回 401</summary>
确认后端已启动，token 有效。登录过期后需重新登录。
</details>

<details>
<summary>账号冻结后无法操作</summary>
冻结用户 (status=1) 的写入请求会被前后端双重拦截。管理员可在用户管理页面解冻。
</details>

## License

[MIT](LICENSE)
