package com.example.spreadsheet.service;

import com.example.spreadsheet.model.UserInfo;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class SpreadsheetGenerator {

    public byte[] generate(List<UserInfo> users) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Users");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("First Name");
            header.createCell(1).setCellValue("Last Name");
            header.createCell(2).setCellValue("Email");
            header.createCell(3).setCellValue("Phone");

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
