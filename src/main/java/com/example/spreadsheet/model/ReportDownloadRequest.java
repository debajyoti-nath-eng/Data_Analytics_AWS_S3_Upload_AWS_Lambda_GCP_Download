package com.example.spreadsheet.model;

import jakarta.validation.constraints.NotBlank;

public record ReportDownloadRequest(
        @NotBlank String objectName
) {
}
