package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.BrandMedicationCsvConversionResult;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BrandMedicationImportConversionServiceTest {

    private final BrandMedicationImportConversionService service = new BrandMedicationImportConversionService();

    @Test
    void shouldConvertExcelToCsvAndSkipInvalidRows() throws Exception {
        MockMultipartFile file = createWorkbook(
                new String[]{"Trade Name", "Registration Number", "Price"},
                new String[][]{
                        {"  Panadol  ", " REG-1 ", "12.50"},
                        {"", "REG-2", "10.00"},
                        {"Augmentin", "REG-3", "abc"},
                        {"", "", ""}
                }
        );

        BrandMedicationCsvConversionResult result = service.convert(file, 100L, "Tablet");
        String csv = new String(result.csvContent(), StandardCharsets.UTF_8);

        assertEquals(3, result.totalRecords());
        assertEquals(1, result.successfulRecords());
        assertEquals(2, result.skippedRecords());

        assertTrue(csv.contains("name,code,price,currency,is_active,billing_rule_id,dosage_form"));
        assertTrue(csv.contains("Panadol,REG-1,12.50,SAR,true,100,Tablet"));
    }

    @Test
    void shouldFailWhenRequiredHeaderIsMissing() throws Exception {
        MockMultipartFile file = createWorkbook(
                new String[]{"Trade Name", "Registration Number"},
                new String[][]{{"Panadol", "REG-1"}}
        );

        BadRequestAlertException ex = assertThrows(BadRequestAlertException.class, () -> service.convert(file, 100L, "Tablet"));
        assertEquals("missingheader", ex.getErrorKey());
    }

    private MockMultipartFile createWorkbook(String[] headers, String[][] rows) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Sheet1");
            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                String[] values = rows[rowIndex];
                for (int col = 0; col < values.length; col++) {
                    row.createCell(col).setCellValue(values[col]);
                }
            }

            workbook.write(outputStream);
            return new MockMultipartFile(
                    "file",
                    "brand-import.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    outputStream.toByteArray()
            );
        }
    }
}

