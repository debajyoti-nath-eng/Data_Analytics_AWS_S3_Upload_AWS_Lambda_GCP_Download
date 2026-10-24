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

@RestController
@RequestMapping("/api/spreadsheets")
public class UserSpreadsheetController {

    private final UserSpreadsheetService spreadsheetService;
    private final String bucket;

    public UserSpreadsheetController(
            UserSpreadsheetService spreadsheetService,
            @Value("${app.aws.s3.bucket}") String bucket
    ) {
        this.spreadsheetService = spreadsheetService;
        this.bucket = bucket;
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public SpreadsheetCreatedResponse createUserSpreadsheet(
            @Valid @RequestBody CreateSpreadsheetRequest request
    ) {
        String objectKey = spreadsheetService.createAndUpload(request.users());
        return new SpreadsheetCreatedResponse(bucket, objectKey);
    }
}
