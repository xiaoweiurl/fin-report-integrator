package com.cairui.finreport.seed;

import com.cairui.finreport.mapping.ColumnMapping;
import com.cairui.finreport.mapping.ColumnMappingRepository;
import com.cairui.finreport.master.Account;
import com.cairui.finreport.master.AccountRepository;
import com.cairui.finreport.master.Partner;
import com.cairui.finreport.master.PartnerRepository;
import com.cairui.finreport.report.ReportFormula;
import com.cairui.finreport.report.ReportFormulaRepository;
import com.cairui.finreport.report.ReportLine;
import com.cairui.finreport.report.ReportLineRepository;
import com.cairui.finreport.settings.SettingsService;
import com.cairui.finreport.user.AppUser;
import com.cairui.finreport.user.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final AccountRepository accounts;
    private final PartnerRepository partners;
    private final ColumnMappingRepository mappings;
    private final ReportLineRepository lines;
    private final ReportFormulaRepository formulas;
    private final SettingsService settings;

    public DataSeeder(
            AppUserRepository users,
            PasswordEncoder encoder,
            AccountRepository accounts,
            PartnerRepository partners,
            ColumnMappingRepository mappings,
            ReportLineRepository lines,
            ReportFormulaRepository formulas,
            SettingsService settings) {
        this.users = users;
        this.encoder = encoder;
        this.accounts = accounts;
        this.partners = partners;
        this.mappings = mappings;
        this.lines = lines;
        this.formulas = formulas;
        this.settings = settings;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.count() == 0) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPasswordHash(encoder.encode("admin123"));
            admin.setDisplayName("系统管理员");
            admin.setRole("ADMIN");
            users.insert(admin);
        }
        if (accounts.count() == 0) {
            seedAccounts();
        }
        if (partners.count() == 0) {
            seedPartners();
        }
        if (mappings.count() == 0) {
            seedMappings();
        }
        if (lines.count() == 0) {
            seedReports();
        }
        if (settings.all().isEmpty()) {
            settings.put(SettingsService.CURRENT_PERIOD, "2025-12");
            settings.put(SettingsService.RECON_TOLERANCE_DAYS, "2");
        }
    }

    private void seedAccounts() {
        acc("1001", "库存现金", "ASSET", "DEBIT", null, 1);
        acc("1002", "银行存款", "ASSET", "DEBIT", null, 1);
        acc("1122", "应收账款", "ASSET", "DEBIT", null, 1);
        acc("1403", "原材料", "ASSET", "DEBIT", null, 1);
        acc("1405", "库存商品", "ASSET", "DEBIT", null, 1);
        acc("1601", "固定资产", "ASSET", "DEBIT", null, 1);
        acc("1602", "累计折旧", "ASSET", "CREDIT", "1601", 2);
        acc("2001", "短期借款", "LIABILITY", "CREDIT", null, 1);
        acc("2202", "应付账款", "LIABILITY", "CREDIT", null, 1);
        acc("2211", "应付职工薪酬", "LIABILITY", "CREDIT", null, 1);
        acc("2221", "应交税费", "LIABILITY", "CREDIT", null, 1);
        acc("3001", "实收资本", "EQUITY", "CREDIT", null, 1);
        acc("3101", "盈余公积", "EQUITY", "CREDIT", null, 1);
        acc("3104", "利润分配", "EQUITY", "CREDIT", null, 1);
        acc("5001", "主营业务收入", "REVENUE", "CREDIT", null, 1);
        acc("5051", "其他业务收入", "REVENUE", "CREDIT", null, 1);
        acc("5301", "营业外收入", "REVENUE", "CREDIT", null, 1);
        acc("5401", "主营业务成本", "EXPENSE", "DEBIT", null, 1);
        acc("5403", "税金及附加", "EXPENSE", "DEBIT", null, 1);
        acc("5601", "销售费用", "EXPENSE", "DEBIT", null, 1);
        acc("5602", "管理费用", "EXPENSE", "DEBIT", null, 1);
        acc("5603", "财务费用", "EXPENSE", "DEBIT", null, 1);
        acc("5711", "营业外支出", "EXPENSE", "DEBIT", null, 1);
        acc("5801", "所得税费用", "EXPENSE", "DEBIT", null, 1);
    }

    private void acc(String code, String name, String cat, String side, String parent, int level) {
        Account a = new Account();
        a.setCode(code);
        a.setName(name);
        a.setCategory(cat);
        a.setBalanceSide(side);
        a.setParentCode(parent);
        a.setLevelNo(level);
        a.setEnabled(true);
        accounts.insert(a);
    }

    private void seedPartners() {
        partner("C001", "华东贸易有限公司", "CUSTOMER");
        partner("C002", "星河科技股份公司", "CUSTOMER");
        partner("S001", "江南原料厂", "SUPPLIER");
        partner("S002", "阳光物流有限公司", "SUPPLIER");
        partner("B001", "中国工商银行城东支行", "OTHER");
    }

    private void partner(String code, String name, String type) {
        Partner p = new Partner();
        p.setCode(code);
        p.setName(name);
        p.setPartnerType(type);
        p.setEnabled(true);
        partners.insert(p);
    }

    private void seedMappings() {
        map("ACCOUNT_BALANCE", "科目余额表默认映射", """
                {
                  "accountCode": ["科目编码", "科目代码", "编码"],
                  "accountName": ["科目名称", "名称"],
                  "openingDebit": ["期初借方", "期初借方余额"],
                  "openingCredit": ["期初贷方", "期初贷方余额"],
                  "periodDebit": ["本期借方", "本期借方发生额"],
                  "periodCredit": ["本期贷方", "本期贷方发生额"],
                  "closingDebit": ["期末借方", "期末借方余额"],
                  "closingCredit": ["期末贷方", "期末贷方余额"],
                  "partnerCode": ["往来编码", "辅助核算"],
                  "period": ["期间", "会计期间"]
                }
                """);
        map("BANK_STATEMENT", "银行流水默认映射", """
                {
                  "txnDate": ["交易日期", "日期"],
                  "income": ["收入金额", "收入", "贷方发生额"],
                  "expense": ["支出金额", "支出", "借方发生额"],
                  "amount": ["金额"],
                  "direction": ["借贷方向", "收付"],
                  "counterparty": ["对方名称", "对方户名"],
                  "summary": ["摘要", "备注"],
                  "bankAccount": ["银行账号", "账号"],
                  "refNo": ["流水号", "凭证号"],
                  "balanceAfter": ["余额", "账户余额"]
                }
                """);
        map("CASH_JOURNAL", "企业日记账默认映射", """
                {
                  "txnDate": ["日期", "记账日期"],
                  "income": ["收入金额", "借方"],
                  "expense": ["支出金额", "贷方"],
                  "amount": ["金额"],
                  "direction": ["方向", "收付"],
                  "accountCode": ["科目编码", "科目"],
                  "counterparty": ["对方名称", "往来单位"],
                  "summary": ["摘要"],
                  "voucherNo": ["凭证号"]
                }
                """);
        map("CHART_OF_ACCOUNTS", "会计科目默认映射", """
                {
                  "accountCode": ["科目编码", "编码"],
                  "accountName": ["科目名称", "名称"],
                  "category": ["类别", "科目类别"],
                  "balanceSide": ["余额方向", "方向"],
                  "parentCode": ["上级编码"]
                }
                """);
        map("PARTNER", "往来单位默认映射", """
                {
                  "partnerCode": ["往来编码", "编码"],
                  "partnerName": ["往来名称", "名称"],
                  "partnerType": ["类型", "往来类型"]
                }
                """);
    }

    private void map(String type, String name, String json) {
        ColumnMapping m = new ColumnMapping();
        m.setImportType(type);
        m.setName(name);
        m.setMappingJson(json);
        m.setIsDefault(true);
        mappings.insert(m);
    }

    private void seedReports() {
        // 资产负债表
        header("BALANCE_SHEET", "BS_H_A", "资产", "ASSET", 10, 0);
        header("BALANCE_SHEET", "BS_H_CA", "流动资产", "ASSET", 20, 0);
        item("BALANCE_SHEET", "BS_CASH", "货币资金", "ASSET", 30, 1);
        item("BALANCE_SHEET", "BS_AR", "应收账款", "ASSET", 40, 1);
        item("BALANCE_SHEET", "BS_INV", "存货", "ASSET", 50, 1);
        total("BALANCE_SHEET", "BS_CA_T", "流动资产合计", "ASSET", "BS_CASH+BS_AR+BS_INV", 60, 0);
        header("BALANCE_SHEET", "BS_H_NCA", "非流动资产", "ASSET", 70, 0);
        item("BALANCE_SHEET", "BS_FA_COST", "固定资产原价", "ASSET", 80, 1);
        item("BALANCE_SHEET", "BS_ACC_DEP", "减：累计折旧", "ASSET", 90, 1);
        total("BALANCE_SHEET", "BS_FA_NET", "固定资产净值", "ASSET", "BS_FA_COST-BS_ACC_DEP", 100, 1);
        total("BALANCE_SHEET", "BS_NCA_T", "非流动资产合计", "ASSET", "BS_FA_NET", 110, 0);
        total("BALANCE_SHEET", "BS_ASSET_T", "资产总计", "ASSET", "BS_CA_T+BS_NCA_T", 120, 0);

        header("BALANCE_SHEET", "BS_H_L", "负债", "LIABILITY", 200, 0);
        item("BALANCE_SHEET", "BS_ST_LOAN", "短期借款", "LIABILITY", 210, 1);
        item("BALANCE_SHEET", "BS_AP", "应付账款", "LIABILITY", 220, 1);
        item("BALANCE_SHEET", "BS_PAYROLL", "应付职工薪酬", "LIABILITY", 230, 1);
        item("BALANCE_SHEET", "BS_TAX", "应交税费", "LIABILITY", 240, 1);
        total("BALANCE_SHEET", "BS_L_T", "负债合计", "LIABILITY", "BS_ST_LOAN+BS_AP+BS_PAYROLL+BS_TAX", 250, 0);

        header("BALANCE_SHEET", "BS_H_E", "所有者权益", "EQUITY", 300, 0);
        item("BALANCE_SHEET", "BS_CAP", "实收资本", "EQUITY", 310, 1);
        item("BALANCE_SHEET", "BS_RES", "盈余公积", "EQUITY", 320, 1);
        item("BALANCE_SHEET", "BS_RE", "未分配利润", "EQUITY", 330, 1);
        item("BALANCE_SHEET", "BS_CY", "本年利润", "EQUITY", 340, 1);
        total("BALANCE_SHEET", "BS_E_T", "所有者权益合计", "EQUITY", "BS_CAP+BS_RES+BS_RE+BS_CY", 350, 0);
        total("BALANCE_SHEET", "BS_LE_T", "负债和所有者权益总计", "EQUITY", "BS_L_T+BS_E_T", 360, 0);

        f("BALANCE_SHEET", "BS_CASH", "1001", 1);
        f("BALANCE_SHEET", "BS_CASH", "1002", 1);
        f("BALANCE_SHEET", "BS_AR", "1122", 1);
        f("BALANCE_SHEET", "BS_INV", "1403", 1);
        f("BALANCE_SHEET", "BS_INV", "1405", 1);
        f("BALANCE_SHEET", "BS_FA_COST", "1601", 1);
        f("BALANCE_SHEET", "BS_ACC_DEP", "1602", 1);
        f("BALANCE_SHEET", "BS_ST_LOAN", "2001", 1);
        f("BALANCE_SHEET", "BS_AP", "2202", 1);
        f("BALANCE_SHEET", "BS_PAYROLL", "2211", 1);
        f("BALANCE_SHEET", "BS_TAX", "2221", 1);
        f("BALANCE_SHEET", "BS_CAP", "3001", 1);
        f("BALANCE_SHEET", "BS_RES", "3101", 1);
        f("BALANCE_SHEET", "BS_RE", "3104", 1);
        f("BALANCE_SHEET", "BS_CY", "5001", 1);
        f("BALANCE_SHEET", "BS_CY", "5051", 1);
        f("BALANCE_SHEET", "BS_CY", "5301", 1);
        f("BALANCE_SHEET", "BS_CY", "5401", -1);
        f("BALANCE_SHEET", "BS_CY", "5403", -1);
        f("BALANCE_SHEET", "BS_CY", "5601", -1);
        f("BALANCE_SHEET", "BS_CY", "5602", -1);
        f("BALANCE_SHEET", "BS_CY", "5603", -1);
        f("BALANCE_SHEET", "BS_CY", "5711", -1);
        f("BALANCE_SHEET", "BS_CY", "5801", -1);

        // 利润表
        item("INCOME_STATEMENT", "IS_REV", "一、营业收入", "PL", 10, 0);
        item("INCOME_STATEMENT", "IS_COGS", "减：营业成本", "PL", 20, 1);
        item("INCOME_STATEMENT", "IS_TAX_SUR", "税金及附加", "PL", 30, 1);
        item("INCOME_STATEMENT", "IS_SELL", "销售费用", "PL", 40, 1);
        item("INCOME_STATEMENT", "IS_ADMIN", "管理费用", "PL", 50, 1);
        item("INCOME_STATEMENT", "IS_FIN", "财务费用", "PL", 60, 1);
        total("INCOME_STATEMENT", "IS_OP", "二、营业利润", "PL", "IS_REV-IS_COGS-IS_TAX_SUR-IS_SELL-IS_ADMIN-IS_FIN", 70, 0);
        item("INCOME_STATEMENT", "IS_NREV", "加：营业外收入", "PL", 80, 1);
        item("INCOME_STATEMENT", "IS_NEXP", "减：营业外支出", "PL", 90, 1);
        total("INCOME_STATEMENT", "IS_EBT", "三、利润总额", "PL", "IS_OP+IS_NREV-IS_NEXP", 100, 0);
        item("INCOME_STATEMENT", "IS_ITAX", "减：所得税费用", "PL", 110, 1);
        total("INCOME_STATEMENT", "IS_NP", "四、净利润", "PL", "IS_EBT-IS_ITAX", 120, 0);

        f("INCOME_STATEMENT", "IS_REV", "5001", 1);
        f("INCOME_STATEMENT", "IS_REV", "5051", 1);
        f("INCOME_STATEMENT", "IS_COGS", "5401", 1);
        f("INCOME_STATEMENT", "IS_TAX_SUR", "5403", 1);
        f("INCOME_STATEMENT", "IS_SELL", "5601", 1);
        f("INCOME_STATEMENT", "IS_ADMIN", "5602", 1);
        f("INCOME_STATEMENT", "IS_FIN", "5603", 1);
        f("INCOME_STATEMENT", "IS_NREV", "5301", 1);
        f("INCOME_STATEMENT", "IS_NEXP", "5711", 1);
        f("INCOME_STATEMENT", "IS_ITAX", "5801", 1);
    }

    private void header(String type, String code, String name, String section, int order, int indent) {
        line(type, code, name, section, "HEADER", null, order, indent);
    }

    private void item(String type, String code, String name, String section, int order, int indent) {
        line(type, code, name, section, "ITEM", null, order, indent);
    }

    private void total(String type, String code, String name, String section, String expr, int order, int indent) {
        line(type, code, name, section, "TOTAL", expr, order, indent);
    }

    private void line(String type, String code, String name, String section, String kind, String expr, int order, int indent) {
        ReportLine l = new ReportLine();
        l.setReportType(type);
        l.setLineCode(code);
        l.setLineName(name);
        l.setSection(section);
        l.setLineKind(kind);
        l.setFormulaExpr(expr);
        l.setSortOrder(order);
        l.setIndent(indent);
        lines.insert(l);
    }

    private void f(String type, String line, String account, int mul) {
        ReportFormula f = new ReportFormula();
        f.setReportType(type);
        f.setLineCode(line);
        f.setAccountCode(account);
        f.setMultiplier(BigDecimal.valueOf(mul));
        formulas.insert(f);
    }
}
