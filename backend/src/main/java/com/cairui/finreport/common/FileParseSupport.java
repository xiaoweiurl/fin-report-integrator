package com.cairui.finreport.common;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class FileParseSupport {
    private FileParseSupport() {
    }

    public static String sha256(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static List<Map<String, String>> parse(MultipartFile file, byte[] bytes) throws IOException {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
            return parseExcel(bytes);
        }
        return parseCsv(bytes);
    }

    private static List<Map<String, String>> parseExcel(byte[] bytes) throws IOException {
        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                return List.of();
            }
            List<String> headers = new ArrayList<>();
            short last = headerRow.getLastCellNum();
            for (int i = 0; i < last; i++) {
                headers.add(stripBom(fmt.formatCellValue(headerRow.getCell(i)).trim()));
            }
            List<Map<String, String>> rows = new ArrayList<>();
            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                Map<String, String> map = new LinkedHashMap<>();
                boolean any = false;
                for (int i = 0; i < headers.size(); i++) {
                    String v = fmt.formatCellValue(row.getCell(i)).trim();
                    if (!v.isEmpty()) {
                        any = true;
                    }
                    map.put(headers.get(i), v);
                }
                if (any) {
                    map.put("_row", String.valueOf(r + 1));
                    rows.add(map);
                }
            }
            return rows;
        }
    }

    private static List<Map<String, String>> parseCsv(byte[] bytes) throws IOException {
        Charset cs = detectCharset(bytes);
        try (CSVParser parser = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .get()
                .parse(new InputStreamReader(new ByteArrayInputStream(bytes), cs))) {
            List<String> headers = parser.getHeaderNames();
            List<Map<String, String>> rows = new ArrayList<>();
            int i = 1;
            for (CSVRecord rec : parser) {
                i++;
                Map<String, String> map = new LinkedHashMap<>();
                boolean any = false;
                for (String h : headers) {
                    String v = rec.isMapped(h) ? rec.get(h) : "";
                    if (v != null && !v.isBlank()) {
                        any = true;
                    }
                    map.put(stripBom(h), v == null ? "" : v.trim());
                }
                if (any) {
                    map.put("_row", String.valueOf(i));
                    rows.add(map);
                }
            }
            return rows;
        }
    }

    private static String stripBom(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\uFEFF", "").trim();
    }

    private static Charset detectCharset(byte[] bytes) {
        if (bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
            return StandardCharsets.UTF_8;
        }
        String utf = new String(bytes, StandardCharsets.UTF_8);
        if (!utf.contains("\uFFFD")) {
            return StandardCharsets.UTF_8;
        }
        return Charset.forName("GBK");
    }
}
