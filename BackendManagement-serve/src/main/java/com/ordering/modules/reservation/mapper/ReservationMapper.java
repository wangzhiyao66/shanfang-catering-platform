package com.ordering.modules.reservation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ordering.modules.reservation.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {
}
