package com.example.spreadsheet.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GcsReportService {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final Storage storage;
    private final String bucket;

    public GcsReportService(Storage storage, @Value("${app.gcp.gcs.bucket}") String bucket) {
        this.storage = storage;
        this.bucket = bucket;
    }

    public DownloadedReport download(String objectName) {
        try {
            Blob blob = storage.get(BlobId.of(bucket, objectName));
            if (blob == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "The requested report was not found in Google Cloud Storage"
                );
            }

            String filename = objectName.substring(objectName.lastIndexOf('/') + 1);
            if (filename.isBlank()) {
                filename = "report";
            }
            String contentType = blob.getContentType();
            return new DownloadedReport(
                    filename,
                    contentType == null ? DEFAULT_CONTENT_TYPE : contentType,
                    blob.getContent()
            );
        } catch (StorageException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not download the report from Google Cloud Storage",
                    exception
            );
        }
    }

    public record DownloadedReport(String filename, String contentType, byte[] content) {
    }
}
