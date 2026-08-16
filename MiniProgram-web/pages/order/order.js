// pages/order/order.js
const { get, post } = require('../../utils/request');
const store = require('../../utils/store');

// 状态机：0待支付 1已支付/待接单 2制作中 3已上菜 4已完成 9取消 5退款中 6已退款 7退单
const STATUS_TEXT = {
  0: '待支付', 1: '已支付·待接单', 2: '制作中', 3: '已上菜', 4: '已完成',
  5: '退款中', 6: '已退款', 7: '退单', 9: '已取消'
};
const STEPS = ['已支付', '制作中', '已上菜', '已完成'];

// 后端 order_item.specs_json 为选中规格名数组，转展示文本
function specTextOf(item) {
  if (!item.specsJson) return '';
  try {
    const arr = JSON.parse(item.specsJson);
    if (Array.isArray(arr)) return arr.join(' / ');
  } catch (e) {}
  return '';
}

Page({
  data: {
    active: 0,
    tabs: ['进行中', '已完成', '全部'],
    orders: [],
    steps: STEPS.map((t) => ({ text: t }))
  },

  onShow() { if (this.data.active !== undefined) this.load(); },

  async load() {
    try {
      const list = await get('/orders');           // 我的全部订单（后端已返回 items）
      const tab = this.data.active;                // 0进行中 1已完成 2全部
      const decorated = list.map((o) => {
        // 优先用后端返回的明细；缺省时回退本地快照（兼容旧订单）
        const items = (o.items && o.items.length)
          ? o.items.map((it) => ({
              name: it.dishName,
              qty: it.qty,
              unitPrice: it.unitPrice,
              specs: specTextOf(it)
            }))
          : (store.getOrderSnapshot(String(o.id)) || []);
        return Object.assign({}, o, {
          items,
          statusText: STATUS_TEXT[o.status] || '未知',
          step: Math.min(Math.max(o.status - 1, 0), 3), // 1..4 -> 0..3
          showSteps: o.status >= 1 && o.status <= 4
        });
      });
      const filtered = decorated.filter((o) => {
        if (tab === 2) return true;
        if (tab === 0) return o.status !== 4 && o.status !== 9;
        if (tab === 1) return o.status === 4;
        return true;
      });
      this.setData({ orders: filtered });
    } catch (e) {}
  },

  onTab(e) {
    this.setData({ active: e.detail.index });
    this.load();
  },

  // 催菜：调用后端 /order/{id}/urge（落库催菜记录，后厨可见）
  async urge(e) {
    const id = e.currentTarget.dataset.id;
    try {
      await post('/order/' + id + '/urge');
      wx.showToast({ title: '已通知后厨', icon: 'none' });
    } catch (err) {
      wx.showToast({ title: '催菜失败', icon: 'none' });
    }
  },

  goMenu() { wx.switchTab({ url: '/pages/menu/menu' }); }
});
