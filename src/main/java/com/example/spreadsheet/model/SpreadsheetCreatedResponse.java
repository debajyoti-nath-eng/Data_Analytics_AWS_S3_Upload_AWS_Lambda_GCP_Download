package com.example.spreadsheet.model;

/**
 * Identifies the uploaded spreadsheet in AWS S3.
 *
 * @param bucket S3 bucket holding the workbook
 * @param objectKey generated key, in the form {@code users/{uuid}.xlsx}
 */
public record SpreadsheetCreatedResponse(String bucket, String objectKey) {
}
