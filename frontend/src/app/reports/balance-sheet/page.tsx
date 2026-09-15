"use client";

import ReportView from "@/components/ReportView";

export default function BalanceSheetPage() {
  return <ReportView title="资产负债表" path="/api/reports/balance-sheet" empty="导入科目余额后即可按公式生成资产负债表。" />;
}
