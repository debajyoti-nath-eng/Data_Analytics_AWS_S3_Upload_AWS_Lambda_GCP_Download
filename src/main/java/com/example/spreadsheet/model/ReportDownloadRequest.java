package com.example.spreadsheet.model;

import jakarta.validation.constraints.NotBlank;

/** Request body identifying a processed report within the configured GCS bucket. */
public record ReportDownloadRequest(
        @NotBlank String objectName
) {
}
