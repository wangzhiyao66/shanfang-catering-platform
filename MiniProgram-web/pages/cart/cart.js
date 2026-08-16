// pages/cart/cart.js
const { get, post } = require('../../utils/request');
const store = require('../../utils/store');
const app = getApp();

Page({
  data: {
    items: [],
    total: 0,             // 菜品合计（分，含规格加价）
    tableId: null,
    coupons: [],          // 可用优惠券
    couponId: null,
    couponText: '不使用优惠券'
  },

  onShow() {
    this.render();
    this.loadCoupons();
  },

  render() {
    const items = store.get().map((i) => ({
      ...i,
      specsText: (i.specs || []).join(' / ')   // 规格名拼接展示
    }));
    this.setData({
      items,
      total: store.total(),
      tableId: app.globalData.tableId
    });
  },

  async loadCoupons() {
    try {
      const list = await get('/member/coupons');
      // 仅展示未使用、且达门槛的券（门槛按菜品合计判断）
      const usable = (list || []).filter((c) => c.status === 0 && (c.threshold || 0) <= this.data.total);
      this.setData({ coupons: usable });
    } catch (e) { /* 无优惠券不影响下单 */ }
  },

  onQty(e) {
    const { skuid, remark, qty } = e.currentTarget.dataset;
    store.setQty(skuid, remark, qty);
    this.render();
    this.loadCoupons();
  },

  remove(e) {
    const { skuid, remark } = e.currentTarget.dataset;
    store.remove(skuid, remark);
    this.render();
    this.loadCoupons();
  },

  // 选择优惠券
  onCoupon(e) {
    const idx = e.currentTarget.dataset.index;
    const c = this.data.coupons[idx];
    const couponId = this.data.couponId === c.id ? null : c.id;
    const couponText = couponId ? `${c.name}（-¥${c.value/100}）` : '不使用优惠券';
    this.setData({ couponId, couponText });
  },

  onCouponNone() {
    this.setData({ couponId: null, couponText: '不使用优惠券', showCoupons: false });
  },

  toggleCoupons() {
    this.setData({ showCoupons: !this.data.showCoupons });
  },

  async checkout() {
    if (store.count() === 0) { wx.showToast({ title: '购物车为空', icon: 'none' }); return; }
    const payload = {
      type: app.globalData.tableId ? 1 : 3,   // 1堂食 3自提
      tableId: app.globalData.tableId || null,
      peopleCount: 1,
      couponId: this.data.couponId,
      items: store.get().map((i) => ({
        dishId: i.dishId,
        skuId: null,
        qty: i.qty,
        specsJson: i.specsJson || JSON.stringify(i.specs || []),
        remark: i.remark || ''
      }))
    };
    try {
      wx.showLoading({ title: '提交中' });
      const orderId = await post('/order', payload);   // 返回 Long 订单ID，状态 0 待支付
      await this.pay(orderId);
    } catch (e) {
      wx.hideLoading();
    }
  },

  async pay(orderId) {
    try {
      const params = await post('/pay/prepay', { orderId });
      wx.hideLoading();
      wx.requestPayment({
        timeStamp: params.timeStamp,
        nonceStr: params.nonceStr,
        package: params.package,
        signType: params.signType || 'RSA',
        paySign: params.paySign,
        success: () => {
          store.clear();
          wx.showToast({ title: '支付成功', icon: 'success' });
          setTimeout(() => wx.switchTab({ url: '/pages/order/order' }), 600);
        },
        fail: () => {
          wx.showToast({ title: '已取消支付', icon: 'none' });
        }
      });
    } catch (e) {
      wx.hideLoading();
    }
  }
});
