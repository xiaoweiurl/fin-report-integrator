package com.cairui.finreport.report;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reports;

    public ReportController(ReportService reports) {
        this.reports = reports;
    }

    @GetMapping("/trial-balance")
    public ReportService.TrialBalanceDto trialBalance(@RequestParam String period) {
        return reports.trialBalance(period);
    }

    @GetMapping("/balance-sheet")
    public ReportService.FinancialReportDto balanceSheet(@RequestParam String period) {
        return reports.generate("BALANCE_SHEET", period);
    }

    @GetMapping("/income-statement")
    public ReportService.FinancialReportDto income(@RequestParam String period) {
        return reports.generate("INCOME_STATEMENT", period);
    }
}
