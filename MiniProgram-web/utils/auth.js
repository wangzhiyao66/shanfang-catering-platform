// utils/auth.js
// 微信登录：wx.login 拿 code -> 后端 /auth/login 换 openid + member
const { post } = require('./request');

function login() {
  return new Promise((resolve, reject) => {
    const cached = wx.getStorageSync('member');
    if (cached && cached.openid) { resolve(cached); return; }

    wx.login({
      success: (res) => {
        if (!res.code) { reject(new Error('wx.login 失败')); return; }
        // 后端用 code 调微信 jscode2session 换 openid，并返回/创建 member
        post('/auth/login', { code: res.code })
          .then((member) => {
            wx.setStorageSync('member', member);
            resolve(member);
          })
          .catch(reject);
      },
      fail: reject
    });
  });
}

module.exports = { login };
