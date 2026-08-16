-- 建表（可重复执行）。字符集 utf8mb4，金额用 INT(分)。
CREATE TABLE IF NOT EXISTS shop (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  name        VARCHAR(64)  NOT NULL DEFAULT '',
  status      TINYINT      NOT NULL DEFAULT 1,
  create_time DATETIME     DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS category (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  shop_id     BIGINT       NOT NULL,
  name        VARCHAR(64)  NOT NULL DEFAULT '',
  sort        INT          NOT NULL DEFAULT 0,
  icon        VARCHAR(255) DEFAULT '',
  status      TINYINT      NOT NULL DEFAULT 1,
  create_time DATETIME     DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS dish (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  shop_id     BIGINT       NOT NULL,
  category_id BIGINT       NOT NULL DEFAULT 0,
  name        VARCHAR(128) NOT NULL DEFAULT '',
  price       INT          NOT NULL DEFAULT 0,
  description VARCHAR(512) DEFAULT '',
  image       VARCHAR(512) DEFAULT '',
  status      TINYINT      NOT NULL DEFAULT 1,
  is_sold_out TINYINT      NOT NULL DEFAULT 0,
  sort        INT          NOT NULL DEFAULT 0,
  create_time DATETIME     DEFAULT NULL,
  update_time DATETIME     DEFAULT NULL,
  deleted     TINYINT      NOT NULL DEFAULT 0,
  version     INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_shop (shop_id),
  KEY idx_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS dish_spec (
  id         BIGINT   NOT NULL AUTO_INCREMENT,
  shop_id    BIGINT   NOT NULL,
  dish_id    BIGINT   NOT NULL DEFAULT 0,
  name       VARCHAR(64) NOT NULL DEFAULT '',
  price_delta INT     NOT NULL DEFAULT 0,
  stock      INT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_shop (shop_id),
  KEY idx_dish (dish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 订单 / 交易域 =====
CREATE TABLE IF NOT EXISTS `order` (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  shop_id       BIGINT       NOT NULL,
  order_no      VARCHAR(32)  NOT NULL,
  type          TINYINT      NOT NULL DEFAULT 1 COMMENT '1堂食2外卖3自提',
  member_id     BIGINT       NOT NULL,
  table_id      BIGINT       DEFAULT NULL,
  status        TINYINT      NOT NULL DEFAULT 0 COMMENT '状态机',
  people_count  INT          DEFAULT 1,
  total_amount  INT          NOT NULL DEFAULT 0 COMMENT '分',
  discount_amount INT        NOT NULL DEFAULT 0,
  pay_amount    INT          NOT NULL DEFAULT 0,
  coupon_id     BIGINT       DEFAULT NULL,
  paid_at       DATETIME     DEFAULT NULL,
  version       INT          NOT NULL DEFAULT 0,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at    DATETIME     DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_shop_status (shop_id, status, created_at),
  KEY idx_member (member_id),
  KEY idx_table (table_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_item (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  order_id   BIGINT       NOT NULL,
  dish_id    BIGINT       DEFAULT NULL,
  sku_id     BIGINT       DEFAULT NULL,
  dish_name  VARCHAR(64)  DEFAULT '',
  qty        INT          NOT NULL DEFAULT 1,
  unit_price INT          NOT NULL DEFAULT 0 COMMENT '分',
  specs_json VARCHAR(512) DEFAULT NULL,
  remark     VARCHAR(128) DEFAULT '',
  PRIMARY KEY (id),
  KEY idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_payment (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  order_id   BIGINT       NOT NULL,
  pay_no     VARCHAR(64)  NOT NULL,
  channel    VARCHAR(16)  DEFAULT 'wechat',
  amount     INT          NOT NULL DEFAULT 0 COMMENT '分',
  status     TINYINT      NOT NULL DEFAULT 0 COMMENT '0待付1成功2失败3退款',
  paid_at    DATETIME     DEFAULT NULL,
  refund_no  VARCHAR(64)  DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_pay_no (pay_no),
  KEY idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 会员 / 顾客域 =====
CREATE TABLE IF NOT EXISTS `member` (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  shop_id      BIGINT       NOT NULL,
  openid       VARCHAR(64)  DEFAULT '',
  unionid      VARCHAR(64)  DEFAULT '',
  phone        VARCHAR(20)  DEFAULT '',
  nickname     VARCHAR(64)  DEFAULT '',
  avatar       VARCHAR(255) DEFAULT '',
  level_id     BIGINT       DEFAULT NULL,
  points       INT          NOT NULL DEFAULT 0,
  balance      INT          NOT NULL DEFAULT 0 COMMENT '分',
  is_blocked   TINYINT      NOT NULL DEFAULT 0,
  last_active_at DATETIME   DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_openid_shop (openid, shop_id),
  KEY idx_shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS member_level (
  id        BIGINT       NOT NULL AUTO_INCREMENT,
  shop_id   BIGINT       NOT NULL,
  name      VARCHAR(32)  DEFAULT '',
  discount  DECIMAL(3,2) DEFAULT 1.00,
  threshold INT          NOT NULL DEFAULT 0 COMMENT '升级门槛（分）',
  PRIMARY KEY (id),
  KEY idx_shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS points_log (
  id        BIGINT       NOT NULL AUTO_INCREMENT,
  member_id BIGINT       NOT NULL,
  `change`  INT          NOT NULL DEFAULT 0,
  type      VARCHAR(16)  DEFAULT '',
  ref_id    BIGINT       DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 桌台 / 场景域 =====
CREATE TABLE IF NOT EXISTS dining_table (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  shop_id         BIGINT       NOT NULL,
  table_no        VARCHAR(16)  DEFAULT '',
  area            VARCHAR(16)  DEFAULT '大厅',
  seats           INT          NOT NULL DEFAULT 2,
  status          TINYINT      NOT NULL DEFAULT 0 COMMENT '0空闲1占用2预定3清洁中',
  current_order_id BIGINT      DEFAULT NULL,
  qr_token        VARCHAR(64)  DEFAULT '',
  version         INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reservation (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  shop_id    BIGINT       NOT NULL,
  member_id  BIGINT       NOT NULL,
  table_id   BIGINT       DEFAULT NULL,
  `date`     DATE         DEFAULT NULL,
  time_slot  VARCHAR(32)  DEFAULT '',
  party_size INT          NOT NULL DEFAULT 1,
  deposit    INT          NOT NULL DEFAULT 0 COMMENT '分',
  status     TINYINT      NOT NULL DEFAULT 0 COMMENT '0待确认1已确认2到店3取消4爽约',
  PRIMARY KEY (id),
  KEY idx_shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 优惠券 / 营销域（会员券，status 0未用 1已用 2过期） =====
CREATE TABLE IF NOT EXISTS coupon (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  shop_id    BIGINT       NOT NULL,
  member_id  BIGINT       NOT NULL,
  name       VARCHAR(64)  DEFAULT '',
  value      INT          NOT NULL DEFAULT 0 COMMENT '面额（分）',
  threshold  INT          NOT NULL DEFAULT 0 COMMENT '使用门槛（分），0=无门槛',
  status     TINYINT      NOT NULL DEFAULT 0 COMMENT '0未用1已用2过期',
  start_time DATETIME     DEFAULT NULL,
  end_time   DATETIME     DEFAULT NULL,
  used_at    DATETIME     DEFAULT NULL,
  created_at DATETIME     DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_shop_member (shop_id, member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 催菜记录（独立表，避免改动 order 主表结构） =====
CREATE TABLE IF NOT EXISTS order_urge (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  shop_id    BIGINT       NOT NULL,
  order_id   BIGINT       NOT NULL,
  member_id  BIGINT       NOT NULL,
  openid     VARCHAR(64)  DEFAULT '',
  created_at DATETIME     DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_order (order_id),
  KEY idx_shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 后厨 / 履约域 =====
CREATE TABLE IF NOT EXISTS kitchen_station (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  shop_id     BIGINT       NOT NULL,
  name        VARCHAR(32)  DEFAULT '',
  printer_id  BIGINT       DEFAULT NULL,
  timeout_min INT          NOT NULL DEFAULT 10,
  PRIMARY KEY (id),
  KEY idx_shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS kitchen_ticket (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  shop_id    BIGINT       NOT NULL,
  order_id   BIGINT       NOT NULL,
  station_id BIGINT       NOT NULL,
  printer_id BIGINT       DEFAULT NULL,
  items_json JSON,
  status     TINYINT      NOT NULL DEFAULT 0 COMMENT '0待做1制作中2完成3退单',
  pushed_at  DATETIME     DEFAULT NULL,
  done_at    DATETIME     DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_station (station_id, status),
  KEY idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 员工 / 角色（RBAC） =====
CREATE TABLE IF NOT EXISTS `role` (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  shop_id     BIGINT       NOT NULL,
  name        VARCHAR(32)  NOT NULL DEFAULT '',
  permissions VARCHAR(1024) DEFAULT '' COMMENT '逗号分隔的权限码；* 表示全部',
  status      TINYINT      NOT NULL DEFAULT 1,
  create_time DATETIME     DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS employee (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  shop_id     BIGINT       NOT NULL,
  name        VARCHAR(32)  NOT NULL DEFAULT '',
  phone       VARCHAR(20)  DEFAULT '',
  account     VARCHAR(32)  DEFAULT '',
  password    VARCHAR(64)  DEFAULT '',
  role_id     BIGINT       DEFAULT NULL,
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1在职0停用',
  create_time DATETIME     DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_shop (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 门店设置（key-value 扩展，避免改动 shop 主表结构） =====
CREATE TABLE IF NOT EXISTS shop_setting (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  shop_id      BIGINT       NOT NULL,
  setting_key  VARCHAR(64)  NOT NULL,
  setting_value VARCHAR(512) DEFAULT '',
  PRIMARY KEY (id),
  UNIQUE KEY uk_shop_key (shop_id, setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
