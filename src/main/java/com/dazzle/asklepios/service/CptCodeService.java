package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CptCode;
import com.dazzle.asklepios.domain.enumeration.CptCategory;
import com.dazzle.asklepios.repository.CptCodeRepository;
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

    /** Response object returned to the client after an import attempt. */
    public record ImportResult(
            int totalRows,
            int inserted,
            int updated,
            List<Conflict> conflicts
    ) {}

    /** A single conflict (same code already exists in DB). */
    public record Conflict(
            String code,
            String incomingDescription,
            String incomingCategory,
            String existingDescription,
            String existingCategory
    ) {}

    @Transactional
    public ImportResult importCsv(MultipartFile file, boolean overwrite) {
        List<CSVRecord> records = readCsv(file);
        LOG.info("Starting CPT CSV import (overwrite={}). Total records: {}", overwrite, records.size());

        Map<String, Long> codeCounts = records.stream()
                .map(r -> val(r, "code"))
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        List<String> dupInFile = codeCounts.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList();

        if (!dupInFile.isEmpty()) {
            throw new BadRequestAlertException(
                    "Duplicate CPT code(s) in CSV: " + String.join(", ", dupInFile),
                    "cptcode",
                    "duplicate"
            );
        }

        List<CsvRow> rows = new ArrayList<>();
        for (CSVRecord rec : records) {
            rows.add(new CsvRow(
                    val(rec, "code").trim(),
                    val(rec, "description").trim(),
                    parseCategory(val(rec, "category"))
            ));
        }
        int totalRows = rows.size();

        List<Conflict> conflicts = new ArrayList<>();
        Map<String, CsvRow> byCode = rows.stream().collect(Collectors.toMap(CsvRow::code, r -> r));

        Map<String, CptCode> existingMap = new HashMap<>();
        for (String code : byCode.keySet()) {
            repository.findByCode(code).ifPresent(e -> existingMap.put(code, e));
        }

        for (Map.Entry<String, CptCode> e : existingMap.entrySet()) {
            CsvRow incoming = byCode.get(e.getKey());
            CptCode ex = e.getValue();
            conflicts.add(new Conflict(
                    incoming.code(),
                    incoming.description(),
                    incoming.category().name(),
                    ex.getDescription(),
                    ex.getCategory().name()
            ));
        }

        if (!overwrite && !conflicts.isEmpty()) {
            LOG.info("CPT import aborted due to {} conflict(s).", conflicts.size());
            return new ImportResult(totalRows, 0, 0, conflicts);
        }

        int inserted = 0, updated = 0;

        for (CsvRow r : rows) {
            CptCode ex = existingMap.get(r.code());
            if (ex == null) {
                repository.save(CptCode.builder()
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

        LOG.info("CPT import done. Inserted={}, Updated={}, Conflicts={}", inserted, updated, conflicts.size());
        return new ImportResult(totalRows, inserted, updated, overwrite ? List.of() : conflicts);
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

    /**
     * Flexible filter based on which parameter is provided.
     * Priority:
     *  - category -> filter by category
     *  - code -> filter by code fragment
     *  - description -> filter by description fragment
     *  - else -> return all
     */
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
        } catch (IOException e) {
            LOG.error("Error reading CPT CSV file: {}", e.getMessage(), e);
            throw new BadRequestAlertException(
                    "Error reading CSV file: " + e.getMessage(),
                    "cptcode",
                    "filereaderror"
            );
        }
    }

    private String val(CSVRecord r, String col) {
        String v = r.get(col);
        if (v == null || v.isBlank()) {
            throw new BadRequestAlertException(
                    "Missing '" + col + "' at line " + r.getRecordNumber(),
                    "cptcode",
                    "missingfield"
            );
        }
        return v;
    }

    private CptCategory parseCategory(String raw) {
        try {
            return CptCategory.valueOf(raw.trim().toUpperCase().replace(' ', '_'));
        } catch (Exception e) {
            throw new BadRequestAlertException(
                    "Invalid category: '" + raw + "'",
                    "cptcode",
                    "badcategory"
            );
        }
    }
}
