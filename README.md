# CommunityManageSystem

基于 **Spring Boot 4.0** + **Vue 3** 的前后端分离小区物业管理系统。后端采用 Spring Security + JWT 实现无状态认证与角色权限控制，前端基于 Vue 3 + Vite + Element Plus + Pinia 构建管理页面，支持物业日常业务的全流程数字化管理。

## 功能概览

| 模块 | 功能 |
| --- | --- |
| 用户管理 | 用户注册/登录、账户初始化、用户列表、角色权限管理、个人设置（头像上传、密码修改、UID 锁定） |
| 财务管理 | 财务面板（数据概览）、账单管理（创建/编辑/缴费/状态流转）、账目类型管理 |
| 资产管理 | 房产信息（住宅/商铺等）、停车位信息管理 |
| 安防管理 | 访客登记与审批、访客类型管理、车辆注册与出入场、通行记录查询、门岗管理 |
| 设施管理 | 公共设施台账、设施类型管理、责任人指派 |
| 工单管理 | 投诉/报修/建议/退款/账户类工单的创建、受理、驳回、完成全流程 |

## 技术栈

### 后端

- **Java 21**, Spring Boot 4.0.6, Spring MVC
- **Spring Security** — 无状态认证 + 角色/权限拦截
- **JJWT** — JWT 令牌签发与校验
- **MyBatis** — ORM，XML 映射文件方式
- **MySQL 8.0+** — 关系型数据库
- **Druid** — 数据库连接池
- **BCrypt** — 密码加密
- **PageHelper** — 物理分页
- **MinIO** — 对象存储（头像等文件上传）
- **FastJSON2** — JSON 序列化
- **Apache POI** — Excel 导入导出
- **Hutool** — 通用工具库
- **AspectJ** — AOP 切面（`@RequirePermission` / `@RequireRole` 注解驱动的权限校验）
- **Spring Actuator** — 应用监控

### 前端

- **Vue 3** (Composition API) + **Vite 8**
- **Element Plus** — 后台管理 UI 组件库
- **Vue Router 4** — 路由管理（含路由守卫）
- **Pinia** — 状态管理（用户状态、页面缓存）
- **Axios** — HTTP 请求封装（含请求缓存、写入失效、冻结拦截）

## 项目结构

```text
CommunityManageSystem
├── src/                                    # 后端 Spring Boot 源码
│   ├── main/java/edu/tut/cms/
│   │   ├── CommunityManageSystemApplication.java   # 启动入口
│   │   ├── common/
│   │   │   ├── aop/                        # AOP 切面（AuthAspect、权限注解）
│   │   │   ├── config/                     # 配置（Security, WebMvc, OSS, PageHelper）
│   │   │   ├── context/                    # 用户上下文（ThreadLocal）
│   │   │   ├── exception/                  # 全局异常处理、业务异常定义
│   │   │   ├── filter/                     # JWT 认证过滤器
│   │   │   ├── result/                     # 统一响应体（Result, PageResult）
│   │   │   └── utils/                      # 工具类（JWT, Password, OSS, JSON）
│   │   ├── pojo/
│   │   │   ├── dto/                        # 数据传输对象（50+）
│   │   │   ├── entity/                     # 实体类（14 个）
│   │   │   └── vo/                         # 视图对象（17 个）
│   │   └── server/
│   │       ├── controller/                 # REST 控制器（8 个业务 Controller）
│   │       ├── service/                    # 业务逻辑层（9 个 Service）
│   │       └── mapper/                     # MyBatis Mapper 接口（17 个）
│   └── main/resources/
│       ├── application.yaml                # 后端配置（数据库、JWT 等）
        ├── env.yaml                        # 密钥配置（需自行创建）
│       └── mapper/                         # MyBatis XML 映射文件（10 个）
├── cms_webui/                              # 前端 Vue 3 项目
│   └── src/
│       ├── api/                            # 接口请求封装（8 个模块）
│       ├── components/                     # 公共组件（头像编辑、抽屉表单等）
│       ├── layout/                         # 布局组件（MainLayout）
│       ├── router/                         # 路由配置（含权限守卫）
│       ├── stores/                         # Pinia 状态管理（user, pageCache）
│       ├── styles/                         # 全局样式（CSS 变量主题）
│       ├── utils/                          # 工具函数（请求封装、校验器、移动端适配）
│       └── views/                          # 页面视图（按模块分目录）
│           ├── asset/                      # 资产管理（房产、停车位）
│           ├── facility/                   # 设施管理
│           ├── finance/                    # 财务管理（面板、账单、类型）
│           ├── security/                   # 安防管理（访客、车辆、通行记录、门岗）
│           ├── task/                       # 工单管理
│           └── user/                       # 用户管理（列表、角色）
├── cms.sql                                 # 数据库初始化脚本（含结构 + 种子数据）
├── pom.xml                                 # Maven 项目配置
├── .gitignore
├── LICENSE
└── README.md
```

## 数据库设计

系统共 **14 张数据表**，核心关系如下：

```
user ──┬── bill (owner/creater) ─── bill_detail ── bill_type
       ├── asset (owner) ─────────── asset_type
       ├── car (owner) ───────────── car_history ── gate
       │    └── car_type
       ├── facility (chief) ──────── facility_type
       ├── task (owner/operator) ─── task_type
       └── visitor (owner/creater) ─ visitor_type

user_role ── user (role)
```

