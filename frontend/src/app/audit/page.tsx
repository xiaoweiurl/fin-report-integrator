"use client";

import AppShell, { Card, PageTitle } from "@/components/AppShell";
import { api } from "@/lib/api";
import { useEffect, useState } from "react";

type Page = {
  content: { id: number; action: string; entityType: string; entityId: string; detail: string; username: string; createdAt: string }[];
  totalElements: number;
};

const ACTION: Record<string, string> = {
  LOGIN: "登录",
  IMPORT: "导入",
  RECON_RUN: "自动对账",
  ACCOUNT_CREATE: "新增科目",
  ACCOUNT_UPDATE: "修改科目",
  ACCOUNT_DELETE: "删除科目",
  PARTNER_CREATE: "新增往来",
  PARTNER_UPDATE: "修改往来",
  PARTNER_DELETE: "删除往来",
  MAPPING_UPDATE: "更新列映射",
  SETTINGS_UPDATE: "更新设置",
};

export default function AuditPage() {
  const [data, setData] = useState<Page | null>(null);
  const [err, setErr] = useState("");

  useEffect(() => {
    api<Page>("/api/audit?page=0&size=80")
      .then(setData)
      .catch((e) => setErr(e.message));
  }, []);

  return (
    <AppShell>
      <PageTitle title="审计日志" subtitle="登录、导入与关键主数据变更均会留痕" />
      {err ? <p className="text-ios-red">{err}</p> : null}
      <Card className="overflow-hidden">
        {!data ? (
          <div className="p-6 text-ios-secondary">加载中…</div>
        ) : data.content.length === 0 ? (
          <div className="p-6 text-ios-secondary">暂无日志</div>
        ) : (
          <ul>
            {data.content.map((r, i) => (
              <li key={r.id} className={`px-4 py-3 ${i ? "border-t border-black/[0.06]" : ""}`}>
                <div className="flex justify-between gap-3">
                  <span className="font-medium">{ACTION[r.action] ?? r.action}</span>
                  <span className="text-[12px] text-ios-secondary tabular">{r.createdAt?.replace("T", " ").slice(0, 19)}</span>
                </div>
                <div className="text-[13px] text-ios-secondary mt-0.5">
                  {r.username} · {r.detail}
                </div>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </AppShell>
  );
}
