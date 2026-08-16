-- 种子数据（INSERT IGNORE，可重复执行）。shop_id = 1 对应演示店铺。
INSERT IGNORE INTO shop (id, name, status, create_time) VALUES (1, '膳房·中餐', 1, NOW());

INSERT IGNORE INTO category (id, shop_id, name, sort, icon, status, create_time) VALUES
(1, 1, '热菜', 1, '', 1, NOW()),
(2, 1, '凉菜', 2, '', 1, NOW()),
(3, 1, '主食', 3, '', 1, NOW()),
(4, 1, '饮品', 4, '', 1, NOW());

INSERT IGNORE INTO dish (id, shop_id, category_id, name, price, description, image, status, is_sold_out, sort, create_time, update_time, deleted, version) VALUES
(1, 1, 1, '宫保鸡丁', 3800, '经典川味，花生鸡丁', '', 1, 0, 1, NOW(), NOW(), 0, 0),
(2, 1, 1, '麻婆豆腐', 2800, '麻辣鲜香',           '', 1, 0, 2, NOW(), NOW(), 0, 0),
(3, 1, 2, '凉拌黄瓜', 1500, '爽口开胃',           '', 1, 0, 1, NOW(), NOW(), 0, 0),
(4, 1, 3, '米饭',     200,  '一碗',               '', 1, 0, 1, NOW(), NOW(), 0, 0),
(5, 1, 4, '酸梅汤',   1200, '解腻',               '', 1, 0, 1, NOW(), NOW(), 0, 0);

INSERT IGNORE INTO dish_spec (id, shop_id, dish_id, name, price_delta, stock) VALUES
(1, 1, 1, '大份', 1000, 99),
(2, 1, 1, '微辣',    0, 99),
(3, 1, 1, '不辣',    0, 99);

-- ===== 会员等级（threshold 为分：500元=50000，2000元=200000） =====
INSERT IGNORE INTO member_level (id, shop_id, name, discount, threshold) VALUES
(1, 1, '普通', 1.00, 0),
(2, 1, '银卡', 0.95, 50000),
(3, 1, '金卡', 0.90, 200000);

-- 演示会员（与骨架登录返回的 demo_openid_<code> 对应，code=test 时 openid=demo_openid_test）
INSERT IGNORE INTO `member` (id, shop_id, openid, nickname, level_id, points, balance, is_blocked) VALUES
(1, 1, 'demo_openid_test', '演示顾客', 1, 120, 0, 0);

-- ===== 桌台 =====
INSERT IGNORE INTO dining_table (id, shop_id, table_no, area, seats, status, current_order_id, qr_token, version) VALUES
(1, 1, 'A01', '大厅', 4, 0, NULL, 'qr_A01', 0),
(2, 1, 'A02', '大厅', 2, 0, NULL, 'qr_A02', 0),
(3, 1, 'B01', '包间', 8, 0, NULL, 'qr_B01', 0);

-- ===== 后厨档口（名称需与菜品分类名一致，接单时按分类拆分出单） =====
INSERT IGNORE INTO kitchen_station (id, shop_id, name, printer_id, timeout_min) VALUES
(1, 1, '热菜', NULL, 10),
(2, 1, '凉菜', NULL, 8),
(3, 1, '主食', NULL, 8),
(4, 1, '饮品', NULL, 5);

-- ===== 优惠券（发给演示会员 id=1；面额/门槛单位为分） =====
-- 满50减10 / 无门槛减5 / 满100减20；有效期 30 天
INSERT IGNORE INTO coupon (id, shop_id, member_id, name, value, threshold, status, start_time, end_time, created_at) VALUES
(1, 1, 1, '满50减10', 1000, 5000, 0, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW()),
(2, 1, 1, '新客立减5元', 500, 0, 0, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW()),
(3, 1, 1, '满100减20', 2000, 10000, 0, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW());
