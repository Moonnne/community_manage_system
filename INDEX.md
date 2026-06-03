# CommunityManageSystem — 项目索引

> 翻斗花园物业管理系统。Spring Boot 4 + Vue 3 全栈项目，面向社区物业的资产、设施、安防、财务、工单管理平台。

---

## 1. 项目概览

| 属性 | 值 |
|------|-----|
| 项目名称 | CommunityManageSystem (翻斗花园物业管理系统) |
| 基础框架 | Spring Boot 4.0.6 (Java 21) |
| 持久层 | MyBatis + MyBatis-Spring-Boot-Starter 4.0.1 + PageHelper 6.1.1 |
| 数据库 | MySQL 8.x, 连接池 Druid 1.2.28 |
| 安全框架 | Spring Security (无状态会话) + JWT (jjwt 0.13.0) + AOP 注解权限 |
| 前端 | Vue 3.5 + Vite 8 + Element Plus 2.13 + Pinia 3 + ECharts 6 |
| 构建工具 | Maven (后端) + Vite (前端) |
| 关键依赖 | Lombok, FastJSON2, MinIO 9.0, Apache POI 5.5, Hutool 5.8 |

### 目录结构

```
CommunityManageSystem/
├── pom.xml                    # Maven 父 POM
├── cms.sql                    # 数据库建表 + 种子数据
├── src/main/java/edu/tut/cms/
│   ├── CommunityManageSystemApplication.java  # 启动类, @MapperScan
│   ├── common/
│   │   ├── aop/               # @RequireRole / @RequirePermission 注解 + AuthAspect 切面
│   │   ├── config/            # SecurityConfig, WebMvcConfig, OssConfig, PageHelperConfig
│   │   ├── context/           # UserContext (ThreadLocal) + CurrentUser DTO
│   │   ├── exception/         # GlobalExceptionHandler + BusinessException/ForbiddenException/UnauthorizedException
│   │   ├── filter/            # JwtAuthenticationFilter (OncePerRequestFilter)
│   │   ├── result/            # Result<T> (统一响应体) + PageResult<T> (分页响应体)
│   │   └── utils/             # JwtUtils, PasswordUtils, OssUtils, JsonUtils
│   ├── pojo/
│   │   ├── dto/               # 入参 DTO (xxxAddDTO, xxxUpdateDTO, xxxLoginDTO 等)
│   │   ├── entity/            # 数据库实体 (Lombok @Builder)
│   │   └── vo/                # 出参 VO (返回给前端)
│   └── server/
│       ├── controller/        # REST 控制器 (共 11 个)
│       ├── service/           # 服务接口 (共 10 个)
│       ├── service/impl/      # 服务实现 (共 10 个)
│       └── mapper/            # MyBatis Mapper 接口 (共 17 个)
├── src/main/resources/
│   ├── application.yaml       # 应用配置 (端口 8080, 数据源, MyBatis, JWT)
│   └── mapper/                # MyBatis XML 映射文件 (共 11 个)
├── src/test/java/edu/tut/cms/ # 测试代码 (WebMvcTest + AOP test config)
└── cms_webui/                 # Vue 3 前端项目
    ├── package.json           # 前端依赖
    ├── vite.config.js         # Vite 配置 (端口 5173, /api 代理到 localhost:8080)
    └── src/
        ├── main.js            # 入口: 创建 app, 注册 Element Plus / Router / Pinia
        ├── App.vue            # 根组件
        ├── api/               # 后端 API 调用 (axios, 每个模块一个文件)
        ├── router/index.js    # Vue Router 路由表 (含导航守卫)
        ├── stores/            # Pinia store (user.js, pageCache.js)
        ├── layout/            # MainLayout.vue (顶栏 + 侧边栏 + 响应式抽屉)
        ├── components/        # 通用组件 (WizardFormDrawer, DualSelectV2, AvatarEditor 等)
        ├── utils/             # request.js (axios 实例+拦截器), validators.js, mobileTableActions.js
        ├── styles/            # theme.css (CSS 变量), index.css
        └── views/             # 页面组件 (按模块分子目录)
```

---

## 2. 数据库表概览 (14 张表)

### 2.1 核心业务表

