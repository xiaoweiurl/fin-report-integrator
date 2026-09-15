import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "财报系统",
  description: "财务数据导入、清洗与报表",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="zh-CN">
      <body className="antialiased">{children}</body>
    </html>
  );
}
