# 接口文档（Swagger / Knife4j）

各微服务独立提供 OpenAPI 文档，启动对应服务后浏览器访问：

| 服务 | 文档地址 |
|------|----------|
| user-service | http://localhost:8081/doc.html |
| appointment-service | http://localhost:8082/doc.html |
| consultation-service | http://localhost:8083/doc.html |
| statistics-service | http://localhost:8084/doc.html |
| notification-service | http://localhost:8085/doc.html |

OpenAPI JSON：`http://localhost:{port}/v3/api-docs`

## 认证

除登录/注册外，需在 Knife4j 右上角 **Authorize** 填入：

```
Bearer {登录返回的 token}
```

登录接口：

```
POST http://localhost:8080/api/v1/user/auth/login
Content-Type: application/json

{"userNo":"admin001","password":"123456"}
```

## URL 规范

```
GET    /api/v1/{resource}          列表
GET    /api/v1/{resource}/{id}     详情
POST   /api/v1/{resource}          新增
PUT    /api/v1/{resource}/{id}     修改
DELETE /api/v1/{resource}/{id}     删除
```

## 核心模块路径前缀

| 模块 | 前缀 |
|------|------|
| 认证 | `/api/v1/user/auth` |
| 用户/咨询师 | `/api/v1/user` |
| 初访预约 | `/api/v1/appointment/first-visit` |
| 值班/时间配置 | `/api/v1/appointment/duty-schedule`、`/time-config` |
| 咨询安排 | `/api/v1/consultation` |
| 初访结果 | `/api/v1/consultation/result` |
| 结案报告 | `/api/v1/consultation/report` |
| 统计分析 | `/api/v1/statistics` |
| 短信通知 | `/api/v1/notification/sms` |

新增接口（组长补齐）：

- `POST /api/v1/user/auth/register` — 学生注册
- `GET /api/v1/consultation/result/assistant-tasks` — 心理助理待办
- `PUT /api/v1/consultation/result/{id}/mark-processed` — 标记已处理
- `POST /api/v1/statistics/download` — 批量 Zip 下载结案报告

## 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## 导入 Apifox

1. 打开 Apifox → 导入 → OpenAPI
2. 填入 `http://localhost:8081/v3/api-docs`（可按服务分别导入后合并）

Gateway(8080) 为纯路由，不提供聚合文档；联调时以各服务文档为准。
