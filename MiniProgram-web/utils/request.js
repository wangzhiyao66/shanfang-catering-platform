// utils/request.js
// 统一请求封装：对接 BackendManagement-serve 的 /api/client/* 接口。
// 约定：所有请求带 X-Shop-Id（租户隔离）；登录态接口额外带 X-Openid。
// 后端返回统一结构 { code:0, data, msg }；401 自动登录后重试一次。
const app = getApp();

function shopHeader() {
  const h = { 'content-type': 'application/json' };
  if (app && app.globalData.shopId != null) h['X-Shop-Id'] = String(app.globalData.shopId);
  return h;
}

function authHeader() {
  const h = shopHeader();
  if (app && app.globalData.openid) h['X-Openid'] = app.globalData.openid;
  return h;
}

function baseURL() {
  return (app && app.globalData.baseURL) || 'http://localhost:3000/api/client';
}

// 底层请求：返回 { statusCode, body }
function rawRequest({ url, method = 'GET', data = {}, header }) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: baseURL() + url,
      method,
      data,
      header,
      success: (res) => resolve({ statusCode: res.statusCode, data: res.data }),
      fail: (err) => reject(err)
    });
  });
}

// 静默登录：wx.login 拿 code -> 后端 /auth/login 换 openid，写入 globalData + 本地缓存
async function ensureLogin() {
  if (app && app.globalData.openid) return;
  const cached = wx.getStorageSync('openid');
  if (cached) { app.globalData.openid = cached; return; }
  const code = await new Promise((res, rej) =>
    wx.login({ success: (r) => (r.code ? res(r.code) : rej(r)), fail: rej }));
  const resp = await rawRequest({ url: '/auth/login', method: 'POST', data: { code }, header: shopHeader() });
  const openid = resp.data && resp.data.data && resp.data.data.openid;
  if (openid) {
    app.globalData.openid = openid;
    wx.setStorageSync('openid', openid);
  }
}

// 统一出口
function request({ url, method = 'GET', data = {}, auth = true }) {
  return new Promise(async (resolve, reject) => {
    try {
      const header = auth ? authHeader() : shopHeader();
      const resp = await rawRequest({ url, method, data, header });
      if (resp.statusCode === 200 && resp.data && resp.data.code === 0) {
        resolve(resp.data.data);
        return;
      }
      if (resp.statusCode === 401 && auth) {
        // 登录态缺失/失效：清掉本地 openid，重新登录并重试一次
        app.globalData.openid = null;
        wx.removeStorageSync('openid');
        await ensureLogin();
        const r2 = await rawRequest({ url, method, data, header: authHeader() });
        if (r2.statusCode === 200 && r2.data && r2.data.code === 0) { resolve(r2.data.data); return; }
        wx.showToast({ title: (r2.data && r2.data.msg) || '请先登录', icon: 'none' });
        reject(r2.data);
        return;
      }
      wx.showToast({ title: (resp.data && resp.data.msg) || '请求失败', icon: 'none' });
      reject(resp.data);
    } catch (e) {
      wx.showToast({ title: '网络错误', icon: 'none' });
      reject(e);
    }
  });
}

module.exports = {
  get: (url, data) => request({ url, method: 'GET', data }),
  post: (url, data) => request({ url, method: 'POST', data }),
  // 公开接口（浏览菜单等，只需 X-Shop-Id，不强制登录）
  pubGet: (url, data) => request({ url, method: 'GET', data, auth: false }),
  pubPost: (url, data) => request({ url, method: 'POST', data, auth: false })
};