| 表名 | 主键 | 业务编码列 | 说明 |
|------|------|-----------|------|
| `user` | `id` (bigint, auto) | `uid` (varchar) | 用户表, 角色 0=管理员 1=经理 2=员工 3=安保 4=业主 5=租户 |
| `user_role` | `role_id` (int, 手动) | — | 角色表, `role_promission` 0=无 1=操作 2=管理 3=系统 |
| `asset` | `id` (bigint, auto) | `asset_code` | 资产表 (房产/停车位), 关联 user/asset_type/building |
| `asset_type` | `asset_type_id` (int, 手动) | — | 资产类型表, `asset_type_short` 作为编码前缀 (B=房产, T=停车位) |
| `building` | `id` (bigint, 手动) | `building_code` | 建筑物表, 含楼层范围 |
| `facility` | `id` (bigint, auto) | `facility_code` | 公共设施表, 关联 user/building/facility_type |
| `facility_type` | `facility_type_id` (int, 手动) | — | 设施类型表 (变电器/水泵/电梯/摄像头/门禁等) |
| `bill` | `id` (bigint, auto) | `bill_code` | 账单主表, 关联 user(owner/creater)/asset |
| `bill_detail` | `id` (bigint, auto) | — | 账单明细/账目表, 关联 bill/bill_type |
| `bill_type` | `bill_type_id` (int, 手动) | — | 账目类型表 (物业费/水电气/维修等) |
| `visitor` | `id` (bigint, auto) | `visitor_code` | 访客表, 关联 user(owner/creater)/asset/visitor_type |
| `visitor_type` | `visitor_type_id` (int, 手动) | — | 访客类型表 (亲友/外卖/快递/家政/公务) |
| `car` | `id` (bigint, auto) | `car_code` | 车辆表, 关联 user/asset/visitor/car_type |
| `car_type` | `car_type_id` (int, 手动) | — | 车辆类型表 (业主/访客/服务/特种) |
| `car_history` | `id` (bigint, auto) | `car_history_code` | 车辆通行记录表, 关联 user/car/gate |
| `gate` | `id` (int, auto) | `gate_code` | 门岗表, 关联 user(负责人) |
| `task` | `id` (bigint, auto) | `task_code` | 工单表, 关联 user(owner/operator)/task_type |
| `task_type` | `task_type_id` (int, 手动) | — | 工单类型表 (投诉/报修/建议/退款/账户) |

### 2.2 外键关系图

```
user ──┬── asset (owner) ──┬── bill (owner)
       │                   ├── bill (asset)
       │                   └── car (asset)
       ├── bill (creater)
       ├── facility (chief)
       ├── gate (chief)
       ├── visitor (owner, creater)
       ├── car (owner)
       ├── car_history (operator)
       └── task (owner, operator)

asset_type ─── asset
building  ──┬── asset
            └── facility

facility_type ─── facility
visitor_type  ─── visitor
visitor       ─── car
car_type      ─── car
car           ─── car_history
gate          ─── car_history
bill_type     ─── bill_detail
bill          ─── bill_detail
task_type     ─── task
user_role     ─── user
```

---

## 3. 后端架构

### 3.1 分层结构

```
Controller (@RestController, @RequestMapping("/api/xxx"))
  ↓ 调用
Service Interface
  ↓ 实现
ServiceImpl (@Service, @Transactional)
  ↓ 调用
Mapper Interface (@Mapper, 注解 SQL 或 XML)
  ↓ 查询
Database (MySQL)
```

### 3.2 统一响应格式

```java
// Result<T> — 所有 API 响应的外壳
{ "code": 200, "message": "success", "data": ... }

// PageResult<T> — 分页响应的 data 格式
{ "total": 100, "pageNum": 1, "pageSize": 10, "pages": 10, "records": [...] }
```

`PageResult.of(List)` 是工具方法，内部用 `PageInfo<T>` 提取分页信息。

### 3.3 安全模型

**三层权限控制：**

1. **Spring Security Filter Chain** (`SecurityConfig.java`):
   - 无状态会话 (`SessionCreationPolicy.STATELESS`)
   - CSRF 禁用
   - `/api/user/login`, `/api/user/register`, `/api/user/captcha` 白名单放行
   - 其他请求需认证

2. **JWT 认证过滤器** (`JwtAuthenticationFilter.java`):
   - 从 `Authorization: Bearer <token>` 头提取 JWT
   - 解析 token 得到用户信息, 校验用户状态 (禁用/冻结)
   - 冻结用户 (status=1): 禁止写操作 (`POST/PUT/DELETE`), 但 GET 仍然可用
   - 用户信息存入 `UserContext` (ThreadLocal), 请求结束自动清理

3. **AOP 注解权限** (`AuthAspect.java`):
   - `@RequireRole({0, 1, 3})` — 仅允许指定角色 ID 列表访问
   - `@RequirePermission(minLevel = 1)` — 按权限等级 (0=查看 1=操作 2=管理 3=系统)
   - 角色到权限等级的映射: `{0:3, 1:2, 2:1, 3:1, 4:0, 5:0}`

