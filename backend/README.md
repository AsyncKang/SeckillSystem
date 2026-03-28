# 后端（backend）

完整说明见仓库根目录 [README.md](../README.md)。

## 本目录速查

| 模块 | 端口 | 职责 |
|------|------|------|
| seckill-gateway | 8080 | 路由、CORS、限流 |
| seckill-user | 8081 | 注册、登录、JWT |
| seckill-product | 8082 | 活动、秒杀、Redis、发 MQ |
| seckill-order | 8083 | 消费 MQ、订单落库 |

```bash
cd backend
docker-compose up -d
mvn clean install -DskipTests
# 再分别启动四个 Spring Boot 应用
```
