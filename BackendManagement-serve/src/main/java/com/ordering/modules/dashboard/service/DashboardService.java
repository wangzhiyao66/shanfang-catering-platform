package com.ordering.modules.dashboard.service;

import com.ordering.modules.dashboard.vo.DashboardVO;

public interface DashboardService {

    /** 工作台统计：核心指标 + 近7日趋势 + 类型分布 + 热销TOP */
    DashboardVO adminDashboard(Long shopId);
}
