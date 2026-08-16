package com.ordering.modules.menu.service;

import com.ordering.modules.menu.entity.Category;
import com.ordering.modules.menu.entity.Dish;

import java.util.List;

public interface MenuService {

    List<Category> listCategories(Long shopId);

    List<Dish> listDishes(Long shopId, Long categoryId);

    Dish getDish(Long shopId, Long id);

    Long createDish(Long shopId, Dish dish);

    void updateDish(Long shopId, Long id, Dish dish);

    void deleteDish(Long shopId, Long id);

    Long createCategory(Long shopId, Category category);
}
