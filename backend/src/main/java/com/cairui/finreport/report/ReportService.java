package com.cairui.finreport.report;

import com.cairui.finreport.ledger.AccountBalance;
import com.cairui.finreport.ledger.AccountBalanceRepository;
import com.cairui.finreport.master.Account;
import com.cairui.finreport.master.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReportService {
    private static final Pattern TOKEN = Pattern.compile("[A-Z0-9_]+");

    private final AccountBalanceRepository balances;
    private final AccountRepository accounts;
    private final ReportLineRepository lines;
    private final ReportFormulaRepository formulas;

    public ReportService(
            AccountBalanceRepository balances,
            AccountRepository accounts,
            ReportLineRepository lines,
            ReportFormulaRepository formulas) {
        this.balances = balances;
        this.accounts = accounts;
        this.lines = lines;
        this.formulas = formulas;
    }

    public TrialBalanceDto trialBalance(String period) {
        List<AccountBalance> rows = balances.findByPeriodOrderByAccountCodeAsc(period);
        BigDecimal openDr = BigDecimal.ZERO, openCr = BigDecimal.ZERO;
        BigDecimal perDr = BigDecimal.ZERO, perCr = BigDecimal.ZERO;
        BigDecimal closeDr = BigDecimal.ZERO, closeCr = BigDecimal.ZERO;
        List<TrialBalanceRow> out = new ArrayList<>();
        Map<String, Account> chart = new LinkedHashMap<>();
        accounts.findAll().forEach(a -> chart.put(a.getCode(), a));
        for (AccountBalance b : rows) {
            openDr = openDr.add(nz(b.getOpeningDebit()));
            openCr = openCr.add(nz(b.getOpeningCredit()));
            perDr = perDr.add(nz(b.getPeriodDebit()));
            perCr = perCr.add(nz(b.getPeriodCredit()));
            closeDr = closeDr.add(nz(b.getClosingDebit()));
            closeCr = closeCr.add(nz(b.getClosingCredit()));
            Account acc = chart.get(b.getAccountCode());
            out.add(new TrialBalanceRow(
                    b.getAccountCode(),
                    b.getAccountName() != null ? b.getAccountName() : (acc == null ? "" : acc.getName()),
                    acc == null ? "" : acc.getCategory(),
                    nz(b.getOpeningDebit()),
                    nz(b.getOpeningCredit()),
                    nz(b.getPeriodDebit()),
                    nz(b.getPeriodCredit()),
                    nz(b.getClosingDebit()),
                    nz(b.getClosingCredit())
            ));
        }
        boolean balanced = closeDr.compareTo(closeCr) == 0;
        return new TrialBalanceDto(period, out, openDr, openCr, perDr, perCr, closeDr, closeCr, balanced);
    }

    public FinancialReportDto generate(String reportType, String period) {
        Map<String, BigDecimal> amounts = resolveAmounts(reportType, accountAmounts(period));
        List<ReportLineDto> result = new ArrayList<>();
        for (ReportLine line : lines.findByReportTypeOrderBySortOrderAsc(reportType)) {
            BigDecimal amt = "HEADER".equals(line.getLineKind())
                    ? null
                    : amounts.getOrDefault(line.getLineCode(), BigDecimal.ZERO);
            result.add(new ReportLineDto(
                    line.getLineCode(),
                    line.getLineName(),
                    line.getSection(),
                    line.getLineKind(),
                    line.getIndent(),
                    amt
            ));
        }
        return new FinancialReportDto(reportType, period, result);
    }

    /** Home KPIs without building full report line DTOs. Loads account balances once. */
    public DashboardKpis dashboardKpis(String period) {
        Map<String, BigDecimal> accountAmt = accountAmounts(period);
        BigDecimal netProfit = resolveAmounts("INCOME_STATEMENT", accountAmt)
                .getOrDefault("IS_NP", BigDecimal.ZERO);
        BigDecimal totalAssets = resolveAmounts("BALANCE_SHEET", accountAmt)
                .getOrDefault("BS_ASSET_T", BigDecimal.ZERO);
        return new DashboardKpis(netProfit, totalAssets);
    }

    private Map<String, BigDecimal> resolveAmounts(String reportType, Map<String, BigDecimal> accountAmt) {
        Map<String, BigDecimal> itemAmounts = new LinkedHashMap<>();
        for (ReportFormula f : formulas.findByReportType(reportType)) {
            BigDecimal acc = accountAmt.getOrDefault(f.getAccountCode(), BigDecimal.ZERO);
            BigDecimal add = acc.multiply(f.getMultiplier()).setScale(2, RoundingMode.HALF_UP);
            itemAmounts.merge(f.getLineCode(), add, (a, b) -> a.add(b));
        }
        Map<String, BigDecimal> resolved = new LinkedHashMap<>();
        for (ReportLine line : lines.findByReportTypeOrderBySortOrderAsc(reportType)) {
            if ("ITEM".equals(line.getLineKind())) {
                resolved.put(line.getLineCode(), itemAmounts.getOrDefault(line.getLineCode(), BigDecimal.ZERO));
            } else if ("TOTAL".equals(line.getLineKind())) {
                resolved.put(line.getLineCode(), evalExpr(line.getFormulaExpr(), resolved));
            }
        }
        return resolved;
    }

    private Map<String, BigDecimal> accountAmounts(String period) {
        Map<String, Account> chart = new LinkedHashMap<>();
        accounts.findAll().forEach(a -> chart.put(a.getCode(), a));
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (AccountBalance b : balances.findByPeriodOrderByAccountCodeAsc(period)) {
            Account acc = chart.get(b.getAccountCode());
            String side = acc != null ? acc.getBalanceSide() : inferSide(b.getAccountCode());
            BigDecimal net;
            if ("CREDIT".equals(side)) {
                net = nz(b.getClosingCredit()).subtract(nz(b.getClosingDebit()));
            } else {
                net = nz(b.getClosingDebit()).subtract(nz(b.getClosingCredit()));
            }
            map.put(b.getAccountCode(), net);
        }
        return map;
    }

    private String inferSide(String code) {
        if (code.startsWith("1") || code.startsWith("54") || code.startsWith("56") || code.startsWith("57") || code.startsWith("58")) {
            return "DEBIT";
        }
        return "CREDIT";
    }

    private BigDecimal evalExpr(String expr, Map<String, BigDecimal> values) {
        if (expr == null || expr.isBlank()) {
            return BigDecimal.ZERO;
        }
        Matcher m = TOKEN.matcher(expr);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String code = m.group();
            BigDecimal v = values.getOrDefault(code, BigDecimal.ZERO);
            m.appendReplacement(sb, v.toPlainString());
        }
        m.appendTail(sb);
        return evalSimple(sb.toString());
    }

    /** Tiny + / - evaluator for report subtotals. */
    private BigDecimal evalSimple(String expr) {
        String s = expr.replace(" ", "");
        BigDecimal total = BigDecimal.ZERO;
        int i = 0;
        int sign = 1;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '+') {
                sign = 1;
                i++;
                continue;
            }
            if (c == '-') {
                sign = -1;
                i++;
                continue;
            }
            int j = i;
            while (j < s.length() && (Character.isDigit(s.charAt(j)) || s.charAt(j) == '.')) {
                j++;
            }
            if (j == i) {
                break;
            }
            BigDecimal n = new BigDecimal(s.substring(i, j));
            total = total.add(n.multiply(BigDecimal.valueOf(sign)));
            i = j;
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    public record TrialBalanceRow(
            String accountCode,
            String accountName,
            String category,
            BigDecimal openingDebit,
            BigDecimal openingCredit,
            BigDecimal periodDebit,
            BigDecimal periodCredit,
            BigDecimal closingDebit,
            BigDecimal closingCredit
    ) {
    }

    public record TrialBalanceDto(
            String period,
            List<TrialBalanceRow> rows,
            BigDecimal openingDebit,
            BigDecimal openingCredit,
            BigDecimal periodDebit,
            BigDecimal periodCredit,
            BigDecimal closingDebit,
            BigDecimal closingCredit,
            boolean balanced
    ) {
    }

    public record ReportLineDto(
            String lineCode,
            String lineName,
            String section,
            String lineKind,
            int indent,
            BigDecimal amount
    ) {
    }

    public record FinancialReportDto(String reportType, String period, List<ReportLineDto> lines) {
    }

    public record DashboardKpis(BigDecimal netProfit, BigDecimal totalAssets) {
    }
}
