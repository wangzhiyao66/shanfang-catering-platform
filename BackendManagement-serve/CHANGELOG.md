# CHANGELOG — BackendManagement-serve（点餐小程序后端服务）

> 本文件记录后端服务的实现与变更执行记录。
> 通用约定：监听端口 **3000**；路由 `/api/client`（小程序端，需 `X-Shop-Id` + 登录后 `X-Openid`）与 `/api/admin`（商家后台，需 JWT `Authorization`）；统一响应 `{ code, data, msg }`；金额一律以 **分(INT)** 传输，前端 ÷100 展示；多租户隔离在 **Service 层显式携带 `shop_id`**（MyBatis-Plus 3.5.17 已移除多租户插件）。

---

## 2026-08-15 — 三大业务模块实现（订单 / 微信支付 v3 / 会员·积分 / 预约订座 / 后厨 KDS）+ 骨架修正

### 基础设施（Phase 0 骨架，本日早些时间已完成并验证）
- 技术栈落地：Java 17（Temurin 17.0.20）+ Spring Boot 3.3 + MyBatis-Plus 3.5.17 + MySQL 8.0 + Redis + SpringDoc。
- 统一返回与切面：`R` 统一响应体、`GlobalExceptionHandler`、`ResponseAdvice`、`RequestContext`（请求级上下文）。
- 拦截器：`ShopInterceptor`（注入 `X-Shop-Id`）、`OpenidInterceptor`（校验 `X-Openid` + `@LoginRequired`）、`JwtInterceptor`（后台 JWT）。
- 配置类：`MybatisPlusConfig`、`RedisConfig`、`SwaggerConfig`、`CorsConfig`、`JwtUtil`、`WechatProperties`。
- 示例模块 `menu`（Shop/Category/Dish + Mapper + Service + Client/Admin Controller）、`auth`（微信登录骨架 + 后台 JWT 登录）、`schema.sql` / `data.sql` 启动自动建表 + 种子数据。
- **关键修正（Phase 0 编译期）**：MyBatis-Plus 3.5.17 已移除 `TenantLineInnerInterceptor` 与 `PaginationInnerInterceptor`，多租户改为 Service 层显式 `shop_id`；`ResponseAdvice` 方法签名改为 `Class<? extends HttpMessageConverter<?>>`。
- 验收：`mvn -B -q package -DskipTests` 通过，产出 `target/ordering-serve-0.0.1-SNAPSHOT.jar`。

### 新增模块 1 — 订单（order）
- 实体 `Order`（`@TableName("`order`")`，保留字加反引号）、`OrderItem`、`OrderPayment` + 各自 Mapper。
- `OrderService` / `OrderServiceImpl`：`createOrder`（服务端按会员等级折扣算 `discountAmount`，金额用分）、`getOrder`、`listMyOrders`、`adminList`、`acceptOrder`（1→2 并自动拆厨位工单）、`updateStatus`（状态机 `canTransit` 校验）、`cancelOrder`。全部写入/查询显式带 `shop_id`；`Order` 与 `DiningTable` 加 `@Version` 乐观锁。
- 状态机：`0待支付 → 1已支付/待接单 → 2制作中 → 3已上菜/待取餐 → 4已完成`；`9取消`；`5退款中 → 6已退款`；`7退单`。
- 表 `order_payment.status`：`0待支付 / 1成功 / 2失败 / 3退款`。

### 新增模块 2 — 微信支付 v3（payment）
- `WechatPayV3Client`（**JDK 原生 RSA(SHA256withRSA) + AES-256-GCM，不引第三方 SDK**）：`jsapiPrepay(openid, outTradeNo, amountFen, description)` 返回小程序 JSAPI 调起参数；`decryptResource`（回调报文解密）；`verifyNotify`（平台证书验签）；内部 `postWithAuth` 拼 `WECHATPAY2-SHA256-RSA2048` 头。
- `PayService` / `PayServiceImpl`：`prepay`（幂等，`payNo = orderNo`）、`handleNotify`（验签 → 解密 → 幂等更新订单 `0→1` → 按 `1元=1分` 加积分 → 广播）。
- `PayController`：`/api/client/pay/prepay`（`@LoginRequired`）、`/api/client/pay/notify`（**已排除 Shop / Openid 拦截器**，回调不带这些头）。

### 新增模块 3 — 会员 / 积分（member）
- 实体 `Member`、`MemberLevel`（`discount` DECIMAL(3,2)）、`PointsLog` + Mapper。
- `MemberService` / `MemberServiceImpl`：`ensureMember`（微信用户首访自动开卡）、`getProfile`、`bindPhone`、`addPoints`（事务内更新积分余额并写流水）、`discountOf(levelId)`。

