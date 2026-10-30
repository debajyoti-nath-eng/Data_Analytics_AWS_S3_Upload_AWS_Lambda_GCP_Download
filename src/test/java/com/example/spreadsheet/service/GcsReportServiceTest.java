package com.example.spreadsheet.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GcsReportServiceTest {

    private final Storage storage = mock(Storage.class);
    private final GcsReportService service = new GcsReportService(storage, "report-bucket");

    @Test
    void returnsReportBytesAndMetadataFromConfiguredBucket() {
        Blob blob = mock(Blob.class);
        when(blob.getContentType()).thenReturn("text/csv");
        when(blob.getContent()).thenReturn(new byte[] {1, 2, 3});
        when(storage.get(BlobId.of("report-bucket", "processed/report.csv"))).thenReturn(blob);

        GcsReportService.DownloadedReport report = service.download("processed/report.csv");

        assertThat(report.filename()).isEqualTo("report.csv");
        assertThat(report.contentType()).isEqualTo("text/csv");
        assertThat(report.content()).containsExactly(1, 2, 3);
    }

    @Test
    void fallsBackWhenMetadataOrFilenameIsMissing() {
        Blob blob = mock(Blob.class);
        when(blob.getContent()).thenReturn(new byte[0]);
        when(storage.get(BlobId.of("report-bucket", "processed/"))).thenReturn(blob);

        GcsReportService.DownloadedReport report = service.download("processed/");

        assertThat(report.filename()).isEqualTo("report");
        assertThat(report.contentType()).isEqualTo("application/octet-stream");
    }

    @Test
    void mapsMissingObjectToNotFound() {
        assertThatThrownBy(() -> service.download("processed/missing.xlsx"))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void mapsGcsFailureToBadGateway() {
        when(storage.get(any(BlobId.class))).thenThrow(new StorageException(503, "unavailable"));

        assertThatThrownBy(() -> service.download("processed/report.xlsx"))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY));
    }
}
