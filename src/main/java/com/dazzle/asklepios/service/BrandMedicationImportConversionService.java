package com.dazzle.asklepios.service;

import com.dazzle.asklepios.service.dto.BrandMedicationCsvConversionResult;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class BrandMedicationImportConversionService {

    private static final Logger LOG = LoggerFactory.getLogger(BrandMedicationImportConversionService.class);
    private static final String ENTITY_NAME = "brandMedicationImport";

    private static final String INPUT_TRADE_NAME = "Trade Name";
    private static final String INPUT_REGISTRATION_NUMBER = "Registration Number";
    private static final String INPUT_PRICE = "Price";

    private static final String OUTPUT_NAME = "name";
    private static final String OUTPUT_CODE = "code";
    private static final String OUTPUT_PRICE = "price";
    private static final String OUTPUT_CURRENCY = "currency";
    private static final String OUTPUT_IS_ACTIVE = "is_active";
    private static final String OUTPUT_BILLING_RULE_ID = "billing_rule_id";
    private static final String OUTPUT_DOSAGE_FORM = "dosage_form";

    private static final String FIXED_CURRENCY = "SAR";
    private static final boolean FIXED_IS_ACTIVE = true;

    public BrandMedicationCsvConversionResult convert(MultipartFile file, Long billingRuleId, String dosageForm) {
        validateRequest(file, billingRuleId, dosageForm);

        long startedAt = System.currentTimeMillis();
        int totalRecords = 0;
        int successfulRecords = 0;
        int skippedRecords = 0;

        try (
                InputStream inputStream = file.getInputStream();
                Workbook workbook = WorkbookFactory.create(inputStream);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(64 * 1024);
                OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                CSVPrinter csvPrinter = new CSVPrinter(
                        writer,
                        CSVFormat.DEFAULT.builder()
                                .setHeader(
                                        OUTPUT_NAME,
                                        OUTPUT_CODE,
                                        OUTPUT_PRICE,
                                        OUTPUT_CURRENCY,
                                        OUTPUT_IS_ACTIVE,
                                        OUTPUT_BILLING_RULE_ID,
                                        OUTPUT_DOSAGE_FORM
                                )
                                .build()
                )
        ) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new BadRequestAlertException("Excel file does not contain any sheet", ENTITY_NAME, "sheetmissing");
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new BadRequestAlertException("Excel file header row is missing", ENTITY_NAME, "headermissing");
            }

            DataFormatter dataFormatter = new DataFormatter(Locale.ROOT);
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Map<String, Integer> headerIndexes = extractHeaderIndexes(headerRow, dataFormatter, formulaEvaluator);
            int requiredColumnsMaxIndex = maxRequiredColumnIndex(headerIndexes);

            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isDataRowEmpty(row, requiredColumnsMaxIndex, dataFormatter, formulaEvaluator)) {
                    continue;
                }

                totalRecords++;

                String name = getCellValue(row, headerIndexes.get(INPUT_TRADE_NAME), dataFormatter, formulaEvaluator);
                String code = getCellValue(row, headerIndexes.get(INPUT_REGISTRATION_NUMBER), dataFormatter, formulaEvaluator);
                String priceValue = getCellValue(row, headerIndexes.get(INPUT_PRICE), dataFormatter, formulaEvaluator);

                if (name.isBlank() || code.isBlank() || priceValue.isBlank()) {
                    skippedRecords++;
                    continue;
                }

                BigDecimal price = parsePrice(priceValue);
                if (price == null) {
                    skippedRecords++;
                    continue;
                }

                csvPrinter.printRecord(
                        name,
                        code,
                        price.toPlainString(),
                        FIXED_CURRENCY,
                        FIXED_IS_ACTIVE,
                        billingRuleId,
                        dosageForm.trim()
                );
                successfulRecords++;
            }

            csvPrinter.flush();
            writer.flush();

            long durationMs = System.currentTimeMillis() - startedAt;
            LOG.info(
                    "[BrandMedicationImport] Conversion done. file='{}' total={} success={} skipped={} durationMs={}",
                    file.getOriginalFilename(),
                    totalRecords,
                    successfulRecords,
                    skippedRecords,
                    durationMs
            );

            return new BrandMedicationCsvConversionResult(
                    outputStream.toByteArray(),
                    totalRecords,
                    successfulRecords,
                    skippedRecords
            );
        } catch (BadRequestAlertException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestAlertException(
                    "Failed to convert excel file to csv: " + ex.getMessage(),
                    ENTITY_NAME,
                    "conversionfailed"
            );
        }
    }

    private void validateRequest(MultipartFile file, Long billingRuleId, String dosageForm) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestAlertException("Excel file is required", ENTITY_NAME, "filerequired");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw new BadRequestAlertException("Only .xlsx files are supported", ENTITY_NAME, "invalidfiletype");
        }

        if (billingRuleId == null) {
            throw new BadRequestAlertException("billingRuleId is required", ENTITY_NAME, "billingrulerequired");
        }

        if (dosageForm == null || dosageForm.trim().isEmpty()) {
            throw new BadRequestAlertException("dosageForm is required", ENTITY_NAME, "dosageformrequired");
        }
    }

    private Map<String, Integer> extractHeaderIndexes(Row headerRow, DataFormatter dataFormatter, FormulaEvaluator evaluator) {
        Map<String, Integer> headerIndexes = new HashMap<>();
        for (Cell cell : headerRow) {
            String headerName = normalizeHeader(dataFormatter.formatCellValue(cell, evaluator));
            if (!headerName.isEmpty()) {
                headerIndexes.putIfAbsent(headerName, cell.getColumnIndex());
            }
        }

        Map<String, String> requiredHeaders = Map.of(
                normalizeHeader(INPUT_TRADE_NAME), INPUT_TRADE_NAME,
                normalizeHeader(INPUT_REGISTRATION_NUMBER), INPUT_REGISTRATION_NUMBER,
                normalizeHeader(INPUT_PRICE), INPUT_PRICE
        );

        for (Map.Entry<String, String> requiredHeader : requiredHeaders.entrySet()) {
            if (!headerIndexes.containsKey(requiredHeader.getKey())) {
                throw new BadRequestAlertException(
                        "Missing required header: " + requiredHeader.getValue(),
                        ENTITY_NAME,
                        "missingheader"
                );
            }
        }

        Map<String, Integer> resolved = new HashMap<>();
        resolved.put(INPUT_TRADE_NAME, headerIndexes.get(normalizeHeader(INPUT_TRADE_NAME)));
        resolved.put(INPUT_REGISTRATION_NUMBER, headerIndexes.get(normalizeHeader(INPUT_REGISTRATION_NUMBER)));
        resolved.put(INPUT_PRICE, headerIndexes.get(normalizeHeader(INPUT_PRICE)));
        return resolved;
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private int maxRequiredColumnIndex(Map<String, Integer> headerIndexes) {
        return Math.max(
                headerIndexes.get(INPUT_TRADE_NAME),
                Math.max(headerIndexes.get(INPUT_REGISTRATION_NUMBER), headerIndexes.get(INPUT_PRICE))
        );
    }

    private boolean isDataRowEmpty(Row row, int maxColumnIndex, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (row == null) {
            return true;
        }

        for (int col = 0; col <= maxColumnIndex; col++) {
            String value = getCellValue(row, col, formatter, evaluator);
            if (!value.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String getCellValue(Row row, Integer columnIndex, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (row == null || columnIndex == null || columnIndex < 0) {
            return "";
        }

        Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return "";
        }

        String value = formatter.formatCellValue(cell, evaluator);
        return value == null ? "" : value.trim();
    }

    private BigDecimal parsePrice(String value) {
        try {
            String normalized = value.replace(",", "").trim();
            if (normalized.isEmpty()) {
                return null;
            }
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

