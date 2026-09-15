package com.cairui.finreport.dashboard;

import com.cairui.finreport.importdata.ImportBatch;
import com.cairui.finreport.importdata.ImportBatchRepository;
import com.cairui.finreport.ledger.AccountBalanceRepository;
import com.cairui.finreport.ledger.BankStatementRepository;
import com.cairui.finreport.ledger.CashJournalRepository;
import com.cairui.finreport.recon.ReconMatchRepository;
import com.cairui.finreport.report.ReportService;
import com.cairui.finreport.settings.SettingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final ImportBatchRepository batches;
    private final AccountBalanceRepository balances;
    private final BankStatementRepository statements;
    private final CashJournalRepository journals;
    private final ReconMatchRepository matches;
    private final ReportService reports;
    private final SettingsService settings;

    public DashboardController(
            ImportBatchRepository batches,
            AccountBalanceRepository balances,
            BankStatementRepository statements,
            CashJournalRepository journals,
            ReconMatchRepository matches,
            ReportService reports,
            SettingsService settings) {
        this.batches = batches;
        this.balances = balances;
        this.statements = statements;
        this.journals = journals;
        this.matches = matches;
        this.reports = reports;
        this.settings = settings;
    }

    @GetMapping
    public Map<String, Object> dashboard(@RequestParam(required = false) String period) {
        String p = period == null || period.isBlank()
                ? settings.get(SettingsService.CURRENT_PERIOD, "2025-12")
                : period;
        List<ImportBatch> list = batches.findByPeriodOrderByCreatedAtDesc(p);
        int errorRows = list.stream().mapToInt(b -> b.getErrorRows() == null ? 0 : b.getErrorRows()).sum();
        BigDecimal closingDebit = nz(balances.sumClosingDebitByPeriod(p));
        BigDecimal closingCredit = nz(balances.sumClosingCreditByPeriod(p));
        long balanceCount = balances.countByPeriod(p);
        var kpi = reports.dashboardKpis(p);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("period", p);
        body.put("batches", list);
        body.put("batchCount", list.size());
        body.put("errorRows", errorRows);
        body.put("trialBalanced", closingDebit.compareTo(closingCredit) == 0);
        body.put("closingDebit", closingDebit);
        body.put("closingCredit", closingCredit);
        body.put("balanceCount", balanceCount);
        body.put("statementCount", statements.countByPeriod(p));
        body.put("journalCount", journals.countByPeriod(p));
        body.put("reconMatchCount", matches.countByPeriod(p));
        body.put("netProfit", kpi.netProfit());
        body.put("totalAssets", kpi.totalAssets());
        body.put("settings", settings.all());
        return body;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
