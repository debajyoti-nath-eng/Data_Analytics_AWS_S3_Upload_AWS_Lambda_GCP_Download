package com.example.spreadsheet.config;

import com.google.cloud.NoCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Creates cloud SDK clients using their standard credential provider chains.
 *
 * <p>Both clients are singleton beans, so services receive them by constructor injection and tests
 * can replace them with mocks. Neither client resolves credentials or contacts its cloud until the
 * first request, which means a missing credential appears as a 502 and not as a startup failure.
 */
@Configuration
public class CloudStorageConfiguration {

    /**
     * Creates the AWS S3 client for the configured region.
     *
     * <p>The SDK also reads {@code AWS_ENDPOINT_URL_S3}, which is how a local S3 emulator is
     * selected without any code path here.
     *
     * @param region AWS region identifier
     * @return an S3 client that resolves credentials from the runtime environment
     */
    @Bean
    S3Client s3Client(@Value("${app.aws.region}") String region) {
        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }

    /**
     * Creates the Google Cloud Storage client.
     *
     * <p>With no endpoint configured the client targets Google Cloud using Application Default
     * Credentials, which is the production path. A configured endpoint targets a local emulator
     * instead, and credentials are disabled so that real tokens never reach it. The endpoint has
     * to be set here because this client does not honor {@code STORAGE_EMULATOR_HOST}.
     *
     * @param endpoint emulator base URL, or blank to use Google Cloud
     * @return a GCS client for the selected target
     */
    @Bean
    Storage gcsStorage(@Value("${app.gcp.gcs.endpoint}") String endpoint) {
        if (endpoint.isBlank()) {
            return StorageOptions.getDefaultInstance().getService();
        }
        return StorageOptions.newBuilder()
                .setHost(endpoint)
                .setProjectId("local-emulator")
                .setCredentials(NoCredentials.getInstance())
                .build()
                .getService();
    }
}
