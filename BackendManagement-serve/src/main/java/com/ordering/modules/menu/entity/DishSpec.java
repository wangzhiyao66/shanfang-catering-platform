package com.ordering.modules.menu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dish_spec")
public class DishSpec {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;
    private Long dishId;
    private String name;            // 如 份量 / 辣度 / 忌口
    private Integer priceDelta;     // 相对菜品价的差额（分）
    private Integer stock;
}
