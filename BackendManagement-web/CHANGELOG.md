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
