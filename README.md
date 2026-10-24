# Spreadsheet Cloud API

A Spring Boot API that creates XLSX spreadsheets from user data and uploads them to AWS S3, then serves processed report files stored in Google Cloud Storage.

## Requirements and configuration

- Java 17+
- Maven 3.9+
- An AWS identity with permission to upload objects to the configured bucket. The AWS SDK uses its default credential provider chain (for example, environment variables, profiles, or an instance role).
- Google Cloud Application Default Credentials with permission to read objects from the report bucket.

Configure `AWS_S3_BUCKET`, `GCS_REPORT_BUCKET`, and optionally `AWS_REGION` (defaults to `us-east-1`). For Google Cloud, configure Application Default Credentials in the runtime environment. Do not put cloud credentials in source control.

Run with:

```powershell
$env:AWS_S3_BUCKET = "your-s3-bucket"
$env:GCS_REPORT_BUCKET = "your-gcs-report-bucket"
mvn spring-boot:run
```

## Endpoints

### Create and upload a user spreadsheet

`POST /api/spreadsheets/users` accepts a non-empty list of users and returns the S3 bucket and generated object key. Each user requires `firstName`, `lastName`, and a valid `email`; `phone` is optional.

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

The response has status `201 Created`:

```json
{
  "bucket": "your-s3-bucket",
  "objectKey": "users/3a11ea14-5d3b-459b-aa3c-38fcbad89636.xlsx"
}
```

### Download a processed report from Google Cloud Storage

`POST /api/reports/download` is intended to be called by the processing Lambda after the report is available in GCS. It returns the object as an attachment.

```json
{
  "objectName": "processed/3a11ea14-5d3b-459b-aa3c-38fcbad89636.xlsx"
}
```

The report bucket is fixed by `GCS_REPORT_BUCKET`. The Lambda must have network access to this API and the service must have GCS object-read permission. Restrict access to the callback endpoint at the API gateway/network layer appropriate to your deployment.
