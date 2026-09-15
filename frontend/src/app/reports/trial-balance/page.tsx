"use client";

import AppShell, { Card, PageTitle, useLivePeriod } from "@/components/AppShell";
import { api, formatAmount } from "@/lib/api";
import { useCallback, useEffect, useState } from "react";

type TB = {
  period: string;
  balanced: boolean;
  openingDebit: number;
  openingCredit: number;
  periodDebit: number;
  periodCredit: number;
  closingDebit: number;
  closingCredit: number;
  rows: {
    accountCode: string;
    accountName: string;
    openingDebit: number;
    openingCredit: number;
    periodDebit: number;
    periodCredit: number;
    closingDebit: number;
    closingCredit: number;
  }[];
};

export default function TrialBalancePage() {
  const period = useLivePeriod();
  const [data, setData] = useState<TB | null>(null);
  const [err, setErr] = useState("");

  const load = useCallback(async () => {
    try {
      setErr("");
      setData(await api<TB>(`/api/reports/trial-balance?period=${period}`));
    } catch (e) {
      setErr(e instanceof Error ? e.message : "加载失败");
    }
  }, [period]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <AppShell>
      <PageTitle title="科目余额表" subtitle="试算平衡检查期末借方与贷方合计" />
      {err ? <p className="text-ios-red mb-3">{err}</p> : null}
      {!data ? (
        <Card className="p-6 text-ios-secondary">正在生成…</Card>
      ) : data.rows.length === 0 ? (
        <Card className="p-6 text-ios-secondary">本期间尚未导入科目余额表。</Card>
      ) : (
        <>
          <Card className={`p-4 mb-4 ${data.balanced ? "bg-white" : "bg-[#FF3B30]/8"}`}>
            <div className="text-[15px] font-semibold">{data.balanced ? "试算已平衡" : "试算不平衡"}</div>
            <div className="mt-1 text-[13px] text-ios-secondary tabular">
              期末借方 {formatAmount(data.closingDebit)} · 期末贷方 {formatAmount(data.closingCredit)}
            </div>
          </Card>
          <Card className="overflow-x-auto">
            <table className="w-full text-[13px] tabular min-w-[720px]">
              <thead className="text-ios-secondary text-[12px]">
                <tr className="border-b border-black/[0.06]">
                  <th className="text-left font-medium px-3 py-3">科目</th>
                  <th className="text-right font-medium px-2">期初借</th>
                  <th className="text-right font-medium px-2">期初贷</th>
                  <th className="text-right font-medium px-2">本期借</th>
                  <th className="text-right font-medium px-2">本期贷</th>
                  <th className="text-right font-medium px-2">期末借</th>
                  <th className="text-right font-medium px-2">期末贷</th>
                </tr>
              </thead>
              <tbody>
                {data.rows.map((r) => (
                  <tr key={r.accountCode} className="border-b border-black/[0.04]">
                    <td className="px-3 py-2.5">
                      <span className="text-ios-secondary mr-2">{r.accountCode}</span>
                      {r.accountName}
                    </td>
                    <td className="text-right px-2">{n(r.openingDebit)}</td>
                    <td className="text-right px-2">{n(r.openingCredit)}</td>
                    <td className="text-right px-2">{n(r.periodDebit)}</td>
                    <td className="text-right px-2">{n(r.periodCredit)}</td>
                    <td className="text-right px-2 font-medium">{n(r.closingDebit)}</td>
                    <td className="text-right px-2 font-medium">{n(r.closingCredit)}</td>
                  </tr>
                ))}
              </tbody>
              <tfoot>
                <tr className="font-semibold">
                  <td className="px-3 py-3">合计</td>
                  <td className="text-right px-2">{formatAmount(data.openingDebit)}</td>
                  <td className="text-right px-2">{formatAmount(data.openingCredit)}</td>
                  <td className="text-right px-2">{formatAmount(data.periodDebit)}</td>
                  <td className="text-right px-2">{formatAmount(data.periodCredit)}</td>
                  <td className="text-right px-2">{formatAmount(data.closingDebit)}</td>
                  <td className="text-right px-2">{formatAmount(data.closingCredit)}</td>
                </tr>
              </tfoot>
            </table>
          </Card>
        </>
      )}
    </AppShell>
  );
}

function n(v: number) {
  return v ? formatAmount(v) : "";
}
