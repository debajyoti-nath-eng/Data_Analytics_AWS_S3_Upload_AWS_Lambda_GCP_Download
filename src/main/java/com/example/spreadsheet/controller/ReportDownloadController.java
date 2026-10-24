package com.example.spreadsheet.controller;

import com.example.spreadsheet.model.ReportDownloadRequest;
import com.example.spreadsheet.service.GcsReportService;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/reports")
public class ReportDownloadController {

    private final GcsReportService reportService;

    public ReportDownloadController(GcsReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/download")
    public ResponseEntity<ByteArrayResource> downloadReport(
            @Valid @RequestBody ReportDownloadRequest request
    ) {
        GcsReportService.DownloadedReport report =
                reportService.download(request.objectName());
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(report.filename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(report.contentType()))
                .contentLength(report.content().length)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new ByteArrayResource(report.content()));
    }
}
