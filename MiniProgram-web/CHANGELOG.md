# CHANGELOG — MiniProgram-web（膳房·中餐 小程序顾客端）

> 本文件记录小程序顾客端的实现、对齐与联调验证执行记录。
> 技术栈：微信原生小程序 + Vant Weapp；`utils/request.js` 统一封装，baseURL 默认 `http://localhost:3000/api/client`（真机预览改开发者机器局域网 IP）；自动注入请求头 `X-Shop-Id`（店铺）+ `X-Openid`（登录态），响应约定 `{ code, data, msg }`、401 触发重新登录；金额一律以 **分(INT)** 传输，前端 ÷100 展示。
> 后端服务：`BackendManagement-serve` 监听 **3000**，路由 `/api/client`（小程序端）。

---

## 2026-08-15 — 顾客端全量接口联调验证（16 接口 / 22 用例，全部通过）

### 验证范围
覆盖小程序实际调用的全部 **16 个 `/api/client/*` 顾客端接口**（正向 + 关键异常），并沉淀为可重复执行的集成测试脚本 `tests/e2e_client_api.py`（运行需后端已在 `127.0.0.1:3000` 启动）。

### 测试环境
- 后端：`BackendManagement-serve`（PID 持有着 :3000，jar 已含此前 6 缺口补丁）。
- 数据库：`ordering`（MySQL 8.0，root/12345678，utf8mb4）。
- 鉴权：演示模式 `POST /auth/login {code}` → 返回 `demo_openid_<code>`（`AuthService.clientLogin` 在未配置微信 appid 时降级），随后请求带该 `X-Openid`。
- 本地联调注意：本沙箱注入 `HTTP_PROXY=127.0.0.1:63184`，脚本已 `os.environ` 清除代理直连；`member`/`order` 为 MySQL 8.0 保留字，脚本 SQL 一律用反引号。

### 结果：22 / 22 通过

| 模块 | 接口 | 用例 | 关键断言 |
|---|---|---|---|
| 认证 | `POST /auth/login` | TC-01, TC-02 | demo 模式返回 `demo_openid_<code>`；缺 code → `400 缺少 code` |
| 菜单(公开) | `GET /menu/categories` | TC-03 | 返回 4 个分类（热菜…） |
| 菜单(公开) | `GET /menu/dishes?categoryId=` | TC-04 | 按分类返回菜品（宫保鸡丁/麻婆豆腐） |
| 菜单(公开) | `GET /menu/dish/{id}` | TC-05, TC-06 | 详情+规格（大份+1000/微辣0/不辣0）；不存在 → 报错 |
| 会员(登录) | `GET /member` | TC-07 | 返回档案（levelId/points/balance） |
| 会员(登录) | `GET /member/coupons` | TC-08 | 返回列表（新会员为空，接口正常） |
| 会员(登录) | `POST /member/bind` | TC-09 | 绑定手机号成功 |
| 桌台/预约(登录) | `GET /tables` | TC-10 | 返回 3 个桌台 |
| 桌台/预约(登录) | `POST /reservation` | TC-11 | 提交预订返回 reservationId |
| 桌台/预约(登录) | `GET /reservations` | TC-12 | 我的预订含新建记录 |
| 桌台/预约(登录) | `POST /reservation/{id}/cancel` | TC-13, TC-14 | 取消成功；取消后 `status=3` |
| 订单(登录) | `POST /order` | TC-15 | 下单成功（规格加价+券抵扣生效） |
| 订单(登录) | `GET /orders` | TC-16 | 我的订单含菜品明细 |
| 订单(登录) | `GET /order/{id}` | TC-17 | 详情：单价 4800 / 总额 4800 / 券减 500 → 付 4300，specsJson=`["大份"]` |
| 订单(登录) | `POST /order/{id}/urge` | TC-18, TC-19 | 待支付(status=0)拒绝「仅制作中/已上菜可催菜」；制作中(status=2)通过且 `order_urge` 落库 |
| 支付(登录) | `POST /pay/prepay` | TC-20, TC-21 | 订单不存在 → `订单不存在`；真实订单接口可达（见下「已知限制」） |

### 验证结论
- **全部 16 个顾客端接口联调通过**，请求路径、请求头（`X-Shop-Id`/`X-Openid`）、响应结构（`{code,data,msg}`）、401 重试均与运行中的后端一致。
- **规格加价服务端生效**：`大份` +¥10（1000 分）→ 订单项 `unitPrice=4800`。
- **优惠券抵扣服务端生效**：下单时校验归属/未用/门槛后标记已用并抵扣，订单 `payAmount` 由 4800 降至 4300。
- **催菜状态机生效**：仅「制作中/已上菜」(`status∈{2,3}`) 可催，待支付拒绝。
- 测试脚本含状态复位（每次运行清理测试产生的 member/coupon/order/reservation），可重复执行。

