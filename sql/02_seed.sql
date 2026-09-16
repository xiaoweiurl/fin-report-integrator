-- 基础主数据 / 列映射 / 报表公式
-- psql -U postgres -d finreport -f sql/02_seed.sql
-- 管理员账号由应用首次启动时写入（admin / admin123，BCrypt）

BEGIN;

INSERT INTO accounts (code, name, category, balance_side, parent_code, level_no, enabled) VALUES
('1001', '库存现金', 'ASSET', 'DEBIT', NULL, 1, TRUE),
('1002', '银行存款', 'ASSET', 'DEBIT', NULL, 1, TRUE),
('1122', '应收账款', 'ASSET', 'DEBIT', NULL, 1, TRUE),
('1403', '原材料', 'ASSET', 'DEBIT', NULL, 1, TRUE),
('1405', '库存商品', 'ASSET', 'DEBIT', NULL, 1, TRUE),
('1601', '固定资产', 'ASSET', 'DEBIT', NULL, 1, TRUE),
('1602', '累计折旧', 'ASSET', 'CREDIT', '1601', 2, TRUE),
('2001', '短期借款', 'LIABILITY', 'CREDIT', NULL, 1, TRUE),
('2202', '应付账款', 'LIABILITY', 'CREDIT', NULL, 1, TRUE),
('2211', '应付职工薪酬', 'LIABILITY', 'CREDIT', NULL, 1, TRUE),
('2221', '应交税费', 'LIABILITY', 'CREDIT', NULL, 1, TRUE),
('3001', '实收资本', 'EQUITY', 'CREDIT', NULL, 1, TRUE),
('3101', '盈余公积', 'EQUITY', 'CREDIT', NULL, 1, TRUE),
('3104', '利润分配', 'EQUITY', 'CREDIT', NULL, 1, TRUE),
('5001', '主营业务收入', 'REVENUE', 'CREDIT', NULL, 1, TRUE),
('5051', '其他业务收入', 'REVENUE', 'CREDIT', NULL, 1, TRUE),
('5301', '营业外收入', 'REVENUE', 'CREDIT', NULL, 1, TRUE),
('5401', '主营业务成本', 'EXPENSE', 'DEBIT', NULL, 1, TRUE),
('5403', '税金及附加', 'EXPENSE', 'DEBIT', NULL, 1, TRUE),
('5601', '销售费用', 'EXPENSE', 'DEBIT', NULL, 1, TRUE),
('5602', '管理费用', 'EXPENSE', 'DEBIT', NULL, 1, TRUE),
('5603', '财务费用', 'EXPENSE', 'DEBIT', NULL, 1, TRUE),
('5711', '营业外支出', 'EXPENSE', 'DEBIT', NULL, 1, TRUE),
('5801', '所得税费用', 'EXPENSE', 'DEBIT', NULL, 1, TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO partners (code, name, partner_type, enabled) VALUES
('C001', '华东贸易有限公司', 'CUSTOMER', TRUE),
('C002', '星河科技股份公司', 'CUSTOMER', TRUE),
('S001', '江南原料厂', 'SUPPLIER', TRUE),
('S002', '阳光物流有限公司', 'SUPPLIER', TRUE),
('B001', '中国工商银行城东支行', 'OTHER', TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO app_settings (setting_key, setting_value) VALUES
('current_period', '2025-12'),
('recon_tolerance_days', '2')
ON CONFLICT (setting_key) DO NOTHING;

INSERT INTO column_mappings (import_type, name, mapping_json, is_default) VALUES
('ACCOUNT_BALANCE', '科目余额表默认映射', $json${
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
}$json$, TRUE),
('BANK_STATEMENT', '银行流水默认映射', $json${
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
}$json$, TRUE),
('CASH_JOURNAL', '企业日记账默认映射', $json${
  "txnDate": ["日期", "记账日期"],
  "income": ["收入金额", "借方"],
  "expense": ["支出金额", "贷方"],
  "amount": ["金额"],
  "direction": ["方向", "收付"],
  "accountCode": ["科目编码", "科目"],
  "counterparty": ["对方名称", "往来单位"],
  "summary": ["摘要"],
  "voucherNo": ["凭证号"]
}$json$, TRUE),
('CHART_OF_ACCOUNTS', '会计科目默认映射', $json${
  "accountCode": ["科目编码", "编码"],
  "accountName": ["科目名称", "名称"],
  "category": ["类别", "科目类别"],
  "balanceSide": ["余额方向", "方向"],
  "parentCode": ["上级编码"]
}$json$, TRUE),
('PARTNER', '往来单位默认映射', $json${
  "partnerCode": ["往来编码", "编码"],
  "partnerName": ["往来名称", "名称"],
  "partnerType": ["类型", "往来类型"]
}$json$, TRUE)
ON CONFLICT (import_type, name) DO NOTHING;

INSERT INTO report_lines (report_type, line_code, line_name, section, line_kind, formula_expr, sort_order, indent) VALUES
('BALANCE_SHEET', 'BS_H_A', '资产', 'ASSET', 'HEADER', NULL, 10, 0),
('BALANCE_SHEET', 'BS_H_CA', '流动资产', 'ASSET', 'HEADER', NULL, 20, 0),
('BALANCE_SHEET', 'BS_CASH', '货币资金', 'ASSET', 'ITEM', NULL, 30, 1),
('BALANCE_SHEET', 'BS_AR', '应收账款', 'ASSET', 'ITEM', NULL, 40, 1),
('BALANCE_SHEET', 'BS_INV', '存货', 'ASSET', 'ITEM', NULL, 50, 1),
('BALANCE_SHEET', 'BS_CA_T', '流动资产合计', 'ASSET', 'TOTAL', 'BS_CASH+BS_AR+BS_INV', 60, 0),
('BALANCE_SHEET', 'BS_H_NCA', '非流动资产', 'ASSET', 'HEADER', NULL, 70, 0),
('BALANCE_SHEET', 'BS_FA_COST', '固定资产原价', 'ASSET', 'ITEM', NULL, 80, 1),
('BALANCE_SHEET', 'BS_ACC_DEP', '减：累计折旧', 'ASSET', 'ITEM', NULL, 90, 1),
('BALANCE_SHEET', 'BS_FA_NET', '固定资产净值', 'ASSET', 'TOTAL', 'BS_FA_COST-BS_ACC_DEP', 100, 1),
('BALANCE_SHEET', 'BS_NCA_T', '非流动资产合计', 'ASSET', 'TOTAL', 'BS_FA_NET', 110, 0),
('BALANCE_SHEET', 'BS_ASSET_T', '资产总计', 'ASSET', 'TOTAL', 'BS_CA_T+BS_NCA_T', 120, 0),
('BALANCE_SHEET', 'BS_H_L', '负债', 'LIABILITY', 'HEADER', NULL, 200, 0),
('BALANCE_SHEET', 'BS_ST_LOAN', '短期借款', 'LIABILITY', 'ITEM', NULL, 210, 1),
('BALANCE_SHEET', 'BS_AP', '应付账款', 'LIABILITY', 'ITEM', NULL, 220, 1),
('BALANCE_SHEET', 'BS_PAYROLL', '应付职工薪酬', 'LIABILITY', 'ITEM', NULL, 230, 1),
('BALANCE_SHEET', 'BS_TAX', '应交税费', 'LIABILITY', 'ITEM', NULL, 240, 1),
('BALANCE_SHEET', 'BS_L_T', '负债合计', 'LIABILITY', 'TOTAL', 'BS_ST_LOAN+BS_AP+BS_PAYROLL+BS_TAX', 250, 0),
('BALANCE_SHEET', 'BS_H_E', '所有者权益', 'EQUITY', 'HEADER', NULL, 300, 0),
('BALANCE_SHEET', 'BS_CAP', '实收资本', 'EQUITY', 'ITEM', NULL, 310, 1),
('BALANCE_SHEET', 'BS_RES', '盈余公积', 'EQUITY', 'ITEM', NULL, 320, 1),
('BALANCE_SHEET', 'BS_RE', '未分配利润', 'EQUITY', 'ITEM', NULL, 330, 1),
('BALANCE_SHEET', 'BS_CY', '本年利润', 'EQUITY', 'ITEM', NULL, 340, 1),
('BALANCE_SHEET', 'BS_E_T', '所有者权益合计', 'EQUITY', 'TOTAL', 'BS_CAP+BS_RES+BS_RE+BS_CY', 350, 0),
('BALANCE_SHEET', 'BS_LE_T', '负债和所有者权益总计', 'EQUITY', 'TOTAL', 'BS_L_T+BS_E_T', 360, 0),
('INCOME_STATEMENT', 'IS_REV', '一、营业收入', 'PL', 'ITEM', NULL, 10, 0),
('INCOME_STATEMENT', 'IS_COGS', '减：营业成本', 'PL', 'ITEM', NULL, 20, 1),
('INCOME_STATEMENT', 'IS_TAX_SUR', '税金及附加', 'PL', 'ITEM', NULL, 30, 1),
('INCOME_STATEMENT', 'IS_SELL', '销售费用', 'PL', 'ITEM', NULL, 40, 1),
('INCOME_STATEMENT', 'IS_ADMIN', '管理费用', 'PL', 'ITEM', NULL, 50, 1),
('INCOME_STATEMENT', 'IS_FIN', '财务费用', 'PL', 'ITEM', NULL, 60, 1),
('INCOME_STATEMENT', 'IS_OP', '二、营业利润', 'PL', 'TOTAL', 'IS_REV-IS_COGS-IS_TAX_SUR-IS_SELL-IS_ADMIN-IS_FIN', 70, 0),
('INCOME_STATEMENT', 'IS_NREV', '加：营业外收入', 'PL', 'ITEM', NULL, 80, 1),
('INCOME_STATEMENT', 'IS_NEXP', '减：营业外支出', 'PL', 'ITEM', NULL, 90, 1),
('INCOME_STATEMENT', 'IS_EBT', '三、利润总额', 'PL', 'TOTAL', 'IS_OP+IS_NREV-IS_NEXP', 100, 0),
('INCOME_STATEMENT', 'IS_ITAX', '减：所得税费用', 'PL', 'ITEM', NULL, 110, 1),
('INCOME_STATEMENT', 'IS_NP', '四、净利润', 'PL', 'TOTAL', 'IS_EBT-IS_ITAX', 120, 0)
ON CONFLICT (report_type, line_code) DO NOTHING;

INSERT INTO report_formulas (report_type, line_code, account_code, multiplier) VALUES
('BALANCE_SHEET', 'BS_CASH', '1001', 1),
('BALANCE_SHEET', 'BS_CASH', '1002', 1),
('BALANCE_SHEET', 'BS_AR', '1122', 1),
('BALANCE_SHEET', 'BS_INV', '1403', 1),
('BALANCE_SHEET', 'BS_INV', '1405', 1),
('BALANCE_SHEET', 'BS_FA_COST', '1601', 1),
('BALANCE_SHEET', 'BS_ACC_DEP', '1602', 1),
('BALANCE_SHEET', 'BS_ST_LOAN', '2001', 1),
('BALANCE_SHEET', 'BS_AP', '2202', 1),
('BALANCE_SHEET', 'BS_PAYROLL', '2211', 1),
('BALANCE_SHEET', 'BS_TAX', '2221', 1),
('BALANCE_SHEET', 'BS_CAP', '3001', 1),
('BALANCE_SHEET', 'BS_RES', '3101', 1),
('BALANCE_SHEET', 'BS_RE', '3104', 1),
('BALANCE_SHEET', 'BS_CY', '5001', 1),
('BALANCE_SHEET', 'BS_CY', '5051', 1),
('BALANCE_SHEET', 'BS_CY', '5301', 1),
('BALANCE_SHEET', 'BS_CY', '5401', -1),
('BALANCE_SHEET', 'BS_CY', '5403', -1),
('BALANCE_SHEET', 'BS_CY', '5601', -1),
('BALANCE_SHEET', 'BS_CY', '5602', -1),
('BALANCE_SHEET', 'BS_CY', '5603', -1),
('BALANCE_SHEET', 'BS_CY', '5711', -1),
('BALANCE_SHEET', 'BS_CY', '5801', -1),
('INCOME_STATEMENT', 'IS_REV', '5001', 1),
('INCOME_STATEMENT', 'IS_REV', '5051', 1),
('INCOME_STATEMENT', 'IS_COGS', '5401', 1),
('INCOME_STATEMENT', 'IS_TAX_SUR', '5403', 1),
('INCOME_STATEMENT', 'IS_SELL', '5601', 1),
('INCOME_STATEMENT', 'IS_ADMIN', '5602', 1),
('INCOME_STATEMENT', 'IS_FIN', '5603', 1),
('INCOME_STATEMENT', 'IS_NREV', '5301', 1),
('INCOME_STATEMENT', 'IS_NEXP', '5711', 1),
('INCOME_STATEMENT', 'IS_ITAX', '5801', 1)
ON CONFLICT (report_type, line_code, account_code) DO NOTHING;

COMMIT;
