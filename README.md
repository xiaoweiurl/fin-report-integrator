# 财报系统

财务数据导入、清洗校验、科目余额 / 资产负债表 / 利润表，以及银行对账（余额调节表草稿）的演示用 MVP。

界面为中文，视觉按 iOS 人机界面风格实现。后端 Java Spring Boot 3.x，前端 Next.js。

## 功能（本版本已实现）

1. **登录**：JWT。演示账号 `admin` / `admin123`
2. **导入中心**：Excel / CSV 导入科目余额表、银行流水、企业日记账、会计科目、往来单位；模板下载；列映射（JSON，可改）；批次记录含文件 SHA-256、期间、状态；异常行队列与 CSV 导出
3. **清洗校验**：必填项、金额解析、期间一致性；科目余额试算（期末借方 = 贷方）；失败行进入异常队列
4. **主数据**：会计科目 CRUD、往来单位 CRUD（并支持文件导入）
5. **报表**：科目余额表 / 试算平衡、资产负债表、利润表（科目 → 报表行公式，已预置小企业常用科目映射）
6. **银行对账**：流水 vs 日记账按「日期 ±N 天 + 等额 + 收支方向」自动匹配；已匹配 / 未达账；余额调节表草稿
7. **审计日志**：登录、导入、对账与主数据关键操作

## 如何运行

需 **JDK 21**、**Node.js 20+**。后端已附 Maven Wrapper，不必预先安装 Maven。

### 1. 启动后端（H2 文件库，默认）

```bash
cd backend
./mvnw spring-boot:run
```

服务地址：`http://127.0.0.1:18443`  
健康检查：`GET /api/health`  
H2 控制台：`http://127.0.0.1:18443/h2-console`  
JDBC URL：`jdbc:h2:file:./data/finreport;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;CASE_INSENSITIVE_IDENTIFIERS=TRUE`  
用户 `sa`，密码空。

首次启动会写入种子数据：管理员、小企业科目表、往来单位、列映射、报表公式。当前会计期间默认为 **2025-12**。

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 `http://127.0.0.1:43123`，使用 `admin` / `admin123` 登录。

前端默认请求 `http://127.0.0.1:18443`。若后端地址不同：

```bash
NEXT_PUBLIC_API_BASE=http://127.0.0.1:18443 npm run dev
```

### 3. 演示路径（建议按此点击）

1. 登录后进入 **导入中心**，期间选 `2025-12`
2. 导入 `samples/科目余额表-2025-12.xlsx`（类型：科目余额表）
3. 打开 **报表 → 科目余额表**，应显示试算已平衡；再打开资产负债表、利润表，应有金额
4. 依次导入 `samples/银行流水-2025-12.xlsx`、`samples/企业日记账-2025-12.xlsx`
5. 打开 **对账**，点击「运行自动匹配」，应看到 4 笔勾对，以及余额调节表两侧勾平
6. 可用 `samples/科目余额表-含错误行.csv` 观察异常队列

示例文件目录：仓库根目录 [`samples/`](samples/)。

| 文件 | 用途 |
| --- | --- |
| `科目余额表-2025-12.xlsx` / `.csv` | 平衡的科目余额（演示报表） |
| `科目余额表-含错误行.csv` | 缺编码、非法金额（演示异常队列） |
| `银行流水-2025-12.xlsx` / `.csv` | 7 笔银行交易 |
| `企业日记账-2025-12.xlsx` / `.csv` | 6 笔日记账（4 笔可自动匹配） |
| `会计科目-增补.csv` | 科目主数据增补 |
| `往来单位.csv` | 往来主数据增补 |

## PostgreSQL 配置

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

默认连接 `jdbc:postgresql://localhost:5432/finreport`，用户/密码 `finreport`。可用环境变量覆盖：

- `DATABASE_URL`
- `DATABASE_USER`
- `DATABASE_PASSWORD`

Flyway 迁移在 `backend/src/main/resources/db/migration/`，H2 与 PostgreSQL 共用同一套 SQL。

## 默认账户与安全说明

| 项目 | 值 |
| --- | --- |
| 用户名 | `admin` |
| 密码 | `admin123` |
| JWT | 配置于 `backend/src/main/resources/application.yml` 的 `app.jwt.secret` |

演示密钥请勿用于生产。生产环境请更换密钥、使用 PostgreSQL，并关闭 H2 控制台。

## 目录结构

```
backend/     Spring Boot 3.5、JPA、Flyway、Spring Security
frontend/    Next.js App Router、TypeScript、Tailwind
samples/     可直接导入的 Excel / CSV
```

后端包结构：`controller` / `service` / `repository` / `entity` 按业务分包（`auth`、`importdata`、`report`、`recon`、`master`、`audit` 等）。CORS 已允许本机前端 `43123` 端口。

## 路线图（本 MVP 未做）

- ERP / 银企直连 API 采集
- PDF / 影像 OCR
- 多公司合并抵销
- 预算与执行分析
- 薪酬模块深层逻辑
- 邮件收件箱自动取数

## 构建

```bash
cd backend && ./mvnw -DskipTests package
cd frontend && npm run build
```
