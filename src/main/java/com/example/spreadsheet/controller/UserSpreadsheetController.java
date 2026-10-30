package com.example.spreadsheet.controller;

import com.example.spreadsheet.model.CreateSpreadsheetRequest;
import com.example.spreadsheet.model.SpreadsheetCreatedResponse;
import com.example.spreadsheet.service.UserSpreadsheetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP boundary for creating and uploading spreadsheets containing user data.
 *
 * <p>This is the first half of the pipeline: the object it writes to S3 is the input for the
 * external Lambda processor. Validation happens here through {@code @Valid}, so the service only
 * ever receives well-formed users and an invalid request never reaches S3.
 */
@RestController
@RequestMapping("/api/spreadsheets")
public class UserSpreadsheetController {

    private final UserSpreadsheetService spreadsheetService;
    private final String bucket;

    /**
     * Creates the controller.
     *
     * @param spreadsheetService generates and uploads the workbook
     * @param bucket S3 bucket name echoed back to the caller
     */
    public UserSpreadsheetController(
            UserSpreadsheetService spreadsheetService,
            @Value("${app.aws.s3.bucket}") String bucket
    ) {
        this.spreadsheetService = spreadsheetService;
        this.bucket = bucket;
    }

    /**
     * Generates an XLSX workbook and uploads it to the configured S3 bucket.
     *
     * @param request validated user data to include in the workbook
     * @return the destination bucket and generated S3 object key
     */
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public SpreadsheetCreatedResponse createUserSpreadsheet(
            @Valid @RequestBody CreateSpreadsheetRequest request
    ) {
        String objectKey = spreadsheetService.createAndUpload(request.users());
        return new SpreadsheetCreatedResponse(bucket, objectKey);
    }
}
