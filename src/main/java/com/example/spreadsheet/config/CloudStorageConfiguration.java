package com.example.spreadsheet.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Creates cloud SDK clients using their standard credential provider chains.
 */
@Configuration
public class CloudStorageConfiguration {

    /**
     * Creates the AWS S3 client for the configured region.
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
     * Creates the Google Cloud Storage client using Application Default Credentials.
     *
     * @return a GCS client configured from the runtime environment
     */
    @Bean
    Storage gcsStorage() {
        return StorageOptions.getDefaultInstance().getService();
    }
}
