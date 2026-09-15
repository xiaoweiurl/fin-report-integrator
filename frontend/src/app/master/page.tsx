"use client";

import AppShell, { Card, PageTitle } from "@/components/AppShell";
import Link from "next/link";
import { ChevronRight } from "lucide-react";

export default function MasterPage() {
  return (
    <AppShell>
      <PageTitle title="主数据" subtitle="科目表与往来单位，支持手工维护或文件导入" />
      <Card className="overflow-hidden">
        <Link href="/master/accounts" className="pressable flex items-center px-4 h-14 border-b border-black/[0.06]">
          <div className="flex-1">
            <div className="text-[17px]">会计科目</div>
            <div className="text-[13px] text-ios-secondary">小企业准则示范科目</div>
          </div>
          <ChevronRight className="text-ios-secondary" size={18} />
        </Link>
        <Link href="/master/partners" className="pressable flex items-center px-4 h-14">
          <div className="flex-1">
            <div className="text-[17px]">往来单位</div>
            <div className="text-[13px] text-ios-secondary">客户、供应商与银行</div>
          </div>
          <ChevronRight className="text-ios-secondary" size={18} />
        </Link>
      </Card>
    </AppShell>
  );
}
