package com.cairui.finreport.importdata;

import com.cairui.finreport.common.ApiException;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/imports")
public class ImportController {
    private final ImportService importService;
    private final ImportBatchRepository batches;
    private final ImportExceptionRepository exceptions;

    public ImportController(ImportService importService, ImportBatchRepository batches, ImportExceptionRepository exceptions) {
        this.importService = importService;
        this.batches = batches;
        this.exceptions = exceptions;
    }

    @GetMapping
    public List<ImportBatch> list(@RequestParam(required = false) String period) {
        if (period == null || period.isBlank()) {
            return batches.findAllByOrderByCreatedAtDesc();
        }
        return batches.findByPeriodOrderByCreatedAtDesc(period);
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        ImportBatch batch = batches.findById(id).orElseThrow(() -> ApiException.notFound("批次不存在"));
        List<ImportExceptionRow> rows = exceptions.findByBatchIdOrderByRowNumberAsc(id);
        return Map.of("batch", batch, "exceptions", rows);
    }

    @PostMapping
    public ImportBatch upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("importType") String importType,
            @RequestParam("period") String period,
            Authentication auth) throws IOException {
        return importService.importFile(file, importType, period, auth.getName());
    }

    @GetMapping("/templates/{importType}")
    public void template(@PathVariable String importType, HttpServletResponse response) throws IOException {
        byte[] bytes = importService.template(importType);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename(importService.templateFilename(importType), StandardCharsets.UTF_8)
                .build()
                .toString());
        response.getOutputStream().write(bytes);
    }

    @GetMapping("/{id}/exceptions/export")
    public void exportExceptions(@PathVariable Long id, HttpServletResponse response) throws IOException {
        ImportBatch batch = batches.findById(id).orElseThrow(() -> ApiException.notFound("批次不存在"));
        List<ImportExceptionRow> rows = exceptions.findByBatchIdOrderByRowNumberAsc(id);
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename("exceptions-batch-" + batch.getId() + ".csv", StandardCharsets.UTF_8)
                .build()
                .toString());
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        try (OutputStreamWriter w = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(w, CSVFormat.DEFAULT.builder()
                     .setHeader("行号", "错误", "原始数据")
                     .get())) {
            for (ImportExceptionRow r : rows) {
                printer.printRecord(r.getRowNumber(), r.getErrorMessage(), r.getRawJson());
            }
        }
    }
}
