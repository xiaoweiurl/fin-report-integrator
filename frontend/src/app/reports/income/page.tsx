"use client";

import ReportView from "@/components/ReportView";

export default function IncomePage() {
  return (
    <ReportView
      title="利润表"
      path="/api/reports/income-statement"
      empty="导入科目余额后即可生成利润表。营业收入、成本与费用均来自期末余额。"
    />
  );
}
