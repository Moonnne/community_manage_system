# CommunityManageSystem

基于 Spring Boot、SSM 框架与 Vue 的小区物业管理系统。项目采用前后端分离结构，后端负责业务接口、数据访问与权限校验，前端负责管理端页面展示与交互操作，适用于小区物业日常管理、用户信息维护、业务数据管理等场景。

## 项目简介

CommunityManageSystem 是一个面向社区物业管理场景的 Web 管理系统。系统后端基于 Spring Boot 构建，并结合 Spring MVC、MyBatis 等 SSM 技术完成接口开发与数据库访问；前端位于 `cms_webui` 目录下，基于 Vue 实现后台管理页面。

项目主要目标是将传统物业管理中的住户、房屋、车位、公告、缴费、维修等业务流程进行数字化管理，提升物业人员对社区基础信息和日常事务的维护效率。

## 技术栈与依赖

### 后端

- Java
- Spring Boot
- Spring MVC
- MyBatis / SSM
- Maven
- MySQL
- Lombok
- 日志组件：SLF4J / Logback
- 数据库连接池：Druid 或 Spring Boot 默认数据源配置

### 前端

- Vue
- Vue Router
- Axios
- Element UI / Element Plus 类后台管理组件库
- Node.js
- npm

### 开发与构建工具

- Git
- Maven / Maven Wrapper
- Node.js 与 npm
- MySQL 数据库
- IntelliJ IDEA / VS Code 等开发工具

## 项目结构

```text
community-manage-system
├── .mvn/                         # Maven Wrapper 相关配置
├── cms_webui/                    # 前端 Vue 项目
│   ├── public/                   # 前端静态资源
│   ├── src/                      # 前端源码
│   │   ├── api/                  # 接口请求封装
│   │   ├── assets/               # 图片、样式等资源
│   │   ├── components/           # 公共组件
│   │   ├── router/               # 路由配置
│   │   ├── views/                # 页面视图
│   │   └── main.js               # 前端入口文件
│   ├── package.json              # 前端依赖与脚本配置
│   └── vue.config.js             # Vue 项目配置，若项目存在
├── src/                          # 后端 Spring Boot 项目源码
│   ├── main/
│   │   ├── java/                 # Java 业务代码
│   │   │   └── ...
│   │   │       ├── controller/   # 控制层，提供 REST 接口
│   │   │       ├── service/      # 业务逻辑层
│   │   │       ├── mapper/       # MyBatis 数据访问层
│   │   │       ├── entity/       # 实体类 / 数据模型
│   │   │       ├── config/       # Spring、跨域、拦截器等配置
│   │   │       └── utils/        # 工具类
│   │   └── resources/            # 配置文件与 Mapper XML
│   │       ├── application.yml   # 后端运行配置
│   │       └── mapper/           # MyBatis XML 映射文件
│   └── test/                     # 测试代码
├── pom.xml                       # Maven 项目配置
├── .gitignore                    # Git 忽略配置
└── README.md                     # 项目说明文档
```

说明：实际目录名称以当前仓库代码为准。若项目中存在数据库脚本文件，请优先在运行前执行该脚本完成数据库初始化。

## 功能特性

- 用户登录与身份认证
- 用户信息管理
- 角色与权限控制
- 住户信息管理
- 房屋信息管理
- 车位信息管理
- 物业公告管理
- 报修记录管理
- 缴费信息管理
- 数据分页查询
- 前后端分离接口交互
- 后台管理页面可视化操作
- 基于 MyBatis 的数据库持久化操作
- 统一接口返回格式，便于前端处理成功、失败和异常信息

## 环境要求

运行项目前，请确认本机已经安装以下软件：

| 软件 | 建议版本 | 说明 |
| --- | --- | --- |
| JDK | 8 或 17 | 用于运行 Spring Boot 后端项目，具体版本以 `pom.xml` 为准 |
| Maven | 3.6+ | 用于后端依赖安装与项目构建；也可使用 Maven Wrapper |
| MySQL | 5.7+ / 8.0+ | 用于存储系统业务数据 |
| Node.js | 14+ / 16+ | 用于运行前端 Vue 项目 |
| npm | 6+ / 8+ | 用于安装前端依赖 |
| Git | 任意较新版本 | 用于拉取项目代码 |

## 构建与运行指南

### 1. 克隆项目

```bash
git clone https://gitee.com/moonne/community-manage-system.git
cd community-manage-system
```

### 2. 初始化数据库

在 MySQL 中创建项目数据库，例如：

```sql
CREATE DATABASE community_manage_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

如果项目中提供了 `.sql` 数据库脚本，请在 MySQL 中导入：

```bash
mysql -u root -p community_manage_system < 数据库脚本文件.sql
```

### 3. 修改后端配置

打开后端配置文件，通常位于：

```text
src/main/resources/application.yml
```

根据本地 MySQL 环境修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/community_manage_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

如项目使用 Druid 数据源，请同时检查 Druid 的连接池参数是否完整。

### 4. 启动后端项目

方式一：使用 Maven Wrapper，推荐优先尝试：

```bash
./mvnw spring-boot:run
```

Windows 环境：

```bash
mvnw.cmd spring-boot:run
```

方式二：使用本地 Maven：

```bash
mvn clean install
mvn spring-boot:run
```

后端启动成功后，默认访问地址通常为：

```text
http://localhost:8080
```

如果端口不同，请以 `application.yml` 中的 `server.port` 配置为准。

### 5. 安装前端依赖

进入前端目录：

```bash
cd cms_webui
npm install
```

如果安装速度较慢，可以使用国内镜像源：

```bash
npm install --registry=https://registry.npmmirror.com
```

### 6. 启动前端项目

```bash
npm run serve
```

或根据 `package.json` 中实际脚本执行：

```bash
npm run dev
```

前端启动成功后，默认访问地址通常为：

```text
http://localhost:8081
```

或终端输出的本地访问地址。

### 7. 前端接口代理配置

如果前端请求后端接口出现跨域或接口地址不匹配问题，请检查前端项目中的代理配置文件，例如：

```text
cms_webui/vue.config.js
```

常见代理配置示例：

```js
module.exports = {
  devServer: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: {
          '^/api': ''
        }
      }
    }
  }
}
```

实际接口前缀应以后端 Controller 与前端 axios 配置为准。

## 打包部署

### 后端打包

在项目根目录执行：

```bash
mvn clean package
```

打包完成后，运行 `target` 目录下的 jar 文件：

```bash
java -jar target/*.jar
```

### 前端打包

进入前端目录：

```bash
cd cms_webui
npm run build
```

打包完成后，通常会生成 `dist` 目录，可部署到 Nginx 或其他静态资源服务器。

## 常见问题

### 1. 后端启动报错：Failed to configure a DataSource

原因通常是数据库连接配置缺失或错误。请检查：

- MySQL 是否启动
- 数据库是否已创建
- `application.yml` 中的 `url`、`username`、`password` 是否正确
- MySQL 驱动依赖是否存在

### 2. 前端接口请求失败

请检查：

- 后端服务是否已启动
- 前端代理地址是否正确
- 后端接口路径是否与前端 axios 请求路径一致
- 是否存在跨域限制

### 3. npm install 速度较慢或失败

可以尝试使用镜像源：

```bash
npm install --registry=https://registry.npmmirror.com
```

也可以删除 `node_modules` 和锁文件后重新安装依赖。
