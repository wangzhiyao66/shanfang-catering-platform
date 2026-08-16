package com.ordering.modules.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("shop")
public class Shop {

    /** 店铺主键，同时作为 shop_id（多租户字段） */
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private Integer status;
    private LocalDateTime createTime;
}
