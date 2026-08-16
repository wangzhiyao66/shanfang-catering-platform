// pages/member/member.js
const { get } = require('../../utils/request');

// 会员等级映射（与后端 member_level 种子一致：1普通 2银卡 3金卡）
const LEVEL_MAP = { 1: '普通会员', 2: '银卡会员', 3: '金卡会员' };

// 优惠券状态文案（0未用 1已用 2过期）
const COUPON_STATUS = { 0: '可用', 1: '已使用', 2: '已过期' };

Page({
  data: {
    member: { nickname: '加载中…', levelName: '普通会员', points: 0, balance: 0 },
    coupons: []
  },

  async onShow() {
    try {
      const member = await get('/member');            // 后端返回 Member（含 levelId/points/balance 分）
      member.levelName = LEVEL_MAP[member.levelId] || '会员';
      this.setData({ member });
      this.loadCoupons();
    } catch (e) {}
  },

  async loadCoupons() {
    try {
      const list = await get('/member/coupons');      // 后端返回 Coupon 列表（value/threshold/name/validTo）
      const coupons = (list || []).map((c) => ({
        ...c,
        statusText: COUPON_STATUS[c.status] || ''
      }));
      this.setData({ coupons });
    } catch (e) {
      this.setData({ coupons: [] });
    }
  },

  goReserve() { wx.navigateTo({ url: '/pages/reserve/reserve' }); }
});
