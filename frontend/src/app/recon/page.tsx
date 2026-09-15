"use client";

import AppShell, { Card, PageTitle, PrimaryButton, useLivePeriod } from "@/components/AppShell";
import { api, formatAmount } from "@/lib/api";
import { useCallback, useEffect, useState, type ReactNode } from "react";

type Line = {
  id: number;
  txnDate: string;
  amount: number;
  direction: string;
  counterparty?: string;
  summary?: string;
  matched: boolean;
};

type View = {
  period: string;
  statementCount: number;
  journalCount: number;
  matchCount: number;
  unmatchedStatements: Line[];
  unmatchedJournals: Line[];
  matches: {
    match: { amount: number; dateDiffDays: number; matchType: string };
    statement: Line;
    journal: Line;
  }[];
  adjustment: {
    bookBalance: number;
    bankReceivedBookNot: number;
    bankPaidBookNot: number;
    adjustedBook: number;
    bankStatementBalance: number;
    bookReceivedBankNot: number;
    bookPaidBankNot: number;
    adjustedBank: number;
    tied: boolean;
  };
};

export default function ReconPage() {
  const period = useLivePeriod();
  const [data, setData] = useState<View | null>(null);
  const [err, setErr] = useState("");
  const [busy, setBusy] = useState(false);
  const [tol, setTol] = useState(2);

  const load = useCallback(async () => {
    try {
      setErr("");
      setData(await api<View>(`/api/recon?period=${period}`));
    } catch (e) {
      setErr(e instanceof Error ? e.message : "加载失败");
    }
  }, [period]);

  useEffect(() => {
    load();
  }, [load]);

  async function run() {
    setBusy(true);
    try {
      setData(await api<View>(`/api/recon/run?period=${period}&toleranceDays=${tol}`, { method: "POST" }));
    } catch (e) {
      setErr(e instanceof Error ? e.message : "对账失败");
    } finally {
      setBusy(false);
    }
  }

  const empty = data && data.statementCount === 0 && data.journalCount === 0;

  return (
    <AppShell>
      <PageTitle title="银行对账" subtitle="按日期容差与等额自动匹配流水与日记账，并生成余额调节表草稿" />
      {err ? <p className="text-ios-red mb-3">{err}</p> : null}
      <Card className="p-4 mb-5 flex flex-wrap items-center gap-3">
        <label className="flex items-center gap-2 text-[15px]">
          日期容差 ±
          <input
            type="number"
            min={0}
            max={30}
            value={tol}
            onChange={(e) => setTol(Number(e.target.value))}
            className="w-16 bg-ios-bg rounded-xl h-10 px-3 tabular"
          />
          天
        </label>
        <PrimaryButton onClick={run} disabled={busy} tone="green">
          {busy ? "正在匹配…" : "运行自动匹配"}
        </PrimaryButton>
        {data ? (
          <span className="text-[13px] text-ios-secondary">
            流水 {data.statementCount} · 日记账 {data.journalCount} · 已匹配 {data.matchCount}
          </span>
        ) : null}
      </Card>

      {!data ? (
        <Card className="p-6 text-ios-secondary">加载中…</Card>
      ) : empty ? (
        <Card className="p-6 text-ios-secondary">请先导入本期银行流水与企业日记账（见 samples/）。</Card>
      ) : (
        <>
          <h3 className="text-[13px] text-ios-secondary px-1 mb-2">银行存款余额调节表（草稿）</h3>
          <Card className="p-5 mb-5">
            <AdjRow label="企业银行存款日记账余额" value={data.adjustment.bookBalance} />
            <AdjRow label="加：银行已收企业未收" value={data.adjustment.bankReceivedBookNot} />
            <AdjRow label="减：银行已付企业未付" value={data.adjustment.bankPaidBookNot} minus />
            <AdjRow label="调节后存款余额" value={data.adjustment.adjustedBook} strong />
            <div className="h-px bg-black/[0.06] my-3" />
            <AdjRow label="银行对账单余额" value={data.adjustment.bankStatementBalance} />
            <AdjRow label="加：企业已收银行未收" value={data.adjustment.bookReceivedBankNot} />
            <AdjRow label="减：企业已付银行未付" value={data.adjustment.bookPaidBankNot} minus />
            <AdjRow label="调节后存款余额" value={data.adjustment.adjustedBank} strong />
            <div className="mt-3">
              <BadgeLike ok={data.adjustment.tied}>{data.adjustment.tied ? "两侧调节后余额一致" : "两侧尚未勾平，请检查未达账项"}</BadgeLike>
            </div>
          </Card>

          <h3 className="text-[13px] text-ios-secondary px-1 mb-2">已匹配</h3>
          <Card className="overflow-hidden mb-5">
            {data.matches.length === 0 ? (
              <div className="p-5 text-ios-secondary">尚无匹配。点击上方按钮运行。</div>
            ) : (
              <ul>
                {data.matches.map((m, i) => (
                  <li key={i} className={`px-4 py-3 ${i ? "border-t border-black/[0.06]" : ""}`}>
                    <div className="flex items-center gap-2">
                      <span className="tabular font-semibold">{formatAmount(m.match.amount)}</span>
                      <Badge>{m.statement.direction === "IN" ? "收入" : "支出"}</Badge>
                      <span className="text-[12px] text-ios-secondary">日期差 {m.match.dateDiffDays} 天</span>
                    </div>
                    <div className="text-[13px] text-ios-secondary mt-1">
                      银行 {m.statement.txnDate} {m.statement.summary} · 账簿 {m.journal.txnDate} {m.journal.summary}
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </Card>

          <div className="grid md:grid-cols-2 gap-4">
            <Unmatched title="未达账 · 银行有企业无" rows={data.unmatchedStatements} />
            <Unmatched title="未达账 · 企业有银行无" rows={data.unmatchedJournals} />
          </div>
        </>
      )}
    </AppShell>
  );
}

function AdjRow({ label, value, minus, strong }: { label: string; value: number; minus?: boolean; strong?: boolean }) {
  return (
    <div className={`flex items-baseline py-1 ${strong ? "font-semibold" : ""}`}>
      <div className="flex-1 text-[15px]">{label}</div>
      <div className="tabular text-[15px]">
        {minus ? "− " : ""}
        {formatAmount(value)}
      </div>
    </div>
  );
}

function Unmatched({ title, rows }: { title: string; rows: Line[] }) {
  return (
    <div>
      <h3 className="text-[13px] text-ios-secondary px-1 mb-2">{title}</h3>
      <Card className="overflow-hidden">
        {rows.length === 0 ? (
          <div className="p-5 text-ios-secondary text-[14px]">全部已匹配</div>
        ) : (
          <ul>
            {rows.map((r, i) => (
              <li key={r.id} className={`px-4 py-3 ${i ? "border-t border-black/[0.06]" : ""}`}>
                <div className="flex justify-between">
                  <span>{r.txnDate}</span>
                  <span className={`tabular ${r.direction === "IN" ? "text-ios-green" : "text-ios-red"}`}>
                    {r.direction === "IN" ? "+" : "−"}
                    {formatAmount(r.amount)}
                  </span>
                </div>
                <div className="text-[13px] text-ios-secondary">{r.counterparty} · {r.summary}</div>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  );
}

function Badge({ children }: { children: ReactNode }) {
  return <span className="inline-flex h-6 px-2 rounded-full bg-[#34C759]/14 text-[#248a3d] text-[12px] font-medium">{children}</span>;
}

function BadgeLike({ ok, children }: { ok: boolean; children: ReactNode }) {
  return (
    <span className={`inline-flex h-7 px-3 rounded-full text-[13px] font-medium ${ok ? "bg-[#34C759]/14 text-[#248a3d]" : "bg-[#FF3B30]/12 text-ios-red"}`}>
      {children}
    </span>
  );
}
