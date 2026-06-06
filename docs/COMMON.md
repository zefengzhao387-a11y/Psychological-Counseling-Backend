# common 模块使用说明

## 引入方式

各微服务 `pom.xml` 已依赖：

```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>common</artifactId>
</dependency>
```

`META-INF/spring.factories` 自动加载 `CommonAutoConfiguration`，无需额外 `@Import`。

## 统一响应体 `R<T>`

```java
return R.ok(data);
return R.ok("操作成功", data);
return R.fail("错误信息");
```

前端约定：`code === 200` 为成功（见 `frontend/src/api/request.js`）。

## 全局异常

抛出 `BusinessException` 即可返回统一错误 JSON：

```java
throw new BusinessException("该时段已约满");
```

## 用户上下文

网关 JWT 校验后向下游转发请求头：

- `X-User-Id`
- `X-Username`
- `X-Role-Code`

业务代码通过 `UserContext` 获取：

```java
Long userId = UserContext.getUserId();
Integer roleCode = UserContext.getRoleCode();
```

## 分页结果

```java
Page<Entity> page = service.page(...);
return R.ok(PageResult.of(page));
```

## JWT 工具

`JwtUtil.generate(...)` / `JwtUtil.validate(token)` — 主要在 user-service 与 gateway 使用。

## OpenAPI / Knife4j

common 中 `OpenApiConfig` 为各服务提供统一文档配置。服务 `application.yml` 开启：

```yaml
knife4j:
  enable: true
springdoc:
  swagger-ui:
    path: /doc.html
```

## 服务间调用（Feign）

`common` 模块已定义 Feign 客户端，例如：

- `org.example.common.feign.NotificationFeignClient` — 短信通知

使用方式：

1. 服务 `pom.xml` 引入 `spring-cloud-starter-openfeign`
2. 启动类加 `@EnableFeignClients(basePackages = "org.example.common.feign")`
3. 配置目标地址，如 `service.notification-url: http://localhost:8085`

`consultation-service` 已通过 `NotificationSupport` 接入。

本地联调也可继续用 Gateway 静态路由（见 `docs/DEV.md`）。

## 角色枚举

`org.example.common.enums.UserRole`：学生(1)、初访员(2)、心理助理(3)、咨询师(4)、中心管理员(5)。
