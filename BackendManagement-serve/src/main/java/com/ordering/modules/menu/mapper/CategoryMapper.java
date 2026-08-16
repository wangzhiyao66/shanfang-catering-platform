package com.ordering.modules.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ordering.modules.menu.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
