-- 财报系统 PostgreSQL 建表脚本
-- 使用前请先创建库：
--   CREATE DATABASE finreport WITH ENCODING 'UTF8' TEMPLATE template0;
-- 然后：
--   psql -U postgres -d finreport -f sql/01_schema.sql
--   psql -U postgres -d finreport -f sql/02_seed.sql

BEGIN;

DROP TABLE IF EXISTS recon_matches CASCADE;
DROP TABLE IF EXISTS import_exceptions CASCADE;
DROP TABLE IF EXISTS account_balances CASCADE;
DROP TABLE IF EXISTS bank_statement_lines CASCADE;
DROP TABLE IF EXISTS cash_journal_lines CASCADE;
DROP TABLE IF EXISTS import_batches CASCADE;
DROP TABLE IF EXISTS report_formulas CASCADE;
DROP TABLE IF EXISTS report_lines CASCADE;
DROP TABLE IF EXISTS column_mappings CASCADE;
DROP TABLE IF EXISTS accounts CASCADE;
DROP TABLE IF EXISTS partners CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS app_settings CASCADE;
DROP TABLE IF EXISTS app_users CASCADE;

CREATE TABLE app_users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(64)  NOT NULL UNIQUE,
    password_hash   VARCHAR(200) NOT NULL,
    display_name    VARCHAR(100) NOT NULL,
    role            VARCHAR(32)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE accounts (
    id            BIGSERIAL PRIMARY KEY,
    code          VARCHAR(32)  NOT NULL UNIQUE,
    name          VARCHAR(200) NOT NULL,
    category      VARCHAR(32)  NOT NULL,
    balance_side  VARCHAR(16)  NOT NULL,
    parent_code   VARCHAR(32),
    level_no      INT          NOT NULL DEFAULT 1,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE partners (
    id            BIGSERIAL PRIMARY KEY,
    code          VARCHAR(32)  NOT NULL UNIQUE,
    name          VARCHAR(200) NOT NULL,
    partner_type  VARCHAR(32)  NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE column_mappings (
    id            BIGSERIAL PRIMARY KEY,
    import_type   VARCHAR(40)  NOT NULL,
    name          VARCHAR(100) NOT NULL,
    mapping_json  TEXT         NOT NULL,
    is_default    BOOLEAN      NOT NULL DEFAULT FALSE,
    UNIQUE (import_type, name)
);

CREATE TABLE import_batches (
    id            BIGSERIAL PRIMARY KEY,
    import_type   VARCHAR(40)  NOT NULL,
    file_name     VARCHAR(255) NOT NULL,
    file_hash     VARCHAR(64)  NOT NULL,
    period        VARCHAR(16)  NOT NULL,
    status        VARCHAR(24)  NOT NULL,
    total_rows    INT          NOT NULL DEFAULT 0,
    success_rows  INT          NOT NULL DEFAULT 0,
    error_rows    INT          NOT NULL DEFAULT 0,
    message       VARCHAR(2000),
    created_by    VARCHAR(64),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_import_batches_period ON import_batches (period);
CREATE INDEX idx_import_batches_hash ON import_batches (file_hash, import_type, period);

CREATE TABLE import_exceptions (
    id             BIGSERIAL PRIMARY KEY,
    batch_id       BIGINT       NOT NULL REFERENCES import_batches (id) ON DELETE CASCADE,
    row_number     INT          NOT NULL,
    raw_json       TEXT,
    error_message  VARCHAR(2000) NOT NULL
);

CREATE INDEX idx_import_exceptions_batch ON import_exceptions (batch_id);

CREATE TABLE account_balances (
    id              BIGSERIAL PRIMARY KEY,
    batch_id        BIGINT        NOT NULL,
    period          VARCHAR(16)   NOT NULL,
    account_code    VARCHAR(32)   NOT NULL,
    account_name    VARCHAR(200),
    opening_debit   NUMERIC(18,2) NOT NULL DEFAULT 0,
    opening_credit  NUMERIC(18,2) NOT NULL DEFAULT 0,
    period_debit    NUMERIC(18,2) NOT NULL DEFAULT 0,
    period_credit   NUMERIC(18,2) NOT NULL DEFAULT 0,
    closing_debit   NUMERIC(18,2) NOT NULL DEFAULT 0,
    closing_credit  NUMERIC(18,2) NOT NULL DEFAULT 0,
    partner_code    VARCHAR(32)
);

CREATE INDEX idx_balances_period ON account_balances (period);
CREATE INDEX idx_balances_batch ON account_balances (batch_id);

CREATE TABLE bank_statement_lines (
    id             BIGSERIAL PRIMARY KEY,
    batch_id       BIGINT        NOT NULL,
    period         VARCHAR(16)   NOT NULL,
    txn_date       DATE          NOT NULL,
    amount         NUMERIC(18,2) NOT NULL,
    direction      VARCHAR(8)    NOT NULL,
    counterparty   VARCHAR(200),
    summary        VARCHAR(500),
    bank_account   VARCHAR(64),
    ref_no         VARCHAR(64),
    balance_after  NUMERIC(18,2),
    matched        BOOLEAN       NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_bank_period ON bank_statement_lines (period);

CREATE TABLE cash_journal_lines (
    id            BIGSERIAL PRIMARY KEY,
    batch_id      BIGINT        NOT NULL,
    period        VARCHAR(16)   NOT NULL,
    txn_date      DATE          NOT NULL,
    amount        NUMERIC(18,2) NOT NULL,
    direction     VARCHAR(8)    NOT NULL,
    account_code  VARCHAR(32),
    counterparty  VARCHAR(200),
    summary       VARCHAR(500),
    voucher_no    VARCHAR(64),
    matched       BOOLEAN       NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_journal_period ON cash_journal_lines (period);

CREATE TABLE recon_matches (
    id              BIGSERIAL PRIMARY KEY,
    period          VARCHAR(16)   NOT NULL,
    statement_id    BIGINT        NOT NULL,
    journal_id      BIGINT        NOT NULL,
    match_type      VARCHAR(16)   NOT NULL,
    amount          NUMERIC(18,2) NOT NULL,
    date_diff_days  INT           NOT NULL DEFAULT 0,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_recon_period ON recon_matches (period);

CREATE TABLE report_lines (
    id            BIGSERIAL PRIMARY KEY,
    report_type   VARCHAR(40)  NOT NULL,
    line_code     VARCHAR(32)  NOT NULL,
    line_name     VARCHAR(200) NOT NULL,
    section       VARCHAR(64),
    line_kind     VARCHAR(16)  NOT NULL,
    formula_expr  VARCHAR(500),
    sort_order    INT          NOT NULL,
    indent        INT          NOT NULL DEFAULT 0,
    UNIQUE (report_type, line_code)
);

CREATE TABLE report_formulas (
    id            BIGSERIAL PRIMARY KEY,
    report_type   VARCHAR(40)   NOT NULL,
    line_code     VARCHAR(32)   NOT NULL,
    account_code  VARCHAR(32)   NOT NULL,
    multiplier    NUMERIC(8,2)  NOT NULL DEFAULT 1,
    UNIQUE (report_type, line_code, account_code)
);

CREATE INDEX idx_formula_line ON report_formulas (report_type, line_code);

CREATE TABLE audit_logs (
    id           BIGSERIAL PRIMARY KEY,
    action       VARCHAR(80) NOT NULL,
    entity_type  VARCHAR(64),
    entity_id    VARCHAR(64),
    detail       VARCHAR(2000),
    username     VARCHAR(64),
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_created ON audit_logs (created_at);

CREATE TABLE app_settings (
    setting_key    VARCHAR(64)  PRIMARY KEY,
    setting_value  VARCHAR(500) NOT NULL
);

COMMIT;
