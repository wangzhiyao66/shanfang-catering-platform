// pages/dish-detail/dish-detail.js
// 菜品详情由后端 /menu/dish/{id} 实时返回（含 dish_spec 规格与 price_delta 加价）。
// 规格为可勾选选项（同一菜品可叠加，如「大份 +¥10」），单价 = 基准价 + Σ选中规格加价。
const { get } = require('../../utils/request');
const store = require('../../utils/store');

Page({
  data: {
    id: null,
    dish: {},
    specs: [],                 // 真实规格选项 [{name, priceDelta}]
    selected: {},              // name -> true/false（多选加项）
    qty: 1,
    remark: '',
    price: 0,                  // 合计（分，含规格加价）
    skuKey: ''                 // 规格组合键，用于购物车去重
  },

  onLoad(opt) {
    if (opt.id) {
      this.setData({ id: opt.id });
      this.loadDetail(opt.id);
    }
  },

  async loadDetail(id) {
    try {
      const dish = await get('/menu/dish/' + id);
      const specs = dish.specs || [];
      const selected = {};
      // 默认不勾选任何加项（基准价起）
      specs.forEach((s) => { selected[s.name] = false; });
      this.setData({ dish, specs, selected });
      this.calcPrice();
    } catch (e) {
      wx.showToast({ title: '菜品加载失败', icon: 'none' });
    }
  },

  onToggleSpec(e) {
    const name = e.currentTarget.dataset.name;
    const selected = Object.assign({}, this.data.selected);
    selected[name] = !selected[name];
    this.setData({ selected });
    this.calcPrice();
  },

  onQty(e) { this.setData({ qty: e.detail }); this.calcPrice(); },

  onRemark(e) { this.setData({ remark: e.detail }); },

  calcPrice() {
    const { dish, specs, selected, qty } = this.data;
    if (!dish.id) return;
    // 基准价 + 命中的规格加价
    let unit = dish.price || 0;
    const chosen = [];
    specs.forEach((s) => {
      if (selected[s.name]) {
        unit += (s.priceDelta || 0);
        chosen.push(s.name);
      }
    });
    const price = unit * qty;
    const skuKey = chosen.slice().sort().join('|');
    this.setData({ price, skuKey });
  },

  addToCart() {
    const { dish, specs, selected, qty, remark, price, skuKey } = this.data;
    if (!dish.id) return;
    // 选中的规格名数组（与后端 dish_spec.name 对齐，用于回算加价）
    const chosen = specs.filter((s) => selected[s.name]).map((s) => s.name);
    store.add({
      dishId: dish.id,
      skuId: skuKey || 'base',     // 同一菜品不同规格视为不同 sku
      name: dish.name,
      price: price / qty,          // 单价（分，含规格加价）
      qty,
      specs: chosen,               // 选中规格名数组（展示用）
      specsJson: JSON.stringify(chosen), // 提交后端，用于回算加价
      remark
    });
    wx.showToast({ title: '已加入购物车', icon: 'success' });
    setTimeout(() => wx.navigateBack(), 600);
  }
});
