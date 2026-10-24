package com.example.spreadsheet.service;

import com.example.spreadsheet.model.UserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;
import java.util.UUID;

@Service
public class UserSpreadsheetService {

    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final S3Client s3Client;
    private final SpreadsheetGenerator spreadsheetGenerator;
    private final String bucket;

    public UserSpreadsheetService(
            S3Client s3Client,
            SpreadsheetGenerator spreadsheetGenerator,
            @Value("${app.aws.s3.bucket}") String bucket
    ) {
        this.s3Client = s3Client;
        this.spreadsheetGenerator = spreadsheetGenerator;
        this.bucket = bucket;
    }

    public String createAndUpload(List<UserInfo> users) {
        byte[] spreadsheet = spreadsheetGenerator.generate(users);
        String objectKey = "users/" + UUID.randomUUID() + ".xlsx";
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(XLSX_CONTENT_TYPE)
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromBytes(spreadsheet));
            return objectKey;
        } catch (SdkException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not upload the spreadsheet to AWS S3",
                    exception
            );
        }
    }
}
