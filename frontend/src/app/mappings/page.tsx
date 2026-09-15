"use client";

import AppShell, { Card, PageTitle, PrimaryButton } from "@/components/AppShell";
import { api } from "@/lib/api";
import { useEffect, useState } from "react";

type Mapping = { id: number; importType: string; name: string; mappingJson: string; isDefault: boolean };

export default function MappingsPage() {
  const [rows, setRows] = useState<Mapping[]>([]);
  const [edit, setEdit] = useState<Mapping | null>(null);
  const [json, setJson] = useState("");
  const [msg, setMsg] = useState("");
  const [err, setErr] = useState("");

  async function load() {
    setRows(await api<Mapping[]>("/api/mappings"));
  }
  useEffect(() => {
    load().catch((e) => setErr(e.message));
  }, []);

  async function save() {
    if (!edit) return;
    try {
      JSON.parse(json);
      await api(`/api/mappings/${edit.id}`, {
        method: "PUT",
        body: JSON.stringify({ mappingJson: json, name: edit.name }),
      });
      setMsg("已保存");
      setEdit(null);
      await load();
    } catch (e) {
      setErr(e instanceof Error ? e.message : "JSON 无效");
    }
  }

  return (
    <AppShell>
      <PageTitle title="列映射" subtitle="字段对应 Excel/CSV 表头，支持别名数组。导入时按此配置取数。" />
      {err ? <p className="text-ios-red mb-3">{err}</p> : null}
      {msg ? <p className="text-ios-green mb-3">{msg}</p> : null}
      {edit ? (
        <Card className="p-4 mb-4">
          <div className="font-semibold mb-2">{edit.name}</div>
          <textarea
            className="w-full h-64 bg-ios-bg rounded-2xl p-3 font-mono text-[13px] outline-none"
            value={json}
            onChange={(e) => setJson(e.target.value)}
          />
          <div className="mt-3 flex gap-3">
            <PrimaryButton onClick={save}>保存映射</PrimaryButton>
            <button className="text-ios-secondary" onClick={() => setEdit(null)}>
              取消
            </button>
          </div>
        </Card>
      ) : null}
      <Card className="overflow-hidden">
        {rows.map((m, i) => (
          <button
            key={m.id}
            className={`pressable w-full text-left px-4 py-3 ${i ? "border-t border-black/[0.06]" : ""}`}
            onClick={() => {
              setEdit(m);
              setJson(m.mappingJson);
              setMsg("");
              setErr("");
            }}
          >
            <div className="text-[16px] font-medium">{m.name}</div>
            <div className="text-[13px] text-ios-secondary">{m.importType}</div>
          </button>
        ))}
      </Card>
    </AppShell>
  );
}
