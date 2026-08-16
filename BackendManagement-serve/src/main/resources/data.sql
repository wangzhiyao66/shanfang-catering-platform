-- 种子数据（INSERT IGNORE，可重复执行）。shop_id = 1 对应演示店铺。
INSERT IGNORE INTO shop (id, name, status, create_time) VALUES (1, '膳房·中餐', 1, NOW());

INSERT IGNORE INTO category (id, shop_id, name, sort, icon, status, create_time) VALUES
(1, 1, '热菜', 1, '', 1, NOW()),
(2, 1, '凉菜', 2, '', 1, NOW()),
(3, 1, '主食', 3, '', 1, NOW()),
(4, 1, '汤羹', 4, '', 1, NOW()),
(5, 1, '饮品', 5, '', 1, NOW()),
(6, 1, '甜点', 6, '', 0, NOW());

INSERT IGNORE INTO dish (id, shop_id, category_id, name, price, description, image, status, is_sold_out, sort, create_time, update_time, deleted, version) VALUES
(1, 1, 1, '宫保鸡丁', 3800, '经典川菜，鸡肉鲜嫩花生酥脆', 'https://picsum.photos/seed/dish1/200/200', 1, 0, 1, NOW(), NOW(), 0, 0),
(2, 1, 1, '鱼香肉丝', 3200, '咸甜酸辣兼备', 'https://picsum.photos/seed/dish2/200/200', 1, 0, 2, NOW(), NOW(), 0, 0),
(3, 1, 1, '红烧肉', 4800, '肥而不腻，入口即化', 'https://picsum.photos/seed/dish3/200/200', 1, 0, 3, NOW(), NOW(), 0, 0),
(4, 1, 1, '麻婆豆腐', 2600, '麻辣鲜香', 'https://picsum.photos/seed/dish4/200/200', 1, 0, 4, NOW(), NOW(), 0, 0),
(5, 1, 2, '凉拌黄瓜', 1200, '清爽解腻', 'https://picsum.photos/seed/dish5/200/200', 1, 0, 1, NOW(), NOW(), 0, 0),
(6, 1, 2, '口水鸡', 3600, '麻辣鲜香，皮滑肉嫩', 'https://picsum.photos/seed/dish6/200/200', 1, 0, 2, NOW(), NOW(), 0, 0),
(7, 1, 2, '白切鸡', 4200, '原汁原味', 'https://picsum.photos/seed/dish7/200/200', 0, 0, 3, NOW(), NOW(), 0, 0),
(8, 1, 3, '米饭', 200, '东北珍珠米', 'https://picsum.photos/seed/dish8/200/200', 1, 0, 1, NOW(), NOW(), 0, 0),
(9, 1, 3, '牛肉面', 2800, '汤鲜面劲', 'https://picsum.photos/seed/dish9/200/200', 1, 0, 2, NOW(), NOW(), 0, 0),
(10, 1, 3, '小笼包', 1800, '皮薄汁多', 'https://picsum.photos/seed/dish10/200/200', 1, 0, 3, NOW(), NOW(), 0, 0),
(11, 1, 4, '西红柿鸡蛋汤', 1600, '家常暖胃', 'https://picsum.photos/seed/dish11/200/200', 1, 0, 1, NOW(), NOW(), 0, 0),
(12, 1, 4, '酸辣汤', 1800, '开胃爽口', 'https://picsum.photos/seed/dish12/200/200', 1, 0, 2, NOW(), NOW(), 0, 0),
(13, 1, 5, '鲜榨橙汁', 1500, '100% 鲜榨', 'https://picsum.photos/seed/dish13/200/200', 1, 0, 1, NOW(), NOW(), 0, 0),
(14, 1, 5, '可乐', 600, '冰镇', 'https://picsum.photos/seed/dish14/200/200', 1, 0, 2, NOW(), NOW(), 0, 0),
(15, 1, 5, '酸梅汤', 1200, '解腻生津', 'https://picsum.photos/seed/dish15/200/200', 1, 0, 3, NOW(), NOW(), 0, 0),
(16, 1, 6, '提拉米苏', 2800, '意式经典', 'https://picsum.photos/seed/dish16/200/200', 0, 0, 1, NOW(), NOW(), 0, 0),
(17, 1, 6, '芒果布丁', 1800, '香甜爽滑', 'https://picsum.photos/seed/dish17/200/200', 0, 0, 2, NOW(), NOW(), 0, 0),
(18, 1, 1, '辣子鸡', 4600, '香辣酥脆', 'https://picsum.photos/seed/dish18/200/200', 1, 0, 5, NOW(), NOW(), 0, 0),
(19, 1, 1, '回锅肉', 3600, '川味家常', 'https://picsum.photos/seed/dish19/200/200', 1, 0, 6, NOW(), NOW(), 0, 0),
(20, 1, 2, '皮蛋豆腐', 1400, '清凉爽口', 'https://picsum.photos/seed/dish20/200/200', 1, 0, 4, NOW(), NOW(), 0, 0);

