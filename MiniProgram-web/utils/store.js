// utils/store.js
// 轻量全局购物车 + 多人合单状态（持久化到本地，跨页面同步）
// item: { dishId, skuId, name, price(单价,分), qty, specs{}, remark, specsJson }
let cart = wx.getStorageSync('cart') || [];
let listeners = [];
// 订单快照：下单成功后缓存菜品明细，用于订单页展示（后端订单接口不返回 items）
let orderSnapshots = wx.getStorageSync('orderSnapshots') || {};

function persist() {
  wx.setStorageSync('cart', cart);
  listeners.forEach((fn) => fn(cart));
}

function findItem(skuId, remark) {
  return cart.find((i) => i.skuId === skuId && (i.remark || '') === (remark || ''));
}

module.exports = {
  init() { cart = wx.getStorageSync('cart') || []; },
  get() { return cart; },
  // 订单快照
  setOrderSnapshot(orderId, items) {
    orderSnapshots[orderId] = items;
    wx.setStorageSync('orderSnapshots', orderSnapshots);
  },
  getOrderSnapshot(orderId) { return orderSnapshots[orderId] || null; },
  add(item) {
    const exist = findItem(item.skuId, item.remark);
    if (exist) exist.qty += item.qty;
    else cart.push(item);
    persist();
  },
  setQty(skuId, remark, qty) {
    const it = findItem(skuId, remark);
    if (!it) return;
    if (qty <= 0) cart = cart.filter((i) => !(i.skuId === skuId && (i.remark || '') === (remark || '')));
    else it.qty = qty;
    persist();
  },
  remove(skuId, remark) {
    cart = cart.filter((i) => !(i.skuId === skuId && (i.remark || '') === (remark || '')));
    persist();
  },
  clear() { cart = []; persist(); },
  count() { return cart.reduce((s, i) => s + i.qty, 0); },
  total() { return cart.reduce((s, i) => s + i.price * i.qty, 0); },
  subscribe(fn) {
    listeners.push(fn);
    return () => { listeners = listeners.filter((f) => f !== fn); };
  }
};
