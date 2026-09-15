"use client";

import AppShell, { Badge, Card, PageTitle, PrimaryButton, statusLabel, statusTone, useLivePeriod } from "@/components/AppShell";
import { IMPORT_TYPES, api, downloadUrl } from "@/lib/api";
import Link from "next/link";
import { useCallback, useEffect, useState } from "react";

type Batch = {
  id: number;
  importType: string;
  fileName: string;
  fileHash: string;
  period: string;
  status: string;
  totalRows: number;
  successRows: number;
  errorRows: number;
  message: string;
  createdAt: string;
};

export default function ImportPage() {
  const period = useLivePeriod();
  const [type, setType] = useState("ACCOUNT_BALANCE");
  const [file, setFile] = useState<File | null>(null);
  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState("");
  const [err, setErr] = useState("");
  const [batches, setBatches] = useState<Batch[]>([]);

  const load = useCallback(async () => {
    setBatches(await api<Batch[]>(`/api/imports?period=${period}`));
  }, [period]);

  useEffect(() => {
    load().catch((e) => setErr(e.message));
  }, [load]);

  async function upload() {
    if (!file) {
      setErr("请选择 Excel 或 CSV 文件");
      return;
    }
    setBusy(true);
    setErr("");
    setMsg("");
    try {
      const fd = new FormData();
      fd.append("file", file);
      fd.append("importType", type);
      fd.append("period", period);
      const batch = await api<Batch>("/api/imports", { method: "POST", body: fd });
      setMsg(batch.message);
      setFile(null);
      await load();
    } catch (e) {
      setErr(e instanceof Error ? e.message : "导入失败");
    } finally {
      setBusy(false);
    }
  }

  async function template() {
    const res = await downloadUrl(`/api/imports/templates/${type}`);
    const blob = await res.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = IMPORT_TYPES.find((t) => t.value === type)?.label + "模板.xlsx";
    a.click();
    URL.revokeObjectURL(url);
  }

  const current = IMPORT_TYPES.find((t) => t.value === type)!;

  return (
    <AppShell>
      <PageTitle title="导入中心" subtitle="上传科目余额、银行流水与日记账，系统会校验并写入本期账套" />

      <Card className="p-5 mb-5">
        <div className="flex gap-2 overflow-x-auto pb-1">
          {IMPORT_TYPES.map((t) => (
            <button
              key={t.value}
              onClick={() => setType(t.value)}
              className={`pressable whitespace-nowrap rounded-full h-9 px-4 text-[14px] font-medium ${
                type === t.value ? "bg-ios-blue text-white" : "bg-ios-fill text-black"
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>
        <p className="mt-3 text-[14px] text-ios-secondary">
          {current.hint}。示例文件可从本页下载，或使用仓库{" "}
          <span className="text-ios-blue">samples/{current.sample}</span>
        </p>
        <a
          href={`/samples/${current.sample}`}
          download
          className="inline-block mt-2 text-[14px] text-ios-blue font-medium"
        >
          下载示例 {current.sample}
        </a>
        <label className="mt-4 flex items-center justify-between bg-ios-bg rounded-2xl px-4 h-14">
          <span className="text-[15px] text-ios-secondary truncate pr-3">{file ? file.name : "选择 .xlsx / .csv"}</span>
          <input
            type="file"
            accept=".xlsx,.xls,.csv"
            className="hidden"
            onChange={(e) => setFile(e.target.files?.[0] ?? null)}
          />
          <span className="text-ios-blue font-semibold">浏览</span>
        </label>
        <div className="mt-4 flex flex-wrap gap-3">
          <PrimaryButton onClick={upload} disabled={busy}>
            {busy ? "正在导入…" : "开始导入"}
          </PrimaryButton>
          <button onClick={template} className="pressable h-12 px-5 rounded-full bg-white text-ios-blue font-semibold ios-shadow">
            下载模板
          </button>
          <Link href="/mappings" className="pressable h-12 px-5 rounded-full bg-white text-black font-semibold ios-shadow flex items-center">
            列映射
          </Link>
        </div>
        {msg ? <p className="mt-3 text-[14px] text-ios-green">{msg}</p> : null}
        {err ? <p className="mt-3 text-[14px] text-ios-red">{err}</p> : null}
      </Card>

      <h3 className="text-[13px] text-ios-secondary px-1 mb-2">本期间批次</h3>
      <Card className="overflow-hidden">
        {batches.length === 0 ? (
          <div className="p-6 text-ios-secondary text-[15px]">暂无导入。先下载模板或使用 samples 目录中的示例。</div>
        ) : (
          <ul>
            {batches.map((b, i) => (
              <li key={b.id} className={i ? "border-t border-black/[0.06]" : ""}>
                <Link href={`/import/${b.id}`} className="flex items-center px-4 py-3 gap-3 pressable">
                  <div className="flex-1 min-w-0">
                    <div className="text-[16px] truncate">{b.fileName}</div>
                    <div className="text-[12px] text-ios-secondary">
                      {b.successRows}/{b.totalRows} 行成功 · 哈希 {b.fileHash.slice(0, 10)}…
                    </div>
                  </div>
                  <Badge tone={statusTone(b.status)}>{statusLabel(b.status)}</Badge>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </AppShell>
  );
}
