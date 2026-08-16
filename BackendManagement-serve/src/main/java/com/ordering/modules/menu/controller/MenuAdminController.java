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
public class MenuAdminController {

    private final MenuService menuService;

    public MenuAdminController(MenuService menuService) {
        this.menuService = menuService;
    }

    /** 后台：菜品列表（需 JWT） GET /api/admin/menu/dishes?categoryId= */
    @GetMapping("/admin/menu/dishes")
    public R<List<Dish>> list(@RequestParam(required = false) Long categoryId) {
        return R.ok(menuService.listDishes(RequestContext.getShopId(), categoryId));
    }

    @GetMapping("/admin/menu/dishes/{id}")
    public R<Dish> get(@PathVariable Long id) {
        return R.ok(menuService.getDish(RequestContext.getShopId(), id));
    }

    @PostMapping("/admin/menu/dishes")
    public R<Long> create(@RequestBody Dish dish) {
        return R.ok(menuService.createDish(RequestContext.getShopId(), dish));
    }

    @PutMapping("/admin/menu/dishes/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Dish dish) {
        menuService.updateDish(RequestContext.getShopId(), id, dish);
        return R.ok();
    }

    @DeleteMapping("/admin/menu/dishes/{id}")
    public R<Void> delete(@PathVariable Long id) {
        menuService.deleteDish(RequestContext.getShopId(), id);
        return R.ok();
    }

    @PostMapping("/admin/menu/categories")
    public R<Long> createCategory(@RequestBody Category category) {
        return R.ok(menuService.createCategory(RequestContext.getShopId(), category));
    }
}
