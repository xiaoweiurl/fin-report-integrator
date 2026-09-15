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
        var tb = reports.trialBalance(p);
        int stmt = statements.findByPeriodOrderByTxnDateAscIdAsc(p).size();
        int jour = journals.findByPeriodOrderByTxnDateAscIdAsc(p).size();
        int match = matches.findByPeriodOrderByIdAsc(p).size();
        BigDecimal netProfit = reports.generate("INCOME_STATEMENT", p).lines().stream()
                .filter(l -> "IS_NP".equals(l.lineCode()))
                .map(l -> l.amount() == null ? BigDecimal.ZERO : l.amount())
                .findFirst()
                .orElse(BigDecimal.ZERO);
        BigDecimal totalAssets = reports.generate("BALANCE_SHEET", p).lines().stream()
                .filter(l -> "BS_ASSET_T".equals(l.lineCode()))
                .map(l -> l.amount() == null ? BigDecimal.ZERO : l.amount())
                .findFirst()
                .orElse(BigDecimal.ZERO);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("period", p);
        body.put("batches", list);
        body.put("batchCount", list.size());
        body.put("errorRows", errorRows);
        body.put("trialBalanced", tb.balanced());
        body.put("closingDebit", tb.closingDebit());
        body.put("closingCredit", tb.closingCredit());
        body.put("balanceCount", balances.findByPeriodOrderByAccountCodeAsc(p).size());
        body.put("statementCount", stmt);
        body.put("journalCount", jour);
        body.put("reconMatchCount", match);
        body.put("netProfit", netProfit);
        body.put("totalAssets", totalAssets);
        body.put("settings", settings.all());
        return body;
    }
}
