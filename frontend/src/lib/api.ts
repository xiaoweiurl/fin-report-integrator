const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "http://127.0.0.1:18443";

const TOKEN_KEY = "finreport.token";
const PERIOD_KEY = "finreport.period";

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string | null) {
  if (typeof window === "undefined") return;
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

export function getPeriod(): string {
  if (typeof window === "undefined") return "2025-12";
  return localStorage.getItem(PERIOD_KEY) ?? "2025-12";
}

export function setPeriod(period: string) {
  localStorage.setItem(PERIOD_KEY, period);
}

class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  const token = getToken();
  if (token) headers.set("Authorization", `Bearer ${token}`);
  if (init.body && !(init.body instanceof FormData) && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  const res = await fetch(`${API_BASE}${path}`, { ...init, headers });
  if (res.status === 401) {
    setToken(null);
    if (typeof window !== "undefined" && !path.startsWith("/api/auth/login")) {
      window.location.href = "/login";
    }
  }
  if (!res.ok) {
    let message = `请求失败 (${res.status})`;
    try {
      const data = await res.json();
      if (data?.error) message = data.error;
    } catch {
      /* ignore */
    }
    throw new ApiError(res.status, message);
  }
  if (res.status === 204) return undefined as T;
  const text = await res.text();
  if (!text) return undefined as T;
  return JSON.parse(text) as T;
}

export function downloadUrl(path: string) {
  const token = getToken();
  return fetch(`${API_BASE}${path}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
}

export function formatAmount(n: number | string | null | undefined): string {
  if (n === null || n === undefined || n === "") return "—";
  const v = typeof n === "string" ? Number(n) : n;
  if (Number.isNaN(v)) return "—";
  return v.toLocaleString("zh-CN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

export const IMPORT_TYPES: { value: string; label: string; hint: string; sample: string }[] = [
  { value: "ACCOUNT_BALANCE", label: "科目余额表", hint: "试算平衡与报表取数来源", sample: "科目余额表-2025-12.xlsx" },
  { value: "BANK_STATEMENT", label: "银行流水", hint: "银行对账单交易明细", sample: "银行流水-2025-12.xlsx" },
  { value: "CASH_JOURNAL", label: "企业日记账", hint: "银行存款科目日记账", sample: "企业日记账-2025-12.xlsx" },
  { value: "CHART_OF_ACCOUNTS", label: "会计科目", hint: "科目主数据增补导入", sample: "会计科目-增补.csv" },
  { value: "PARTNER", label: "往来单位", hint: "客户 / 供应商", sample: "往来单位.csv" },
];
