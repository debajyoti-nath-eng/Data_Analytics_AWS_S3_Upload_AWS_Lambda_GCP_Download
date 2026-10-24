package com.example.spreadsheet.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Basic user fields written as one row in the generated spreadsheet. */
public record UserInfo(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        String phone
) {
}
