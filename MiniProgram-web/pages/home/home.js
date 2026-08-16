// pages/home/home.js
// 首页：店名 + 搜索入口 + 会员日 banner + 快捷入口（扫码点餐/外卖/到店自提/预约订座）
// + 店长推荐（真实菜品）+ 热销榜（真实菜品，按价格展示）。
const { pubGet } = require('../../utils/request');

function mapDish(d) {
  return {
    id: d.id,
    name: d.name,
    image: d.image,
    // 金额按分->元展示（后端以分为单位）
    price: (d.price / 100).toFixed(2)
  };
}

Page({
  data: {
    recommend: [], // 店长推荐（卡片）
    hot: []        // 热销榜（列表，带 rank）
  },

  onShow() {
    this.loadDishes();
  },

  // 拉取真实菜品，区分「店长推荐」与「热销榜」
  async loadDishes() {
    try {
      const list = (await pubGet('/menu/dishes')) || [];
      const onSale = list.filter(d => d.status === 1 && !d.isSoldOut);
      const rec = onSale.slice(0, 3).map(mapDish);
      const hot = onSale.slice(3, 7).map((d, i) => Object.assign(mapDish(d), { rank: i + 1 }));
      this.setData({ recommend: rec, hot });
    } catch (e) {
      // 首页降级：不阻断浏览，推荐/热销留空
    }
  },

  // 快捷入口：扫码点餐 / 外卖 / 到店自提 -> 点餐 tab
  goMenu() {
    wx.switchTab({ url: '/pages/menu/menu' });
  },

  // 快捷入口：预约订座 -> 预订页（非 tab，navigateTo）
  goReserve() {
    wx.navigateTo({ url: '/pages/reserve/reserve' });
  },

  // 点击菜品 -> 详情
  goDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/dish-detail/dish-detail?id=' + id });
  }
});
