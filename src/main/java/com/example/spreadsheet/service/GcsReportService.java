package com.example.spreadsheet.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Reads processed report objects from the deployment-configured GCS bucket.
 *
 * <p>The bucket comes from configuration and never from the request, so a caller can name an object
 * but cannot reach a bucket outside this deployment. Any object in that bucket can be named,
 * which is why the endpoint must sit behind deployment-level access control.
 */
@Service
public class GcsReportService {

    private static final Logger log = LoggerFactory.getLogger(GcsReportService.class);
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final Storage storage;
    private final String bucket;

    /**
     * Creates the service.
     *
     * @param storage GCS client
     * @param bucket report bucket name
     */
    public GcsReportService(Storage storage, @Value("${app.gcp.gcs.bucket}") String bucket) {
        this.storage = storage;
        this.bucket = bucket;
    }

    /**
     * Fetches an object and its download metadata from GCS.
     *
     * <p>This makes two GCS calls: a metadata lookup, which returns {@code null} for a missing
     * object, and then the content read. The whole object is held in memory.
     *
     * @param objectName object key in the configured report bucket
     * @return the report filename, content type, and bytes
     * @throws org.springframework.web.server.ResponseStatusException with 404 if absent,
     *         or 502 if GCS cannot be reached or refuses the request
     */
    public DownloadedReport download(String objectName) {
        try {
            Blob blob = storage.get(BlobId.of(bucket, objectName));
            if (blob == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "The requested report was not found in Google Cloud Storage"
                );
            }

            // GCS names are flat keys, so the last path segment becomes the download filename.
            // A name ending in "/" has no such segment and falls back to a generic one.
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
            // The HTTP response deliberately omits the cause, so this log line is the only record.
            log.error("GCS download failed for gs://{}/{}", bucket, objectName, exception);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not download the report from Google Cloud Storage",
                    exception
            );
        }
    }

    /**
     * Contents and response metadata for a report retrieved from GCS.
     *
     * @param filename download filename, taken from the last segment of the object name
     * @param contentType content type stored with the object, or the binary default if absent
     * @param content complete object bytes
     */
    public record DownloadedReport(String filename, String contentType, byte[] content) {
    }
}
