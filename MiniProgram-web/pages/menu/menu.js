// pages/menu/menu.js
const { get } = require('../../utils/request');
const store = require('../../utils/store');

Page({
  data: {
    statusBarHeight: 20,     // 自定义导航栏：状态栏高度
    tableName: '',           // 桌号（真实场景由扫码桌台二维码下发）
    activeCategory: 0,
    categories: [],
    dishes: [],          // 当前分类下的菜品
    dishMap: {},         // categoryId -> dishes（本地缓存，避免重复请求）
    cartQty: {},         // dishId -> 列表直加数量（base sku），用于卡片步进器回显
    cartCount: 0,
    cartTotal: 0
  },

  onLoad() {
    const win = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync();
    const app = getApp();
    const tid = app && app.globalData.tableId;
    this.setData({
      statusBarHeight: win.statusBarHeight || 20,
      // 演示桌号：A + 两位编号（对应种子数据 dining_table A01=1）
      tableName: tid ? 'A' + ('0' + tid).slice(-2) : ''
    });
    this._specCache = {};    // dishId -> 是否有规格（决定列表直加 or 跳详情）
  },

  onShow() {
    this.refreshCart();
    this._unsub = store.subscribe(() => this.refreshCart());
    if (this.data.categories.length === 0) this.loadCategories();
  },

  onHide() { this._unsubscribe(); },
  onUnload() { this._unsubscribe(); },
  _unsubscribe() {
    if (this._unsub) { this._unsub(); this._unsub = null; }
  },

  async loadCategories() {
    try {
      const cats = await get('/menu/categories');
      this.setData({ categories: cats });
      if (cats.length) this.selectCategory(0);
    } catch (e) { /* 错误已在 request 内 toast */ }
  },

  // 兼容两种调用：WXML tap 事件（取 dataset.index）与 JS 内部数字下标
  async selectCategory(e) {
    const idx = typeof e === 'number' ? e : Number(e.currentTarget.dataset.index);
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

  // 列表直加（原型交互）：无规格 → 直接加 base sku；有规格 → 跳详情页选择
  async addToCart(e) {
    const id = e.currentTarget.dataset.id;
    const dish = this.data.dishes.find((d) => d.id === id);
    if (!dish || dish.isSoldOut === 1) return;
    if (this._specCache[id] === undefined) {
      try {
        const detail = await get('/menu/dish/' + id);
        this._specCache[id] = (detail.specs || []).length > 0;
      } catch (err) { this._specCache[id] = false; }
    }
    if (this._specCache[id]) {
      wx.navigateTo({ url: '/pages/dish-detail/dish-detail?id=' + id });
      return;
    }
    store.add({
      dishId: id,
      skuId: 'base',
      name: dish.name,
      price: dish.price,      // 单价（分）
      qty: 1,
      specs: [],
      specsJson: '[]',
      remark: ''
    });
  },

  // 步进器减号：只减列表直加的 base sku（规格项在购物车页内调整）
  minusCart(e) {
    const id = e.currentTarget.dataset.id;
    const item = store.get().find((i) => i.dishId === id && i.skuId === 'base');
    if (item) store.setQty('base', item.remark, item.qty - 1);
  },

  // 点卡片（图/名称）进详情，查看规格与介绍
  goDetail(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) return;
    wx.navigateTo({ url: '/pages/dish-detail/dish-detail?id=' + id });
  },

  refreshCart() {
    const qtyMap = {};
    store.get().forEach((i) => {
      if (i.skuId === 'base') qtyMap[i.dishId] = (qtyMap[i.dishId] || 0) + i.qty;
    });
    this.setData({ cartQty: qtyMap, cartCount: store.count(), cartTotal: store.total() });
  },

  goCart() {
    if (store.count() === 0) { wx.showToast({ title: '购物车为空', icon: 'none' }); return; }
    wx.navigateTo({ url: '/pages/cart/cart' });
  }
});
