# BackendManagement-web 更新日志

## 2026-08-15 — 后台管理系统 9 大页面实现（Mock 驱动，真实后端路径对齐）

### 新增
- **Mock 数据层** `src/mock/`：`data.ts`（菜品/分类/桌台/订单/会员/员工/角色/优惠券/门店种子数据）
  与 `index.ts`（axios adapter 路由处理器）。仅当 `VITE_USE_MOCK=true`（开发默认）时介入，
  复用 `request.ts` 的拦截器与 `{code,data,msg}` 契约；后端就绪后将 `.env.development` 的
  `VITE_USE_MOCK` 改为 `false` 即可无缝切换真实接口。
- **通用组件** `src/components/BaseChart.vue`：基于 ECharts 的图表封装（自适应/销毁/重渲染）。
- **工具** `src/utils/format.ts`：金额「分→元」展示（`yuan` / `yuanShort`）。
- **API 模块**（9 域，`src/api/`）：dashboard、menu（菜品 CRUD+规格组+分类）、table、order、
  member、report、employee、marketing、setting。其中 **menu / order 的路径与方法已对齐真实后端**。
- **页面**（9 屏，`src/views/`）：
  - 工作台 `dashboard`：核心指标卡 + 7 日销售趋势 + 订单类型分布 + 热销 TOP（ECharts）
  - 菜品管理 `menu`：完整 CRUD、规格组（多规格多选项）、库存、上/下架、分类管理
  - 桌台管理 `table`：拓扑卡片、区域/状态筛选、新增/编辑
  - 订单管理 `order`：列表+类型/状态/关键字筛选、详情抽屉、接单/出餐/完成/退款流转
  - 会员管理 `member`：列表+等级筛选、详情抽屉
  - 数据报表 `report`：30 日营业额/客单价趋势、菜品销量 TOP、订单类型占比（ECharts）
  - 员工权限 `employee`：员工列表 + 角色权限矩阵（RBAC 可视化）
  - 营销工具 `marketing`：优惠券列表 + 新建（满减/折扣）
  - 基础设置 `setting`：门店信息维护
- **路由/侧边栏**：`router/index.ts` 补全 9 条子路由（带 `meta.title/icon/permission`），AdminLayout 自动生成侧边栏。
- **全局** `$perms(perm)` 辅助方法（main.ts），与 `v-permission` 指令配合做按钮级权限。
- **CHANGELOG.md** 本文件（AGENTS.md 纪律要求）。

### 变更
- `src/utils/request.ts`：在 `VITE_USE_MOCK` 时挂载 mock adapter。
- `src/api/order.ts`：新增 `acceptOrder` / `cancelOrder`，`updateOrderStatus` 改为
  `POST /admin/order/{id}/status`（对齐 `OrderAdminController`）。
- `vite.config.ts`：`server.host` 改为 `127.0.0.1`（沙箱无法解析 `localhost`），proxy target 同步。
- `.env.development`：新增 `VITE_USE_MOCK=true`。
- `vite-env.d.ts`：补充 `VITE_USE_MOCK` 类型。

### 后端契约对齐与待办（重要）
经核查，`BackendManagement-serve` 后端**已存在且编译通过**（81 个 .java、44MB jar），已暴露真实
`/api/admin` 接口。前端 mock 与其差异如下，作为后续"接真"清单：
- ✅ 已对齐路径：menu（GET/POST/PUT/DELETE `/admin/menu/dishes`、POST `/admin/menu/categories`）、
  order（POST `/admin/order/{id}/accept|status|cancel`）。
- ⚠️ 待对齐：
  1. **菜单列表**：真实后端返回裸数组（非 `{list,total}` 分页），接入时需适配分页。
  2. **订单状态机**：后端枚举为 0待支付→1已支付/待接单→2制作中→3已上菜/待取餐→4已完成；9取消；5退款中→6已退款；7退单。
     前端当前演示用 1-8 自有枚举，接真时需统一。
  3. **桌台**：后端仅提供 `GET /admin/tables` 与 `POST /admin/table/{id}/status`，无完整 CRUD；前端新增/编辑为 mock 特有。
  4. **缺后端接口的模块**：dashboard 统计、report 报表、employee/roles、marketing 优惠券、setting 门店设置——当前为纯前端 mock，
     需后端补相应 admin 接口方可联调。
- 🚧 后端本沙箱因 MySQL 凭据（root@localhost 拒绝 + ordering 库未建）无法启动，故优先以 Mock 驱动演示；
  接真前置：建 `ordering` 库(utf8mb4) + 改 `application.yml` 密码 + 启动 jar + 关 `VITE_USE_MOCK`。

## 2026-08-16 — 联调真实后端（按路径 mock 兜底，5 大模块吃真实数据）

