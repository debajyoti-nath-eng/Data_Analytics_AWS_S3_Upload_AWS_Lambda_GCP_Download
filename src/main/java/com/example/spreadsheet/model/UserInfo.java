package com.example.spreadsheet.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Basic user fields written as one row in the generated spreadsheet.
 *
 * @param firstName required first name
 * @param lastName required last name
 * @param email required, well-formed email address
 * @param phone optional phone number, written as an empty cell when absent
 */
public record UserInfo(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        String phone
) {
}
