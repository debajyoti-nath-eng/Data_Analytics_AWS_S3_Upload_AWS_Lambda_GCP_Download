package com.example.spreadsheet.service;

import com.example.spreadsheet.model.UserInfo;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Converts user details into an XLSX workbook held in memory.
 *
 * <p>This class has no cloud dependency, so the workbook layout can be tested on its own. The
 * column order below is the input contract for the downstream Lambda processor; changing it
 * requires a matching change there.
 */
@Component
public class SpreadsheetGenerator {

    /**
     * Creates a workbook with one row per user and a header row.
     *
     * <p>Every value is written as a string cell, so input such as {@code =1+1} stays literal text
     * and is not evaluated as a formula when the file is opened.
     *
     * @param users user details to write
     * @return the complete XLSX file as bytes
     * @throws IllegalStateException if the workbook cannot be serialized
     */
    public byte[] generate(List<UserInfo> users) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Users");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("First Name");
            header.createCell(1).setCellValue("Last Name");
            header.createCell(2).setCellValue("Email");
            header.createCell(3).setCellValue("Phone");

            // Row 0 is the header, so user N is written to row N + 1.
            for (int index = 0; index < users.size(); index++) {
                UserInfo user = users.get(index);
                Row row = sheet.createRow(index + 1);
                row.createCell(0).setCellValue(user.firstName());
                row.createCell(1).setCellValue(user.lastName());
                row.createCell(2).setCellValue(user.email());
                row.createCell(3).setCellValue(user.phone() == null ? "" : user.phone());
            }

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not generate the user spreadsheet", exception);
        }
    }
}
