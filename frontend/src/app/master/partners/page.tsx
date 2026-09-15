"use client";

import AppShell, { Badge, Card, PageTitle, PrimaryButton } from "@/components/AppShell";
import { api } from "@/lib/api";
import { useEffect, useState } from "react";

type Partner = { id: number; code: string; name: string; partnerType: string; enabled: boolean };
const TYPE: Record<string, string> = { CUSTOMER: "客户", SUPPLIER: "供应商", OTHER: "其他" };

export default function PartnersPage() {
  const [rows, setRows] = useState<Partner[]>([]);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({ code: "", name: "", partnerType: "CUSTOMER" });
  const [err, setErr] = useState("");

  async function load() {
    setRows(await api<Partner[]>("/api/partners"));
  }
  useEffect(() => {
    load().catch((e) => setErr(e.message));
  }, []);

  async function create() {
    try {
      await api("/api/partners", { method: "POST", body: JSON.stringify({ ...form, enabled: true }) });
      setOpen(false);
      setForm({ code: "", name: "", partnerType: "CUSTOMER" });
      await load();
    } catch (e) {
      setErr(e instanceof Error ? e.message : "保存失败");
    }
  }

  async function remove(id: number) {
    await api(`/api/partners/${id}`, { method: "DELETE" });
    await load();
  }

  return (
    <AppShell>
      <PageTitle title="往来单位" subtitle="用于银行流水与日记账的对方单位对照" />
      <div className="mb-4">
        <PrimaryButton onClick={() => setOpen(true)}>新增往来</PrimaryButton>
      </div>
      {err ? <p className="text-ios-red mb-3">{err}</p> : null}
      {open ? (
        <Card className="p-4 mb-4 space-y-3">
          <label className="flex items-center gap-3">
            <span className="w-16 text-ios-secondary">编码</span>
            <input className="flex-1 bg-ios-bg rounded-xl h-11 px-3" value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value })} />
          </label>
          <label className="flex items-center gap-3">
            <span className="w-16 text-ios-secondary">名称</span>
            <input className="flex-1 bg-ios-bg rounded-xl h-11 px-3" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          </label>
          <label className="flex items-center gap-3">
            <span className="w-16 text-ios-secondary">类型</span>
            <select className="flex-1 bg-ios-bg rounded-xl h-11 px-3" value={form.partnerType} onChange={(e) => setForm({ ...form, partnerType: e.target.value })}>
              <option value="CUSTOMER">客户</option>
              <option value="SUPPLIER">供应商</option>
              <option value="OTHER">其他</option>
            </select>
          </label>
          <div className="flex gap-3">
            <PrimaryButton onClick={create}>保存</PrimaryButton>
            <button onClick={() => setOpen(false)} className="text-ios-secondary">
              取消
            </button>
          </div>
        </Card>
      ) : null}
      <Card className="overflow-hidden">
        {rows.length === 0 ? (
          <div className="p-6 text-ios-secondary">暂无往来单位</div>
        ) : (
          <ul>
            {rows.map((p, i) => (
              <li key={p.id} className={`flex items-center px-4 h-14 gap-3 ${i ? "border-t border-black/[0.06]" : ""}`}>
                <div className="w-16 text-ios-secondary">{p.code}</div>
                <div className="flex-1">{p.name}</div>
                <Badge>{TYPE[p.partnerType] ?? p.partnerType}</Badge>
                <button className="text-ios-red text-[14px]" onClick={() => remove(p.id)}>
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
