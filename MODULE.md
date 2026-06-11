# CommunityManageSystem 模块文件索引

本文档按项目的七个业务模块整理后端与前端文件：AI Agent、用户管理、财务管理、资产管理、安防管理、设施管理、工单管理。七个模块共同依赖的启动类、配置、安全、异常、通用响应、前端框架、公共组件等单独列为"七模块共用支撑"。

当前工作树已同步到本地 `master` 最新提交：`5946d16`。版本: `1.1.0-RELEASE`。

## 1. AI Agent 模块 (v1.1 新增)

功能范围：自然语言驱动的页面跳转、数据查询、操作确认、SSE 流式对话、Spring AI DeepSeek 集成。

### 后端

| 类型 | 文件 |
|---|---|
| Controller | `src/main/java/edu/tut/cms/server/controller/AgentController.java` |
| Service | `src/main/java/edu/tut/cms/server/service/AgentService.java` |
| ServiceImpl | `src/main/java/edu/tut/cms/server/service/impl/AgentServiceImpl.java` |
| Config | `src/main/java/edu/tut/cms/common/config/DeepSeekAgentProperties.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/AgentChatDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/AgentExecuteDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/AgentMessageDTO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/AgentActionVO.java` |

### 前端

| 类型 | 文件 |
|---|---|
| API | `cms_webui/src/api/agent.js` |
| 组件 | `cms_webui/src/components/AgentChatWindow.vue` |
| 工具 | `cms_webui/src/utils/agentControl.js` |

## 2. 用户管理模块

功能范围：登录、注册、验证码、用户资料、账户初始化、修改密码、头像上传、用户列表、用户状态、角色/权限管理。

### 后端

| 类型 | 文件 |
|---|---|
| Controller | `src/main/java/edu/tut/cms/server/controller/UserController.java` |
| Service | `src/main/java/edu/tut/cms/server/service/UserService.java` |
| ServiceImpl | `src/main/java/edu/tut/cms/server/service/impl/UserServiceImpl.java` |
| Mapper | `src/main/java/edu/tut/cms/server/mapper/UserMapper.java` |
| Mapper | `src/main/java/edu/tut/cms/server/mapper/UserRoleMapper.java` |
| Mapper XML | `src/main/resources/mapper/UserMapper.xml` |
| Entity | `src/main/java/edu/tut/cms/pojo/entity/User.java` |
| Entity | `src/main/java/edu/tut/cms/pojo/entity/UserRole.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/UserAddDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/UserInitializeDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/UserLoginDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/UserPasswordDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/UserRegisterDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/UserRoleAddDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/UserStatusDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/UserUpdateDTO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/UserVO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/UserRoleVO.java` |

### 前端

| 类型 | 文件 |
|---|---|
| API | `cms_webui/src/api/user.js` |
| 页面 | `cms_webui/src/views/Login.vue` |
| 页面 | `cms_webui/src/views/Register.vue` |
| 页面 | `cms_webui/src/views/UserSettings.vue` |
| 页面 | `cms_webui/src/views/user/UserList.vue` |
| 页面 | `cms_webui/src/views/user/UserRole.vue` |
| Store | `cms_webui/src/stores/user.js` |
| 组件 | `cms_webui/src/components/AccountInitializationDialog.vue` |
| 组件 | `cms_webui/src/components/AvatarEditor.vue` |
| 组件 | `cms_webui/src/components/InputOtpCompat.vue` |

## 3. 财务管理模块

功能范围：财务面板、账单管理、账单明细、账单类型、支付、账单状态流转、个人账单汇总。

### 后端

| 类型 | 文件 |
|---|---|
| Controller | `src/main/java/edu/tut/cms/server/controller/BillController.java` |
| Service | `src/main/java/edu/tut/cms/server/service/BillService.java` |
| ServiceImpl | `src/main/java/edu/tut/cms/server/service/impl/BillServiceImpl.java` |
| Mapper | `src/main/java/edu/tut/cms/server/mapper/BillMapper.java` |
| Mapper | `src/main/java/edu/tut/cms/server/mapper/BillDetailMapper.java` |
| Mapper | `src/main/java/edu/tut/cms/server/mapper/BillTypeMapper.java` |
| Mapper XML | `src/main/resources/mapper/BillMapper.xml` |
| Mapper XML | `src/main/resources/mapper/BillDetailMapper.xml` |
| Entity | `src/main/java/edu/tut/cms/pojo/entity/Bill.java` |
| Entity | `src/main/java/edu/tut/cms/pojo/entity/BillDetail.java` |
| Entity | `src/main/java/edu/tut/cms/pojo/entity/BillType.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/BillAddDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/BillBatchDeleteDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/BillDetailAddDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/BillDetailPayDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/BillDetailUpdateDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/BillPayDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/BillStatusDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/BillTypeAddDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/BillUpdateDTO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/BillVO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/BillDetailVO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/BillSummaryVO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/BillTypeVO.java` |

