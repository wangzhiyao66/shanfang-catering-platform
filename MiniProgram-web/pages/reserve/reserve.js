// pages/reserve/reserve.js
const { get, post } = require('../../utils/request');

// 桌台状态文案（0空闲 1占用 2预定 3清洁中）
const TABLE_STATUS = { 0: '空闲', 1: '占用', 2: '已预定', 3: '清洁中' };

function isoDate(d) {
  const m = `${d.getMonth() + 1}`.padStart(2, '0');
  const day = `${d.getDate()}`.padStart(2, '0');
  return `${d.getFullYear()}-${m}-${day}`;
}

Page({
  data: {
    dates: [],          // [{label, value}]
    times: ['11:00', '12:00', '13:00', '17:30', '18:30', '19:30'],
    tables: [],         // 后端桌台列表
    dateIndex: 0,
    timeIndex: 0,
    tableIndex: null,
    party: 2,
    tableId: null
  },

  onLoad() {
    this.initDates();
    this.loadTables();
  },

  async loadTables() {
    try {
      const list = await get('/tables');   // 后端 GET /api/client/tables
      const tables = (list || []).map((t) => ({
        id: t.id,
        name: `${t.tableNo}（${t.area}·${t.seats}人）`,
        status: t.status
      }));
      this.setData({ tables });
    } catch (e) {
      this.setData({ tables: [] });
    }
  },

  initDates() {
    const dates = [];
    const d = new Date();
    for (let i = 0; i < 7; i++) {
      const t = new Date(d.getTime() + i * 86400000);
      dates.push({ label: `${t.getMonth() + 1}月${t.getDate()}日`, value: isoDate(t) });
    }
    this.setData({ dates, dateIndex: 0 });
  },

  onDate(e) { this.setData({ dateIndex: e.detail.value }); },
  onTime(e) { this.setData({ timeIndex: e.detail.value }); },
  onParty(e) { this.setData({ party: e.detail }); },
  onTable(e) {
    const idx = e.detail.value;
    this.setData({ tableIndex: idx, tableId: this.data.tables[idx].id });
  },

  async submit() {
    const { dates, times, dateIndex, timeIndex, party, tableId } = this.data;
    const payload = {
      date: dates[dateIndex].value,      // yyyy-MM-dd
      timeSlot: times[timeIndex],
      partySize: party,
      tableId: tableId || null
    };
    try {
      wx.showLoading({ title: '提交中' });
      await post('/reservation', payload);
      wx.hideLoading();
      wx.showToast({ title: '预订成功', icon: 'success' });
      setTimeout(() => wx.switchTab({ url: '/pages/member/member' }), 800);
    } catch (e) {
      wx.hideLoading();
    }
  }
});
