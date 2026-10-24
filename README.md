# Spreadsheet Cloud API

Spring Boot service for creating user-information spreadsheets in AWS S3 and returning processed report files from Google Cloud Storage (GCS).

> This repository contains the API, not the AWS Lambda or the spreadsheet-processing pipeline. The Lambda/process is expected to read the original object from S3, write its report to GCS, and then call this API to download that report.

For component responsibilities, sequence diagrams, cloud boundaries, and operational considerations, see [Architecture](docs/ARCHITECTURE.md).

## Requirements

- Java 17 or later
- Maven 3.9 or later
- AWS credentials authorized to upload to the configured S3 bucket
- Google Cloud Application Default Credentials (ADC) authorized to read from the configured GCS report bucket

The AWS SDK uses its default credential provider chain. The Google Cloud client uses ADC. Assign only the required permissions to the runtime identity, and do not commit credentials to the repository.

## Configuration and local run

| Environment variable | Required | Default | Purpose |
|---|---:|---|---|
| `AWS_S3_BUCKET` | Yes | — | Destination bucket for generated user spreadsheets |
| `GCS_REPORT_BUCKET` | Yes | — | Bucket from which the API reads processed reports |
| `AWS_REGION` | No | `us-east-1` | AWS region used by the S3 client |
| `PORT` | No | `8080` | HTTP server port |

In PowerShell:

```powershell
$env:AWS_S3_BUCKET = "your-s3-bucket"
$env:GCS_REPORT_BUCKET = "your-gcs-report-bucket"
$env:AWS_REGION = "us-east-1"
# Configure AWS credentials using a profile, environment, or workload identity.
# Configure Google Cloud ADC for the runtime identity.
mvn spring-boot:run
```

Run tests with `mvn test`.

## REST API

### Create and upload a user spreadsheet

`POST /api/spreadsheets/users`

The request must contain a non-empty `users` list. `firstName`, `lastName`, and a valid `email` are required for each user; `phone` is optional.

```json
{
  "users": [
    {
      "firstName": "Ada",
      "lastName": "Lovelace",
      "email": "ada@example.com",
      "phone": "+1-555-0100"
    }
  ]
}
```

On success, the API responds with `201 Created` and returns the S3 bucket and generated object key:

```json
{
  "bucket": "your-s3-bucket",
  "objectKey": "users/3a11ea14-5d3b-459b-aa3c-38fcbad89636.xlsx"
}
```

The object key contains a generated UUID, so a successful request creates a new object under the `users/` prefix.

### Download a processed report from GCS

`POST /api/reports/download`

After the report exists in GCS, the Lambda or another authorized caller requests it by object name. The bucket is selected by `GCS_REPORT_BUCKET`, not supplied by the caller.

```json
{
  "objectName": "processed/3a11ea14-5d3b-459b-aa3c-38fcbad89636.xlsx"
}
```

The response body contains the report bytes with attachment and content-type headers based on GCS object metadata. The Lambda caller receives these bytes in the HTTP response; this endpoint does not trigger Lambda, write the report to GCS, or copy the report to S3.

### Validation and errors

- Invalid or incomplete JSON input is rejected by Spring validation with `400 Bad Request`.
- A report object that does not exist returns `404 Not Found`.
- Cloud storage failures are reported as `502 Bad Gateway`.

## Security and deployment notes

The report endpoint has no built-in authentication. In a deployed environment, restrict access using an authenticated API gateway, network policy, or equivalent controls; do not expose it publicly without protection. The runtime AWS identity needs S3 upload permission for the configured bucket (ideally scoped to `users/*`); its GCP identity needs object-read permission for the report bucket. Use TLS for cloud and API traffic.

Spreadsheet generation, S3 upload, and GCS report download currently hold complete file contents in memory. Apply request-size limits and appropriate instance memory sizing; large-file workloads may require streaming or a different transfer design.