### 前端

| 类型 | 文件 |
|---|---|
| API | `cms_webui/src/api/bill.js` |
| 页面 | `cms_webui/src/views/finance/FinanceDashboard.vue` |
| 页面 | `cms_webui/src/views/finance/BillList.vue` |
| 页面 | `cms_webui/src/views/finance/BillType.vue` |

## 4. 资产管理模块

功能范围：资产面板、房产、停车位、资产类型、建筑物、资产到期与位置管理。

### 后端

| 类型 | 文件 |
|---|---|
| Controller | `src/main/java/edu/tut/cms/server/controller/AssetController.java` |
| Controller | `src/main/java/edu/tut/cms/server/controller/BuildingController.java` |
| Service | `src/main/java/edu/tut/cms/server/service/AssetService.java` |
| Service | `src/main/java/edu/tut/cms/server/service/BuildingService.java` |
| ServiceImpl | `src/main/java/edu/tut/cms/server/service/impl/AssetServiceImpl.java` |
| ServiceImpl | `src/main/java/edu/tut/cms/server/service/impl/BuildingServiceImpl.java` |
| Mapper | `src/main/java/edu/tut/cms/server/mapper/AssetMapper.java` |
| Mapper | `src/main/java/edu/tut/cms/server/mapper/AssetTypeMapper.java` |
| Mapper | `src/main/java/edu/tut/cms/server/mapper/BuildingMapper.java` |
| Mapper XML | `src/main/resources/mapper/AssetMapper.xml` |
| Mapper XML | `src/main/resources/mapper/BuildingMapper.xml` |
| Entity | `src/main/java/edu/tut/cms/pojo/entity/Asset.java` |
| Entity | `src/main/java/edu/tut/cms/pojo/entity/AssetType.java` |
| Entity | `src/main/java/edu/tut/cms/pojo/entity/Building.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/AssetAddDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/AssetTypeAddDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/AssetUpdateDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/BuildingAddDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/BuildingUpdateDTO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/AssetVO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/AssetTypeVO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/BuildingVO.java` |

### 前端

| 类型 | 文件 |
|---|---|
| API | `cms_webui/src/api/asset.js` |
| API | `cms_webui/src/api/building.js` |
| 页面 | `cms_webui/src/views/asset/AssetDashboard.vue` |
| 页面 | `cms_webui/src/views/asset/AssetList.vue` |
| 页面 | `cms_webui/src/views/asset/PropertyList.vue` |
| 页面 | `cms_webui/src/views/asset/ParkingList.vue` |
| 页面 | `cms_webui/src/views/asset/AssetType.vue` |
| 页面 | `cms_webui/src/views/asset/BuildingList.vue` |

## 5. 安防管理模块

功能范围：安防面板、访客登记与审核、访客类型、车辆管理、车辆类型、通行记录、门岗配置。

### 后端

