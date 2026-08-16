package com.ordering.modules.kitchen.service;

import com.ordering.modules.kitchen.entity.KitchenTicket;
import com.ordering.modules.order.entity.Order;

import java.util.List;

public interface KitchenService {

    /** 接单后按档口拆分出厨打分单（kitchen_ticket），并推送给后厨 KDS。 */
    void createTicketsForOrder(Order order);

    /** 后厨待做/进行中列表 */
    List<KitchenTicket> listTickets(Long shopId, Integer status);

    /** 标记某分单制作中(1) */
    void startTicket(Long shopId, Long ticketId);

    /** 标记某分单完成(2)；若整单分单全部完成则推进 order 状态到「已上菜/待取餐(3)」 */
    void doneTicket(Long shopId, Long ticketId);
}
