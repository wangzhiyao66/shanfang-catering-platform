# BackendManagement-serve（点餐小程序后端）

中餐单店点餐小程序的后端服务。技术栈：**Java 17 + Spring Boot 3.3 + MyBatis-Plus + MySQL 8 + Redis + JWT + SpringDoc**。

`/api/client`（小程序顾客端）与 `/api/admin`（商家后台）共用同一 Service / DAO 与同一数据库，仅接入身份与可见范围不同。

## 目录结构
```
src/main/java/com/ordering/
├── OrderingApplication.java          # 启动类（端口 3000）
├── common/                           # 跨域通用
│   ├── result/   R(统一返回) / CodeEnum / BizException
│   ├── advice/   ResponseAdvice(包成 {code,data,msg}) / GlobalExceptionHandler
│   ├── context/  RequestContext(ThreadLocal: shopId/openid/memberId/adminId)
│   ├── interceptor/ ShopInterceptor / OpenidInterceptor / JwtInterceptor
│   ├── config/   WebMvcConfig / MybatisPlusConfig(乐观锁插件) / RedisConfig / SwaggerConfig / CorsConfig / 配置属性
│   └── util/     JwtUtil
└── modules/
    ├── auth/       AuthController / AuthService（微信登录 + 后台 JWT 登录）
    ├── menu/       MenuClientController / MenuAdminController / MenuService / entity / mapper
    ├── shop/       Shop 实体与 Mapper
    ├── order/      Order + OrderItem + OrderPayment 实体/mapper；OrderService（下单/接单/状态机/取消）；OrderClientController / OrderAdminController
    ├── payment/    WechatPayV3Client（JDK 原生 RSA/AES，无 SDK）；PayService / PayController（prepay + 微信回调验签解密）
    ├── member/     Member / MemberLevel / PointsLog 实体/mapper；MemberService（自动开卡/绑手机/积分）；MemberClientController / MemberAdminController
    ├── reservation/ Reservation / DiningTable 实体/mapper；ReservationService（预订/取消/确认）；ReservationClientController / ReservationAdminController / DiningTableController
    └── kitchen/    KitchenStation / KitchenTicket 实体/mapper；KitchenService（按分类拆厨位工单）；KitchenPushService（WebSocket 会话注册/广播）；KitchenWebSocketHandler / WebSocketConfig / KitchenAdminController
src/main/resources/
├── application.yml                   # 端口 3000、数据源、redis、jwt/wechat 配置
├── schema.sql / data.sql             # 启动自动建表 + 种子数据（可重复执行）
```

## 前置条件
- **Java 17**：已通过 `~/.bash_profile` 切换默认 `java`（新开终端生效）。
- **MySQL 8.0.x** 运行中，并建库（注意连接串字符集用 `UTF-8`，库本身用 `utf8mb4`）：
  ```sql
  CREATE DATABASE ordering CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
  ```
  > 数据源 URL 已固定为 `characterEncoding=UTF-8&connectionCollation=utf8mb4_general_ci`（MySQL Connector/J 不认 `utf8mb4` 作为 Java 字符集名，曾导致启动报 `Unsupported character encoding 'utf8mb4'`）。
  > 默认账号 `root/root`；若你的 MySQL root 密码不同，请改 `application.yml` 的 `spring.datasource.password`。
- **Redis**（缓存 / 分布式锁，可选）：`docker compose up -d redis` 或 `brew install redis`。业务代码当前未注入 RedisTemplate，启动不强制连 Redis；后续分布式锁/缓存再启用。

## 运行
```bash
cd BackendManagement-serve
mvn spring-boot:run          # 默认 3000 端口
```
启动会自动执行 `schema.sql` + `data.sql`（建表 + 种子数据：一家演示店 shop_id=1，含热菜/凉菜/主食/饮品 与示例菜品）。

## 接口联调示例
### 顾客端（只需带 `X-Shop-Id` 头）
```bash
curl -H "X-Shop-Id: 1" http://localhost:3000/api/client/menu/categories
curl -H "X-Shop-Id: 1" "http://localhost:3000/api/client/menu/dishes?categoryId=1"
```

### 微信登录（骨架：未配真实 appid 时返回 demo openid）
```bash
curl -X POST -H "X-Shop-Id: 1" -H "Content-Type: application/json" \
  -d '{"code":"abc"}' http://localhost:3000/api/client/auth/login
```

### 后台登录（拿 JWT）
```bash
curl -X POST -H "X-Shop-Id: 1" -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' http://localhost:3000/api/admin/auth/login
# → {"code":0,"data":{"token":"eyJ..."},"msg":"ok"}
```

