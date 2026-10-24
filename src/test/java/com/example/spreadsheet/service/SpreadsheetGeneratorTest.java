package com.example.spreadsheet.service;

import com.example.spreadsheet.model.UserInfo;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpreadsheetGeneratorTest {

    private final SpreadsheetGenerator generator = new SpreadsheetGenerator();

    @Test
    void generatesWorkbookWithUserInformation() throws Exception {
        byte[] bytes = generator.generate(List.of(
                new UserInfo("Ada", "Lovelace", "ada@example.com", "555-0100")
        ));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheet("Users");
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("First Name");
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("Ada");
            assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("Lovelace");
            assertThat(sheet.getRow(1).getCell(2).getStringCellValue()).isEqualTo("ada@example.com");
            assertThat(sheet.getRow(1).getCell(3).getStringCellValue()).isEqualTo("555-0100");
        }
    }
}
