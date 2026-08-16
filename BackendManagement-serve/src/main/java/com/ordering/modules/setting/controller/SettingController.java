package com.ordering.modules.setting.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ordering.common.context.RequestContext;
import com.ordering.common.result.R;
import com.ordering.modules.setting.entity.ShopSetting;
import com.ordering.modules.setting.mapper.ShopSettingMapper;
import com.ordering.modules.shop.entity.Shop;
import com.ordering.modules.shop.mapper.ShopMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SettingController {

    private final ShopMapper shopMapper;
    private final ShopSettingMapper settingMapper;

    public SettingController(ShopMapper shopMapper, ShopSettingMapper settingMapper) {
        this.shopMapper = shopMapper;
        this.settingMapper = settingMapper;
    }

    /** 门店信息：GET /api/admin/shop */
    @GetMapping("/admin/shop")
    public R<Shop> shop() {
        return R.ok(shopMapper.selectById(RequestContext.getShopId()));
    }

    /** 更新门店名称/状态：PUT /api/admin/shop */
    @PutMapping("/admin/shop")
    public R<Void> updateShop(@RequestBody Shop shop) {
        Long shopId = RequestContext.getShopId();
        Shop upd = new Shop();
        upd.setId(shopId);
        if (shop.getName() != null) upd.setName(shop.getName());
        if (shop.getStatus() != null) upd.setStatus(shop.getStatus());
        shopMapper.updateById(upd);
        return R.ok();
    }

    /** 门店设置（key-value）：GET /api/admin/setting */
    @GetMapping("/admin/setting")
    public R<Map<String, String>> setting() {
        Long shopId = RequestContext.getShopId();
        List<ShopSetting> list = settingMapper.selectList(
                new LambdaQueryWrapper<ShopSetting>().eq(ShopSetting::getShopId, shopId));
        Map<String, String> map = list.stream()
                .collect(Collectors.toMap(ShopSetting::getSettingKey, ShopSetting::getSettingValue, (a, b) -> b));
        return R.ok(map);
    }

    /** 批量更新门店设置：PUT /api/admin/setting  { key: value, ... } */
    @PutMapping("/admin/setting")
    public R<Void> updateSetting(@RequestBody Map<String, String> body) {
        Long shopId = RequestContext.getShopId();
        for (Map.Entry<String, String> e : body.entrySet()) {
            ShopSetting existing = settingMapper.selectOne(new LambdaQueryWrapper<ShopSetting>()
                    .eq(ShopSetting::getShopId, shopId)
                    .eq(ShopSetting::getSettingKey, e.getKey()));
            if (existing == null) {
                ShopSetting s = new ShopSetting();
                s.setShopId(shopId);
                s.setSettingKey(e.getKey());
                s.setSettingValue(e.getValue());
                settingMapper.insert(s);
            } else {
                existing.setSettingValue(e.getValue());
                settingMapper.updateById(existing);
            }
        }
        return R.ok();
    }
}