### 后台菜品（带 JWT）
```bash
TOKEN=$(curl -s -X POST -H "X-Shop-Id: 1" -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' http://localhost:3000/api/admin/auth/login \
  | sed 's/.*"token":"\([^"]*\)".*/\1/')
curl -H "X-Shop-Id: 1" -H "Authorization: Bearer $TOKEN" http://localhost:3000/api/admin/menu/dishes
```

### 下单（顾客端，需 `X-Openid` + `@LoginRequired`）
```bash
# 1) 微信登录拿 openid（demo 环境返回 demo_openid_test）
OPENID=$(curl -s -X POST -H "X-Shop-Id: 1" -H "Content-Type: application/json" \
  -d '{"code":"abc"}' http://localhost:3000/api/client/auth/login \
  | sed 's/.*"openid":"\([^"]*\)".*/\1/')
# 2) 下单（堂食 type=1，带桌台与菜品明细）
curl -X POST -H "X-Shop-Id: 1" -H "X-Openid: $OPENID" -H "Content-Type: application/json" \
  -d '{"type":1,"tableId":1,"peopleCount":2,"items":[{"dishId":1,"quantity":2,"unitPrice":3800}]}' \
  http://localhost:3000/api/client/order
# 3) 调起微信支付（返回 JSAPI 参数；未配真实商户私钥时抛清晰异常）
curl -X POST -H "X-Shop-Id: 1" -H "X-Openid: $OPENID" -H "Content-Type: application/json" \
  -d '{"orderId":1}' http://localhost:3000/api/client/pay/prepay
```

### 会员 / 积分 / 预约 / 后厨（后台）
```bash
curl -H "X-Shop-Id: 1" -H "Authorization: Bearer $TOKEN" http://localhost:3000/api/admin/members
curl -H "X-Shop-Id: 1" -H "Authorization: Bearer $TOKEN" http://localhost:3000/api/admin/member/levels
curl -H "X-Shop-Id: 1" -H "Authorization: Bearer $TOKEN" http://localhost:3000/api/admin/reservations
curl -H "X-Shop-Id: 1" -H "Authorization: Bearer $TOKEN" http://localhost:3000/api/admin/tables
curl -H "X-Shop-Id: 1" -H "Authorization: Bearer $TOKEN" http://localhost:3000/api/admin/kitchen/tickets
# 接单后会按菜品分类自动拆厨位工单：POST /api/admin/order/{id}/accept
```

### 后厨 KDS（WebSocket）
```
ws://localhost:3000/ws/kitchen?shopId=1     # 连接后实时推送工单/订单状态变化
```

Swagger 文档：http://localhost:3000/swagger-ui.html

## 关键约定
- 统一返回 `{ code, msg, data }`；`code=0` 成功，`401` 未登录（与小程序 `request.js` 对齐）。
- 租户 `shop_id` 由 `X-Shop-Id` 头注入 `RequestContext`；`MenuServiceImpl` 中**每条 SELECT/INSERT/UPDATE/DELETE 都显式带 `shop_id`**，从代码层杜绝越权（MyBatis-Plus 3.5.17 已移除 `TenantLineInnerInterceptor` 多租户插件，故不依赖插件）。乐观锁 `@Version` 由 `OptimisticLockerInnerInterceptor` 处理。
- 金额单位：**分（INT）**，前端 ÷100 展示；会员折扣在服务端按等级 `discount` 计算（1元=1积分）。
- 订单状态机用 `@Version` 乐观锁；接单后按菜品所属分类匹配 `kitchen_station` 名称自动拆厨位工单（热菜/凉菜/主食/饮品），全部工单完成则订单 制作中→已上菜/待取餐。
- 微信支付 v3 用 **JDK 原生 RSA(SHA256withRSA) + AES-256-GCM** 实现下单签名、回调验签与 resource 解密，**不引入额外 SDK**；真实联调需在 `application.yml` 填 `ordering.wechat.private-key / merchant-serial-no / platform-cert / api-v3-key / mch-id`。回调地址 `notify_url` 已排除 Shop/Openid 拦截器。

## 已实现模块（原「后续扩展点」）
- 订单 + 微信支付 v3（order / payment）：下单、支付预下单、回调幂等更新、状态机。
- 会员 / 积分 / 储值（member）：自动开卡、绑手机、积分变动流水、等级折扣。
- 预约订座（reservation）：预订/取消/确认、桌台状态（空闲/占用/预定/清洁中）。
- 后厨 KDS（kitchen + WebSocket）：按分类拆单、工单开始/完成、WebSocket 实时推送。
- 对象存储 COS、营销优惠券等按同结构续加。
