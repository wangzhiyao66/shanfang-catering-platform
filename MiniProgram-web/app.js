// app.js
const { login } = require('./utils/auth');
const store = require('./utils/store');

App({
  globalData: {
    shopName: '膳房·中餐',                  // 小程序展示名称
    shopId: 1,                              // 演示门店ID，实际由扫码/后端下发
    openid: null,                          // 微信唯一标识（对应 member.openid）
    memberId: null,
    // 演示用：默认落座 A01（dining_table.id=1），使下单为「堂食」业态；
    // 真实场景由顾客扫码桌台二维码写入对应 tableId。设为 null 则下单为「自提」。
    tableId: 1,
    // 后端地址：
    // - 开发者工具【模拟器】可用 127.0.0.1（避免 localhost 解析到 IPv6 ::1 导致连接失败）
    // - 【真机预览/扫码预览】必须换成开发者机器的局域网 IP，例如 http://192.168.1.198:3000/api/client
    //   手机不在开发机上，127.0.0.1 会指向手机自身 → 请求必然失败
    //   ⚠️ IP 变动后（如换 WiFi/路由）需同步更新此处
    baseURL: 'http://192.168.1.198:3000/api/client'
  },

  onLaunch() {
    store.init();
    // 静默登录：拿到 openid -> 解析 member
    login()
      .then((member) => {
        this.globalData.openid = member.openid;
        this.globalData.memberId = member.id;
        if (member.tableId) this.globalData.tableId = member.tableId;
      })
      .catch(() => {/* 允许未登录浏览菜单，下单时再登录 */});
  }
});