### 新增 / 变更
- `src/utils/request.ts`：把 `VITE_USE_MOCK` 由"全量 mock"改为**"按路径兜底"**——后端已实现的接口（dishes/orders/tables/members/reservations）走真实后端；
  后端未实现的（dashboard/report/employee/marketing/setting/shop、订单详情）由 `mockAdapter` 兜底。`MOCK_FALLBACK_PATHS` 集中管理，后端补齐后移除对应项即可。
- `src/mock/index.ts`：精简路由至兜底范围（dashboard/report/employee/roles/coupons/shop/订单详情）；新增 `GET /api/admin/order/{id}` 详情路由（含菜品明细，供订单抽屉）。
- `src/api/menu.ts`：listDishes 返回裸数组；add/update 提交时 `price×100`、`status` 布尔→1/0；listCategories 改调真实 `/client/menu/categories`；移除 updateDishStatus（上下架改走 updateDish）。
- `src/api/order.ts`：listOrders 返回裸数组；getOrder 改 `/admin/order/{id}`（admin 无详情接口，走 mock）；移除 refundOrder（退款改走 updateOrderStatus）。
- `src/api/table.ts`：新增 `updateTableStatus`（POST /admin/table/{id}/status）；移除 add/update（后端仅支持状态变更）。
- `src/api/member.ts`：listMembers 返回裸数组；移除 getMember，新增 `listMemberLevels`（levelId→名称映射）。
- 页面适配：menu（裸数组+分类下拉+价格÷100+上下架走 updateDish）、order（**真实状态机 0-9** + 接单/出餐/完成/退款/取消 + 详情走 mock + tableId 映射桌台号）、
  table（tableNo 字段 + 状态变更 + 移除增删改）、member（真实字段 nickname/phone/levelId/points/balance/lastActiveAt + 等级名映射 + 本地详情）。
- 启动 dev server 时清除 egress 代理变量（`HTTP_PROXY/HTTPS_PROXY/ALL_PROXY` 置空、`NO_PROXY=127.0.0.1,localhost`），使 Vite `/api` 代理直连后端 3000；
  否则沙箱 egress 代理会把 localhost 请求误导向 63184，导致代理 000。

### 修复
- `src/views/login/index.vue`：默认密码由 `123456` 改为 `admin123`（与后端 `application.yml` 配置的管理员账号一致），提示文案同步；否则默认登录会因凭证不符被后端拒绝。

### 后端契约实测结论（重要）
- ✅ 真实有数据：dishes(GET 裸数组)、orders(GET 裸数组 + accept/status/cancel)、tables(GET + status)、members(GET + levels)、reservations(GET)。
- ❌ 后端完全缺控制器：dashboard、report、employee、coupon(营销)、setting、shop(后台) —— 这些页面暂由 mock 兜底。
- ⚠️ 缺口：admin 无菜单分类列表 GET（复用 client 接口）、无订单详情 GET（mock 兜底）、桌台无增删改（仅状态变更）。
- 管理员账号：`application.yml` 配置项 `admin / admin123`（**非数据库**）；登录必须带 `X-Shop-Id`（前端拦截器已注入 shopId=1）。

### 后端待补齐接口（接真清单 v2）
1. `GET /api/admin/menu/categories`（分类列表，当前复用 client 接口）
2. `GET /api/admin/order/{id}`（订单详情，当前 mock 兜底）
3. `POST/PUT /api/admin/tables`（桌台增改，当前后端仅支持状态变更）
4. dashboard / report / employee / coupons / setting / shop 的 admin 控制器（当前全 mock）

## 2026-08-16 — 第二阶段：补齐后端缺失接口 + 前端全量真实数据联调（9 页全真实数据）

### 背景
上一阶段（接真清单 v2）标记 dashboard/report/employee/coupons/setting/shop 控制器"当前全 mock"。本阶段在
`BackendManagement-serve` 补齐了全部缺失的 admin 接口，并将 `BackendManagement-web` 的 mock 兜底彻底移除、全量对齐真实后端。

### 后端新增接口（落地于 BackendManagement-serve，本 CHANGELOG 仅记前端侧）
- `GET /api/admin/dashboard`（聚合：今日/待处理/会员数 + 近 7 日趋势 + 类型分布 + 热销 TOP5）
- `GET /api/admin/report?days=30`（聚合：营收/订单/客单价趋势 + 类型占比 + 热销 TOP10 + 汇总）
- `GET /api/admin/employees`、`POST/PUT/DELETE /api/admin/employee/{id}`
- `GET /api/admin/roles`、`POST/PUT/DELETE /api/admin/role/{id}`
- `GET/POST /api/admin/marketing/coupons`、`DELETE /api/admin/marketing/coupons/{id}`（按会员发放）
- `GET/PUT /api/admin/shop`、`GET/PUT /api/admin/setting`（key-value 扩展设置）
- `GET /api/admin/order/{id}`（订单详情，聚合菜品明细/支付单/会员名/桌台号）
- 新增表 `role` / `employee` / `shop_setting` 及种子数据（员工：王店长/小李/张厨；角色：超级管理员/店长/服务员）

