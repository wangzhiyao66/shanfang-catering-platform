package com.ordering.modules.menu.controller;

import com.ordering.common.context.RequestContext;
import com.ordering.common.result.R;
import com.ordering.modules.menu.entity.Category;
import com.ordering.modules.menu.entity.Dish;
import com.ordering.modules.menu.service.MenuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MenuClientController {

    private final MenuService menuService;

    public MenuClientController(MenuService menuService) {
        this.menuService = menuService;
    }

    /** 顾客端：分类列表（仅需 X-Shop-Id） GET /api/client/menu/categories */
    @GetMapping("/client/menu/categories")
    public R<List<Category>> categories() {
        return R.ok(menuService.listCategories(RequestContext.getShopId()));
    }

    /** 顾客端：菜品列表（按分类筛选） GET /api/client/menu/dishes?categoryId= */
    @GetMapping("/client/menu/dishes")
    public R<List<Dish>> dishes(@RequestParam(required = false) Long categoryId) {
        return R.ok(menuService.listDishes(RequestContext.getShopId(), categoryId));
    }

    /** 顾客端：菜品详情（含规格选项） GET /api/client/menu/dish/{id} */
    @GetMapping("/client/menu/dish/{id}")
    public R<Dish> dishDetail(@PathVariable Long id) {
        return R.ok(menuService.getDish(RequestContext.getShopId(), id));
    }
}
