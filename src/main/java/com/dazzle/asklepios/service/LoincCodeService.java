package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.LoincCode;
import com.dazzle.asklepios.domain.enumeration.LoincCategory;
import com.dazzle.asklepios.repository.LoincCodeRepository;
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

    public record ImportResult(int totalRows, int inserted, int updated, List<Conflict> conflicts) {}
    public record Conflict(String code, String incomingDescription, String incomingCategory, String existingDescription, String existingCategory) {}

    /** Import CSV file of LOINC codes with conflict handling */
    @Transactional
    public ImportResult importCsv(MultipartFile file, boolean overwrite) {
        List<CSVRecord> records = readCsv(file);
        LOG.info("Starting LOINC CSV import (overwrite={}). Total records: {}", overwrite, records.size());

        // Detect duplicates in file
        Map<String, Long> codeCounts = records.stream()
                .map(r -> val(r, "code"))
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        List<String> dupInFile = codeCounts.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList();

        if (!dupInFile.isEmpty()) {
            throw new BadRequestAlertException("Duplicate LOINC code(s): " + String.join(", ", dupInFile), "loincode", "duplicate");
        }

        // Build row list
        List<CsvRow> rows = new ArrayList<>();
        for (CSVRecord rec : records) {
            rows.add(new CsvRow(
                    val(rec, "code").trim(),
                    val(rec, "description").trim(),
                    parseCategory(val(rec, "category"))
            ));
        }

        Map<String, CsvRow> byCode = rows.stream().collect(Collectors.toMap(CsvRow::code, r -> r));
        Map<String, LoincCode> existingMap = new HashMap<>();
        byCode.keySet().forEach(code -> repository.findByCode(code).ifPresent(e -> existingMap.put(code, e)));

        // Detect DB conflicts
        List<Conflict> conflicts = new ArrayList<>();
        for (Map.Entry<String, LoincCode> e : existingMap.entrySet()) {
            CsvRow incoming = byCode.get(e.getKey());
            LoincCode ex = e.getValue();
            conflicts.add(new Conflict(
                    incoming.code(),
                    incoming.description(),
                    incoming.category().name(),
                    ex.getDescription(),
                    ex.getCategory().name()
            ));
        }

        if (!overwrite && !conflicts.isEmpty()) {
            return new ImportResult(rows.size(), 0, 0, conflicts);
        }

        // Insert or update
        int inserted = 0, updated = 0;
        for (CsvRow r : rows) {
            LoincCode ex = existingMap.get(r.code());
            if (ex == null) {
                repository.save(LoincCode.builder()
                        .code(r.code())
                        .description(r.description())
                        .category(r.category())
                        .lastUpdated(Instant.now())
                        .build());
                inserted++;
            } else if (overwrite) {
                ex.setDescription(r.description());
                ex.setCategory(r.category());
                ex.setLastUpdated(Instant.now());
                repository.save(ex);
                updated++;
            }
        }

        return new ImportResult(rows.size(), inserted, updated, overwrite ? List.of() : conflicts);
    }

    /** Get all LOINC codes (paged) */
    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<LoincCode> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /** Filter by category */
    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<LoincCode> findByCategory(LoincCategory category, Pageable pageable) {
        return repository.findByCategory(category, pageable);
    }

    /** Search by partial code */
    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<LoincCode> findByCodeContainingIgnoreCase(String code, Pageable pageable) {
        return repository.findByCodeContainingIgnoreCase(code, pageable);
    }

    /** Search by partial description */
    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<LoincCode> findByDescriptionContainingIgnoreCase(String description, Pageable pageable) {
        return repository.findByDescriptionContainingIgnoreCase(description, pageable);
    }

    /** Apply flexible filtering */
    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<LoincCode> filter(String code, String description, LoincCategory category, Pageable pageable) {
        if (category != null) return findByCategory(category, pageable);
        if (code != null && !code.isBlank()) return findByCodeContainingIgnoreCase(code, pageable);
        if (description != null && !description.isBlank()) return findByDescriptionContainingIgnoreCase(description, pageable);
        return findAll(pageable);
    }

    private record CsvRow(String code, String description, LoincCategory category) {}

    /** Read CSV file and parse records */
    private List<CSVRecord> readCsv(MultipartFile file) {
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withDelimiter(',')
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {
            return parser.getRecords();
        } catch (IOException e) {
            throw new BadRequestAlertException("Error reading CSV file: " + e.getMessage(), "loincode", "filereaderror");
        }
    }

    /** Retrieve a column value with BOM/header cleanup */
    private String val(CSVRecord r, String col) {
        try {
            String v = r.get(col);
            if (v != null && !v.isBlank()) return v;
        } catch (IllegalArgumentException ignore) {}

        String bomCol = "\uFEFF" + col;
        try {
            String v = r.get(bomCol);
            if (v != null && !v.isBlank()) return v;
        } catch (IllegalArgumentException ignore) {}

        Map<String, Integer> headerMap = r.getParser().getHeaderMap();
        if (headerMap != null) {
            String wanted = col.replace(" ", "").toLowerCase(Locale.ROOT);
            for (String h : headerMap.keySet()) {
                String norm = h.replace("\uFEFF", "").replace("\u200B", "").replace("\u00A0", " ").trim();
                if (norm.replace(" ", "").equalsIgnoreCase(wanted)) {
                    String v = r.get(h);
                    if (v != null && !v.isBlank()) return v;
                }
            }
            String seen = headerMap.keySet().stream()
                    .map(h -> "'" + h.replace("\uFEFF", "").trim() + "'")
                    .collect(Collectors.joining(", "));
            throw new BadRequestAlertException("Mapping for '" + col + "' not found, expected one of [" + seen + "]", "loincode", "badheader");
        }

        throw new BadRequestAlertException("Missing '" + col + "' at line " + r.getRecordNumber(), "loincode", "missingfield");
    }

    /** Parse string into LoincCategory enum */
    private LoincCategory parseCategory(String raw) {
        try {
            return LoincCategory.valueOf(raw.trim().toUpperCase().replace(' ', '_'));
        } catch (Exception e) {
            throw new BadRequestAlertException("Invalid category: '" + raw + "'", "loincode", "badcategory");
        }
    }
}