| 类型 | 文件 |
|---|---|
| Controller | `src/main/java/edu/tut/cms/server/controller/VisitorController.java` |
| Controller | `src/main/java/edu/tut/cms/server/controller/CarController.java` |
| Controller | `src/main/java/edu/tut/cms/server/controller/CarHistoryController.java` |
| Controller | `src/main/java/edu/tut/cms/server/controller/GateController.java` |
| Service | `src/main/java/edu/tut/cms/server/service/VisitorService.java` |
| Service | `src/main/java/edu/tut/cms/server/service/CarService.java` |
| Service | `src/main/java/edu/tut/cms/server/service/CarHistoryService.java` |
| Service | `src/main/java/edu/tut/cms/server/service/GateService.java` |
| ServiceImpl | `src/main/java/edu/tut/cms/server/service/impl/VisitorServiceImpl.java` |
| ServiceImpl | `src/main/java/edu/tut/cms/server/service/impl/CarServiceImpl.java` |
| ServiceImpl | `src/main/java/edu/tut/cms/server/service/impl/CarHistoryServiceImpl.java` |
| ServiceImpl | `src/main/java/edu/tut/cms/server/service/impl/GateServiceImpl.java` |
| Mapper | `src/main/java/edu/tut/cms/server/mapper/VisitorMapper.java` |
| Mapper | `src/main/java/edu/tut/cms/server/mapper/VisitorTypeMapper.java` |
| Mapper | `src/main/java/edu/tut/cms/server/mapper/CarMapper.java` |
| Mapper | `src/main/java/edu/tut/cms/server/mapper/CarTypeMapper.java` |
| Mapper | `src/main/java/edu/tut/cms/server/mapper/CarHistoryMapper.java` |
| Mapper | `src/main/java/edu/tut/cms/server/mapper/GateMapper.java` |
| Mapper XML | `src/main/resources/mapper/VisitorMapper.xml` |
| Mapper XML | `src/main/resources/mapper/CarMapper.xml` |
| Mapper XML | `src/main/resources/mapper/CarHistoryMapper.xml` |
| Mapper XML | `src/main/resources/mapper/GateMapper.xml` |
| Entity | `src/main/java/edu/tut/cms/pojo/entity/Visitor.java` |
| Entity | `src/main/java/edu/tut/cms/pojo/entity/VisitorType.java` |
| Entity | `src/main/java/edu/tut/cms/pojo/entity/Car.java` |
| Entity | `src/main/java/edu/tut/cms/pojo/entity/CarType.java` |
| Entity | `src/main/java/edu/tut/cms/pojo/entity/CarHistory.java` |
| Entity | `src/main/java/edu/tut/cms/pojo/entity/Gate.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/VisitorAddDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/VisitorTypeAddDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/VisitorUpdateDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/CarAddDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/CarTypeAddDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/CarUpdateDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/CarHistoryAddDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/CarHistoryBatchDeleteDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/CarHistoryUpdateDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/GateAddDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/GateUpdateDTO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/VisitorVO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/VisitorTypeVO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/CarVO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/CarTypeVO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/CarHistoryVO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/GateVO.java` |

### 前端

| 类型 | 文件 |
|---|---|
| API | `cms_webui/src/api/visitor.js` |
| API | `cms_webui/src/api/car.js` |
| API | `cms_webui/src/api/gate.js` |
| 页面 | `cms_webui/src/views/security/SecurityDashboard.vue` |
| 页面 | `cms_webui/src/views/security/VisitorList.vue` |
| 页面 | `cms_webui/src/views/security/VisitorType.vue` |
| 页面 | `cms_webui/src/views/security/CarList.vue` |
| 页面 | `cms_webui/src/views/security/CarHistory.vue` |
| 页面 | `cms_webui/src/views/security/GateList.vue` |

## 6. 设施管理模块

功能范围：设施面板、公共设施台账、设施类型、检查周期、临期提醒、负责人指派。

### 后端

| 类型 | 文件 |
|---|---|
| Controller | `src/main/java/edu/tut/cms/server/controller/FacilityController.java` |
| Service | `src/main/java/edu/tut/cms/server/service/FacilityService.java` |
| ServiceImpl | `src/main/java/edu/tut/cms/server/service/impl/FacilityServiceImpl.java` |
| Mapper | `src/main/java/edu/tut/cms/server/mapper/FacilityMapper.java` |
| Mapper | `src/main/java/edu/tut/cms/server/mapper/FacilityTypeMapper.java` |
| Mapper XML | `src/main/resources/mapper/FacilityMapper.xml` |
| Entity | `src/main/java/edu/tut/cms/pojo/entity/Facility.java` |
| Entity | `src/main/java/edu/tut/cms/pojo/entity/FacilityType.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/FacilityAddDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/FacilityTypeAddDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/FacilityUpdateDTO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/FacilityVO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/FacilityTypeVO.java` |

### 前端

| 类型 | 文件 |
|---|---|
| API | `cms_webui/src/api/facility.js` |
| 页面 | `cms_webui/src/views/facility/FacilityDashboard.vue` |
| 页面 | `cms_webui/src/views/facility/FacilityList.vue` |
| 页面 | `cms_webui/src/views/facility/FacilityType.vue` |

## 7. 工单管理模块

功能范围：工单面板、工单列表、工单类型、提交、处理、状态流转、回复。

### 后端

| 类型 | 文件 |
|---|---|
| Controller | `src/main/java/edu/tut/cms/server/controller/TaskController.java` |
| Service | `src/main/java/edu/tut/cms/server/service/TaskService.java` |
| ServiceImpl | `src/main/java/edu/tut/cms/server/service/impl/TaskServiceImpl.java` |
| Mapper | `src/main/java/edu/tut/cms/server/mapper/TaskMapper.java` |
| Mapper | `src/main/java/edu/tut/cms/server/mapper/TaskTypeMapper.java` |
| Mapper XML | `src/main/resources/mapper/TaskMapper.xml` |
| Entity | `src/main/java/edu/tut/cms/pojo/entity/Task.java` |
| Entity | `src/main/java/edu/tut/cms/pojo/entity/TaskType.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/TaskAddDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/TaskTypeAddDTO.java` |
| DTO | `src/main/java/edu/tut/cms/pojo/dto/TaskUpdateDTO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/TaskVO.java` |
| VO | `src/main/java/edu/tut/cms/pojo/vo/TaskTypeVO.java` |

