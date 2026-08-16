package com.ordering.modules.menu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ordering.common.result.BizException;
import com.ordering.common.result.CodeEnum;
import com.ordering.modules.menu.entity.Category;
import com.ordering.modules.menu.entity.Dish;
import com.ordering.modules.menu.entity.DishSpec;
import com.ordering.modules.menu.mapper.CategoryMapper;
import com.ordering.modules.menu.mapper.DishMapper;
import com.ordering.modules.menu.mapper.DishSpecMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuServiceImpl implements MenuService {

    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;
    private final DishSpecMapper dishSpecMapper;

    public MenuServiceImpl(CategoryMapper categoryMapper, DishMapper dishMapper, DishSpecMapper dishSpecMapper) {
        this.categoryMapper = categoryMapper;
        this.dishMapper = dishMapper;
        this.dishSpecMapper = dishSpecMapper;
    }

    /**
     * 多租户在 Service 层显式携带 shop_id，确保每条 SQL 都限定本店数据。
     * （MyBatis-Plus 3.5.17 已移除 TenantLineInnerInterceptor 自动注入插件）
     */
    @Override
    public List<Category> listCategories(Long shopId) {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getShopId, shopId)
                        .orderByAsc(Category::getSort));
    }

    @Override
    public List<Dish> listDishes(Long shopId, Long categoryId) {
        LambdaQueryWrapper<Dish> q = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getShopId, shopId)
                .orderByAsc(Dish::getSort);
        if (categoryId != null) {
            q.eq(Dish::getCategoryId, categoryId);
        }
        return dishMapper.selectList(q);
    }

    @Override
    public Dish getDish(Long shopId, Long id) {
        Dish d = dishMapper.selectOne(
                new LambdaQueryWrapper<Dish>()
                        .eq(Dish::getId, id)
                        .eq(Dish::getShopId, shopId));
        if (d == null) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "菜品不存在");
        }
        // 回填规格选项（dish_spec：份量/辣度/忌口等，含 price_delta 加价）
        d.setSpecs(dishSpecMapper.selectList(
                new LambdaQueryWrapper<DishSpec>()
                        .eq(DishSpec::getDishId, id)
                        .eq(DishSpec::getShopId, shopId)
                        .orderByAsc(DishSpec::getId)));
        return d;
    }

    @Override
    public Long createDish(Long shopId, Dish dish) {
        dish.setShopId(shopId);
        dish.setVersion(0);
        dishMapper.insert(dish);
        return dish.getId();
    }

    @Override
    public void updateDish(Long shopId, Long id, Dish dish) {
        // 先按「本店 + id」取当前记录，拿到 version 供乐观锁
        Dish existing = dishMapper.selectOne(
                new LambdaQueryWrapper<Dish>()
                        .eq(Dish::getId, id)
                        .eq(Dish::getShopId, shopId));
        if (existing == null) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "菜品不存在");
        }
        dish.setId(id);
        dish.setShopId(shopId);
        dish.setVersion(existing.getVersion()); // 携带版本，触发 OptimisticLockerInnerInterceptor
        int rows = dishMapper.updateById(dish);
        if (rows == 0) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "菜品已被他人修改，请刷新后重试");
        }
    }

    @Override
    public void deleteDish(Long shopId, Long id) {
        int rows = dishMapper.delete(
                new LambdaQueryWrapper<Dish>()
                        .eq(Dish::getId, id)
                        .eq(Dish::getShopId, shopId));
        if (rows == 0) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "菜品不存在");
        }
    }

    @Override
    public Long createCategory(Long shopId, Category category) {
        category.setShopId(shopId);
        categoryMapper.insert(category);
        return category.getId();
    }
}
