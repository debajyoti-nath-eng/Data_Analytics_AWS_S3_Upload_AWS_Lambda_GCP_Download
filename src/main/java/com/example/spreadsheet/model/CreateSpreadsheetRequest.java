package com.example.spreadsheet.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** Request body for creating a spreadsheet; the user list must not be empty. */
public record CreateSpreadsheetRequest(
        @NotEmpty List<@Valid UserInfo> users
) {
}
