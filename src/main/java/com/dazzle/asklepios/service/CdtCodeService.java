package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CdtCode;
import com.dazzle.asklepios.domain.enumeration.CdtClass;
import com.dazzle.asklepios.repository.CdtCodeRepository;
import com.dazzle.asklepios.service.dto.CdtConflictDTO;
import com.dazzle.asklepios.service.dto.CdtImportResultDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CdtCodeService {

    private static final Logger LOG = LoggerFactory.getLogger(CdtCodeService.class);
    private final CdtCodeRepository repository;

    // ====================== IMPORT ======================

    @Transactional
    public CdtImportResultDTO importCsv(MultipartFile uploadedFile, boolean overwriteExistingRecords) {
        List<CSVRecord> csvRecords = readCsv(uploadedFile);
        LOG.info("Starting CDT CSV import (overwrite={}). Total records: {}", overwriteExistingRecords, csvRecords.size());

        Map<String, Long> codeOccurrences = csvRecords.stream()
                .map(record -> getValue(record, "code"))
                .collect(Collectors.groupingBy(code -> code, Collectors.counting()));

        List<String> duplicateCodes = codeOccurrences.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList();

        if (!duplicateCodes.isEmpty()) {
            throw new BadRequestAlertException(
                    "Duplicate CDT code(s) in CSV: " + String.join(", ", duplicateCodes),
                    "cdtcode",
                    "duplicate"
            );
        }

        List<CsvRow> csvRows = new ArrayList<>();
        for (CSVRecord csvRecord : csvRecords) {
            csvRows.add(new CsvRow(
                    getValue(csvRecord, "code").trim(),
                    getValue(csvRecord, "description").trim(),
                    parseClass(getValue(csvRecord, "class")),
                    parseBoolean(getValue(csvRecord, "is active"))
            ));
        }

        Integer totalRowsCount = csvRows.size();

        Map<String, CsvRow> incomingRowsByCode = csvRows.stream()
                .collect(Collectors.toMap(CsvRow::code, csvRow -> csvRow));

        Map<String, CdtCode> existingCodesByCode = new HashMap<>();
        for (String code : incomingRowsByCode.keySet()) {
            repository.findByCode(code).ifPresent(existingCode -> existingCodesByCode.put(code, existingCode));
        }

        List<CdtConflictDTO> conflictList = new ArrayList<>();
        for (Map.Entry<String, CdtCode> entry : existingCodesByCode.entrySet()) {
            CsvRow incomingRow = incomingRowsByCode.get(entry.getKey());
            CdtCode existingCode = entry.getValue();
            conflictList.add(new CdtConflictDTO(
                    incomingRow.code(),
                    incomingRow.description(),
                    incomingRow.cdtClass().name(),
                    incomingRow.isActive(),
                    existingCode.getDescription(),
                    existingCode.getCdtClass().name(),
                    existingCode.getIsActive()
            ));
        }

        if (!overwriteExistingRecords && !conflictList.isEmpty()) {
            LOG.info("CDT import aborted due to {} conflict(s).", conflictList.size());
            return new CdtImportResultDTO(totalRowsCount, 0, 0, conflictList);
        }

        Integer insertedCount = 0;
        Integer updatedCount = 0;
        for (CsvRow csvRow : csvRows) {
            CdtCode existingCode = existingCodesByCode.get(csvRow.code());
            if (existingCode == null) {
                repository.save(CdtCode.builder()
                        .code(csvRow.code())
                        .description(csvRow.description())
                        .cdtClass(csvRow.cdtClass())
                        .isActive(csvRow.isActive())
                        .lastUpdated(Instant.now())
                        .build());
                insertedCount++;
            } else if (overwriteExistingRecords) {
                existingCode.setDescription(csvRow.description());
                existingCode.setCdtClass(csvRow.cdtClass());
                existingCode.setIsActive(csvRow.isActive());
                existingCode.setLastUpdated(Instant.now());
                repository.save(existingCode);
                updatedCount++;
            }
        }

        LOG.info("CDT import complete. Inserted={}, Updated={}, Conflicts={}",
                insertedCount, updatedCount, conflictList.size());

        return new CdtImportResultDTO(totalRowsCount, insertedCount, updatedCount,
                overwriteExistingRecords ? List.of() : conflictList);
    }

    // ====================== READ / FILTER ======================

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<CdtCode> findAll(Pageable pageable) {
        LOG.debug("Fetching all CDT codes: pageable={}", pageable);
        return repository.findAll(pageable);
    }

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<CdtCode> findByClass(CdtClass cdtClass, Pageable pageable) {
        LOG.debug("Filtering CDT codes by class={} pageable={}", cdtClass, pageable);
        return repository.findByCdtClass(cdtClass, pageable);
    }

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<CdtCode> findByIsActive(boolean isActive, Pageable pageable) {
        LOG.debug("Filtering CDT codes by isActive={} pageable={}", isActive, pageable);
        return repository.findByIsActive(isActive, pageable);
    }

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<CdtCode> findByCodeContainingIgnoreCase(String code, Pageable pageable) {
        LOG.debug("Filtering CDT codes by code like='{}' pageable={}", code, pageable);
        return repository.findByCodeContainingIgnoreCase(code, pageable);
    }

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<CdtCode> findByDescriptionContainingIgnoreCase(String description, Pageable pageable) {
        LOG.debug("Filtering CDT codes by description like='{}' pageable={}", description, pageable);
        return repository.findByDescriptionContainingIgnoreCase(description, pageable);
    }

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<CdtCode> filter(String code, String description, CdtClass cdtClass, Boolean isActive, Pageable pageable) {
        LOG.debug("CDT filter code='{}', description='{}', class='{}', isActive='{}', pageable={}",
                code, description, cdtClass, isActive, pageable);

        if (cdtClass != null) return findByClass(cdtClass, pageable);
        if (code != null && !code.isBlank()) return findByCodeContainingIgnoreCase(code, pageable);
        if (description != null && !description.isBlank()) return findByDescriptionContainingIgnoreCase(description, pageable);
        if (isActive != null) return findByIsActive(isActive, pageable);

        return findAll(pageable);
    }

    // ====================== Helpers ======================

    private record CsvRow(String code, String description, CdtClass cdtClass, Boolean isActive) {}

    private static final Set<String> REQUIRED_HEADERS = Set.of("code", "description", "class", "is active");

    private static final Map<String, Set<String>> HEADER_ALIASES = Map.of(
            "code", Set.of("code"),
            "description", Set.of("description", "desc"),
            "class", Set.of("class", "category", "cdtclass", "cdt_class"),
            "is active", Set.of("is active", "is_active", "isActive", "active")
    );

    private List<CSVRecord> readCsv(MultipartFile uploadedFile) {
        try (Reader reader = new InputStreamReader(uploadedFile.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withDelimiter(',')
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            ensureHeaders(parser);
            return parser.getRecords();
        } catch (IOException exception) {
            LOG.error("Error reading CDT CSV file: {}", exception.getMessage(), exception);
            throw new BadRequestAlertException(
                    "Error reading CSV file: " + exception.getMessage(),
                    "cdtcode",
                    "filereaderror"
            );
        }
    }

    private void ensureHeaders(CSVParser parser) {
        Set<String> headers = parser.getHeaderMap().keySet().stream()
                .filter(Objects::nonNull)
                .map(this::cleanHeaderKey)
                .collect(Collectors.toSet());

        for (String requiredHeader : REQUIRED_HEADERS) {
            Set<String> aliases = HEADER_ALIASES.getOrDefault(requiredHeader, Set.of(requiredHeader));
            boolean present = headers.stream().anyMatch(header ->
                    aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(header)));
            if (!present) {
                throw new BadRequestAlertException(
                        "Missing required column header: '" + requiredHeader + "'",
                        "cdtcode",
                        "missingheader"
                );
            }
        }
    }

    private String getValue(CSVRecord record, String canonicalColumn) {
        try {
            String directValue = record.get(canonicalColumn);
            if (directValue != null && !directValue.isBlank()) return directValue;
        } catch (IllegalArgumentException ignored) {}

        Set<String> aliases = HEADER_ALIASES.getOrDefault(canonicalColumn, Set.of(canonicalColumn));
        for (String headerKey : record.toMap().keySet()) {
            if (headerKey == null) continue;
            String cleaned = cleanHeaderKey(headerKey);
            if (aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(cleaned))) {
                String value = record.get(headerKey);
                if (value == null || value.isBlank()) {
                    throw new BadRequestAlertException(
                            "Missing '" + canonicalColumn + "' at line " + record.getRecordNumber(),
                            "cdtcode",
                            "missingfield"
                    );
                }
                return value;
            }
        }

        throw new BadRequestAlertException(
                "Missing '" + canonicalColumn + "' at line " + record.getRecordNumber(),
                "cdtcode",
                "missingfield"
        );
    }

    private String cleanHeaderKey(String header) {
        return header.replace("\uFEFF", "").trim();
    }

    private CdtClass parseClass(String rawClassValue) {
        try {
            String normalized = rawClassValue.trim()
                    .toUpperCase()
                    .replace("&", " AND ")
                    .replaceAll("[^A-Z0-9]+", "_")
                    .replaceAll("_+", "_")
                    .replaceAll("^_|_$", "");
            return CdtClass.valueOf(normalized);
        } catch (Exception e) {
            throw new BadRequestAlertException(
                    "Invalid class: '" + rawClassValue + "'. Please match one of the defined CDT classes.",
                    "cdtcode",
                    "badclass"
            );
        }
    }

    private Boolean parseBoolean(String rawValue) {
        String lower = rawValue.trim().toLowerCase();
        if (lower.equals("true") || lower.equals("t") || lower.equals("yes") || lower.equals("y") || lower.equals("1")) return true;
        if (lower.equals("false") || lower.equals("f") || lower.equals("no") || lower.equals("n") || lower.equals("0")) return false;

        throw new BadRequestAlertException(
                "Invalid 'is active' value: '" + rawValue + "'. Expected TRUE/FALSE, YES/NO, or 1/0.",
                "cdtcode",
                "badboolean"
        );
    }
}
