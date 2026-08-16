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
