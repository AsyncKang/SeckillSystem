# 高并发商品秒杀系统

基于 Spring Cloud + Vue 的秒杀系统，支持高并发抢购、库存防超卖、异步下单、死信队列，预留 K8s 部署能力。

## 技术栈

- **后端**: Spring Boot 3.2、Spring Cloud Gateway、JPA、Redis、RocketMQ、JWT
- **前端**: Vue 3、Vite、Vue Router、Axios

## 项目结构

```
当前目录（与你的需求文档同级）：
├── backend                 # 后端（Spring Cloud 多模块）
│   ├── pom.xml
│   ├── seckill-gateway      # 网关：限流、路由（端口 8080）
│   ├── seckill-user         # 用户服务：注册、登录、JWT（端口 8081）
│   ├── seckill-product      # 秒杀/商品服务：活动、库存、抢购（端口 8082）
│   ├── seckill-order        # 订单服务：MQ 消费、死信队列（端口 8083）
│   ├── docker-compose.yml   # Redis、RocketMQ（MySQL 使用本机）
│   ├── init-db              # 建库脚本（本机 MySQL 可手动执行）
│   └── README.md
├── frontend                # 前端（Vue）
│   └── seckill-web
└── 高并发商品秒杀系统需求文档（精简版）.pdf
```

## 本地运行

### 1. 启动中间件

```bash
cd backend
docker-compose up -d
```

MySQL 使用本机安装的，请先确保本机 MySQL 已启动，并执行一次建库脚本：

```sql
CREATE DATABASE IF NOT EXISTS seckill_user;
CREATE DATABASE IF NOT EXISTS seckill_product;
CREATE DATABASE IF NOT EXISTS seckill_order;
```

（脚本文件在 `backend/init-db/01-databases.sql`，你也可以复制内容到数据库工具里执行。）

### 2. 启动后端（按顺序）

在 `backend` 目录：

```bash
# 编译
mvn clean install -DskipTests

# 启动各服务（各开一个终端）
mvn -pl seckill-gateway spring-boot:run
mvn -pl seckill-user spring-boot:run
mvn -pl seckill-product spring-boot:run
mvn -pl seckill-order spring-boot:run
```

或使用 IDE 分别运行各模块的 `*Application` 主类。

### 3. 创建管理员

用户服务使用 JPA 自动建表。首次注册的账号默认为普通用户。创建管理员方式之一：

- 在数据库 `seckill_user` 的 `users` 表中，将某用户的 `admin` 字段设为 `1`（例如先注册一个账号，再执行 `UPDATE users SET admin = 1 WHERE username = 'admin';`）。

### 4. 启动前端

```bash
cd ../frontend/seckill-web
npm install
npm run dev
```

浏览器访问前端开发地址（如 http://localhost:5173），接口通过网关 http://localhost:8080 访问。

## 核心流程

1. **管理员**：登录 → 管理后台 → 发布商品 → 创建秒杀活动（时间、库存）→ 点击「上线」预热 Redis 库存。
2. **用户**：注册/登录 → 首页查看已上线活动与倒计时 → 活动开始后点击「立即抢购」→ 三层校验（登录、资格、库存）→ 异步扣减缓存并发送 MQ → 订单服务消费 MQ 落库。
3. **高并发**：网关限流、库存走 Redis、分布式锁防超卖、订单异步 MQ 削峰；失败订单可进入死信队列 `seckill.order.dlq` 排查。

## 配置说明

- **数据库（本机 MySQL）**：各服务 `application.yml` 中 JDBC 为 `localhost:3306`，库名分别为 `seckill_user`、`seckill_product`、`seckill_order`。如你的本机账号密码不是 `root/12345678`，请修改各服务的 `application.yml`。
- **Redis**：各服务默认 `localhost:6379`。
- **RocketMQ**：NameServer `localhost:9876`，Broker 默认端口 `10911/10909/10912`。
- **JWT**：各服务需使用相同 `app.jwt.secret`，否则跨服务解析会失败。

## 后期扩展（K8s）

- 各服务可打成镜像（Dockerfile），通过 Deployment 部署，配合 Service/Ingress 暴露。
- 网关、用户、商品、订单均可水平扩缩容；Redis、RocketMQ 可改为集群或使用云服务。

## 许可证

MIT