### 变更（前端）
- **`src/utils/request.ts`**：删除 `MOCK_FALLBACK_PATHS` / `USE_MOCK_FALLBACK` 与 `mockAdapter` 注入逻辑，不再依赖 mock。
- **删除 `src/mock/`**（`index.ts` / `data.ts`），无引用残留。
- **`src/api/dashboard.ts`**：由 4 个拆分接口改为单接口 `getDashboard()`→`/admin/dashboard`，类型对齐 `DashboardVO`（weekRevenue/orderTypeDist/topDishes）。
- **`src/api/report.ts`**：由 4 个拆分接口改为 `getReport(days)`→`/admin/report?days=`，类型对齐 `ReportVO`（revenueTrend/typeDist/topDishes/summary）。
- **`src/api/employee.ts`**：字段对齐 `EmployeeVO{roleName}` / `Role{permissions 逗号串}`；路径保持复数 `/admin/employees`、`/admin/roles`。
- **`src/api/marketing.ts`**：改为 `/admin/marketing/coupons`，类型对齐 `CouponVO`（按会员发放：memberId/memberName/value/threshold/status/validTo）+ `IssueCouponDTO`；新增 `deleteCoupon`。
- **`src/api/setting.ts`**：保留 `getShopInfo/updateShopInfo`（→`/admin/shop`，仅 name/status）；新增 `getShopSettings/updateShopSettings`（→`/admin/setting`，批量 key-value）。
- **`src/api/member.ts`**（新增）：`listMembers`→`/admin/members`、`listMemberLevels`→`/admin/member/levels`。
- **`src/api/order.ts`**：新增 `OrderDetail` 类型（dishName/unitPrice/specsJson 等真实字段），`getOrder` 返回 `OrderDetail`。

### 变更（视图，对齐真实 VO）
- **dashboard/index.vue**：单接口驱动；4 卡（今日订单/今日营业额/待处理/会员总数）+ 7 日趋势 + 类型分布（type→堂食/外卖/自提 label）+ 热销 TOP。
- **report/index.vue**：单接口驱动；新增汇总卡片（总营收/总订单/客单价/退款）+ 营收&订单数双轴趋势 + 客单价趋势 + 菜品 TOP + 类型占比。
- **employee/index.vue**：按 `roleName` 显示角色；`permissions` 逗号串 `split(',')` 后渲染权限标签；权限弹窗按角色名匹配。
- **marketing/index.vue**：改为"按会员发券"模型——列表展示 会员/名称/门槛/面额/有效期至/状态(未使用/已使用/已过期)/作废；新建弹窗选会员 + 面额(元→分) + 门槛(元→分) + 有效期天数。
- **setting/index.vue**：拆分提交——`name`→`/admin/shop`，`address/phone/businessHours/notice`→`/admin/setting`；移除不存在的 printer/autoAccept 字段。
- **order/index.vue**：详情抽屉改用真实字段 `memberName`/`tableNo` 与菜品 `dishName`/`unitPrice`/`specsJson`；`current` 类型改为 `OrderDetail`。

### 修复
- 营销优惠券语义对齐：真实后端为**按会员发放的单券**（非前端原先假设的"满减/折扣模板 + 发放总量"），故列表/新建/删除均按真实契约重写。
- 订单详情抽屉原引用 `name/spec/price/customerName/phone/remark/tableId` 等前端假设字段，已全部替换为后端真实字段。

### 验证
- `npm run build` 通过（Vite 生产构建无报错）。
- 启动 dev server（127.0.0.1:5173，proxy `/api`→后端 3000，已清 egress 代理变量）+ 后端 3000；
  以 `admin/admin123` 登录取 token 后，以下端点均返回 `code:0` 真实数据：
  `dashboard / report?days=30 / employees / roles / marketing/coupons / shop / setting / members / orders / order/{id}`。
- 演示数据校准：将 3 笔种子订单 `created_at` 调整为当前日期（原 08-15 偏移到 08-16），使"今日"指标与趋势图有真实数据可展示。

### 遗留 / 后续
- 菜单分类列表仍复用 client 接口（`/client/menu/categories`），未单独补 admin 控制器（非本次范围）。
- 桌台仅支持状态变更（后端无增删改 admin 接口），前台增删改为本地/禁用。
- 营销"按会员发券"模型与早期 PRD 的"优惠模板"概念不同，若产品侧需模板化营销活动，需后端再扩展。
