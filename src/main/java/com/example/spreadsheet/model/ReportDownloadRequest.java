package com.example.spreadsheet.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body identifying a processed report within the configured GCS bucket.
 *
 * @param objectName full object name, for example {@code processed/report.xlsx}
 */
public record ReportDownloadRequest(
        @NotBlank String objectName
) {
}