### 前端

| 类型 | 文件 |
|---|---|
| API | `cms_webui/src/api/task.js` |
| 页面 | `cms_webui/src/views/task/TaskDashboard.vue` |
| 页面 | `cms_webui/src/views/task/TaskList.vue` |
| 页面 | `cms_webui/src/views/task/TaskType.vue` |

## 8. 七模块共用支撑

这些文件不属于某个单一业务模块，而是被所有业务模块共同使用。

### 后端启动与配置

| 类型 | 文件 |
|---|---|
| 启动类 | `src/main/java/edu/tut/cms/CommunityManageSystemApplication.java` |
| 应用配置 | `src/main/resources/application.yaml` |
| Dashboard 聚合控制器 | `src/main/java/edu/tut/cms/server/controller/DashboardController.java` |
| Web 配置 | `src/main/java/edu/tut/cms/common/config/WebMvcConfig.java` |
| Security 配置 | `src/main/java/edu/tut/cms/common/config/SecurityConfig.java` |
| PageHelper 配置 | `src/main/java/edu/tut/cms/common/config/PageHelperConfig.java` |
| OSS 配置 | `src/main/java/edu/tut/cms/common/config/OssConfig.java` |
| OSS 属性 | `src/main/java/edu/tut/cms/common/config/OssProperties.java` |

### 后端安全、上下文、AOP、异常、响应

| 类型 | 文件 |
|---|---|
| AOP | `src/main/java/edu/tut/cms/common/aop/AuthAspect.java` |
| AOP 注解 | `src/main/java/edu/tut/cms/common/aop/RequirePermission.java` |
| AOP 注解 | `src/main/java/edu/tut/cms/common/aop/RequireRole.java` |
| 上下文 | `src/main/java/edu/tut/cms/common/context/CurrentUser.java` |
| 上下文 | `src/main/java/edu/tut/cms/common/context/UserContext.java` |
| 过滤器 | `src/main/java/edu/tut/cms/common/filter/JwtAuthenticationFilter.java` |
| 异常 | `src/main/java/edu/tut/cms/common/exception/BusinessException.java` |
| 异常 | `src/main/java/edu/tut/cms/common/exception/ForbiddenException.java` |
| 异常 | `src/main/java/edu/tut/cms/common/exception/UnauthorizedException.java` |
| 异常处理 | `src/main/java/edu/tut/cms/common/exception/GlobalExceptionHandler.java` |
| 响应对象 | `src/main/java/edu/tut/cms/common/result/Result.java` |
| 分页响应 | `src/main/java/edu/tut/cms/common/result/PageResult.java` |

### 后端工具

| 类型 | 文件 |
|---|---|
| 工具 | `src/main/java/edu/tut/cms/common/utils/JwtUtils.java` |
| 工具 | `src/main/java/edu/tut/cms/common/utils/PasswordUtils.java` |
| 工具 | `src/main/java/edu/tut/cms/common/utils/OssUtils.java` |
| 工具 | `src/main/java/edu/tut/cms/common/utils/JsonUtils.java` |

### 前端应用骨架

| 类型 | 文件 |
|---|---|
| 入口 | `cms_webui/src/main.js` |
| 根组件 | `cms_webui/src/App.vue` |
| 路由 | `cms_webui/src/router/index.js` |
| 主布局 | `cms_webui/src/layout/MainLayout.vue` |
| 首页 | `cms_webui/src/views/Welcome.vue` |
| Dashboard API 聚合 | `cms_webui/src/api/dashboard.js` |
| 请求封装 | `cms_webui/src/utils/request.js` |
| 校验工具 | `cms_webui/src/utils/validators.js` |
| 移动端表格工具 | `cms_webui/src/utils/mobileTableActions.js` |
| 页面缓存 Store | `cms_webui/src/stores/pageCache.js` |
| 全局样式 | `cms_webui/src/styles/index.css` |
| 主题样式 | `cms_webui/src/styles/theme.css` |

### 前端公共组件

| 类型 | 文件 |
|---|---|
| 表单抽屉 | `cms_webui/src/components/WizardFormDrawer.vue` |
| 双列选择器 | `cms_webui/src/components/DualSelectV2.vue` |
| 确认信息表 | `cms_webui/src/components/ConfirmInfoTable.vue` |
| 技术背景 | `cms_webui/src/components/TechBackground.vue` |
