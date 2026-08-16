package com.ordering.modules.setting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 门店设置（key-value 扩展，避免改动 shop 主表结构）。
 */
@Data
@TableName("shop_setting")
public class ShopSetting {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;
    private String settingKey;
    private String settingValue;
}
