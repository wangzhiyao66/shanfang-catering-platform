package com.ordering.modules.order.service;

import com.ordering.modules.order.dto.OrderCreateDTO;
import com.ordering.modules.order.entity.Order;
import com.ordering.modules.order.vo.OrderAdminDetailVO;

import java.util.List;

public interface OrderService {

    /** 顾客端下单：服务端按菜品价计算金额，返回订单ID */
    Long createOrder(Long shopId, String openid, OrderCreateDTO dto);

    /** 顾客端查自己的订单 */
    Order getOrder(Long shopId, Long memberId, Long id);

    /** 顾客端我的订单列表 */
    List<Order> listMyOrders(Long shopId, Long memberId);

    /** 后台订单列表（按状态/桌台筛选） */
    List<Order> adminList(Long shopId, Integer status, Long tableId);

    /** 后台接单：已支付/待接单(1) → 制作中(2)，并生成后厨分单 */
    void acceptOrder(Long shopId, Long id);

    /** 状态机推进（校验合法流转） */
    void updateStatus(Long shopId, Long id, Integer targetStatus);

    /** 取消订单（→9） */
    void cancelOrder(Long shopId, Long id);

    /** 后台订单详情：订单 + 菜品明细 + 支付单 + 会员名 + 桌台号 */
    OrderAdminDetailVO adminGetOrder(Long shopId, Long id);

    /** 顾客端催菜：落库催菜记录（订单须处于制作中/已上菜） */
    void urgeOrder(Long shopId, Long memberId, Long orderId);
}
