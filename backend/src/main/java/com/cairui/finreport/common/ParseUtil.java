package com.cairui.finreport.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public final class ParseUtil {
    private static final List<DateTimeFormatter> DATES = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy/M/d"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy-M-d"),
            DateTimeFormatter.ofPattern("yyyyMMdd"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("yyyy年M月d日")
    );

    private ParseUtil() {
    }

    public static BigDecimal amount(String raw) {
        if (raw == null) {
            return BigDecimal.ZERO;
        }
        String n = raw.replace("￥", "")
                .replace("¥", "")
                .replace(",", "")
                .replace("，", "")
                .replace(" ", "")
                .replace("\u00A0", "")
                .trim();
        if (n.isEmpty() || "-".equals(n) || "—".equals(n)) {
            return BigDecimal.ZERO;
        }
        if (n.startsWith("(") && n.endsWith(")")) {
            n = "-" + n.substring(1, n.length() - 1);
        }
        try {
            return new BigDecimal(n).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            throw ApiException.badRequest("无法解析金额: " + raw);
        }
    }

    public static BigDecimal amountOrInvalid(String raw, List<String> errors, String field) {
        try {
            return amount(raw);
        } catch (ApiException ex) {
            errors.add(field + "金额无法解析: " + raw);
            return BigDecimal.ZERO;
        }
    }

    public static LocalDate date(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String s = raw.trim();
        if (s.contains(" ")) {
            s = s.split(" ")[0];
        }
        if (s.matches("\\d+(\\.\\d+)?")) {
            try {
                double serial = Double.parseDouble(s);
                if (serial > 20000 && serial < 80000) {
                    return LocalDate.of(1899, 12, 30).plusDays((long) serial);
                }
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        for (DateTimeFormatter fmt : DATES) {
            try {
                return LocalDate.parse(s, fmt);
            } catch (DateTimeParseException ignored) {
                // try next
            }
        }
        return null;
    }

    public static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public static String headerKey(String header) {
        return header == null ? "" : header.replace(" ", "").replace("\u00A0", "").trim();
    }
}
