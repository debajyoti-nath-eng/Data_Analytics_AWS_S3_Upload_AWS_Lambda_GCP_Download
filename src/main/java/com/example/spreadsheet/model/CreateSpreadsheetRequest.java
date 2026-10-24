package com.example.spreadsheet.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateSpreadsheetRequest(
        @NotEmpty List<@Valid UserInfo> users
) {
}
