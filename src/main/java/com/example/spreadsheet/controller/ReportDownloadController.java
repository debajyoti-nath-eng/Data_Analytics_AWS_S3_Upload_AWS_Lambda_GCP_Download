package com.example.spreadsheet.controller;

import com.example.spreadsheet.model.ReportDownloadRequest;
import com.example.spreadsheet.service.GcsReportService;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * HTTP boundary for downloading a processed report from the configured GCS bucket.
 *
 * <p>This is the second half of the pipeline. AWS Lambda calls this endpoint after it has written
 * the processed spreadsheet to GCS, so the API is invoked by Lambda and never invokes it. The
 * controller only translates between HTTP and {@link GcsReportService}; a
 * {@link org.springframework.web.server.ResponseStatusException} raised by the service passes
 * through unchanged and becomes the response status.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportDownloadController {

    private final GcsReportService reportService;

    /**
     * Creates the controller.
     *
     * @param reportService reads report objects from GCS
     */
    public ReportDownloadController(GcsReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Returns the requested GCS object as an attachment.
     *
     * <p>{@code POST} is used so that the object name travels in a validated JSON body rather than
     * in the URL, where a {@code /}-separated name would need encoding and would reach access logs.
     *
     * @param request GCS object name, normally supplied by the processing Lambda
     * @return report bytes and download headers
     */
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
                .contentType(toMediaType(report.contentType()))
                .contentLength(report.content().length)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new ByteArrayResource(report.content()));
    }

    /**
     * Parses the content type stored with the object. GCS keeps this value as free text, so a
     * value that is not a valid media type must not fail an otherwise good download.
     *
     * @param contentType content type from GCS metadata
     * @return the parsed media type, or {@code application/octet-stream} if it is invalid
     */
    private static MediaType toMediaType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (InvalidMediaTypeException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