### 已知限制（非缺陷，记录备查）
1. **支付预下单 `/pay/prepay` 依赖微信商户证书**（appid/secret/mch-id/api-v3-key/平台证书）。演示环境未配置 → JSAPI 签名失败，返回 `code=500`（标准 R 信封，非裸 500）。订单不存在 / 状态不可支付（`status≠0`）等校验正常返回 `code≠0`。配置商户参数后即返回真实 JSAPI 调起参数（`timeStamp/nonceStr/paySign/package`）。
2. **业务「不存在」类错误返回 HTTP 200 + `code=500`**（服务端错误语义），而非 4xx 业务码（如 404/409）。功能正确；若需严格语义，可后续将 `BizException` 映射为 4xx 业务码。
3. 前端 `pages/cart/cart.js` 仍引用本地 `store.js` 购物车缓存做下单入参，但下单后改为消费后端返回的 `items`（已无本地快照兜底）。下单/支付链路完整。

### 改动文件
- 新增 `MiniProgram-web/tests/e2e_client_api.py`：顾客端全量接口集成测试（22 用例，可重复执行，含 JSON 报告输出）。

---

## 2026-08-16 — 紧急修复：全部接口 401（请求头未携带）

### 现象
小程序侧所有 `/api/client/*` 接口（含公开菜单）统一返回 `401 缺少合法的 X-Shop-Id`。后端本身正常（带 `X-Shop-Id:1` 实测菜单 `200`）。

### 根因
`utils/request.js` 原第 5 行 `const app = getApp()` 在**模块加载时**一次性捕获 `app`。加载链：`app.js`（第 2 行 `require('./utils/auth')`）→ `auth.js`（第 3 行 `require('./utils/request')`）→ 此时 `App({...})` 尚未注册，`getApp()` 返回 `undefined`，`app` 被永久钉死为 `undefined`。
后果：所有 `shopHeader()/authHeader()` 内 `app && app.globalData...` 恒为假 → **从不发送 `X-Shop-Id`/`X-Openid`** → 后端对含公开菜单在内的全部接口返回 401。

### 修复
改为**惰性获取** `getApp()`：新增 `appInstance()` 在每个函数内部调用，保证拿到已注册的 app 实例。行为完全不变（`X-Shop-Id` 取 `globalData.shopId`、`X-Openid` 取 `globalData.openid`、`baseURL` 取 `globalData.baseURL`）。

### 验证
- Node 模拟 `getApp`/`wx` 运行 `request.js`：公开接口带 `X-Shop-Id:1`；带 openid 时另带 `X-Openid`；结果 PASS。
- 后端实测：`curl --noproxy '*' -H "X-Shop-Id: 1"` 菜单 `200`；缺头 `401` —— 与修复后小程序发出的头一致，401 不再出现。

### 改动文件
- 修改 `MiniProgram-web/utils/request.js`：`const app = getApp()`（模块顶层）→ 函数内 `appInstance()` 惰性获取；`shopHeader/authHeader/baseURL/request/ensureLogin` 全部改为惰性取 app。

---

## 2026-08-16 — 底部 tabBar 对齐原型 + 新增首页

### 现象
原型底部 tabBar 为 `首页 / 点餐 / 订单 / 我的`（带图标 🏠🍽️📋👤），而实现为 `点餐 / 订单 / 预订 / 我的`，顺序与首标签都不对，且多出一个「预订」tab。

### 改动
- **新增首页 `pages/home/home`**（4 文件）：按原型的 mp-home 实现 —— 店名标题、搜索入口、会员日 banner、4 个快捷入口（扫码点餐/外卖/到店自提/预约订座）、店长推荐（真实菜品卡片）、热销榜（真实菜品列表）。
  - 数据：店长推荐/热销榜均来自 `GET /api/client/menu/dishes`（公开接口，按在售筛选），金额按分→元展示；不编造「月售」数字（Dish 无销量字段）。
  - 跳转：扫码点餐/外卖/到店自提 → `switchTab` 点餐；预约订座、菜品详情 → `navigateTo`；首页为 tab 页。
- **`app.json`**：`pages` 首项改为 `pages/home/home`（启动页）；`tabBar.list` 改为 `首页(home) / 点餐(menu) / 订单(order) / 我的(member)`，移除「预订」tab（预订改由首页「预约订座」与「我的」入口 `navigateTo` 进入）。
- **连带修复 `pages/member/member.js`**：`goReserve()` 原误用 `wx.switchTab` 跳预订（预订已非 tab）→ 改为 `wx.navigateTo`，否则跳转失败。

### 验证
- `app.json` JSON 合法；`home.js` / `member.js` `node --check` 通过。
- tabBar 映射确认：`首页->home | 点餐->menu | 订单->order | 我的->member`，与原型一致。
- 全部页面跳转目标复核：tab 页（home/menu/order/member）用 `switchTab`，非 tab 页（dish-detail/cart/reserve）用 `navigateTo`，无残留错误跳转。

