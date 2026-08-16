package com.ordering.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ordering.modules.order.entity.OrderPayment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderPaymentMapper extends BaseMapper<OrderPayment> {
}
