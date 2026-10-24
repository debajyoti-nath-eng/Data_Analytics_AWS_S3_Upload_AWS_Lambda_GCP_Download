package com.example.spreadsheet.model;

/** Identifies the uploaded spreadsheet in AWS S3. */
public record SpreadsheetCreatedResponse(String bucket, String objectKey) {
}
