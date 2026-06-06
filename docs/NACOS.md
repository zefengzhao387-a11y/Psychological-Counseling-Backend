# Nacos 模式（可选）

本地开发默认使用 **直连端口**（见 `DEV.md`）。需要 Nacos 服务发现时：

## 1. 启动 Nacos

默认地址：`http://localhost:8848/nacos`（账号/密码均为 `nacos`）

## 2. 启用 Nacos Profile

各服务启动时增加：

```powershell
java -jar user-service-....jar --spring.profiles.active=nacos
java -jar gateway-....jar --spring.profiles.active=nacos
```

## 3. Gateway 路由

`gateway/src/main/resources/application-nacos.yml` 已配置 `lb://` 负载均衡路由，服务名需与 `spring.application.name` 一致：

| 服务名 | 端口 |
|--------|------|
| user-service | 8081 |
| appointment-service | 8082 |
| consultation-service | 8083 |
| statistics-service | 8084 |
| notification-service | 8085 |

## 4. 各微服务

各服务 `application-nacos.yml` 仅开启：

```yaml
spring.cloud.nacos.discovery.enabled: true
spring.cloud.nacos.discovery.server-addr: localhost:8848
```

Feign 在 Nacos 模式下可将 `url` 改为服务名（需配合 `@LoadBalanced RestTemplate` 或 Feign + discovery）。
