package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.LoincCode;
import com.dazzle.asklepios.domain.enumeration.LoincCategory;
import com.dazzle.asklepios.repository.LoincCodeRepository;
import com.dazzle.asklepios.service.dto.LoincConflictDTO;
import com.dazzle.asklepios.service.dto.LoincImportResultDTO;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** Service for importing and managing LOINC codes */
@Service
@RequiredArgsConstructor
public class LoincCodeService {

    private static final Logger LOG = LoggerFactory.getLogger(LoincCodeService.class);
    private final LoincCodeRepository repository;

    /** Import CSV file of LOINC codes with conflict handling */
    @Transactional
    public LoincImportResultDTO importCsv(MultipartFile file, boolean overwrite) {
        List<CSVRecord> records = readCsv(file);
        LOG.info("Starting LOINC CSV import (overwrite={}). Total records: {}", overwrite, records.size());

        // Detect duplicates in file
        Map<String, Long> codeCounts = records.stream()
                .map(record -> val(record, "code"))
                .collect(Collectors.groupingBy(code -> code, Collectors.counting()));

        List<String> duplicatesInFile = codeCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList();

        if (!duplicatesInFile.isEmpty()) {
            throw new BadRequestAlertException(
                    "Duplicate LOINC code(s): " + String.join(", ", duplicatesInFile),
                    "loincode",
                    "duplicate"
            );
        }

        // Build row list
        List<CsvRow> rows = new ArrayList<>();
        for (CSVRecord record : records) {
            rows.add(new CsvRow(
                    val(record, "code").trim(),
                    val(record, "description").trim(),
                    parseCategory(val(record, "category"))
            ));
        }
        Integer totalRows = rows.size();

        Map<String, CsvRow> rowsByCode = rows.stream().collect(Collectors.toMap(CsvRow::code, row -> row));
        Map<String, LoincCode> existingByCode = new HashMap<>();
        rowsByCode.keySet().forEach(code ->
                repository.findByCode(code).ifPresent(existing -> existingByCode.put(code, existing))
        );

        // Detect DB conflicts
        List<LoincConflictDTO> conflicts = new ArrayList<>();
        for (Map.Entry<String, LoincCode> entry : existingByCode.entrySet()) {
            CsvRow incoming = rowsByCode.get(entry.getKey());
            LoincCode existing = entry.getValue();
            conflicts.add(new LoincConflictDTO(
                    incoming.code(),
                    incoming.description(),
                    incoming.category().name(),
                    existing.getDescription(),
                    existing.getCategory().name()
            ));
        }

        if (!overwrite && !conflicts.isEmpty()) {
            LOG.info("LOINC import aborted due to {} conflict(s).", conflicts.size());
            return new LoincImportResultDTO(totalRows, 0, 0, conflicts);
        }

        // Insert or update
        Integer inserted = 0;
        Integer updated = 0;
        for (CsvRow row : rows) {
            LoincCode existing = existingByCode.get(row.code());
            if (existing == null) {
                repository.save(LoincCode.builder()
                        .code(row.code())
                        .description(row.description())
                        .category(row.category())
                        .lastUpdated(Instant.now())
                        .build());
                inserted++;
            } else if (overwrite) {
                existing.setDescription(row.description());
                existing.setCategory(row.category());
                existing.setLastUpdated(Instant.now());
                repository.save(existing);
                updated++;
            }
        }

        LOG.info("LOINC import done. Inserted={}, Updated={}, Conflicts={}", inserted, updated, conflicts.size());
        return new LoincImportResultDTO(totalRows, inserted, updated, overwrite ? List.of() : conflicts);
    }

    // ====================== READ / FILTER ======================

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<LoincCode> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<LoincCode> findByCategory(LoincCategory category, Pageable pageable) {
        return repository.findByCategory(category, pageable);
    }

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<LoincCode> findByCodeContainingIgnoreCase(String code, Pageable pageable) {
        return repository.findByCodeContainingIgnoreCase(code, pageable);
    }

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<LoincCode> findByDescriptionContainingIgnoreCase(String description, Pageable pageable) {
        return repository.findByDescriptionContainingIgnoreCase(description, pageable);
    }

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<LoincCode> filter(String code, String description, LoincCategory category, Pageable pageable) {
        if (category != null) return findByCategory(category, pageable);
        if (code != null && !code.isBlank()) return findByCodeContainingIgnoreCase(code, pageable);
        if (description != null && !description.isBlank()) return findByDescriptionContainingIgnoreCase(description, pageable);
        return findAll(pageable);
    }

    // ====================== Internal helpers ======================

    private record CsvRow(String code, String description, LoincCategory category) {}

    private List<CSVRecord> readCsv(MultipartFile file) {
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withDelimiter(',')
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {
            return parser.getRecords();
        } catch (IOException ex) {
            throw new BadRequestAlertException("Error reading CSV file: " + ex.getMessage(), "loincode", "filereaderror");
        }
    }

    private String val(CSVRecord record, String column) {
        try {
            String value = record.get(column);
            if (value != null && !value.isBlank()) return value;
        } catch (IllegalArgumentException ignore) {}

        String bomColumn = "\uFEFF" + column;
        try {
            String value = record.get(bomColumn);
            if (value != null && !value.isBlank()) return value;
        } catch (IllegalArgumentException ignore) {}

        Map<String, Integer> headerMap = record.getParser().getHeaderMap();
        if (headerMap != null) {
            String wanted = column.replace(" ", "").toLowerCase(Locale.ROOT);
            for (String header : headerMap.keySet()) {
                String normalized = header.replace("\uFEFF", "")
                        .replace("\u200B", "")
                        .replace("\u00A0", " ")
                        .trim();
                if (normalized.replace(" ", "").equalsIgnoreCase(wanted)) {
                    String value = record.get(header);
                    if (value != null && !value.isBlank()) return value;
                }
            }
            String seen = headerMap.keySet().stream()
                    .map(h -> "'" + h.replace("\uFEFF", "").trim() + "'")
                    .collect(Collectors.joining(", "));
            throw new BadRequestAlertException(
                    "Mapping for '" + column + "' not found, expected one of [" + seen + "]",
                    "loincode",
                    "badheader"
            );
        }

        throw new BadRequestAlertException(
                "Missing '" + column + "' at line " + record.getRecordNumber(),
                "loincode",
                "missingfield"
        );
    }

    private LoincCategory parseCategory(String raw) {
        try {
            return LoincCategory.valueOf(raw.trim().toUpperCase().replace(' ', '_'));
        } catch (Exception ex) {
            throw new BadRequestAlertException("Invalid category: '" + raw + "'", "loincode", "badcategory");
        }
    }
}
