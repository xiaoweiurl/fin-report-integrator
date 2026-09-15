package com.cairui.finreport.recon;

import com.cairui.finreport.audit.AuditService;
import com.cairui.finreport.ledger.AccountBalance;
import com.cairui.finreport.ledger.AccountBalanceRepository;
import com.cairui.finreport.ledger.BankStatementLine;
import com.cairui.finreport.ledger.BankStatementRepository;
import com.cairui.finreport.ledger.CashJournalLine;
import com.cairui.finreport.ledger.CashJournalRepository;
import com.cairui.finreport.settings.SettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ReconService {
    private final BankStatementRepository statements;
    private final CashJournalRepository journals;
    private final ReconMatchRepository matches;
    private final AccountBalanceRepository balances;
    private final SettingsService settings;
    private final AuditService audit;

    public ReconService(
            BankStatementRepository statements,
            CashJournalRepository journals,
            ReconMatchRepository matches,
            AccountBalanceRepository balances,
            SettingsService settings,
            AuditService audit) {
        this.statements = statements;
        this.journals = journals;
        this.matches = matches;
        this.balances = balances;
        this.settings = settings;
        this.audit = audit;
    }

    @Transactional
    public ReconView run(String period, Integer toleranceDays, String username) {
        int tol = toleranceDays != null ? toleranceDays
                : Integer.parseInt(settings.get(SettingsService.RECON_TOLERANCE_DAYS, "2"));
        List<BankStatementLine> stmts = statements.findByPeriodOrderByTxnDateAscIdAsc(period);
        List<CashJournalLine> jours = journals.findByPeriodOrderByTxnDateAscIdAsc(period);
        stmts.forEach(s -> s.setMatched(false));
        jours.forEach(j -> j.setMatched(false));
        matches.deleteByPeriod(period);

        List<ReconMatch> created = new ArrayList<>();
        for (BankStatementLine s : stmts) {
            CashJournalLine best = null;
            long bestDiff = Long.MAX_VALUE;
            for (CashJournalLine j : jours) {
                if (Boolean.TRUE.equals(j.getMatched())) {
                    continue;
                }
                if (!s.getDirection().equals(j.getDirection())) {
                    continue;
                }
                if (s.getAmount().compareTo(j.getAmount()) != 0) {
                    continue;
                }
                long diff = Math.abs(ChronoUnit.DAYS.between(s.getTxnDate(), j.getTxnDate()));
                if (diff <= tol && diff < bestDiff) {
                    best = j;
                    bestDiff = diff;
                }
            }
            if (best != null) {
                s.setMatched(true);
                best.setMatched(true);
                ReconMatch m = new ReconMatch();
                m.setPeriod(period);
                m.setStatementId(s.getId());
                m.setJournalId(best.getId());
                m.setMatchType("AUTO");
                m.setAmount(s.getAmount());
                m.setDateDiffDays((int) bestDiff);
                created.add(m);
            }
        }
        statements.saveAll(stmts);
        journals.saveAll(jours);
        matches.saveAll(created);
        audit.log("RECON_RUN", "RECON", period, "自动匹配 " + created.size() + " 笔，容差 ±" + tol + " 天", username);
        return view(period);
    }

    public ReconView view(String period) {
        List<BankStatementLine> stmts = statements.findByPeriodOrderByTxnDateAscIdAsc(period);
        List<CashJournalLine> jours = journals.findByPeriodOrderByTxnDateAscIdAsc(period);
        List<ReconMatch> ms = matches.findByPeriodOrderByIdAsc(period);
        List<MatchRow> matchRows = new ArrayList<>();
        for (ReconMatch m : ms) {
            BankStatementLine s = stmts.stream().filter(x -> x.getId().equals(m.getStatementId())).findFirst().orElse(null);
            CashJournalLine j = jours.stream().filter(x -> x.getId().equals(m.getJournalId())).findFirst().orElse(null);
            matchRows.add(new MatchRow(m, s, j));
        }
        List<BankStatementLine> unmatchedBank = stmts.stream().filter(s -> !Boolean.TRUE.equals(s.getMatched())).toList();
        List<CashJournalLine> unmatchedBook = jours.stream().filter(j -> !Boolean.TRUE.equals(j.getMatched())).toList();
        AdjustmentDraft adj = adjustment(period, stmts, unmatchedBank, unmatchedBook);
        return new ReconView(period, stmts.size(), jours.size(), ms.size(), matchRows, unmatchedBank, unmatchedBook, adj);
    }

    private AdjustmentDraft adjustment(
            String period,
            List<BankStatementLine> stmts,
            List<BankStatementLine> unmatchedBank,
            List<CashJournalLine> unmatchedBook) {
        BigDecimal book = balances.findByPeriodOrderByAccountCodeAsc(period).stream()
                .filter(b -> "1002".equals(b.getAccountCode()))
                .map(b -> nz(b.getClosingDebit()).subtract(nz(b.getClosingCredit())))
                .findFirst()
                .orElse(BigDecimal.ZERO);
        BigDecimal bankInUnmatched = unmatchedBank.stream().filter(s -> "IN".equals(s.getDirection()))
                .map(BankStatementLine::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal bankOutUnmatched = unmatchedBank.stream().filter(s -> "OUT".equals(s.getDirection()))
                .map(BankStatementLine::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal bookInUnmatched = unmatchedBook.stream().filter(s -> "IN".equals(s.getDirection()))
                .map(CashJournalLine::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal bookOutUnmatched = unmatchedBook.stream().filter(s -> "OUT".equals(s.getDirection()))
                .map(CashJournalLine::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal adjustedBook = book.add(bankInUnmatched).subtract(bankOutUnmatched);
        BigDecimal bankStmtBal = stmts.stream()
                .filter(s -> s.getBalanceAfter() != null)
                .max(Comparator.comparing(BankStatementLine::getTxnDate).thenComparing(BankStatementLine::getId))
                .map(BankStatementLine::getBalanceAfter)
                .orElse(adjustedBook.subtract(bookInUnmatched).add(bookOutUnmatched));
        BigDecimal adjustedBank = bankStmtBal.add(bookInUnmatched).subtract(bookOutUnmatched);
        boolean tied = adjustedBook.compareTo(adjustedBank) == 0;
        return new AdjustmentDraft(
                book, bankInUnmatched, bankOutUnmatched, adjustedBook,
                bankStmtBal, bookInUnmatched, bookOutUnmatched, adjustedBank, tied);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    public record MatchRow(ReconMatch match, BankStatementLine statement, CashJournalLine journal) {
    }

    public record AdjustmentDraft(
            BigDecimal bookBalance,
            BigDecimal bankReceivedBookNot,
            BigDecimal bankPaidBookNot,
            BigDecimal adjustedBook,
            BigDecimal bankStatementBalance,
            BigDecimal bookReceivedBankNot,
            BigDecimal bookPaidBankNot,
            BigDecimal adjustedBank,
            boolean tied
    ) {
    }

    public record ReconView(
            String period,
            int statementCount,
            int journalCount,
            int matchCount,
            List<MatchRow> matches,
            List<BankStatementLine> unmatchedStatements,
            List<CashJournalLine> unmatchedJournals,
            AdjustmentDraft adjustment
    ) {
    }
}