**角色定义:**
| ID | 名称 | 权限等级 |
|----|------|---------|
| 0 | 系统管理员 | 3 (系统) |
| 1 | 物业经理 | 2 (管理) |
| 2 | 物业员工 | 1 (操作) |
| 3 | 安保人员 | 1 (操作) |
| 4 | 业主 | 0 (查看) |
| 5 | 租户 | 0 (查看) |

### 3.4 数据权限 (行级)

在 Service 层实现, 非通用模式:
- **业主/租户只能看自己的数据**: 在 `pageXxx` 方法中检查 `currentUser.getUserRole() >= 4`, 则强制将 `ownerId` 设为当前用户 ID
- 适用的模块: Asset, Bill, Car, Visitor, Task

### 3.5 异常处理

`GlobalExceptionHandler` (@RestControllerAdvice) 统一处理:
- `BusinessException` → 500 + 自定义消息
- `UnauthorizedException` → 401
- `ForbiddenException` → 403
- `MethodArgumentNotValidException` → 取第一个字段错误消息返回
- `Exception` → 500 + "服务器内部错误"

### 3.6 控制器清单

| Controller | 路径前缀 | 主要功能 |
|------------|---------|---------|
| `UserController` | `/api/user` | 登录/注册/CRUD/角色管理/初始化/改密码/状态管理/头像上传 |
| `AssetController` | `/api/asset` | 资产 CRUD/分页/Excel 导出/资产类型管理 |
| `BuildingController` | `/api/building` | 建筑物 CRUD/分页 |
| `FacilityController` | `/api/facility` | 设施 CRUD/分页/临期查询/设施类型管理 |
| `BillController` | `/api/bill` | 账单 CRUD/批量删除/支付/明细管理/汇总/导出/账单类型管理 |
| `VisitorController` | `/api/visitor` | 访客 CRUD/分页/访客类型管理 |
| `CarController` | `/api/car` | 车辆 CRUD/分页/车辆类型查询 |
| `CarHistoryController` | `/api/car_history` | 通行记录新增/分页查询 |
| `GateController` | `/api/gate` | 门岗 CRUD/列表 |
| `TaskController` | `/api/task` | 工单 CRUD/分页/处理/工单类型管理 |
| `DashboardController` | `/api/dashboard` | 安全面板/设施面板/资产面板/工单面板数据聚合 |

---

## 4. 业务编码生成规则 (重要!)

不同模块使用**不同的编码生成策略**, 这是理解和修改项目的关键:

### 4.1 时间戳型 (简单, 无冲突检测)

| 模块 | 前缀 | 格式 | 代码位置 |
|------|------|------|---------|
| **Visitor** | `V` | `V` + `System.currentTimeMillis()` | `VisitorServiceImpl.java:39` |
| **CarHistory** | `CH` | `CH` + `System.currentTimeMillis()` | `CarHistoryServiceImpl.java:35` |

⚠️ **风险**: 高并发下同毫秒可能重复, 且无 UNIQUE 约束保护。

### 4.2 随机字符串型 (低冲突概率)

| 模块 | 前缀 | 格式 | 代码位置 |
|------|------|------|---------|
| **Bill** | `B-` | `B-` + 16位随机小写字母数字 (SecureRandom) | `BillServiceImpl.java:437-449` |

有冲突检测 (最多重试 20 次后抛异常)。

### 4.3 结构化编码型 (含位置/类型信息, 有冲突检测)

| 模块 | 前缀 | 格式 | 代码位置 |
|------|------|------|---------|
| **Asset** | 类型缩写 | 类型缩写(1-2位大写字母) + 建筑物编码后2位 + 楼层2位 + 8位随机数 | `AssetServiceImpl.java:226-240` |
| **Facility** | `F` | `F` + 建筑物编码后2位 + 楼层2位 + 8位随机数 | `FacilityServiceImpl.java:135-143` |
| **Task** | `T` | `T` + `yyyyMMddHH` (10位) + 4位随机数 | `TaskServiceImpl.java:164-173` |

示例: 资产编码 `B0001` = 类型B(房产) + 建筑物01 + 楼层01 + 00000000

### 4.4 序列型

| 模块 | 格式 | 代码位置 |
|------|------|---------|
| **Building** | `B%04d` (B + 4位数字 = 自增 ID) | `BuildingServiceImpl.java:32` |

### 4.5 手动输入型

| 模块 | 说明 |
|------|------|
| **Car** | `car_code` 由前端/调用方传入, 后端不做生成 |
| **Gate** | `gate_code` 由前端/调用方传入, 后端不做生成 |

