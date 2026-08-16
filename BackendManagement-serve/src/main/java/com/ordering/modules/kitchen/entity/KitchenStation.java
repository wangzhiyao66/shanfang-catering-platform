package com.ordering.modules.kitchen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 档口：热菜/凉菜/饮品/烧烤；用于按档口拆分出单。 */
@Data
@TableName("kitchen_station")
public class KitchenStation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;
    private String name;
    private Long printerId;
    private Integer timeoutMin;
}
