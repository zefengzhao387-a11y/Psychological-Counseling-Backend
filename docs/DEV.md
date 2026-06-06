# 本地开发与联调说明

## 环境要求

| 组件 | 版本建议 |
|------|----------|
| JDK | 21 |
| Maven | 3.9+（或使用项目根目录 `mvnw.ps1`） |
| MySQL | 8.x |
| Node.js | 18+ |
| RabbitMQ | 可选（通知服务异步短信） |

## 数据库初始化

```bash
mysql -u root -p < sql/init-all.sql
```

默认 MySQL 账号：`root` / `123456`（与各服务 `application.yml` 一致）。

## 构建后端

```powershell
cd Psychological_counseling
.\mvnw.ps1 package -DskipTests
```

## 启动后端（JAR 方式）

```powershell
$root = ".\"
$log = ".\logs"
New-Item -ItemType Directory -Force -Path $log | Out-Null

java -jar "$root\user-service\target\user-service-1.0-SNAPSHOT.jar"
java -jar "$root\appointment-service\target\appointment-service-1.0-SNAPSHOT.jar"
java -jar "$root\consultation-service\target\consultation-service-1.0-SNAPSHOT.jar"
java -jar "$root\statistics-service\target\statistics-service-1.0-SNAPSHOT.jar"
java -jar "$root\notification-service\target\notification-service-1.0-SNAPSHOT.jar"
java -jar "$root\gateway\target\gateway-1.0-SNAPSHOT.jar"
```

各服务端口：

| 服务 | 端口 |
|------|------|
| gateway | 8080 |
| user-service | 8081 |
| appointment-service | 8082 |
| consultation-service | 8083 |
| statistics-service | 8084 |
| notification-service | 8085 |

## 启动前端

```powershell
cd frontend
npm install
npm run dev
```

访问：**http://localhost:3000**

前端 Vite 将 `/api/v1/*` 代理到对应微服务端口（见 `frontend/vite.config.js`）。

## 测试账号

密码均为 `123456`：

| 角色 | user_no |
|------|---------|
| 中心管理员 | admin001 |
| 学生 | stu001 |
| 初访员 | fv001 |
| 心理助理 | pa001 |
| 咨询师 | co001 |

## Nacos 说明

当前为**本地直连模式**：各服务 `nacos.discovery.enabled: false`，Gateway 使用静态 `localhost:808x` 路由。

启用 Nacos 详见 **[NACOS.md](./NACOS.md)**（`--spring.profiles.active=nacos`）。

## RabbitMQ（通知服务）

默认 **不启用 MQ**（无 RabbitMQ 也可启动 notification-service）。

启用异步短信：

1. 启动 RabbitMQ（5672）
2. 启动 notification-service 时加参数：`--spring.profiles.active=mq`

## 接口文档

见 [API.md](./API.md)。各服务启动后访问 `http://localhost:{port}/doc.html`。

## 常见问题

- **登录 401**：确认 gateway(8080) 与 user-service(8081) 已启动；登录路径为 `/api/v1/user/auth/login`
- **编译失败**：使用 `.\mvnw.ps1 package -DskipTests` 查看具体模块
- **端口占用**：`netstat -ano | findstr :8080` 查找并结束占用进程
