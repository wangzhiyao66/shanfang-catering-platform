package com.ordering.modules.dashboard.controller;

import com.ordering.common.context.RequestContext;
import com.ordering.common.result.R;
import com.ordering.modules.dashboard.service.DashboardService;
import com.ordering.modules.dashboard.vo.DashboardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /** 工作台统计：GET /api/admin/dashboard */
    @GetMapping("/admin/dashboard")
    public R<DashboardVO> dashboard() {
        return R.ok(dashboardService.adminDashboard(RequestContext.getShopId()));
    }
}
