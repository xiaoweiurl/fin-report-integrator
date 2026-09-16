-- 在 postgres 库下执行一次（不要连到 finreport 再执行本文件）
--   psql -U postgres -d postgres -f sql/00_create_database.sql
-- 若库已存在会跳过。
-- 用 pgAdmin 时请改连到 postgres 库后手工执行：
--   CREATE DATABASE finreport WITH ENCODING 'UTF8' TEMPLATE template0;

SELECT 'CREATE DATABASE finreport WITH ENCODING ''UTF8'' TEMPLATE template0'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'finreport')\gexec