| 表 | 说明 |
| --- | --- |
| `user` | 用户信息（UID、密码、角色、状态、头像、账户初始化标记） |
| `user_role` | 角色定义（系统管理员/物业经理/物业员工/安保人员/业主/租户） |
| `asset` | 资产信息（房产/停车位，含面积、楼栋、楼层、状态） |
| `asset_type` | 资产类型（房产、停车位） |
| `bill` | 账单（创建人/所有人/资产关联，含应收/实收、状态流转） |
| `bill_detail` | 账目明细（类型、数量、单价、金额、状态） |
| `bill_type` | 账目类型（物业费/采暖费/水费/电费/燃气费/维修/社区事务/车位租金） |
| `car` | 车辆注册（车主/车位/车牌号/车辆类型/状态） |
| `car_history` | 通行记录（车辆/门岗/出入类型/时间） |
| `car_type` | 车辆类型（业主/访客/服务/特种） |
| `facility` | 公共设施（名称/负责人/类型/状态/位置/保质期） |
| `facility_type` | 设施类型（变电器/水泵/供热泵/弱电机房/基站/电梯/摄像头/门禁） |
| `gate` | 门岗（名称/负责人） |
| `task` | 工单（投诉申诉/报修/意见建议/退款/账户） |
| `task_type` | 工单类型 |
| `visitor` | 访客（被访业主/资产/类型/状态/访问时间） |
| `visitor_type` | 访客类型（亲友/外卖/快递/家政维修/公务人员） |

## 角色权限体系

| 角色 | ID | 权限等级 | 说明 |
| --- | --- | --- | --- |
| 系统管理员 | 0 | 3（系统权限） | 全部功能可见，可管理用户、角色 |
| 物业经理 | 1 | 2（管理权限） | 管理物业日常业务 |
| 物业员工 | 2 | 1（操作权限） | 执行具体操作 |
| 安保人员 | 3 | 1（操作权限） | 门岗、车辆、访客相关操作 |
| 业主 | 4 | 0（无权限） | 查看与自己相关的资产、账单 |
| 租户 | 5 | 0（无权限） | 权限同业主 |

后端通过 `@RequireRole` 和 `@RequirePermission` 注解 + AOP 切面实现声明式权限控制。

## 内置测试账号

> 所有测试账号默认密码：`123456`

| 用户名 | UID | 角色 |
| --- | --- | --- |
| TestAdmin | admin | 系统管理员 |
| TestManger | manager | 物业经理 |
| TestEmployee | employee | 物业员工 |
| TestUser | user | 业主 |

## 环境要求

| 软件 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 21 | `pom.xml` 中 `java.version` 指定 |
| Maven | 3.6+ | 或使用 Maven Wrapper（`mvnw`） |
| MySQL | 8.0+ | 数据库字符集 utf8mb4 |
| Node.js | 18+ | 用于运行前端 Vite 项目 |
| npm | 9+ | 前端包管理 |

## 快速开始

### 1. 克隆项目

```bash
git clone https://gitee.com/moonne/community-manage-system.git
cd community-manage-system
```

### 2. 初始化数据库

在 MySQL 中创建数据库并导入脚本：

```sql
CREATE DATABASE cms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

```bash
mysql -u root -p cms < cms.sql
```

脚本包含完整的表结构、外键约束和种子数据（角色、类型字典、测试用户等）。

### 3. 配置后端环境变量

在 `src/main/resources/` 下创建 `env.yaml`（已加入 `.gitignore`）：

```yaml
datasource:
  url: jdbc:mysql://localhost:3306/cms?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
  username: root
  password: your_password

jwt:
  secret: your-jwt-secret-key-at-least-256-bits
  expiration: 86400000
```

### 4. 启动后端

```bash
# 使用 Maven Wrapper（推荐）
./mvnw spring-boot:run

# 或使用本地 Maven
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`，可通过 Actuator 端点 `/actuator` 查看运行状态。

### 5. 启动前端

```bash
cd cms_webui
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`，请求自动代理到后端 `localhost:8080`（Vite proxy 配置在 `vite.config.js` 中）。

### 6. 登录系统

打开浏览器访问 `http://localhost:5173`，使用内置测试账号登录。

## 打包部署

### 后端

```bash
mvn clean package -DskipTests
java -jar target/CommunityManageSystem-*.jar
```

### 前端

```bash
cd cms_webui
npm run build
```

生成的 `dist/` 目录可部署到 Nginx 等静态资源服务器。生产环境下需将 `/api` 请求反向代理到后端服务。

## API 接口约定

- 统一前缀：`/api`
- 认证方式：`Authorization: Bearer <token>`（登录/注册/验证码接口除外）
- 统一响应格式：`{ code: 200, message: "success", data: ... }`
- 分页响应：`{ code: 200, message: "success", data: { total, list, pageNum, pageSize, pages } }`
- 错误码：`401` 未认证、`403` 权限不足/账号冻结

## 常见问题

**Q: 后端启动报 `Failed to configure a DataSource`**

检查 `env.yaml` 是否存在且数据库连接信息正确，确认 MySQL 已启动且 `cms` 库已创建。

**Q: 前端请求返回 401**

确认后端已启动，登录接口正常返回 token。未登录直接访问管理页面会被路由守卫重定向到登录页。

**Q: `npm install` 失败或缓慢**

```bash
npm install --registry=https://registry.npmmirror.com
```

**Q: 账号被冻结无法操作**

前端在写入请求时主动拦截冻结账号，`403` 响应中携带「冻结」关键词时自动更新本地状态。管理员可在用户管理页面解冻。

## License

[MIT License](LICENSE)
