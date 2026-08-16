// pages/menu/menu.js
const { get } = require('../../utils/request');
const store = require('../../utils/store');

Page({
  data: {
    activeCategory: 0,
    categories: [],
    dishes: [],          // 当前分类下的菜品
    dishMap: {},         // categoryId -> dishes（本地缓存，避免重复请求）
    cartCount: 0,
    cartTotal: 0
  },

  onShow() {
    this.refreshCart();
    store.subscribe(() => this.refreshCart());
    if (this.data.categories.length === 0) this.loadCategories();
  },

  onHide() { store.subscribe(() => {}); },

  async loadCategories() {
    try {
      const cats = await get('/menu/categories');
      this.setData({ categories: cats });
      if (cats.length) this.selectCategory(0);
    } catch (e) { /* 错误已在 request 内 toast */ }
  },

  async selectCategory(idx) {
    const cat = this.data.categories[idx];
    if (!cat) return;
    this.setData({ activeCategory: idx });
    if (this.data.dishMap[cat.id]) {
      this.setData({ dishes: this.data.dishMap[cat.id] });
    } else {
      const list = await get('/menu/dishes?categoryId=' + cat.id);
      const map = this.data.dishMap;
      map[cat.id] = list;
      this.setData({ dishes: list, dishMap: map });
    }
  },

  // 点菜：携带菜品 id 跳详情页（详情由后端 /menu/dish/{id} 实时返回，含规格）
  goDetail(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) return;
    wx.navigateTo({ url: '/pages/dish-detail/dish-detail?id=' + id });
  },

  refreshCart() {
    this.setData({ cartCount: store.count(), cartTotal: store.total() });
  },

  goCart() {
    if (store.count() === 0) { wx.showToast({ title: '购物车为空', icon: 'none' }); return; }
    wx.navigateTo({ url: '/pages/cart/cart' });
  },

  // 演示：呼叫服务员（可对接后端 socket/订阅消息通知后厨）
  callWaiter() {
    wx.showToast({ title: '已呼叫服务员', icon: 'success' });
  }
});
