package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CptCode;
import com.dazzle.asklepios.repository.CptCodeRepository;
import com.dazzle.asklepios.service.dto.CptConflictDTO;
import com.dazzle.asklepios.service.dto.CptImportResultDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CptCodeService {

    private static final Logger LOG = LoggerFactory.getLogger(CptCodeService.class);
    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    private static final Set<String> REQUIRED_HEADERS = Set.of("code", "description", "service_category", "main_category");

    private static final Map<String, Set<String>> HEADER_ALIASES = Map.of(
            "code", Set.of("code", "cpt code", "cpt_code"),
            "description", Set.of("description", "cpt description", "cpt_description"),
            "service_category", Set.of("service category", "service_category"),
            "code_category", Set.of("code category", "code_category"),
            "main_category", Set.of("main category", "main_category", "category")
    );

    private final CptCodeRepository repository;

    @Transactional
    public CptImportResultDTO importFile(MultipartFile file, boolean overwrite) {
        validateUploadedFile(file);

        List<ImportRow> rows = isExcelFile(file) ? readExcel(file) : readCsv(file);
        LOG.info("Starting CPT import (overwrite={}). Total records: {}", overwrite, rows.size());

        Map<String, Long> codeCounts = rows.stream()
                .map(ImportRow::code)
                .collect(Collectors.groupingBy(code -> code, Collectors.counting()));

        List<String> duplicatesInFile = codeCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList();

        if (!duplicatesInFile.isEmpty()) {
            throw new BadRequestAlertException(
                    "Duplicate CPT code(s) in file: " + String.join(", ", duplicatesInFile),
                    "cptcode",
                    "duplicate"
            );
        }

        int totalRows = rows.size();
        Map<String, ImportRow> rowsByCode = rows.stream().collect(Collectors.toMap(ImportRow::code, row -> row, (a, b) -> b));

        Map<String, CptCode> existingByCode = new HashMap<>();
        for (String code : rowsByCode.keySet()) {
            repository.findByCode(code).ifPresent(existing -> existingByCode.put(code, existing));
        }

        List<CptConflictDTO> conflicts = new ArrayList<>();
        for (Map.Entry<String, CptCode> entry : existingByCode.entrySet()) {
            ImportRow incoming = rowsByCode.get(entry.getKey());
            CptCode existing = entry.getValue();
            conflicts.add(new CptConflictDTO(
                    incoming.code(),
                    incoming.description(),
                    incoming.codeCategory(),
                    incoming.serviceCategory(),
                    incoming.mainCategory(),
                    existing.getDescription(),
                    existing.getCodeCategory(),
                    existing.getServiceCategory(),
                    existing.getMainCategory()
            ));
        }

        if (!overwrite && !conflicts.isEmpty()) {
            LOG.info("CPT import aborted due to {} conflict(s).", conflicts.size());
            return new CptImportResultDTO(totalRows, 0, 0, conflicts);
        }

        int inserted = 0;
        int updated = 0;

        for (ImportRow row : rows) {
            CptCode existing = existingByCode.get(row.code());
            if (existing == null) {
                repository.save(CptCode.builder()
                        .code(row.code())
                        .description(row.description())
                        .codeCategory(row.codeCategory())
                        .serviceCategory(row.serviceCategory())
                        .mainCategory(row.mainCategory())
                        .lastUpdated(Instant.now())
                        .build());
                inserted++;
            } else if (overwrite) {
                existing.setDescription(row.description());
                existing.setCodeCategory(row.codeCategory());
                existing.setServiceCategory(row.serviceCategory());
                existing.setMainCategory(row.mainCategory());
                existing.setLastUpdated(Instant.now());
                repository.save(existing);
                updated++;
            }
        }

        LOG.info("CPT import done. Inserted={}, Updated={}, Conflicts={}", inserted, updated, conflicts.size());
        return new CptImportResultDTO(totalRows, inserted, updated, overwrite ? List.of() : conflicts);
    }

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<CptCode> findAll(Pageable pageable) {
        LOG.debug("Fetching all CPT codes: pageable={}", pageable);
        return repository.findAll(pageable);
    }

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<CptCode> findByMainCategory(String mainCategory, Pageable pageable) {
        LOG.debug("Filtering CPT codes by mainCategory='{}' pageable={}", mainCategory, pageable);
        return repository.findByMainCategoryIgnoreCase(mainCategory.trim(), pageable);
    }

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<CptCode> findByServiceCategory(String serviceCategory, Pageable pageable) {
        LOG.debug("Filtering CPT codes by serviceCategory='{}' pageable={}", serviceCategory, pageable);
        return repository.findByServiceCategoryIgnoreCase(serviceCategory.trim(), pageable);
    }

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<CptCode> findByCodeContainingIgnoreCase(String code, Pageable pageable) {
        LOG.debug("Filtering CPT codes by code like='{}' pageable={}", code, pageable);
        return repository.findByCodeContainingIgnoreCase(code, pageable);
    }

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<CptCode> findByDescriptionContainingIgnoreCase(String description, Pageable pageable) {
        LOG.debug("Filtering CPT codes by description like='{}' pageable={}", description, pageable);
        return repository.findByDescriptionContainingIgnoreCase(description, pageable);
    }

    private record ImportRow(
            String code,
            String description,
            String codeCategory,
            String serviceCategory,
            String mainCategory
    ) {}

    private void validateUploadedFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestAlertException("File is required", "cptcode", "filerequired");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!name.endsWith(".csv") && !name.endsWith(".xlsx") && !name.endsWith(".xls")) {
            throw new BadRequestAlertException(
                    "Unsupported file type. Upload .csv, .xlsx, or .xls",
                    "cptcode",
                    "badfiletype"
            );
        }
    }

    private boolean isExcelFile(MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        return name.endsWith(".xlsx") || name.endsWith(".xls");
    }

    private List<ImportRow> readCsv(MultipartFile file) {
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withDelimiter(',')
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {
            ensureHeaders(parser.getHeaderMap().keySet());
            List<ImportRow> rows = new ArrayList<>();
            for (CSVRecord record : parser.getRecords()) {
                if (isBlankRow(record.toMap())) {
                    continue;
                }
                rows.add(toImportRow(record.toMap(), record.getRecordNumber()));
            }
            return rows;
        } catch (IOException ex) {
            LOG.error("Error reading CPT CSV file: {}", ex.getMessage(), ex);
            throw new BadRequestAlertException(
                    "Error reading CSV file: " + ex.getMessage(),
                    "cptcode",
                    "filereaderror"
            );
        }
    }

    private List<ImportRow> readExcel(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new BadRequestAlertException("Excel file has no sheets", "cptcode", "emptysheet");
            }

            int headerRowIndex = findHeaderRowIndex(sheet);
            Row headerRow = sheet.getRow(headerRowIndex);
            Map<String, Integer> headerIndex = buildHeaderIndex(headerRow);
            ensureHeaders(headerIndex.keySet());

            List<ImportRow> rows = new ArrayList<>();
            for (int rowIndex = headerRowIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlankExcelRow(row, headerIndex)) {
                    continue;
                }
                Map<String, String> values = readExcelRowValues(row, headerIndex);
                rows.add(toImportRow(values, rowIndex + 1L));
            }
            return rows;
        } catch (BadRequestAlertException ex) {
            throw ex;
        } catch (Exception ex) {
            LOG.error("Error reading CPT Excel file: {}", ex.getMessage(), ex);
            throw new BadRequestAlertException(
                    "Error reading Excel file: " + ex.getMessage(),
                    "cptcode",
                    "filereaderror"
            );
        }
    }

    private int findHeaderRowIndex(Sheet sheet) {
        int lastRow = Math.min(sheet.getLastRowNum(), 20);
        for (int rowIndex = 0; rowIndex <= lastRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            Map<String, Integer> headers = buildHeaderIndex(row);
            if (headers.containsKey("code") && headers.containsKey("description")) {
                return rowIndex;
            }
        }
        throw new BadRequestAlertException(
                "Could not find header row with code and description columns",
                "cptcode",
                "missingheader"
        );
    }

    private Map<String, Integer> buildHeaderIndex(Row headerRow) {
        Map<String, Integer> headerIndex = new LinkedHashMap<>();
        if (headerRow == null) {
            return headerIndex;
        }
        for (Cell cell : headerRow) {
            String rawHeader = cleanHeaderKey(getCellValue(cell));
            if (rawHeader.isBlank()) {
                continue;
            }
            String canonical = resolveCanonicalHeader(rawHeader);
            if (canonical != null) {
                headerIndex.putIfAbsent(canonical, cell.getColumnIndex());
            }
        }
        return headerIndex;
    }

    private Map<String, String> readExcelRowValues(Row row, Map<String, Integer> headerIndex) {
        Map<String, String> values = new HashMap<>();
        for (Map.Entry<String, Integer> entry : headerIndex.entrySet()) {
            Cell cell = row.getCell(entry.getValue());
            values.put(entry.getKey(), normalizeValue(getCellValue(cell)));
        }
        return values;
    }

    private ImportRow toImportRow(Map<String, String> values, long lineNumber) {
        String code = requiredValue(values, "code", lineNumber);
        String description = requiredValue(values, "description", lineNumber);
        String serviceCategory = requiredValue(values, "service_category", lineNumber);
        String mainCategory = requiredValue(values, "main_category", lineNumber);
        String codeCategory = optionalValue(values, "code_category");
        if (codeCategory.isBlank()) {
            codeCategory = "CPT";
        }
        return new ImportRow(
                code.trim(),
                description.trim(),
                codeCategory.trim(),
                serviceCategory.trim(),
                mainCategory.trim()
        );
    }

    private void ensureHeaders(Set<String> rawHeaders) {
        Set<String> normalizedHeaders = rawHeaders.stream()
                .filter(Objects::nonNull)
                .map(this::cleanHeaderKey)
                .map(this::resolveCanonicalHeader)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (String requiredHeader : REQUIRED_HEADERS) {
            if (!normalizedHeaders.contains(requiredHeader)) {
                throw new BadRequestAlertException(
                        "Missing required column header: '" + requiredHeader + "'",
                        "cptcode",
                        "missingheader"
                );
            }
        }
    }

    private String resolveCanonicalHeader(String rawHeader) {
        String cleaned = cleanHeaderKey(rawHeader);
        for (Map.Entry<String, Set<String>> entry : HEADER_ALIASES.entrySet()) {
            for (String alias : entry.getValue()) {
                if (alias.equalsIgnoreCase(cleaned)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private String requiredValue(Map<String, String> values, String canonicalColumn, long lineNumber) {
        String value = values.get(canonicalColumn);
        if (value == null || value.isBlank()) {
            throw new BadRequestAlertException(
                    "Missing '" + canonicalColumn + "' at line " + lineNumber,
                    "cptcode",
                    "missingfield"
            );
        }
        return value;
    }

    private String optionalValue(Map<String, String> values, String canonicalColumn) {
        String value = values.get(canonicalColumn);
        return value == null ? "" : value.trim();
    }

    private String cleanHeaderKey(String header) {
        return header == null ? "" : header.replace("\uFEFF", "").trim();
    }

    private String normalizeValue(String value) {
        return value == null ? "" : value.trim();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        return DATA_FORMATTER.formatCellValue(cell).trim();
    }

    private boolean isBlankRow(Map<String, String> values) {
        return values.values().stream().allMatch(value -> value == null || value.isBlank());
    }

    private boolean isBlankExcelRow(Row row, Map<String, Integer> headerIndex) {
        for (Integer columnIndex : headerIndex.values()) {
            String value = getCellValue(row.getCell(columnIndex));
            if (!value.isBlank()) {
                return false;
            }
        }
        return true;
    }
}
