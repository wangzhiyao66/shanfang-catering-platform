package com.ordering.modules.report.service;

import com.ordering.modules.report.vo.ReportVO;

public interface ReportService {

    /** 数据报表：近 days 日趋势 + 类型占比 + 热销 + 汇总（days 默认 30，范围 1~365） */
    ReportVO adminReport(Long shopId, Integer days);
}
