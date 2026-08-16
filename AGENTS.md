# AGENTS.md — 点餐小程序项目 AI 智能体编码指南

> 本文件用于记录项目目录结构导航、技术栈、契约与边界，方便后续开发与协作时快速对齐。
> 事实源优先级：本项目《数据库设计文档_v1》> 两份《技术选型与实现方案_v1》> 《PRD_v1》/《原型_v1》。同一目录中文档冲突时，优先使用适用范围更具体、标记"已确认"的专题文档。

## 项目根目录文件导航

| 目录 / 文件                | 用途说明                                              |
| -------------------------- | ----------------------------------------------------- |
| `BackendManagement-serve/` | 后端服务开发目录（Spring Boot API、数据库、业务逻辑） |
| `BackendManagement-web/`   | 后台管理系统（商家后台 Web）开发目录                  |
| `MiniProgram-web/`         | 微信小程序（顾客端）开发目录                          |
| `TaskProgress/`            | 任务拆分与执行进度（各阶段任务的拆解与进度跟踪）      |

## 已产出文档（规划/设计阶段）

- `点餐小程序功能调研_v1.html` — 功能调研报告（中餐单店版）
- `点餐小程序PRD_v1.html` — 产品需求文档（小程序端 + 后端管理系统）
- `点餐小程序原型_v1.html` — 可交互原型（小程序 7 屏 + 后台 7 屏）
- `点餐小程序数据库设计文档_v1.html` — 后端数据库设计（26 张表、ER 图、边界与多端兼容）★★ 边界事实源
- `点餐小程序后端技术选型与实现方案_v1.html` — 后端栈（Java 17 + Spring Boot 3.x）★★
- `点餐小程序后台管理系统前端技术选型与实现方案_v1.html` — 后台前端栈（Vue 3 + Vite + TS）★★

## 备注

- 中餐单店点餐小程序，三端（小程序顾客端 / 商家后台 / 后厨端）**共用同一套数据库**，不是三套库。
- 后端监听 **3000 端口**，路由 `/api/client`（小程序端）与 `/api/admin`（后台端）共用同一 Service/DAO 与同一库。
- 各开发目录已搭出骨架（小程序原生骨架、后端 config + 示例、后台脚手架），功能尚未全部实现。

---

## 一、技术栈总览

### 1.1 后端（BackendManagement-serve）— 已确认走 Java Spring Boot

| 层         | 选型                                                          | 说明 / 备选                                                  |
| ---------- | ------------------------------------------------------------- | ------------------------------------------------------------ |
| JDK        | **Java 17 (LTS)**                                             | Spring Boot 3 要求 JDK 17+；备选 Java 21                     |
| 框架       | **Spring Boot 3.x**（内嵌 Tomcat，`server.port=3000`）        | 自动配置；端口对齐小程序 `request.js` baseURL                |
| Web/鉴权   | **Spring MVC** + **Spring Validation** + **jjwt**(admin)      | Interceptor 读 `X-Openid/X-Shop-Id`；后台 JWT 鉴权           |
| ORM        | **MyBatis-Plus**                                              | 内置**多租户插件**（自动拼 `shop_id`）、**乐观锁**(`@Version`)、分页；备选 JPA |
| 主数据库   | **MySQL 8.0**（连接池 HikariCP 默认）                         | 文档已定；备选 PG14                                         |
| 缓存 / 锁  | **Redis** + **Redisson**                                      | 菜单树缓存、桌台状态、购物车、排队号；**Redisson 分布式锁**（接单/桌台/库存/支付幂等） |
| 对象存储   | **腾讯云 COS**（Java SDK）                                    | 微信生态同源，服务端签名直传 + CDN；备选阿里 OSS             |
| 微信集成   | 登录 `code2session` 自建；支付 **微信支付 v3**                | `wechatpay-java` / 自封装 RSA 签名与平台证书                 |
| 接口文档   | **SpringDoc OpenAPI**（Knife4j 出中文 UI）                    | 三端联调单一接口源                                           |
| 构建/配置  | **Maven** + `application.yml` 多环境（dev/prod）             | 备选 Gradle；Nacos(可选) 配置中心                           |
| 测试       | **JUnit 5** + **Mockito**                                     | 订单状态机、支付幂等、分布式锁重点覆盖                       |
| 部署       | Spring Boot fat-jar → **Docker** + **Nginx**；TencentDB(MySQL/Redis)；HTTPS 必备 | 本地 `docker-compose` 起 mysql+redis；生产 Nginx 反代 + 域名备案 |

