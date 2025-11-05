package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CptCode;
import com.dazzle.asklepios.domain.enumeration.CptCategory;
import com.dazzle.asklepios.repository.CptCodeRepository;
import com.dazzle.asklepios.service.dto.CptConflictDTO;
import com.dazzle.asklepios.service.dto.CptImportResultDTO;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CptCodeService {

    private static final Logger LOG = LoggerFactory.getLogger(CptCodeService.class);
    private final CptCodeRepository repository;

    @Transactional
    public CptImportResultDTO importCsv(MultipartFile file, boolean overwrite) {
        List<CSVRecord> records = readCsv(file);
        LOG.info("Starting CPT CSV import (overwrite={}). Total records: {}", overwrite, records.size());

        Map<String, Long> codeCounts = records.stream()
                .map(record -> val(record, "code"))
                .collect(Collectors.groupingBy(code -> code, Collectors.counting()));

        List<String> duplicatesInFile = codeCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList();

        if (!duplicatesInFile.isEmpty()) {
            throw new BadRequestAlertException(
                    "Duplicate CPT code(s) in CSV: " + String.join(", ", duplicatesInFile),
                    "cptcode",
                    "duplicate"
            );
        }

        List<CsvRow> rows = new ArrayList<>();
        for (CSVRecord record : records) {
            rows.add(new CsvRow(
                    val(record, "code").trim(),
                    val(record, "description").trim(),
                    parseCategory(val(record, "category"))
            ));
        }
        int totalRows = rows.size();

        List<CptConflictDTO> conflicts = new ArrayList<>();
        Map<String, CsvRow> rowsByCode = rows.stream().collect(Collectors.toMap(CsvRow::code, row -> row));

        Map<String, CptCode> existingByCode = new HashMap<>();
        for (String code : rowsByCode.keySet()) {
            repository.findByCode(code).ifPresent(existing -> existingByCode.put(code, existing));
        }

        for (Map.Entry<String, CptCode> entry : existingByCode.entrySet()) {
            CsvRow incoming = rowsByCode.get(entry.getKey());
            CptCode existing = entry.getValue();
            conflicts.add(new CptConflictDTO(
                    incoming.code(),
                    incoming.description(),
                    incoming.category().name(),
                    existing.getDescription(),
                    existing.getCategory().name()
            ));
        }

        if (!overwrite && !conflicts.isEmpty()) {
            LOG.info("CPT import aborted due to {} conflict(s).", conflicts.size());
            return new CptImportResultDTO(totalRows, 0, 0, conflicts);
        }

        int inserted = 0;
        int updated = 0;

        for (CsvRow row : rows) {
            CptCode existing = existingByCode.get(row.code());
            if (existing == null) {
                repository.save(CptCode.builder()
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

        LOG.info("CPT import done. Inserted={}, Updated={}, Conflicts={}", inserted, updated, conflicts.size());
        return new CptImportResultDTO(totalRows, inserted, updated, overwrite ? List.of() : conflicts);
    }

    // ====================== READ / FILTER ONLY ======================

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<CptCode> findAll(Pageable pageable) {
        LOG.debug("Fetching all CPT codes: pageable={}", pageable);
        return repository.findAll(pageable);
    }

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<CptCode> findByCategory(CptCategory category, Pageable pageable) {
        LOG.debug("Filtering CPT codes by category={} pageable={}", category, pageable);
        return repository.findByCategory(category, pageable);
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

    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<CptCode> filter(String code, String description, CptCategory category, Pageable pageable) {
        LOG.debug("CPT filter code='{}', description='{}', category='{}', pageable={}",
                code, description, category, pageable);

        if (category != null) {
            return findByCategory(category, pageable);
        }
        if (code != null && !code.isBlank()) {
            return findByCodeContainingIgnoreCase(code, pageable);
        }
        if (description != null && !description.isBlank()) {
            return findByDescriptionContainingIgnoreCase(description, pageable);
        }
        return findAll(pageable);
    }

    // ====================== Internal helpers ======================

    private record CsvRow(String code, String description, CptCategory category) {}

    private List<CSVRecord> readCsv(MultipartFile file) {
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withDelimiter(',')
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {
            return parser.getRecords();
        } catch (IOException ex) {
            LOG.error("Error reading CPT CSV file: {}", ex.getMessage(), ex);
            throw new BadRequestAlertException(
                    "Error reading CSV file: " + ex.getMessage(),
                    "cptcode",
                    "filereaderror"
            );
        }
    }

    private String val(CSVRecord record, String column) {
        String value = record.get(column);
        if (value == null || value.isBlank()) {
            throw new BadRequestAlertException(
                    "Missing '" + column + "' at line " + record.getRecordNumber(),
                    "cptcode",
                    "missingfield"
            );
        }
        return value;
    }

    private CptCategory parseCategory(String raw) {
        try {
            return CptCategory.valueOf(raw.trim().toUpperCase().replace(' ', '_'));
        } catch (Exception ex) {
            throw new BadRequestAlertException(
                    "Invalid category: '" + raw + "'",
                    "cptcode",
                    "badcategory"
            );
        }
    }
}