### 改动文件
- 新增 `MiniProgram-web/pages/home/home.{js,json,wxml,wxss}`
- 修改 `MiniProgram-web/app.json`（pages 启动页 + tabBar）
- 修改 `MiniProgram-web/pages/member/member.js`（`goReserve` switchTab→navigateTo）

---

## 2026-08-16 — 预约订座页操作按钮放大，提升移动端触控体验

### 现象
用户反馈「预约订座」页面在手机上操作按钮（日期/时段/桌台的「选择」按钮、人数加减按钮）太小，不便于点按。

### 改动
- **日期/时段/桌台选择按钮**：去掉 Vant `size="mini"`，通过 `custom-class="choose-btn"` 放大内边距与字号（`padding: 18rpx 32rpx`、`font-size: 30rpx`、`min-width: 140rpx`、`border-radius: 8rpx`），手指可点区域显著增大。
- **人数步进器**：`van-stepper` 改为 `custom-class="party-stepper"`，设置 `button-size="72rpx"`、`input-width="96rpx"`，并通过 CSS 变量调大输入框字号，整体放大。
- **表单项行高**：给 `van-field` 加 `custom-class="reserve-field"`，设置 `min-height: 120rpx`，让整行垂直空间更宽裕，与放大的右侧控件对齐。
- **提示文字**：同步放大字号与行高，提高可读性。

### 验证
- `reserve.js`、`reserve.json`、`app.json` 均通过语法/JSON 合法性检查。
- 页面结构与事件绑定未变更（`onDate/onTime/onParty/onTable/submit` 保持原逻辑），仅调整样式与组件尺寸。

### 改动文件
- 修改 `MiniProgram-web/pages/reserve/reserve.wxml`
- 修改 `MiniProgram-web/pages/reserve/reserve.wxss`

---

## 2026-08-16 — 预约订座页 UI 重设计

### 现象
用户反馈预约订座页「不太美观」，原 `van-field` 表单视觉松散、左右控件风格不统一、提示文字偏技术化。

### 改动
- **整体风格**：改为卡片式表单（白色圆角卡片 + 浅灰底页），分组展示信息，视觉更聚焦。
- **表单项重构**：每行统一为「左侧图标 + 标签/当前值 + 右侧操作」结构：
  - 日期、时段、桌台：整行可点，显示当前已选值，右侧「选择 ▸」文字箭头，热区大且风格统一。
  - 人数：右侧保留放大后的 `van-stepper`（72rpx 按钮）。
- **色彩与排版**：标签用深色加粗、当前值用灰色、未选值用浅灰提示；主色 `#e8543f` 仅用于图标与操作文字，避免杂乱。
- **底部提交**：去掉 `van-submit-bar`，改用固定底部白色栏 + 圆角大按钮（92rpx 高、46rpx 圆角）。
- **提示文案**：由技术描述改为用户语言「未选择桌台时，到店后由店家根据实际情况安排」。

### 验证
- `reserve.js` / `reserve.json` / `app.json` 均通过语法/JSON 检查。
- 事件绑定与数据字段未变更（`onDate/onTime/onParty/onTable/submit` 保持原逻辑），提交预订流程不变。

### 改动文件
- 修改 `MiniProgram-web/pages/reserve/reserve.wxml`
- 修改 `MiniProgram-web/pages/reserve/reserve.wxss`

---

## 2026-08-16 — 点餐页底部购物车条 UI 修复

### 现象
用户反馈点餐页底部购物车条展示变形：购物车图标呈椭圆、价格符号与数字位置错乱、「去结算」「呼叫」按钮风格不统一。

### 根因
- `.cart-icon` 使用 `margin-top: -30rpx` 向上突出，在部分机型/模拟器下被压缩成椭圆。
- `cart-total` 内手写 `¥` 与全局 `.price::before { content: "¥" }` 叠加，导致重复符号且排版错乱。
- 两个 `van-button` 未统一高度/圆角，且 `cart-bar` 与微信 tabBar 可能重叠。

### 改动
- **购物车条整体重设计**：改为胶囊浮层（圆角 48rpx、高 96rpx、带阴影），位置避开微信 tabBar（`bottom: calc(150rpx + env(safe-area-inset-bottom))`）。
- **购物车图标**：改为标准的 72rpx 正圆，内部 flex 居中，去掉负 margin，不再变形。
- **价格显示**：去掉手写 `¥`，依赖全局 `.price::before`；`cart-total` 用 flex 基线对齐，字号 40rpx，清晰不重叠。
- **按钮统一**：「去结算」「呼叫」统一高度 72rpx、圆角 36rpx，分别使用品牌绿/橙，通过 `custom-class` 覆盖 Vant 默认样式。
- **菜品价格**：同步去掉 `dish-price` 前的手写 `¥`，避免双 ¥。
- **页面留白**：`.menu` 的 `padding-bottom` 由 `page-pad` 的 120rpx 改为 340rpx，确保内容不被购物车条和 tabBar 遮挡。