推荐项目结构：`modules/{auth,menu,order,member,reserve,marketing,printer,kitchen,system}`（Controller/Service/Mapper 分层）；`common/{result,advice,interceptor,annotation,context,config}`（统一返回、全局异常、租户/Openid/JWT 拦截、ShopContext 等）。

### 1.2 后台管理系统前端（BackendManagement-web）— 推荐栈 Vue 3 + Vite + TS

| 层       | 选型                                            | 说明 / 备选                                            |
| -------- | ----------------------------------------------- | ------------------------------------------------------ |
| 构建     | **Vite 5+**                                     | 极速冷启动/热更新；备选 Vue CLI（已停更）              |
| 框架     | **Vue 3**（`<script setup>` + Composition API）  | 与后端解耦靠 REST，无语言绑定                          |
| 语言     | **TypeScript**（推荐）                          | 表单/表格/接口多，类型显著降低 BUG；备选 JS            |
| UI 库    | **Element Plus**（推荐）                        | 后台表单/表格/RBAC/报表场景最成熟；备选 TDesign Vue Next（腾讯系视觉统一） |
| 状态     | **Pinia**                                       | 存 user(角色/权限)、app(主题/折叠)、shop(当前门店)     |
| 路由     | **Vue Router 4**                                | 路由级权限守卫 + 动态路由注册（按角色过滤菜单）        |
| HTTP     | **Axios**                                       | 拦截器注入 `Authorization: Bearer` + `X-Shop-Id`，统一 `{code,data,msg}` 解析与 401 处理 |
| 图表     | **ECharts**（vue-echarts）                      | 数据报表：营业额/订单量/热销榜/翻台率/客单价/复购      |
| 实时     | **原生 WebSocket** 封装 / Socket.IO 客户端      | 订单推送与 KDS 厨显；**具体协议待后端确认**            |
| 工具     | dayjs / lodash-es / @vueuse/core                | 日期、工具函数、组合式 hooks                            |
| 部署     | build → **Nginx 静态托管** + 反代 `/api`        | 开发期 Vite proxy 到 `:3000` 解决 CORS；生产同域/子域 + HTTPS |

目录：`src/{api,assets,components,layouts,router,store,utils,views}`；`api/` 与后端 Controller 同名，降低联调心智负担。已搭骨架，含登录 + RBAC 守卫 + Axios 封装 + 侧边栏布局 + 菜品管理示例页。

### 1.3 微信小程序顾客端（MiniProgram-web）— 微信原生小程序 + Vant Weapp

| 层     | 选型                                              | 说明                                                       |
| ------ | ------------------------------------------------- | ---------------------------------------------------------- |
| 框架   | **微信原生小程序**（WXML / WXSS / JS，不跨端、微信单端） | 若未来想与后台视觉统一，可换 TDesign 小程序版              |
| 组件库 | **Vant Weapp**（`@vant/weapp`）                    | `app.json` 已注册 button/tag/card/popup/stepper/submit-bar/radio/steps/field/cell/toast/dialog/icon 等；主题色 `#e8543f` |
| 网络   | 自封装 `utils/request.js`                          | 自动注入 `X-Openid` / `X-Shop-Id`，baseURL → `BackendManagement-serve /api/client`，统一 `{code,data,msg}`，`401` 触发重新 `wx.login` |
| 登录   | `wx.login` → `code2session`（后端换 openid/member）| openid / shopId 缓存于 `app.globalData`                    |
| 支付   | `wx.requestPayment`（后端返回 `payParams`）        | 需回调验签                                                 |
| 页面   | menu / dish-detail / cart / order / member / reserve | 已搭原生骨架（app.* + utils + pages）                     |

> **视觉边界**：小程序走 Vant 移动端组件，后台走 Element Plus PC 组件，两者在视觉上自然区分（后台若迁 TDesign 则全栈视觉统一但生态小于 Element Plus）。

### 1.4 三端契约与共识（所有端必须对齐，禁止私自偏离）

