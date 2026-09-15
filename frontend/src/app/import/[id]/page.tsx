"use client";

import AppShell, { Badge, Card, PageTitle, statusLabel, statusTone } from "@/components/AppShell";
import { api, downloadUrl } from "@/lib/api";
import { useParams } from "next/navigation";
import { useEffect, useState, type ReactNode } from "react";

type Detail = {
  batch: {
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
    createdBy: string;
    createdAt: string;
  };
  exceptions: { id: number; rowNumber: number; rawJson: string; errorMessage: string }[];
};

export default function ImportDetailPage() {
  const params = useParams<{ id: string }>();
  const [data, setData] = useState<Detail | null>(null);
  const [err, setErr] = useState("");

  useEffect(() => {
    api<Detail>(`/api/imports/${params.id}`)
      .then(setData)
      .catch((e) => setErr(e.message));
  }, [params.id]);

  async function exportEx() {
    const res = await downloadUrl(`/api/imports/${params.id}/exceptions/export`);
    const blob = await res.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `exceptions-batch-${params.id}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }

  return (
    <AppShell>
      <PageTitle title="导入批次" subtitle={data ? data.batch.fileName : "正在加载"} />
      {err ? <p className="text-ios-red">{err}</p> : null}
      {data ? (
        <>
          <Card className="p-5 mb-5 space-y-2 text-[15px]">
            <Row k="期间" v={data.batch.period} />
            <Row k="状态" v={<Badge tone={statusTone(data.batch.status)}>{statusLabel(data.batch.status)}</Badge>} />
            <Row k="文件哈希" v={<span className="tabular text-[12px] break-all">{data.batch.fileHash}</span>} />
            <Row k="成功 / 失败" v={`${data.batch.successRows} / ${data.batch.errorRows}（共 ${data.batch.totalRows}）`} />
            <Row k="说明" v={data.batch.message} />
            <Row k="操作人" v={`${data.batch.createdBy} · ${data.batch.createdAt?.replace("T", " ").slice(0, 19)}`} />
          </Card>
          <div className="flex items-center justify-between mb-2 px-1">
            <h3 className="text-[13px] text-ios-secondary">异常队列</h3>
            {data.exceptions.length > 0 ? (
              <button onClick={exportEx} className="text-ios-blue text-[15px] font-medium">
                导出错误行
              </button>
            ) : null}
          </div>
          <Card className="overflow-hidden">
            {data.exceptions.length === 0 ? (
              <div className="p-6 text-ios-secondary">没有异常行。</div>
            ) : (
              <ul>
                {data.exceptions.map((e, i) => (
                  <li key={e.id} className={`px-4 py-3 ${i ? "border-t border-black/[0.06]" : ""}`}>
                    <div className="text-[15px] font-medium text-ios-red">第 {e.rowNumber} 行 · {e.errorMessage}</div>
                    <pre className="mt-1 text-[12px] text-ios-secondary whitespace-pre-wrap break-all">{e.rawJson}</pre>
                  </li>
                ))}
              </ul>
            )}
          </Card>
        </>
      ) : null}
    </AppShell>
  );
}

function Row({ k, v }: { k: string; v: ReactNode }) {
  return (
    <div className="flex gap-4">
      <div className="w-24 shrink-0 text-ios-secondary">{k}</div>
      <div className="flex-1">{v}</div>
    </div>
  );
}
