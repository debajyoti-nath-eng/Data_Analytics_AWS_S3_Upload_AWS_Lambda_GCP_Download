package com.example.spreadsheet.service;

import com.example.spreadsheet.model.UserInfo;
import org.apache.poi.ss.usermodel.CellType;
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

    @Test
    void writesFormulaLikeInputAsTextAndMissingPhoneAsEmpty() throws Exception {
        byte[] bytes = generator.generate(List.of(
                new UserInfo("=1+1", "Hopper", "grace@example.com", null)
        ));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var row = workbook.getSheet("Users").getRow(1);
            assertThat(row.getCell(0).getCellType()).isEqualTo(CellType.STRING);
            assertThat(row.getCell(0).getStringCellValue()).isEqualTo("=1+1");
            assertThat(row.getCell(3).getStringCellValue()).isEmpty();
        }
    }
}