- **后端地址**：小程序 `http://localhost:3000/api/client`；后台 `/api/admin`；后厨 `/api/kitchen`（WebSocket）。
- **统一返回**：`{ code:0, data, msg }`；`code===0` 为成功；`401` 表示未登录/无 shopId → 触发重新授权或跳登录。
- **身份头**：`X-Openid`（解析会员）、`X-Shop-Id`（租户隔离）；后台额外用 `Authorization: Bearer <JWT>`。
- **金额单位**：一律 **INT（分）** 存储与传输，前端仅做 `÷100` 展示。
- **订单 `type`**：`1堂食 / 2外卖 / 3自提`，三种业态落同一订单表与同一状态机。

---

## 二、约束与边界（来自《数据库设计文档_v1》与两份技术选型）

> 以下为"已确认"边界，编码时必须遵守。标注"**待确认/前置**"的项，确认前不得扩展表结构或接口承诺（见第三节 3.1）。

### 2.1 接口与数据契约
- 返回统一 `{ code, data, msg }`；Controller 只做参数校验和调用 Service，业务逻辑放 Service。
- 时间戳 `created_at/updated_at`；金额 INT(分)；主键 `id` BIGINT；软删 `deleted_at`（NULL 为未删）。
- 订单/支付/会员/菜品等核心数据**永不物理删除**，走软删 + 审计。

### 2.2 身份与租户隔离
- 每张业务表带 `shop_id`；**所有查询必须带 `shop_id` 条件**（由 MyBatis-Plus 多租户插件在框架层自动拼接，Service 层无需手写，从源头杜绝越权）。
- 小程序端按 `member_id=自己` 隔离；后台按 `role.data_scope` 收缩。
- 单店起步、未来可平滑升级连锁，结构不变。

### 2.3 业务边界（域解耦）
- 菜单域 / 交易域 / 履约域 / 会员域 / 营销域 / 权限域各自独立建表，以 `order` 主表为"聚合根"串联。
- 跨域操作（如下单后推后厨分单）由**服务层事务**保证，不在数据库层硬耦合。
- 订单明细 `order_item` 存 `dish_name/unit_price/specs_json` **落定快照**，菜品后续改名改价不影响历史账单。

### 2.4 数据边界（隔离 / 软删 / 审计 / 快照）
- 租户隔离 `shop_id`、软删除 `deleted_at`、快照 `order_item`、审计 `operation_log` + `points_log/balance_log` 记录资金与权益变动（可追溯）。

### 2.5 并发边界（防超卖 / 重复支付 / 重复接单）
- **乐观锁**：`order.version`、`dining_table.version` 更新时校验版本，失败重试（防重复接单/改状态）。
- **幂等**：`order.order_no`、`order_payment.pay_no` 唯一索引，重复提交直接返回原结果；微信支付回调按 `out_trade_no` 幂等。
- **状态机约束**：状态只许按合法路径流转，非法变更在 Service 层拒绝。
- **分布式锁（Redisson）**：接单、改桌台状态、库存扣减加锁，避免多服务员并发冲突。

### 2.6 金额边界（分单位 + 服务端计算）
- 所有金额 INT(分) 存储与传输，杜绝浮点误差。
- 优惠（满减/折扣/券/会员折扣）**全部服务端计算**，`pay_amount = total - discount`；前端传的优惠金额只作展示、不被信任。
- 退款走 `order_payment` 新记录（status=3），原支付保留，账目清晰。

### 2.7 状态机边界（核心实体，单一真相）
- **`order.status`**（堂食/外卖/自提语义一致）：
  `待支付(0) → 已支付/待接单(1) → 制作中(2) → 已上菜/待取餐(3) → 已完成(4)`
  任意非终态可 `→ 退款中(5) → 已退款(6)`；`待支付→取消(9)`；`制作中/待接单→退单(7)`。
- **`dining_table.status`**：空闲(0) → 预定(2) → 占用(1) → 清洁中(3) → 空闲(0)。
- **`order_payment.status`**：待支付(0) → 成功(1)/失败(2) → 退款(3)。
- **`kitchen_ticket.status`**：待做(0) → 制作中(1) → 完成(2)/退单(3)。
- 非法流转（如"已完成"再改"制作中"）在 Service 层用状态机校验拦截。