---

## 5. 前端架构

### 5.1 技术栈

| 依赖 | 版本 | 用途 |
|------|------|------|
| vue | ^3.5.32 | 核心框架 |
| vite | ^8.0.10 | 构建工具 |
| element-plus | ^2.13.7 | UI 组件库 |
| pinia | ^3.0.4 | 状态管理 |
| vue-router | ^4.6.4 | 路由 |
| axios | ^1.16.0 | HTTP 请求 |
| echarts + vue-echarts | ^6.1.0 / ^8.0.1 | 图表 |

### 5.2 路由结构

```
/login                    → Login.vue (公开)
/register                 → Register.vue (公开)
/ (MainLayout)
  /welcome                → Welcome.vue (首页)
  /user/list              → UserList.vue (用户信息, 角色 0/1)
  /user/role              → UserRole.vue (权限管理, 角色 0)
  /finance/dashboard      → FinanceDashboard.vue (财务面板)
  /finance/bill           → BillList.vue (账单管理)
  /finance/type           → BillType.vue (账单类型)
  /security/dashboard     → SecurityDashboard.vue (安全面板)
  /security/visitor       → VisitorList.vue (访客管理)
  /security/visitor-type  → VisitorType.vue (访客类型)
  /security/car           → CarList.vue (车辆管理)
  /security/car-history   → CarHistory.vue (通行记录)
  /security/gate          → GateList.vue (门岗管理)
  /facility/dashboard     → FacilityDashboard.vue (设施面板)
  /facility/list          → FacilityList.vue (设施信息)
  /facility/type          → FacilityType.vue (设施类型)
  /asset/dashboard        → AssetDashboard.vue (资产面板)
  /asset/property         → PropertyList.vue (房产管理)
  /asset/parking          → ParkingList.vue (停车位)
  /asset/type             → AssetType.vue (资产类型)
  /asset/building         → BuildingList.vue (建筑物管理)
  /task/dashboard         → TaskDashboard.vue (工单面板)
  /task/list              → TaskList.vue (工单列表)
  /task/type              → TaskType.vue (工单类型)
  /settings               → UserSettings.vue (个人设置)
```

### 5.3 导航守卫

- `to.path` 不是 `/login` 或 `/register` 且未登录 → 重定向到 `/login`
- 已登录用户访问公开页 → 重定向到 `/welcome`
- 前端角色可见性控制: `MainLayout.vue` 中的 `allNavItems` 和 `sidebarConfig` 均定义了 `roles` 数组, 根据 `userStore.userRole` 过滤

### 5.4 Axios 拦截器 (request.js)

**请求拦截器:**
- 自动附加 `Authorization: Bearer <token>` 头
- 冻结用户 (status=1) 拦截写操作, 在请求阶段就拒绝
- GET 请求结果缓存 (`pageCache.js`), 以 URL+用户ID 为 key
- 写请求 (`POST/PUT/DELETE`) 自动清除全部缓存

**响应拦截器:**
- `code !== 200` → 显示错误消息
- `code === 401` → 清除 token, 跳转登录页
- `code === 403` (含"冻结") → 更新本地用户状态为冻结
- 写请求成功后清除缓存, GET 请求结果写入缓存

### 5.5 Pinia Store

**useUserStore** (`stores/user.js`):
- `token` / `userInfo` 持久化到 localStorage
- 计算属性: `isLoggedIn`, `userRole`, `userStatus`, `canOperate`, `isFrozen`, `isDisabled`
- 角色常量: `ROLE_ADMIN=0`, `ROLE_MANAGER=1`, `ROLE_PROPERTY=2`, `ROLE_SECURITY=3`, `ROLE_OWNER=4`

**usePageCacheStore** (`stores/pageCache.js`):
- 内存级 GET 请求缓存 (Map)
- 写操作触发全部清空

### 5.6 通用组件

| 组件 | 用途 |
|------|------|
| `WizardFormDrawer.vue` | 分步骤的表单抽屉 (添加/编辑通用组件) |
| `DualSelectV2.vue` | 双列表穿梭选择器 |
| `ConfirmInfoTable.vue` | 确认信息展示表格 |
| `AvatarEditor.vue` | 头像裁剪上传编辑器 |
| `AccountInitializationDialog.vue` | 新用户首次登录的初始化弹窗 |
| `InputOtpCompat.vue` | OTP 验证码输入 |
| `TechBackground.vue` | 登录/注册页科技感背景 |

### 5.7 前端 API 文件

