package com.example.spreadsheet.service;

import com.example.spreadsheet.model.UserInfo;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserSpreadsheetServiceTest {

    private static final List<UserInfo> USERS =
            List.of(new UserInfo("Ada", "Lovelace", "ada@example.com", null));

    private final S3Client s3Client = mock(S3Client.class);
    private final UserSpreadsheetService service =
            new UserSpreadsheetService(s3Client, new SpreadsheetGenerator(), "input-bucket");

    @Test
    void uploadsWorkbookUnderGeneratedKey() {
        String objectKey = service.createAndUpload(USERS);

        ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(request.capture(), any(RequestBody.class));
        assertThat(objectKey).matches("users/[0-9a-f-]{36}\\.xlsx");
        assertThat(request.getValue().bucket()).isEqualTo("input-bucket");
        assertThat(request.getValue().key()).isEqualTo(objectKey);
        assertThat(request.getValue().contentType())
                .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @Test
    void generatesDistinctKeyForEachUpload() {
        assertThat(service.createAndUpload(USERS)).isNotEqualTo(service.createAndUpload(USERS));
    }

    @Test
    void mapsS3FailureToBadGateway() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(SdkClientException.create("connection refused"));

        assertThatThrownBy(() -> service.createAndUpload(USERS))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY));
    }
}