### 2.8 权限边界（谁能看/改什么）
- 小程序端：只能读写 `member_id=自己` 的数据（订单/购物车/优惠券/评价）；菜单公开只读。
- 后台端：按 `role.data_scope` —— 老板/店长全店；服务员仅本桌；后厨仅本档口（`kitchen_ticket.station_id` 过滤）。
- **字段级**：`dish.cost_price`、利润类字段只在后台 API 返回；小程序 API 用字段白名单剔除。

### 2.9 多端兼容（一套库服务小程序 + 后台）
- 数据库与业务服务**只有一套**，差异只在"接入身份"与"可见范围"。
- `/api/client/*` 与 `/api/admin/*` 共用同一 Service/DAO 与同一库——避免逻辑分叉导致数据不一致。
- 同表复用：`order/dish/dining_table/coupon` 同一张表，靠 `type/member_id/status` 区分场景，不建副本。
- 桌台二维码 `dining_table.qr_token` 双用（小程序落座 + 后台打印绑定）。
- 状态机单一真相：无论小程序"催菜"还是后台"出餐"，都走同一 `order.status`，靠 `updated_at`/版本号保持一致。

### 2.10 部署与合规（前置）
- 小程序后台必须配置 **request 合法域名** 指向后端 HTTPS 地址；服务器域名需 **ICP 备案**。
- 微信支付需**企业/个体户商户号**且类目匹配；生产 **HTTPS 必备**（微信支付与合法域名均要求）。
- 密钥（微信 secret、COS 密钥、支付证书）**只存后端**，前端只拿临时直传凭证，严禁下发前端。

### 2.11 待确认 / 前置事项（确认前不得承诺）
- **WebSocket 协议**：原生 WS / STOMP / Socket.IO 待后端定（订单推送与 KDS 厨显）。
- **后厨 KDS 形态**：建议并入后台路由，或独立大屏；触屏/断网缓存需明确。
- **多门店**：门店切换器 + `X-Shop-Id`，单店可先不做（结构已预留）。
- **备案与商户号**：生产前置，影响上线节奏。

---

## 三、纪律红线 — 不可违反

### 3.1 事实源与决策
- 进入开发前，必须先读本项目《数据库设计文档_v1》与对应的《技术选型与实现方案_v1》，并在 `TaskProgress/` 中拆分任务。
- 同一目录中文档冲突时，优先使用适用范围更具体、标记"已确认"的专题文档。
- 标记为"待确认"的内容，**确认前不得扩展表结构或接口承诺**（见 2.11）。

### 3.2 代码生成规则
- 必须先拆分任务，不能直接开始写代码。将任务拆分成多个子任务，每个子任务对应一个功能模块，并记录在 `TaskProgress/` 目录下。
- 每个 public 方法必须有 Javadoc 注释，包含 `@param` 和 `@return`。
- 禁止提交密钥、API 密钥或凭证。
- 禁止使用 `System.out.println()` — 使用 `@Slf4j` 的 `log.info()` / `log.warn()` / `log.error()`。
- 禁止捕获通用 `Exception` 后只 `e.printStackTrace()` — 必须抛出业务异常或记录日志并处理。
- 禁止使用 FastJSON — 使用 Jackson 或 Hutool JSONUtil。
- 禁止在 Controller 层写业务逻辑 — Controller 只做参数校验和调用 Service。

### 3.3 代码生成完成后规则
- 生成代码完成后必须更新 `CHANGELOG.md` 文件，追加更新操作日志。

### 3.4 禁止幻觉性代码
- 禁止在代码中写"幻觉性"的逻辑，比如"如果 A 为 true，那么 B 一定为 false"。
- 禁止在代码中写"幻觉性"的注释，比如"这个方法返回 true"。
- 如果不确定某个功能的实现需要哪些 API 或数据库操作，必须明确说明"需要查阅官方文档确认"，严禁编造。

---

## 四、工作风格

- **先拆分任务再写**：将每个公共功能模块拆分成多个子任务，每个子任务对应一个功能点。
- **最小改动**：只改必须改的，不顺手"改进"无关代码。每一行变更都应能追溯到用户需求。
- **先问后做**：如果需求有多种理解，列出来让用户选，不要默默选一个。如果不确定，停下来问。
- **验证后交付**：写完代码后运行 `mvn compile`（后端）或 `npm run build`（前端）确认通过，不要说"应该没问题"。
