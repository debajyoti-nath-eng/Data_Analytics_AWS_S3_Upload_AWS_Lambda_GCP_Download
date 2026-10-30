/**
 * Business logic: workbook generation, the S3 upload, and the GCS download. Services translate
 * cloud SDK failures into {@link org.springframework.web.server.ResponseStatusException}, so no
 * SDK exception type reaches the controllers.
 */
package com.example.spreadsheet.service;
