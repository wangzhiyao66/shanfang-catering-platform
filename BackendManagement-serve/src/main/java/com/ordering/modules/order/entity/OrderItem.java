package com.ordering.modules.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 订单明细：落定快照（dish_name / unit_price / specs_json），菜品改价改名不影响历史账单。
 */
@Data
@TableName("order_item")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long dishId;       // 菜品（快照引用）
    private Long skuId;        // 售卖单元
    private String dishName;   // 落定快照名
    private Integer qty;
    private Integer unitPrice; // 单价（分，落定）
    private String specsJson;  // 规格/辣度/忌口快照
    private String remark;     // 备注
}
