package com.example.spreadsheet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Starts the full context, including the real AWS and GCP SDK clients, to catch wiring and
 * classpath conflicts. Neither client contacts its cloud until the first request.
 */
@SpringBootTest(properties = {
        "app.aws.s3.bucket=test-input-bucket",
        "app.gcp.gcs.bucket=test-report-bucket"
})
class SpreadsheetApiApplicationTests {

    @Test
    void contextLoads() {
    }
}
