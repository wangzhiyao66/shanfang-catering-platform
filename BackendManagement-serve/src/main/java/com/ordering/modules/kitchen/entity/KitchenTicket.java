package com.ordering.modules.kitchen.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 厨打分单：订单按档口拆分出的出单，状态独立。
 * status 0待做 1制作中 2完成 3退单；items_json 本档口菜品明细。
 */
@Data
@TableName("kitchen_ticket")
public class KitchenTicket {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;
    private Long orderId;
    private Long stationId;
    private Long printerId;
    private String itemsJson;
    private Integer status;
    private LocalDateTime pushedAt;
    private LocalDateTime doneAt;
}
