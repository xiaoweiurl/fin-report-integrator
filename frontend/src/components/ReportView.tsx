"use client";

import AppShell, { Card, PageTitle, useLivePeriod } from "@/components/AppShell";
import { api, formatAmount } from "@/lib/api";
import { useCallback, useEffect, useState } from "react";

type Rpt = {
  reportType: string;
  period: string;
  lines: { lineCode: string; lineName: string; lineKind: string; indent: number; amount: number | null }[];
};

export default function ReportView({ title, path, empty }: { title: string; path: string; empty: string }) {
  const period = useLivePeriod();
  const [data, setData] = useState<Rpt | null>(null);
  const [err, setErr] = useState("");

  const load = useCallback(async () => {
    try {
      setErr("");
      setData(await api<Rpt>(`${path}?period=${period}`));
    } catch (e) {
      setErr(e instanceof Error ? e.message : "加载失败");
    }
  }, [path, period]);

  useEffect(() => {
    load();
  }, [load]);

  const hasAmount = data?.lines.some((l) => l.amount && l.amount !== 0);

  return (
    <AppShell>
      <PageTitle title={title} subtitle={`${period} · 行项目由科目公式配置驱动`} />
      {err ? <p className="text-ios-red mb-3">{err}</p> : null}
      {!data ? (
        <Card className="p-6 text-ios-secondary">正在生成…</Card>
      ) : !hasAmount ? (
        <Card className="p-6 text-ios-secondary">{empty}</Card>
      ) : (
        <Card className="overflow-hidden py-2">
          {data.lines.map((l) => {
            const header = l.lineKind === "HEADER";
            const total = l.lineKind === "TOTAL";
            return (
              <div
                key={l.lineCode}
                className={`flex items-baseline px-4 ${total ? "py-3 border-t border-black/[0.06]" : "py-2.5"}`}
                style={{ paddingLeft: 16 + l.indent * 16 }}
              >
                <div className={`flex-1 ${header ? "text-[13px] text-ios-secondary tracking-widest" : "text-[16px]"} ${total ? "font-semibold" : ""}`}>
                  {l.lineName}
                </div>
                {!header ? (
                  <div className={`tabular text-[16px] ${total ? "font-semibold" : ""}`}>{formatAmount(l.amount)}</div>
                ) : null}
              </div>
            );
          })}
        </Card>
      )}
    </AppShell>
  );
}
