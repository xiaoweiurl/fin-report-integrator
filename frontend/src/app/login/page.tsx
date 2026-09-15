"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { api, setToken } from "@/lib/api";
import { PrimaryButton } from "@/components/AppShell";

export default function LoginPage() {
  const router = useRouter();
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("admin123");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      const res = await api<{ token: string }>("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ username, password }),
      });
      setToken(res.token);
      router.replace("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "登录失败");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-ios-bg flex items-center justify-center px-6 pt-safe pb-safe">
      <div className="w-full max-w-[400px]">
        <div className="text-center mb-8">
          <div className="mx-auto mb-5 h-16 w-16 rounded-[22px] bg-ios-blue text-white flex items-center justify-center text-[28px] font-bold ios-shadow">
            财
          </div>
          <h1 className="text-[34px] font-bold tracking-tight">财报系统</h1>
          <p className="mt-2 text-[15px] text-ios-secondary">导入账表 · 试算平衡 · 银行对账</p>
        </div>
        <form onSubmit={onSubmit} className="bg-white rounded-[20px] ios-shadow overflow-hidden">
          <label className="flex items-center gap-3 px-4 h-14 border-b border-black/[0.08]">
            <span className="w-16 text-[15px] text-ios-secondary">账号</span>
            <input
              className="flex-1 outline-none text-[17px] bg-transparent"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
            />
          </label>
          <label className="flex items-center gap-3 px-4 h-14">
            <span className="w-16 text-[15px] text-ios-secondary">密码</span>
            <input
              type="password"
              className="flex-1 outline-none text-[17px] bg-transparent"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
            />
          </label>
          {error ? <p className="px-4 py-3 text-ios-red text-[14px]">{error}</p> : null}
          <div className="p-4">
            <PrimaryButton type="submit" disabled={loading} className="w-full">
              {loading ? "正在登录…" : "登录"}
            </PrimaryButton>
          </div>
        </form>
        <p className="mt-6 text-center text-[13px] text-ios-secondary">演示账号 admin / admin123</p>
      </div>
    </div>
  );
}