### 新增模块 4 — 预约订座 / 桌台（reservation）
- 实体 `DiningTable`（`@Version`、状态 `0空闲 / 1占用 / 2预定 / 3清洁中`）、`Reservation`（日期 / 时段 / 人数 / 定金 / `status`） + Mapper。
- `ReservationService` / `ReservationServiceImpl`：`createReservation`、`cancel`、`listMine`、`adminList`、`confirm`（确认后若绑定桌台则标记桌台状态 `2预定`）。`reservation.status`：`0待确认 / 1已确认 / 2到店 / 3取消 / 4爽约`。
- 控制器：`ReservationClientController`（@LoginRequired）、`ReservationAdminController`、`DiningTableController`。

### 新增模块 5 — 后厨 KDS（kitchen，WebSocket）
- 实体 `KitchenStation`、`KitchenTicket`（按分类聚合的工单，状态 `0待做 / 1制作中 / 2完成 / 3退单`） + Mapper。
- `KitchenService` / `KitchenServiceImpl`：接单时按**菜品分类名 == `kitchen_station` 名**自动拆工单；全部工单完成则订单 `2制作中 → 3已上菜/待取餐`。
- `KitchenPushService` / `KitchenPushServiceImpl`：内存维护 `Map<shopId, List<WebSocketSession>>`，接单 / 工单状态变更时向该店后厨广播。
- `KitchenWebSocketHandler` + `WebSocketConfig`（`@EnableWebSocket`，注册 `/ws/kitchen?shopId=`）+ `KitchenAdminController`（工单列表 / 开始 / 完成）。

### 本阶段修掉的缺陷（编译 + 启动烟测）
1. 4 个顾客端 Controller 误 `import com.ordering.annotation.LoginRequired` → 修正为 `com.ordering.common.annotation.LoginRequired`。
2. `ReservationServiceImpl` 缺 `import java.util.List;`。
3. `WechatPayV3Client` 的 `sign/loadPrivateKey/loadPublicKey` 原 `throws Exception`，调用处未处理 → 改为包成 `RuntimeException`。
4. 数据源 URL 误用 `characterEncoding=utf8mb4`，MySQL Connector/J 不认（报 `Unsupported character encoding 'utf8mb4'`）→ 改为 `characterEncoding=UTF-8&connectionCollation=utf8mb4_general_ci`（库本身仍 utf8mb4）。
5. `MybatisPlusConfig` 多余的 `@MapperScan`（14 个 Mapper 已带 `@Mapper`）导致重复注册告警 → 移除，启动扫描干净。

### 构建验收
- `mvn -B -q package -DskipTests` ✅ 通过，产出 `target/ordering-serve-0.0.1-SNAPSHOT.jar`（44 MB）。
- 本沙箱**首次实跑 Spring 启动**：Tomcat 正常起、12 个 Controller 全部扫描、14 个 Mapper 全部正确注入、无任何 bean 接线 / 重复 RequestMapping 错误。

### 已知限制（环境，非代码）
- 启动止于 DB 阶段：本沙箱 MySQL（官方安装 `/usr/local/mysql`，无 sudo，`ps` 受限）`root@localhost` 密码未知，且 `ordering` 库是否存在未知 → `Access denied for user 'root'@'localhost'`。**代码层无缺陷**。
- 用户本机跑通 3 步：① `CREATE DATABASE ordering CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;` ② 按本机 root 密码改 `application.yml` 的 `spring.datasource.password` ③ `mvn spring-boot:run`（或 `java -jar target/*.jar`）。
- 微信支付真实联调另需填 `application.yml` 的 `ordering.wechat`：`mch-id / api-v3-key / private-key / merchant-serial-no / platform-cert`。

---

## 已暴露接口一览（后端当前版本）

**小程序端 `/api/client`**
- `POST /auth/login` 微信登录
- `GET  /menu/categories`、`GET /menu/dishes` 菜单浏览
- `POST /order/create`、`GET /orders`、`GET /order/{id}`
- `POST /pay/prepay`、`POST /pay/notify`（回调，免拦截器）
- `GET  /member`、`POST /member/bind` 会员
- `POST /reservation`、`GET /reservations`、`POST /reservation/{id}/cancel` 预约

**商家后台 `/api/admin`**
- `POST /auth/admin/login`
- `GET/POST/PUT/DELETE /menu/*` 菜品与分类管理
- `GET /orders`、`POST /order/{id}/accept`、`POST /order/{id}/status`、`POST /order/{id}/cancel`
- `GET /members`、`GET /member/levels`
- `GET /tables`、`POST /table/{id}/status`、`GET /reservations`、`POST /reservation/{id}/confirm`
- `GET /kitchen/tickets`、`POST /kitchen/ticket/{id}/start`、`POST /kitchen/ticket/{id}/done`

**后厨 WebSocket**
- `ws://host:3000/ws/kitchen?shopId={shopId}` 实时推送接单 / 工单状态变更