INSERT IGNORE INTO dish_spec (id, shop_id, dish_id, name, price_delta, stock) VALUES
(1, 1, 1, '标准', 0, 99),
(2, 1, 1, '大份', 800, 99),
(3, 1, 3, '标准', 0, 99),
(4, 1, 3, '小份', -1000, 99),
(5, 1, 8, '1 碗', 0, 99),
(6, 1, 8, '2 碗', 200, 99),
(7, 1, 9, '微辣', 0, 99),
(8, 1, 9, '中辣', 0, 99),
(9, 1, 9, '特辣', 0, 99),
(10, 1, 13, '中杯', 0, 99),
(11, 1, 13, '大杯', 500, 99),
(12, 1, 14, '罐装', 0, 99),
(13, 1, 14, '瓶装', 200, 99);

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
(4, 1, '饮品', NULL, 5),
(5, 1, '汤羹', NULL, 8),
(6, 1, '甜点', NULL, 6);

-- ===== 优惠券（发给演示会员 id=1；面额/门槛单位为分） =====
-- 满50减10 / 无门槛减5 / 满100减20；有效期 30 天
INSERT IGNORE INTO coupon (id, shop_id, member_id, name, value, threshold, status, start_time, end_time, created_at) VALUES
(1, 1, 1, '满50减10', 1000, 5000, 0, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW()),
(2, 1, 1, '新客立减5元', 500, 0, 0, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW()),
(3, 1, 1, '满100减20', 2000, 10000, 0, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW());

-- ===== 角色（权限码：dish:manage,order:manage,table:manage,member:manage,report:view,employee:manage,marketing:manage,setting:manage,dashboard:view；* = 全部） =====
INSERT IGNORE INTO `role` (id, shop_id, name, permissions, status, create_time) VALUES
(1, 1, '超级管理员', '*', 1, NOW()),
(2, 1, '店长', 'dish:manage,order:manage,table:manage,member:manage,report:view,marketing:manage,setting:manage', 1, NOW()),
(3, 1, '服务员', 'order:manage,table:manage,member:manage', 1, NOW());

-- ===== 员工（演示密码统一 123456） =====
INSERT IGNORE INTO employee (id, shop_id, name, phone, account, password, role_id, status, create_time) VALUES
(1, 1, '王店长', '13800000001', 'wang', '123456', 2, 1, NOW()),
(2, 1, '小李',   '13800000002', 'li',   '123456', 3, 1, NOW()),
(3, 1, '张厨',   '13800000003', 'zhang','123456', 1, 1, NOW());

-- ===== 门店设置（基础设置页演示数据） =====
INSERT IGNORE INTO shop_setting (id, shop_id, setting_key, setting_value) VALUES
(1, 1, 'address', '上海市浦东新区xx路88号'),
(2, 1, 'phone', '021-88888888'),
(3, 1, 'businessHours', '10:00-22:00'),
(4, 1, 'notice', '本店主打家常川菜，欢迎光临'),
(5, 1, 'logo', '');
