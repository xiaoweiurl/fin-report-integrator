"use client";

import AppShell, { Badge, Card, PageTitle, PrimaryButton } from "@/components/AppShell";
import { api } from "@/lib/api";
import { useEffect, useState } from "react";

type Account = {
  id: number;
  code: string;
  name: string;
  category: string;
  balanceSide: string;
  parentCode?: string;
  enabled: boolean;
};

const CAT: Record<string, string> = {
  ASSET: "资产",
  LIABILITY: "负债",
  EQUITY: "权益",
  REVENUE: "收入",
  EXPENSE: "费用",
};

export default function AccountsPage() {
  const [rows, setRows] = useState<Account[]>([]);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({ code: "", name: "", category: "ASSET", balanceSide: "DEBIT" });
  const [err, setErr] = useState("");

  async function load() {
    setRows(await api<Account[]>("/api/accounts"));
  }
  useEffect(() => {
    load().catch((e) => setErr(e.message));
  }, []);

  async function create() {
    setErr("");
    try {
      await api("/api/accounts", { method: "POST", body: JSON.stringify({ ...form, enabled: true }) });
      setOpen(false);
      setForm({ code: "", name: "", category: "ASSET", balanceSide: "DEBIT" });
      await load();
    } catch (e) {
      setErr(e instanceof Error ? e.message : "保存失败");
    }
  }

  async function remove(id: number) {
    await api(`/api/accounts/${id}`, { method: "DELETE" });
    await load();
  }

  return (
    <AppShell>
      <PageTitle title="会计科目" subtitle="已预置小企业会计准则常用科目，可继续增补" />
      <div className="mb-4">
        <PrimaryButton onClick={() => setOpen(true)}>新增科目</PrimaryButton>
      </div>
      {err ? <p className="text-ios-red mb-3">{err}</p> : null}
      {open ? (
        <Card className="p-4 mb-4 space-y-3">
          <Field label="编码" value={form.code} onChange={(v) => setForm({ ...form, code: v })} />
          <Field label="名称" value={form.name} onChange={(v) => setForm({ ...form, name: v })} />
          <label className="flex items-center gap-3">
            <span className="w-16 text-ios-secondary text-[15px]">类别</span>
            <select
              className="flex-1 bg-ios-bg rounded-xl h-11 px-3"
              value={form.category}
              onChange={(e) =>
                setForm({
                  ...form,
                  category: e.target.value,
                  balanceSide: ["ASSET", "EXPENSE"].includes(e.target.value) ? "DEBIT" : "CREDIT",
                })
              }
            >
              {Object.entries(CAT).map(([k, v]) => (
                <option key={k} value={k}>
                  {v}
                </option>
              ))}
            </select>
          </label>
          <div className="flex gap-3">
            <PrimaryButton onClick={create}>保存</PrimaryButton>
            <button className="text-ios-secondary px-3" onClick={() => setOpen(false)}>
              取消
            </button>
          </div>
        </Card>
      ) : null}
      <Card className="overflow-hidden">
        {rows.length === 0 ? (
          <div className="p-6 text-ios-secondary">暂无科目</div>
        ) : (
          <ul>
            {rows.map((a, i) => (
              <li key={a.id} className={`flex items-center px-4 h-14 gap-3 ${i ? "border-t border-black/[0.06]" : ""}`}>
                <div className="w-16 tabular text-[15px] text-ios-secondary">{a.code}</div>
                <div className="flex-1 text-[16px]">{a.name}</div>
                <Badge tone="gray">{CAT[a.category] ?? a.category}</Badge>
                <button className="text-ios-red text-[14px]" onClick={() => remove(a.id)}>
                  删除
                </button>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </AppShell>
  );
}

function Field({ label, value, onChange }: { label: string; value: string; onChange: (v: string) => void }) {
  return (
    <label className="flex items-center gap-3">
      <span className="w-16 text-ios-secondary text-[15px]">{label}</span>
      <input className="flex-1 bg-ios-bg rounded-xl h-11 px-3 text-[16px] outline-none" value={value} onChange={(e) => onChange(e.target.value)} />
    </label>
  );
}