`cms_webui/src/api/` 下每个文件对应一个后端 Controller:
`asset.js`, `bill.js`, `building.js`, `car.js`, `dashboard.js`, `facility.js`, `gate.js`, `task.js`, `user.js`, `visitor.js`

---

## 6. 关键设计模式与约定

### 6.1 代码风格

- **Lombok**: 所有 Entity/DTO/VO 使用 `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- **构造器注入**: 使用 `@RequiredArgsConstructor` (Lombok) 代替 `@Autowired`
- **日志**: 使用 `@Slf4j` + `log`
- **事务**: Service 写操作使用 `@Transactional(rollbackFor = Exception.class)`
- **参数校验**: DTO 使用 `@Valid` + Jakarta Validation 注解 (如 `@NotBlank`, `@NotNull`)
- **Controller**: `@RestController` + `@RequestMapping` + `@RequiredArgsConstructor`

### 6.2 MyBatis 模式

两种 SQL 编写方式共存:
- **注解 SQL**: 简单 CRUD (insert/deleteById/selectById/selectAll) 直接在 Mapper 接口上用 `@Insert/@Delete/@Select`
- **XML SQL**: 复杂查询 (分页查询 `selectPage`, 动态更新 `updateById`, 聚合查询) 在 `resources/mapper/*.xml` 中

### 6.3 分页模式

所有分页查询遵循统一流程:
1. `PageHelper.startPage(pageNum, pageSize)` 启动分页
2. 调用 Mapper 的 `selectPage(...)` 查询列表
3. 用 `PageInfo<>(list)` 获取分页信息
4. 组装 `PageResult<T>` 返回

⚠️ **注意**: 部分 ServiceImpl 手动设置 PageResult 字段而不是使用 `PageResult.of()`。

### 6.4 配置管理

- `application.yaml` 中敏感值 (数据源/JWT) 使用 `${}` 占位符, 实际值来自 `classpath:env.yaml` (未提交到 Git)
- OSS 配置在 `OssConfig.java` / `OssProperties.java` 中

---

## 7. 模块依赖关系 (业务功能角度)

```
用户管理 (User) ─── 所有模块的基础 (用户作为 owner/creater/chief/operator)
  ├── 资产管理 (Asset + Building + AssetType)
  │     ├── 财务/账单 (Bill + BillDetail + BillType)
  │     └── 车辆 (Car)  [车辆可关联车位资产]
  ├── 公共设施 (Facility + FacilityType) [设施可关联建筑物]
  ├── 社区安全
  │     ├── 访客管理 (Visitor + VisitorType)
  │     ├── 车辆管理 (Car + CarType)
  │     ├── 通行记录 (CarHistory)  [关联 Car + Gate]
  │     └── 门岗管理 (Gate)
  └── 工单管理 (Task + TaskType)
```

---

## 8. 仪表盘 API (DashboardController)

四个面板端点, 用于首页数据聚合:

| 端点 | 功能 | 关键统计指标 |
|------|------|------------|
| `GET /api/dashboard/security?range=` | 安全面板 | 今日访客数, 滞留访客数, 车流量, 同比变化率, 时间序列流量 |
| `GET /api/dashboard/facility` | 设施面板 | 注册数, 临期数, 需检修数, 异常数 |
| `GET /api/dashboard/asset` | 资产面板 | 资产数, 即将到期数, 已过期数, 资产列表 |
| `GET /api/dashboard/task` | 工单面板 | 我的工单列表, 待处理工单列表, 是否有处理权限 |

---

## 9. 文件上传

- 头像上传: `POST /api/user/upload-avatar` → MinIO (OSS)
- OSS 配置通过 `OssConfig` / `OssProperties` 注入

---

## 10. 已知问题与注意事项

1. **Visitor 编码并发安全**: `V + System.currentTimeMillis()` 无冲突检测/无 UNIQUE 约束, 并发下可能重复
2. **CarHistory 编码同上**: `CH + System.currentTimeMillis()` 同样问题
3. **分页结果构建不一致**: 有的 ServiceImpl 用 `PageResult.of()`, 有的手动 set 各字段
4. **TaskServiceImpl.pageTasks** 中 `PageResult` 手动组装而不是用 `PageResult.of()`
5. **部分实体主键策略不统一**: asset_type / bill_type 等手动指定 ID (int), user 是自增 (bigint)
6. **building.id** 手动赋值 (`selectMaxId + 1`) 而非自增, 有并发问题
7. **Car.code 和 Gate.code** 无后端生成逻辑, 完全依赖前端/调用方传入
8. **Bill 编码变更**: 旧数据用 `B00000001` 格式, 新数据用 `B-` + 16位随机字符 (`B-fkvdyvxplxyr5fzj`)
