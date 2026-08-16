package com.ordering.modules.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ordering.modules.menu.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
