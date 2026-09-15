"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { ReactNode, useEffect, useState } from "react";
import {
  BookOpen,
  FileSpreadsheet,
  Home,
  Landmark,
  LogOut,
  ScrollText,
  Users,
} from "lucide-react";
import { getPeriod, getToken, setPeriod, setToken } from "@/lib/api";

const NAV = [
  { href: "/", label: "首页", icon: Home },
  { href: "/import", label: "导入", icon: FileSpreadsheet },
  { href: "/master", label: "主数据", icon: Users },
  { href: "/reports", label: "报表", icon: BookOpen },
  { href: "/recon", label: "对账", icon: Landmark },
  { href: "/audit", label: "审计", icon: ScrollText },
];

export default function AppShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const [ready, setReady] = useState(false);
  const [period, setPeriodState] = useState("2025-12");

  useEffect(() => {
    if (!getToken()) {
      router.replace("/login");
      return;
    }
    setPeriodState(getPeriod());
    setReady(true);
  }, [router]);

  function onPeriod(v: string) {
    setPeriod(v);
    setPeriodState(v);
    window.dispatchEvent(new CustomEvent("period-change", { detail: v }));
  }

  function logout() {
    setToken(null);
    router.replace("/login");
  }

  if (!ready) {
    return (
      <div className="min-h-screen bg-ios-bg flex items-center justify-center text-ios-secondary">
        正在进入…
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-ios-bg pb-[calc(5.5rem+env(safe-area-inset-bottom,0px))] md:pb-8 md:pl-[240px]">
      <aside className="hidden md:flex fixed inset-y-0 left-0 w-[240px] flex-col border-r border-black/5 bg-white/70 backdrop-blur-2xl">
        <div className="px-6 pt-8 pb-6">
          <div className="text-[13px] font-semibold tracking-[0.18em] text-ios-blue">CAIRUI</div>
          <h1 className="mt-1 text-[22px] font-semibold tracking-tight">财报系统</h1>
        </div>
        <nav className="px-3 flex-1 space-y-1">
          {NAV.map((item) => {
            const active = item.href === "/" ? pathname === "/" : pathname.startsWith(item.href);
            const Icon = item.icon;
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`pressable flex items-center gap-3 rounded-2xl px-3 h-12 text-[16px] ${
                  active ? "bg-ios-blue text-white" : "text-black/80 hover:bg-black/[0.04]"
                }`}
              >
                <Icon size={20} strokeWidth={1.8} />
                {item.label}
              </Link>
            );
          })}
        </nav>
        <button
          onClick={logout}
          className="pressable m-3 mb-6 flex items-center gap-3 rounded-2xl px-3 h-12 text-[15px] text-ios-red"
        >
          <LogOut size={18} />
          退出登录
        </button>
      </aside>

      <header className="sticky top-0 z-20 bg-ios-bg/75 backdrop-blur-xl border-b border-black/[0.06] pt-safe">
        <div className="max-w-5xl mx-auto px-4 md:px-8 h-14 flex items-center justify-between">
          <div className="md:hidden text-[17px] font-semibold">财报系统</div>
          <div className="hidden md:block text-[13px] text-ios-secondary">会计期间</div>
          <label className="flex items-center gap-2 bg-white rounded-full px-3 h-9 ios-shadow text-[15px]">
            <span className="text-ios-secondary text-[13px]">期间</span>
            <input
              type="month"
              value={period}
              onChange={(e) => onPeriod(e.target.value)}
              className="bg-transparent outline-none font-medium"
            />
          </label>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-4 md:px-8 pt-5">{children}</main>

      <nav className="md:hidden fixed bottom-0 inset-x-0 bg-white/80 backdrop-blur-xl border-t border-black/10 pb-safe">
        <div className="grid grid-cols-6 h-[64px]">
          {NAV.map((item) => {
            const active = item.href === "/" ? pathname === "/" : pathname.startsWith(item.href);
            const Icon = item.icon;
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`flex flex-col items-center justify-center gap-0.5 text-[10px] ${
                  active ? "text-ios-blue" : "text-ios-secondary"
                }`}
              >
                <Icon size={22} strokeWidth={active ? 2.2 : 1.7} />
                {item.label}
              </Link>
            );
          })}
        </div>
      </nav>
    </div>
  );
}

export function PageTitle({ title, subtitle }: { title: string; subtitle?: string }) {
  return (
    <div className="mb-5">
      <h2 className="text-[34px] leading-tight font-bold tracking-tight">{title}</h2>
      {subtitle ? <p className="mt-1 text-[15px] text-ios-secondary">{subtitle}</p> : null}
    </div>
  );
}

export function Card({
  children,
  className = "",
  onClick,
}: {
  children: ReactNode;
  className?: string;
  onClick?: () => void;
}) {
  const cls = `bg-white rounded-[20px] ios-shadow ${className}`;
  if (onClick) {
    return (
      <button type="button" onClick={onClick} className={`pressable text-left w-full ${cls}`}>
        {children}
      </button>
    );
  }
  return <div className={cls}>{children}</div>;
}

export function Badge({
  tone = "gray",
  children,
}: {
  tone?: "gray" | "blue" | "green" | "red" | "orange";
  children: ReactNode;
}) {
  const map = {
    gray: "bg-ios-fill text-black/70",
    blue: "bg-[#007AFF]/12 text-ios-blue",
    green: "bg-[#34C759]/14 text-[#248a3d]",
    red: "bg-[#FF3B30]/12 text-ios-red",
    orange: "bg-[#FF9500]/14 text-[#c93400]",
  };
  return (
    <span className={`inline-flex items-center h-6 px-2.5 rounded-full text-[12px] font-medium ${map[tone]}`}>
      {children}
    </span>
  );
}

export function PrimaryButton({
  children,
  onClick,
  type = "button",
  disabled,
  tone = "blue",
  className = "",
}: {
  children: ReactNode;
  onClick?: () => void;
  type?: "button" | "submit";
  disabled?: boolean;
  tone?: "blue" | "green" | "red";
  className?: string;
}) {
  const bg = tone === "green" ? "bg-ios-green" : tone === "red" ? "bg-ios-red" : "bg-ios-blue";
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={`pressable ${bg} text-white rounded-full h-12 px-6 text-[17px] font-semibold disabled:opacity-40 ${className}`}
    >
      {children}
    </button>
  );
}

export function useLivePeriod() {
  const [period, setP] = useState(getPeriod());
  useEffect(() => {
    const h = (e: Event) => setP((e as CustomEvent<string>).detail);
    window.addEventListener("period-change", h);
    return () => window.removeEventListener("period-change", h);
  }, []);
  return period;
}

export function statusTone(status: string): "green" | "red" | "orange" | "gray" {
  if (status === "SUCCESS") return "green";
  if (status === "FAILED") return "red";
  if (status === "PARTIAL") return "orange";
  return "gray";
}

export function statusLabel(status: string) {
  return (
    {
      SUCCESS: "成功",
      FAILED: "失败",
      PARTIAL: "部分成功",
      PENDING: "处理中",
    } as Record<string, string>
  )[status] ?? status;
}
