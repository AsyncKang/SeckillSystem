# 高并发商品秒杀系统（SeckillSystem）

基于 Spring Cloud Gateway 与 Vue 3 的秒杀演示项目：高并发抢购、Redis 库存与分布式锁防超卖、RocketMQ 异步下单。远程仓库：[AsyncKang/SeckillSystem](https://github.com/AsyncKang/SeckillSystem)。

## 技术栈

- **后端**：Spring Boot 3.2、Spring Cloud Gateway、JPA、Redis、Redisson、RocketMQ、JWT  
- **前端**：Vue 3、Vite、Vue Router、Axios  

## 仓库结构

```
.
├── README.md                              # 本说明（入口文档）
├── 高并发商品秒杀系统需求文档（精简版）.pdf   # 需求说明
├── backend/                               # 后端多模块（Maven 父工程）
│   ├── pom.xml
│   ├── seckill-gateway/                   # 网关，端口 8080
│   ├── seckill-user/                      # 用户服务，8081
│   ├── seckill-product/                   # 商品/秒杀，8082
│   ├── seckill-order/                     # 订单/MQ 消费，8083
│   ├── docker-compose.yml                 # Redis、RocketMQ（MySQL 用本机）
│   ├── init-db/                           # 建库 SQL
│   ├── k6/                                # 压测脚本示例
│   └── README.md                          # 后端目录补充说明
└── frontend/seckill-web/                  # 前端（Vite）
```

## 本地运行

### 1. 中间件

```bash
cd backend
docker-compose up -d
```

本机安装并启动 **MySQL**，执行建库（内容与 `backend/init-db/01-databases.sql` 一致）：

```sql
CREATE DATABASE IF NOT EXISTS seckill_user;
CREATE DATABASE IF NOT EXISTS seckill_product;
CREATE DATABASE IF NOT EXISTS seckill_order;
```

### 2. 后端

在 `backend` 目录：

```bash
mvn clean install -DskipTests
mvn -pl seckill-gateway spring-boot:run
mvn -pl seckill-user spring-boot:run
mvn -pl seckill-product spring-boot:run
mvn -pl seckill-order spring-boot:run
```

或在 IDE 中分别运行各模块 `*Application` 主类。

### 3. 管理员

在 `seckill_user.users` 中将某用户 `admin` 置为 `1`，例如：

```sql
UPDATE users SET admin = 1 WHERE username = '你的用户名';
```

### 4. 前端

```bash
cd frontend/seckill-web
npm install
npm run dev
```

浏览器访问 Vite 提示的地址（如 `http://localhost:5173`）。接口经网关 `http://localhost:8080/api`。

## 核心流程

1. **管理员**：登录 → 管理后台 → 发布商品 → 创建秒杀活动 → 「上线」预热 Redis 库存。  
2. **用户**：注册/登录 → 首页活动与倒计时 → 「立即抢购」→ 校验后 Redis 扣减并发 MQ → 订单服务落库。  
3. **高并发要点**：网关限流、Redis 库存、Redisson 锁、MQ 削峰；生产环境需单独配置死信与监控。

## 配置说明

- **MySQL**：各服务 `application.yml` 默认 `localhost:3306`，库名 `seckill_user` / `seckill_product` / `seckill_order`；账号密码默认 `root` / `12345678`，请按本机修改。  
- **Redis**：`localhost:6379`。  
- **RocketMQ**：NameServer `localhost:9876`。  
- **JWT**：各服务 `app.jwt.secret` 需一致。  

## 许可证

MIT
