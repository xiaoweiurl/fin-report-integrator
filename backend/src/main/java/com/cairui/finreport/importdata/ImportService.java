package com.cairui.finreport.importdata;

import com.cairui.finreport.audit.AuditService;
import com.cairui.finreport.common.ApiException;
import com.cairui.finreport.common.FileParseSupport;
import com.cairui.finreport.common.ParseUtil;
import com.cairui.finreport.ledger.AccountBalance;
import com.cairui.finreport.ledger.AccountBalanceRepository;
import com.cairui.finreport.ledger.BankStatementLine;
import com.cairui.finreport.ledger.BankStatementRepository;
import com.cairui.finreport.ledger.CashJournalLine;
import com.cairui.finreport.ledger.CashJournalRepository;
import com.cairui.finreport.mapping.ColumnMapping;
import com.cairui.finreport.mapping.ColumnMappingRepository;
import com.cairui.finreport.master.Account;
import com.cairui.finreport.master.AccountRepository;
import com.cairui.finreport.master.Partner;
import com.cairui.finreport.master.PartnerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ImportService {
    public static final String ACCOUNT_BALANCE = "ACCOUNT_BALANCE";
    public static final String BANK_STATEMENT = "BANK_STATEMENT";
    public static final String CASH_JOURNAL = "CASH_JOURNAL";
    public static final String CHART_OF_ACCOUNTS = "CHART_OF_ACCOUNTS";
    public static final String PARTNER = "PARTNER";

    private final ImportBatchRepository batches;
    private final ImportExceptionRepository exceptions;
    private final ColumnMappingRepository mappings;
    private final AccountBalanceRepository balances;
    private final BankStatementRepository statements;
    private final CashJournalRepository journals;
    private final AccountRepository accounts;
    private final PartnerRepository partners;
    private final AuditService audit;
    private final ObjectMapper mapper;

    public ImportService(
            ImportBatchRepository batches,
            ImportExceptionRepository exceptions,
            ColumnMappingRepository mappings,
            AccountBalanceRepository balances,
            BankStatementRepository statements,
            CashJournalRepository journals,
            AccountRepository accounts,
            PartnerRepository partners,
            AuditService audit,
            ObjectMapper mapper) {
        this.batches = batches;
        this.exceptions = exceptions;
        this.mappings = mappings;
        this.balances = balances;
        this.statements = statements;
        this.journals = journals;
        this.accounts = accounts;
        this.partners = partners;
        this.audit = audit;
        this.mapper = mapper;
    }

    @Transactional
    public ImportBatch importFile(MultipartFile file, String importType, String period, String username) throws IOException {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("请选择文件");
        }
        if (period == null || !period.matches("\\d{4}-\\d{2}")) {
            throw ApiException.badRequest("期间格式应为 YYYY-MM");
        }
        byte[] bytes = file.getBytes();
        String hash = FileParseSupport.sha256(bytes);
        batches.findFirstByFileHashAndImportTypeAndPeriod(hash, importType, period).ifPresent(existing -> {
            throw new ApiException(HttpStatus.CONFLICT, "相同文件已导入过本期间（批次 #" + existing.getId() + "）");
        });

        ImportBatch batch = new ImportBatch();
        batch.setImportType(importType);
        batch.setFileName(file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename());
        batch.setFileHash(hash);
        batch.setPeriod(period);
        batch.setStatus("PENDING");
        batch.setCreatedBy(username);
        batches.save(batch);

        List<Map<String, String>> rows = FileParseSupport.parse(file, bytes);
        batch.setTotalRows(rows.size());
        JsonNode mapping = loadMapping(importType);

        int ok = 0;
        int err = 0;
        List<AccountBalance> balBuf = new ArrayList<>();
        List<BankStatementLine> stmtBuf = new ArrayList<>();
        List<CashJournalLine> jourBuf = new ArrayList<>();

        for (Map<String, String> row : rows) {
            int rowNum = Integer.parseInt(row.getOrDefault("_row", "0"));
            List<String> errors = new ArrayList<>();
            try {
                switch (importType) {
                    case ACCOUNT_BALANCE -> {
                        AccountBalance b = parseBalance(row, mapping, period, batch.getId(), errors);
                        if (errors.isEmpty()) {
                            balBuf.add(b);
                            ok++;
                        }
                    }
                    case BANK_STATEMENT -> {
                        BankStatementLine line = parseStatement(row, mapping, period, batch.getId(), errors);
                        if (errors.isEmpty()) {
                            stmtBuf.add(line);
                            ok++;
                        }
                    }
                    case CASH_JOURNAL -> {
                        CashJournalLine line = parseJournal(row, mapping, period, batch.getId(), errors);
                        if (errors.isEmpty()) {
                            jourBuf.add(line);
                            ok++;
                        }
                    }
                    case CHART_OF_ACCOUNTS -> {
                        upsertAccount(row, mapping, errors);
                        if (errors.isEmpty()) {
                            ok++;
                        }
                    }
                    case PARTNER -> {
                        upsertPartner(row, mapping, errors);
                        if (errors.isEmpty()) {
                            ok++;
                        }
                    }
                    default -> throw ApiException.badRequest("不支持的导入类型: " + importType);
                }
            } catch (Exception ex) {
                errors.add(ex.getMessage() == null ? "解析失败" : ex.getMessage());
            }
            if (!errors.isEmpty()) {
                err++;
                ImportExceptionRow exRow = new ImportExceptionRow();
                exRow.setBatchId(batch.getId());
                exRow.setRowNumber(rowNum);
                try {
                    exRow.setRawJson(mapper.writeValueAsString(row));
                } catch (JsonProcessingException e) {
                    exRow.setRawJson(row.toString());
                }
                exRow.setErrorMessage(String.join("; ", errors));
                exceptions.save(exRow);
            }
        }

        if (importType.equals(ACCOUNT_BALANCE) && !balBuf.isEmpty()) {
            balances.deleteByPeriod(period);
            balances.saveAll(balBuf);
        }
        if (importType.equals(BANK_STATEMENT) && !stmtBuf.isEmpty()) {
            statements.deleteByPeriod(period);
            statements.saveAll(stmtBuf);
        }
        if (importType.equals(CASH_JOURNAL) && !jourBuf.isEmpty()) {
            journals.deleteByPeriod(period);
            journals.saveAll(jourBuf);
        }

        batch.setSuccessRows(ok);
        batch.setErrorRows(err);
        String extra = "";
        if (importType.equals(ACCOUNT_BALANCE) && !balBuf.isEmpty()) {
            BigDecimal d = BigDecimal.ZERO;
            BigDecimal c = BigDecimal.ZERO;
            for (AccountBalance row : balBuf) {
                d = d.add(row.getClosingDebit() == null ? BigDecimal.ZERO : row.getClosingDebit());
                c = c.add(row.getClosingCredit() == null ? BigDecimal.ZERO : row.getClosingCredit());
            }
            if (d.compareTo(c) != 0) {
                extra = " 试算不平衡：期末借方 " + d + " ≠ 贷方 " + c;
            } else {
                extra = " 试算平衡，借方/贷方均为 " + d;
            }
        }
        if (ok == 0) {
            batch.setStatus("FAILED");
            batch.setMessage("全部行校验失败" + extra);
        } else if (err > 0) {
            batch.setStatus("PARTIAL");
            batch.setMessage("部分成功：" + ok + " 行通过，" + err + " 行进入异常队列" + extra);
        } else {
            batch.setStatus("SUCCESS");
            batch.setMessage("导入成功" + extra);
        }
        batches.save(batch);
        audit.log("IMPORT", importType, String.valueOf(batch.getId()),
                batch.getFileName() + " " + period + " " + batch.getStatus(), username);
        return batch;
    }

    public byte[] template(String importType) throws IOException {
        JsonNode mapping = loadMapping(importType);
        List<String> headers = new ArrayList<>();
        Iterator<String> fields = mapping.fieldNames();
        while (fields.hasNext()) {
            String field = fields.next();
            JsonNode n = mapping.get(field);
            if (n.isArray() && n.size() > 0) {
                headers.add(n.get(0).asText());
            } else if (n.isTextual()) {
                headers.add(n.asText());
            } else {
                headers.add(field);
            }
        }
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("模板");
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                header.createCell(i).setCellValue(headers.get(i));
                sheet.setColumnWidth(i, 18 * 256);
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    public String templateFilename(String importType) {
        return switch (importType) {
            case ACCOUNT_BALANCE -> "科目余额表模板.xlsx";
            case BANK_STATEMENT -> "银行流水模板.xlsx";
            case CASH_JOURNAL -> "企业日记账模板.xlsx";
            case CHART_OF_ACCOUNTS -> "会计科目模板.xlsx";
            case PARTNER -> "往来单位模板.xlsx";
            default -> "导入模板.xlsx";
        };
    }

    private JsonNode loadMapping(String importType) {
        ColumnMapping m = mappings.findFirstByImportTypeAndIsDefaultTrue(importType)
                .orElseThrow(() -> ApiException.notFound("未配置列映射: " + importType));
        try {
            return mapper.readTree(m.getMappingJson());
        } catch (JsonProcessingException e) {
            throw ApiException.badRequest("列映射 JSON 无效");
        }
    }

    private String cell(Map<String, String> row, JsonNode mapping, String field) {
        JsonNode n = mapping.get(field);
        if (n == null) {
            return firstHeader(row, field);
        }
        if (n.isTextual()) {
            return firstHeader(row, n.asText());
        }
        if (n.isArray()) {
            for (JsonNode alias : n) {
                String v = firstHeader(row, alias.asText());
                if (v != null && !v.isBlank()) {
                    return v;
                }
            }
        }
        return "";
    }

    private String firstHeader(Map<String, String> row, String header) {
        String want = ParseUtil.headerKey(header).toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> e : row.entrySet()) {
            if ("_row".equals(e.getKey())) {
                continue;
            }
            if (ParseUtil.headerKey(e.getKey()).toLowerCase(Locale.ROOT).equals(want)) {
                return e.getValue();
            }
        }
        return "";
    }

    private AccountBalance parseBalance(Map<String, String> row, JsonNode mapping, String period, Long batchId, List<String> errors) {
        String code = cell(row, mapping, "accountCode");
        if (code == null || code.isBlank()) {
            errors.add("科目编码不能为空");
        }
        String filePeriod = cell(row, mapping, "period");
        if (filePeriod != null && !filePeriod.isBlank()) {
            String p = filePeriod.trim().replace("/", "-");
            if (p.matches("\\d{6}")) {
                p = p.substring(0, 4) + "-" + p.substring(4);
            }
            if (!p.startsWith(period)) {
                errors.add("期间与批次不一致: " + filePeriod);
            }
        }
        AccountBalance b = new AccountBalance();
        b.setBatchId(batchId);
        b.setPeriod(period);
        b.setAccountCode(code == null ? "" : code.trim());
        b.setAccountName(ParseUtil.blankToNull(cell(row, mapping, "accountName")));
        b.setOpeningDebit(ParseUtil.amountOrInvalid(cell(row, mapping, "openingDebit"), errors, "期初借方"));
        b.setOpeningCredit(ParseUtil.amountOrInvalid(cell(row, mapping, "openingCredit"), errors, "期初贷方"));
        b.setPeriodDebit(ParseUtil.amountOrInvalid(cell(row, mapping, "periodDebit"), errors, "本期借方"));
        b.setPeriodCredit(ParseUtil.amountOrInvalid(cell(row, mapping, "periodCredit"), errors, "本期贷方"));
        b.setClosingDebit(ParseUtil.amountOrInvalid(cell(row, mapping, "closingDebit"), errors, "期末借方"));
        b.setClosingCredit(ParseUtil.amountOrInvalid(cell(row, mapping, "closingCredit"), errors, "期末贷方"));
        b.setPartnerCode(ParseUtil.blankToNull(cell(row, mapping, "partnerCode")));
        boolean allZero = b.getOpeningDebit().signum() == 0 && b.getOpeningCredit().signum() == 0
                && b.getPeriodDebit().signum() == 0 && b.getPeriodCredit().signum() == 0
                && b.getClosingDebit().signum() == 0 && b.getClosingCredit().signum() == 0;
        if (allZero) {
            errors.add("金额全部为空");
        }
        return b;
    }

    private BankStatementLine parseStatement(Map<String, String> row, JsonNode mapping, String period, Long batchId, List<String> errors) {
        LocalDate date = ParseUtil.date(cell(row, mapping, "txnDate"));
        if (date == null) {
            errors.add("交易日期无效");
        } else if (!String.format("%04d-%02d", date.getYear(), date.getMonthValue()).equals(period)) {
            errors.add("交易日期不在期间 " + period);
        }
        BigDecimal income = ParseUtil.amountOrInvalid(cell(row, mapping, "income"), errors, "收入");
        BigDecimal expense = ParseUtil.amountOrInvalid(cell(row, mapping, "expense"), errors, "支出");
        String dirRaw = cell(row, mapping, "direction");
        BigDecimal amount;
        String direction;
        if (income.signum() > 0 && expense.signum() == 0) {
            amount = income;
            direction = "IN";
        } else if (expense.signum() > 0 && income.signum() == 0) {
            amount = expense;
            direction = "OUT";
        } else {
            amount = ParseUtil.amountOrInvalid(cell(row, mapping, "amount"), errors, "金额");
            direction = resolveDirection(dirRaw, amount);
            amount = amount.abs();
        }
        if (amount.signum() == 0) {
            errors.add("金额不能为 0");
        }
        BankStatementLine line = new BankStatementLine();
        line.setBatchId(batchId);
        line.setPeriod(period);
        line.setTxnDate(date == null ? LocalDate.now() : date);
        line.setAmount(amount);
        line.setDirection(direction);
        line.setCounterparty(ParseUtil.blankToNull(cell(row, mapping, "counterparty")));
        line.setSummary(ParseUtil.blankToNull(cell(row, mapping, "summary")));
        line.setBankAccount(ParseUtil.blankToNull(cell(row, mapping, "bankAccount")));
        line.setRefNo(ParseUtil.blankToNull(cell(row, mapping, "refNo")));
        String bal = cell(row, mapping, "balanceAfter");
        if (bal != null && !bal.isBlank()) {
            line.setBalanceAfter(ParseUtil.amountOrInvalid(bal, errors, "余额"));
        }
        line.setMatched(false);
        return line;
    }

    private CashJournalLine parseJournal(Map<String, String> row, JsonNode mapping, String period, Long batchId, List<String> errors) {
        LocalDate date = ParseUtil.date(cell(row, mapping, "txnDate"));
        if (date == null) {
            errors.add("日期无效");
        } else if (!String.format("%04d-%02d", date.getYear(), date.getMonthValue()).equals(period)) {
            errors.add("日期不在期间 " + period);
        }
        BigDecimal income = ParseUtil.amountOrInvalid(cell(row, mapping, "income"), errors, "收入");
        BigDecimal expense = ParseUtil.amountOrInvalid(cell(row, mapping, "expense"), errors, "支出");
        BigDecimal amount;
        String direction;
        if (income.signum() > 0 && expense.signum() == 0) {
            amount = income;
            direction = "IN";
        } else if (expense.signum() > 0 && income.signum() == 0) {
            amount = expense;
            direction = "OUT";
        } else {
            amount = ParseUtil.amountOrInvalid(cell(row, mapping, "amount"), errors, "金额");
            direction = resolveDirection(cell(row, mapping, "direction"), amount);
            amount = amount.abs();
        }
        if (amount.signum() == 0) {
            errors.add("金额不能为 0");
        }
        CashJournalLine line = new CashJournalLine();
        line.setBatchId(batchId);
        line.setPeriod(period);
        line.setTxnDate(date == null ? LocalDate.now() : date);
        line.setAmount(amount);
        line.setDirection(direction);
        line.setAccountCode(ParseUtil.blankToNull(cell(row, mapping, "accountCode")));
        line.setCounterparty(ParseUtil.blankToNull(cell(row, mapping, "counterparty")));
        line.setSummary(ParseUtil.blankToNull(cell(row, mapping, "summary")));
        line.setVoucherNo(ParseUtil.blankToNull(cell(row, mapping, "voucherNo")));
        line.setMatched(false);
        return line;
    }

    private String resolveDirection(String raw, BigDecimal amount) {
        if (raw != null) {
            String d = raw.trim();
            if (d.contains("收") || d.contains("借") || "IN".equalsIgnoreCase(d) || "DR".equalsIgnoreCase(d)) {
                return "IN";
            }
            if (d.contains("付") || d.contains("贷") || "OUT".equalsIgnoreCase(d) || "CR".equalsIgnoreCase(d)) {
                return "OUT";
            }
        }
        return amount.signum() < 0 ? "OUT" : "IN";
    }

    private void upsertAccount(Map<String, String> row, JsonNode mapping, List<String> errors) {
        String code = cell(row, mapping, "accountCode");
        String name = cell(row, mapping, "accountName");
        if (code == null || code.isBlank()) {
            errors.add("科目编码不能为空");
            return;
        }
        if (name == null || name.isBlank()) {
            errors.add("科目名称不能为空");
            return;
        }
        Account a = accounts.findByCode(code.trim()).orElseGet(Account::new);
        a.setCode(code.trim());
        a.setName(name.trim());
        String cat = cell(row, mapping, "category");
        a.setCategory(cat == null || cat.isBlank() ? inferCategory(code) : cat.trim());
        String side = cell(row, mapping, "balanceSide");
        a.setBalanceSide(side == null || side.isBlank() ? inferSide(a.getCategory()) : side.trim());
        a.setParentCode(ParseUtil.blankToNull(cell(row, mapping, "parentCode")));
        a.setLevelNo(code.trim().length() <= 4 ? 1 : 2);
        a.setEnabled(true);
        accounts.save(a);
    }

    private void upsertPartner(Map<String, String> row, JsonNode mapping, List<String> errors) {
        String code = cell(row, mapping, "partnerCode");
        String name = cell(row, mapping, "partnerName");
        if (code == null || code.isBlank()) {
            errors.add("往来编码不能为空");
            return;
        }
        if (name == null || name.isBlank()) {
            errors.add("往来名称不能为空");
            return;
        }
        Partner p = partners.findByCode(code.trim()).orElseGet(Partner::new);
        p.setCode(code.trim());
        p.setName(name.trim());
        String t = cell(row, mapping, "partnerType");
        p.setPartnerType(t == null || t.isBlank() ? "OTHER" : t.trim());
        p.setEnabled(true);
        partners.save(p);
    }

    private String inferCategory(String code) {
        if (code.startsWith("1")) {
            return "ASSET";
        }
        if (code.startsWith("2")) {
            return "LIABILITY";
        }
        if (code.startsWith("3")) {
            return "EQUITY";
        }
        if (code.startsWith("5") && (code.startsWith("500") || code.startsWith("505") || code.startsWith("530"))) {
            return "REVENUE";
        }
        return "EXPENSE";
    }

    private String inferSide(String category) {
        return switch (category) {
            case "ASSET", "EXPENSE" -> "DEBIT";
            default -> "CREDIT";
        };
    }
}
