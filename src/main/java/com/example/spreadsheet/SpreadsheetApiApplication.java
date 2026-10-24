package com.example.spreadsheet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starts the spreadsheet API and its AWS/GCP integration components.
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
