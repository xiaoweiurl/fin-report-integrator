"use client";

import AppShell, { Badge, Card, PageTitle, useLivePeriod } from "@/components/AppShell";
import { api, formatAmount } from "@/lib/api";
import { ChevronRight } from "lucide-react";
import Link from "next/link";
import { useCallback, useEffect, useState } from "react";

type Dash = {
  period: string;
  batchCount: number;
  errorRows: number;
  trialBalanced: boolean;
  closingDebit: number;
  closingCredit: number;
  balanceCount: number;
  statementCount: number;
  journalCount: number;
  reconMatchCount: number;
  netProfit: number;
  totalAssets: number;
  batches: { id: number; importType: string; status: string; fileName: string; createdAt: string }[];
};

const TYPE_LABEL: Record<string, string> = {
  ACCOUNT_BALANCE: "科目余额",
  BANK_STATEMENT: "银行流水",
  CASH_JOURNAL: "日记账",
  CHART_OF_ACCOUNTS: "科目",
  PARTNER: "往来",
};

export default function HomePage() {
  const period = useLivePeriod();
  const [data, setData] = useState<Dash | null>(null);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    try {
      setError("");
      setData(await api<Dash>(`/api/dashboard?period=${period}`));
    } catch (e) {
      setError(e instanceof Error ? e.message : "加载失败");
    }
  }, [period]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <AppShell>
      <PageTitle title="概览" subtitle={`${period} 期间 · 导入、报表与对账入口`} />
      {error ? <p className="text-ios-red mb-4">{error}</p> : null}
      {!data ? (
        <Card className="p-6 text-ios-secondary">正在读取本期数据…</Card>
      ) : (
        <>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-5">
            <Stat label="试算平衡" value={data.trialBalanced ? "已平衡" : data.balanceCount ? "不平衡" : "未导入"} tone={data.trialBalanced ? "green" : data.balanceCount ? "red" : "gray"} />
            <Stat label="资产总计" value={formatAmount(data.totalAssets)} />
            <Stat label="本期净利润" value={formatAmount(data.netProfit)} />
            <Stat label="异常行" value={String(data.errorRows)} tone={data.errorRows ? "red" : "gray"} />
          </div>

          <h3 className="text-[13px] uppercase tracking-widest text-ios-secondary px-1 mb-2">快捷入口</h3>
          <div className="grid md:grid-cols-2 gap-3 mb-6">
            <Shortcut href="/import" title="导入中心" desc="上传 Excel / CSV，下载模板" />
            <Shortcut href="/reports/trial-balance" title="科目余额 / 试算平衡" desc={`期末借方 ${formatAmount(data.closingDebit)}`} />
            <Shortcut href="/reports/balance-sheet" title="资产负债表" desc="按科目公式汇总" />
            <Shortcut href="/reports/income" title="利润表" desc="收入、成本与净利润" />
            <Shortcut href="/recon" title="银行对账" desc={`流水 ${data.statementCount} · 日记账 ${data.journalCount} · 已匹配 ${data.reconMatchCount}`} />
            <Shortcut href="/import" title="异常队列" desc="校验失败行可导出" />
          </div>

          <h3 className="text-[13px] uppercase tracking-widest text-ios-secondary px-1 mb-2">本期导入</h3>
          <Card className="overflow-hidden">
            {data.batches.length === 0 ? (
              <div className="p-6 text-[15px] text-ios-secondary">
                本期间还没有导入记录。请打开导入中心，使用仓库 <code className="text-ios-blue">samples/</code> 中的示例文件。
              </div>
            ) : (
              <ul>
                {data.batches.map((b, i) => (
                  <li key={b.id} className={i ? "border-t border-black/[0.06]" : ""}>
                    <Link href={`/import/${b.id}`} className="pressable flex items-center gap-3 px-4 h-14">
                      <div className="flex-1 min-w-0">
                        <div className="text-[16px] truncate">{b.fileName}</div>
                        <div className="text-[12px] text-ios-secondary">{TYPE_LABEL[b.importType] ?? b.importType}</div>
                      </div>
                      <Badge tone={b.status === "SUCCESS" ? "green" : b.status === "FAILED" ? "red" : "orange"}>
                        {b.status === "SUCCESS" ? "成功" : b.status === "FAILED" ? "失败" : "部分"}
                      </Badge>
                      <ChevronRight size={18} className="text-ios-secondary" />
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </Card>
        </>
      )}
    </AppShell>
  );
}

function Stat({ label, value, tone }: { label: string; value: string; tone?: "green" | "red" | "gray" }) {
  const color =
    tone === "green" ? "text-ios-green" : tone === "red" ? "text-ios-red" : "text-black";
  return (
    <Card className="p-4">
      <div className="text-[13px] text-ios-secondary">{label}</div>
      <div className={`mt-1 text-[20px] font-semibold tabular ${color}`}>{value}</div>
    </Card>
  );
}

function Shortcut({ href, title, desc }: { href: string; title: string; desc: string }) {
  return (
    <Link href={href} className="pressable block">
      <Card className="p-4 flex items-center">
        <div className="flex-1">
          <div className="text-[17px] font-semibold">{title}</div>
          <div className="text-[13px] text-ios-secondary mt-0.5">{desc}</div>
        </div>
        <ChevronRight className="text-ios-secondary" size={20} />
      </Card>
    </Link>
  );
}
