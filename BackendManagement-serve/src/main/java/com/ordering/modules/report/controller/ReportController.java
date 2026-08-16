package com.ordering.modules.report.controller;

import com.ordering.common.context.RequestContext;
import com.ordering.common.result.R;
import com.ordering.modules.report.service.ReportService;
import com.ordering.modules.report.vo.ReportVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** 数据报表：GET /api/admin/report?days=30 */
    @GetMapping("/admin/report")
    public R<ReportVO> report(@RequestParam(required = false) Integer days) {
        return R.ok(reportService.adminReport(RequestContext.getShopId(), days));
    }
}
