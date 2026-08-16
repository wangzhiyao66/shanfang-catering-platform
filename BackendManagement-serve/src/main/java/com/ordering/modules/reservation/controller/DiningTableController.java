package com.ordering.modules.reservation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ordering.common.context.RequestContext;
import com.ordering.common.result.BizException;
import com.ordering.common.result.CodeEnum;
import com.ordering.common.result.R;
import com.ordering.modules.reservation.entity.DiningTable;
import com.ordering.modules.reservation.mapper.DiningTableMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DiningTableController {

    private final DiningTableMapper diningTableMapper;

    public DiningTableController(DiningTableMapper diningTableMapper) {
        this.diningTableMapper = diningTableMapper;
    }

    /** 后台桌台列表：GET /api/admin/tables */
    @GetMapping("/admin/tables")
    public R<List<DiningTable>> list() {
        return R.ok(diningTableMapper.selectList(new LambdaQueryWrapper<DiningTable>()
                .eq(DiningTable::getShopId, RequestContext.getShopId())
                .orderByAsc(DiningTable::getTableNo)));
    }

    /** 后台改桌台状态：POST /api/admin/table/{id}/status  { status } */
    @PostMapping("/admin/table/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null) {
            throw new BizException(CodeEnum.PARAM_ERROR.getCode(), "status 为空");
        }
        DiningTable t = diningTableMapper.selectOne(new LambdaQueryWrapper<DiningTable>()
                .eq(DiningTable::getId, id).eq(DiningTable::getShopId, RequestContext.getShopId()));
        if (t == null) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "桌台不存在");
        }
        DiningTable upd = new DiningTable();
        upd.setId(id);
        upd.setStatus(status);
        upd.setVersion(t.getVersion());
        if (diningTableMapper.updateById(upd) == 0) {
            throw new BizException(CodeEnum.BIZ_ERROR.getCode(), "桌台状态已变化，请刷新");
        }
        return R.ok();
    }
}
