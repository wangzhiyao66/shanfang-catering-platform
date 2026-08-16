# MiniProgram-web — 微信小程序（顾客端）

中餐点餐小程序的**原生微信小程序**工程，对应《点餐小程序PRD_v1》的顾客端功能。

## 技术栈

| 项 | 选择 | 说明 |
|---|---|---|
| 框架 | 微信原生小程序（WXML/WXSS/JS） | 仅微信单端，生态兼容最好、包最小 |
| UI 组件 | **Vant Weapp**（有赞） | 50+ 组件，点餐场景最成熟（Sku/Stepper/SubmitBar/Tabs…） |
| 语言 | JavaScript（可平滑升级 TS） | — |
| 状态 | `utils/store.js` 轻量购物车/合单 | 持久化到本地，跨页面同步 |
| 请求 | `utils/request.js` 统一封装 | 自动注入 openid / shopId，对接后端 |

## 目录结构

```
MiniProgram-web/
├── app.js / app.json / app.wxss      # 全局配置（注册页面 + 全局 Vant 组件 + 品牌色）
├── project.config.json / sitemap.json
├── utils/
│   ├── request.js                    # 请求封装，baseURL 指向 BackendManagement-serve
│   ├── auth.js                       # wx.login → /auth/login 换 openid/member
│   └── store.js                      # 购物车 + 多人合单状态
└── pages/
    ├── menu/          # 菜单点餐（左分类 + 菜品列表 + 底部购物车条）
    ├── dish-detail/   # 菜品详情（规格弹窗：份量/辣度/忌口 + 步进器）★中餐特色
    ├── cart/          # 购物车/结算（明细 + 微信支付）
    ├── order/         # 订单列表/状态（Steps 进度 + 催菜）
    ├── member/        # 会员中心（等级/积分/储值/优惠券）
    └── reserve/       # 预约订座（日期/时段/人数/包间）
```

## 本地运行

1. 用**微信开发者工具**打开本目录（`MiniProgram-web/`）。
2. 在 `project.config.json` 填入你的 `appid`（或选「测试号」）。
3. 安装 Vant Weapp：
   ```bash
   cd MiniProgram-web
   npm install @vant/weapp
   ```
   然后在开发者工具菜单：**工具 → 构建 npm**（每次增删依赖后需重新构建）。
4. 配置后端地址：编辑 `app.js` 里的 `baseURL`，指向你的 `BackendManagement-serve`
   （默认 `http://localhost:3000/api/client`，手机预览需换成本机/局域网 IP）。
5. 真机预览前，在开发者工具「详情 → 本地设置」勾选「不校验合法域名」（正式需在小程序后台配置 request 合法域名）。

## 与后端 / PRD 的映射（已对齐真实 `/api/client/*` 接口）

> 所有请求统一走 `utils/request.js`：自动注入 `X-Shop-Id`（租户隔离）与 `X-Openid`（登录态）；
> 后端返回 `{ code:0, data, msg }`，401 自动登录并重试一次。

| 页面 | 对应 PRD 功能 | 真实后端接口（路径相对于 `/api/client`） |
|---|---|---|
| menu | 菜品浏览、分类 | `GET /menu/categories`、`GET /menu/dishes?categoryId=` |
| dish-detail | 规格选择（份量/辣度/忌口）、备注 | 无单品接口，菜品对象由菜单页带入；规格本地选择后写入 `specsJson` |
| cart | 购物车、合单、微信支付 | `POST /order`（返回订单ID）→ `POST /pay/prepay`（返回 JSAPI 参数）→ `wx.requestPayment` |
| order | 订单状态、催菜 | `GET /orders`（我的全部订单，无 group 过滤） |
| member | 会员中心、优惠券 | `GET /member`（`levelId` 前端映射为等级名） |
| reserve | 预约订座/包间 | `POST /reservation`、`GET /reservations`、`POST /reservation/{id}/cancel` |

### 当前后端尚未提供的 client 端点（已在小程序端做降级处理）
- **菜品规格/单品详情**：无 `GET /menu/dish/{id}` 与 `/specs`，小程序用菜单列表数据 + 本地规格模板。
- **订单菜品明细**：`Order` 列表/详情不返回 `items`，小程序用下单时缓存的「订单快照」在订单页展示。
- **优惠券**：无 `GET /member/coupons`，会员页优惠券置空（显示空态）。
- **催菜**：无 `POST /order/{id}/urge`，订单页「催菜」暂为本地提示（KDS 走 WebSocket）。
- **包间列表**：无 `GET /client/tables`，预订页包间用种子数据占位（待补 client 接口）。
- **规格差价**：`createOrder` 按菜品基准价计费（不计入 sku 差价），故小程序购物车金额与实付一致；如需「大份+¥X」需后端在 `createOrder` 应用 `dish_spec.price_delta`。

> 字段与表结构见《点餐小程序数据库设计文档_v1》。小程序只做展示层，所有数据来自 `BackendManagement-serve`。
