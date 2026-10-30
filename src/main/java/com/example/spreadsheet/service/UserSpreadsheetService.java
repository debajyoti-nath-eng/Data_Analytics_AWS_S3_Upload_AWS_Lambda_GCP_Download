package com.example.spreadsheet.service;

import com.example.spreadsheet.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Coordinates spreadsheet generation and upload to the configured S3 bucket.
 *
 * <p>Writing the object is the service's last step and its handoff to the pipeline. An S3 event or
 * workflow configured outside this application is what starts the Lambda processor.
 */
@Service
public class UserSpreadsheetService {

    private static final Logger log = LoggerFactory.getLogger(UserSpreadsheetService.class);
    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final S3Client s3Client;
    private final SpreadsheetGenerator spreadsheetGenerator;
    private final String bucket;

    /**
     * Creates the service.
     *
     * @param s3Client S3 client
     * @param spreadsheetGenerator converts users into workbook bytes
     * @param bucket destination bucket name
     */
    public UserSpreadsheetService(
            S3Client s3Client,
            SpreadsheetGenerator spreadsheetGenerator,
            @Value("${app.aws.s3.bucket}") String bucket
    ) {
        this.s3Client = s3Client;
        this.spreadsheetGenerator = spreadsheetGenerator;
        this.bucket = bucket;
    }

    /**
     * Creates a unique XLSX object under the {@code users/} key prefix and uploads it.
     *
     * <p>The UUID key means two requests can never overwrite each other, and it keeps user data out
     * of the object name. It also means a client retry creates a second object, not a replacement.
     *
     * @param users non-empty user list to include in the spreadsheet
     * @return the generated S3 object key
     * @throws org.springframework.web.server.ResponseStatusException if S3 rejects the upload
     */
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
            // The HTTP response deliberately omits the cause, so this log line is the only record.
            log.error("S3 upload failed for s3://{}/{}", bucket, objectKey, exception);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not upload the spreadsheet to AWS S3",
                    exception
            );
        }
    }
}
