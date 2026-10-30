package com.example.spreadsheet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starts the spreadsheet API and its AWS/GCP integration components.
 *
 * <p>The API has two entry points in one pipeline: a client creates a user spreadsheet in AWS S3,
 * and AWS Lambda later downloads the processed result from Google Cloud Storage. See
 * {@code docs/ARCHITECTURE.md} for the high-level design and {@code docs/DESIGN.md} for the
 * low-level design.
 */
@SpringBootApplication
public class SpreadsheetApiApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments forwarded to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(SpreadsheetApiApplication.class, args);
    }
}
