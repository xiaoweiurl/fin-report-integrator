"use client";

import AppShell, { Card, PageTitle } from "@/components/AppShell";
import { ChevronRight } from "lucide-react";
import Link from "next/link";

export default function ReportsIndex() {
  return (
    <AppShell>
      <PageTitle title="财务报表" subtitle="基于本期科目余额与公式配置生成" />
      <Card className="overflow-hidden">
        <Item href="/reports/trial-balance" title="科目余额表 / 试算平衡" desc="借方合计应等于贷方合计" />
        <Item href="/reports/balance-sheet" title="资产负债表" desc="资产 = 负债 + 所有者权益" />
        <Item href="/reports/income" title="利润表" desc="营业收入至净利润" last />
      </Card>
    </AppShell>
  );
}

function Item({ href, title, desc, last }: { href: string; title: string; desc: string; last?: boolean }) {
  return (
    <Link href={href} className={`pressable flex items-center px-4 py-3 ${last ? "" : "border-b border-black/[0.06]"}`}>
      <div className="flex-1">
        <div className="text-[17px]">{title}</div>
        <div className="text-[13px] text-ios-secondary">{desc}</div>
      </div>
      <ChevronRight className="text-ios-secondary" size={18} />
    </Link>
  );
}
