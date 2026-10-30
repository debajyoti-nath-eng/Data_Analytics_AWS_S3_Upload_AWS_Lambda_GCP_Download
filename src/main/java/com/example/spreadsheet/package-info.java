/**
 * Spring Boot API that creates user spreadsheets in AWS S3 and serves processed reports from
 * Google Cloud Storage to AWS Lambda.
 *
 * <p>Dependencies point one way: {@code controller} calls {@code service}, both use {@code model},
 * and {@code config} supplies the cloud clients that {@code service} needs.
 */
package com.example.spreadsheet;