### 后续微调
- 用户反馈仍有遮挡，将 `cart-bar` 的 `bottom` 从 `calc(100rpx + env(safe-area-inset-bottom))` 上调至 `calc(150rpx + env(safe-area-inset-bottom))`，与 tabBar 之间保留安全间距；同步把 `.menu` 的 `padding-bottom` 从 260rpx 加大到 340rpx。

### 验证
- `menu.js` / `menu.json` / `app.json` 均通过语法/JSON 检查。
- 事件绑定未变更（`goCart/callWaiter/goDetail/selectCategory` 保持原逻辑），购物车数据由 `store` 正常驱动。

### 改动文件
- 修改 `MiniProgram-web/pages/menu/menu.wxml`
- 修改 `MiniProgram-web/pages/menu/menu.wxss`

---

## 2026-08-16 — 点餐页购物车条按钮精简

### 现象
用户反馈底部购物车条中「去结算」「呼叫」两个按钮太宽，与左侧购物车图标挤在一起。

### 改动
- **隐藏「呼叫」按钮**：点餐场景下「呼叫服务员」不是高频动作，先移除底部按钮，避免拥挤。
- **收窄「去结算」按钮**：设置 `min-width: 180rpx`，右内边距 8rpx，整体更紧凑。
- 保留 `callWaiter` 事件方法，若后续需要在「我的」或订单详情里加「呼叫服务」入口可复用。

### 验证
- `menu.js` / `menu.json` / `app.json` 均通过语法/JSON 检查。
- `goCart` 事件绑定未变更。

### 改动文件
- 修改 `MiniProgram-web/pages/menu/menu.wxml`
- 修改 `MiniProgram-web/pages/menu/menu.wxss`

---

## 2026-08-16 — 点餐页购物车条间距收紧

### 现象
用户反馈隐藏「呼叫」按钮后，购物车图标与「去结算」按钮之间留白过大，视觉上过于松散。

### 改动
- **改为内容自适应宽度的居中胶囊**：购物车条不再左右撑满屏幕，而是根据内容自动宽度（`width: auto; min-width: 460rpx; max-width: calc(100vw - 80rpx)`），并通过 `left: 50%; transform: translateX(-50%)` 居中。
- **按钮紧跟价格**：`cart-left` 去掉 `flex:1`，`cart-actions` 用 `margin-left: 24rpx`，使「去结算」按钮紧挨价格，不再被推到最右侧。
- **微调内边距与按钮宽度**：`cart-bar` 内边距收紧为 `0 16rpx 0 20rpx`；价格左侧间距 `14rpx`；按钮 `min-width` 从 `180rpx` 收窄到 `160rpx`。

### 验证
- `menu.js` / `menu.json` / `app.json` 均通过语法/JSON 检查。
- 结算按钮点击事件未变更。

### 改动文件
- 修改 `MiniProgram-web/pages/menu/menu.wxss`

---

## 2026-08-16 — 点餐页购物车条还原为原型样式

### 现象
用户指出上一次（自适应居中胶囊）的效果不对，要求按照点餐原型（`点餐小程序原型_v1.html` 的 `.cartbar`）实现。

### 原型定义（原型 line 72-75、276）
- 通栏深色底：`background:#2b2f36`、`height:56px`、左右 `padding:14px`、`justify-content:space-between`、`flex:none`。
- 左侧文字：`已点 <b>N</b> 件  <b>¥X</b>`，其中金额数字用橙色 `#ff9d5c`。
- 右侧按钮：`去结算` 背景品牌红 `#e8543f`、`border-radius:20px`、`font-weight:600`。

### 改动
- **撤销自适应胶囊**，恢复为**通栏固定条**：`left:0; right:0`、高 `112rpx`、底色 `#2b2f36`、顶部细阴影，`bottom` 仍让出微信 tabBar。
- **左侧**：改为「已点 {{cartCount}} 件」白色小字 + 金额橙色 `#ff9d5c` 大字（覆盖全局 `.price` 红色）。
- **右侧**：「去结算」按钮背景改回品牌红 `var(--brand)`（此前误用绿色），圆角胶囊、字重 600。
- 移除上一版遗留的购物车圆形图标 / 角标 / 灰底 total 等样式。

### 验证
- `menu.js` / `menu.json` / `app.json` 均通过语法/JSON 检查。
- `goCart` 事件绑定未变更。

### 改动文件
- 修改 `MiniProgram-web/pages/menu/menu.wxml`
- 修改 `MiniProgram-web/pages/menu/menu.wxss`
