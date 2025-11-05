package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CdtCode;
import com.dazzle.asklepios.domain.enumeration.CdtClass;
import com.dazzle.asklepios.repository.CdtCodeRepository;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CdtCodeService {

    private static final Logger LOG = LoggerFactory.getLogger(CdtCodeService.class);
    private final CdtCodeRepository repository;

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
            String incomingClass,
            Boolean incomingIsActive,
            String existingDescription,
            String existingClass,
            Boolean existingIsActive
    ) {}

    // ====================== IMPORT ======================

    @Transactional
    public ImportResult importCsv(MultipartFile file, boolean overwrite) {
        List<CSVRecord> records = readCsv(file);
        LOG.info("Starting CDT CSV import (overwrite={}). Total records: {}", overwrite, records.size());

        // Check duplicates inside the uploaded CSV (by code)
        Map<String, Long> codeCounts = records.stream()
                .map(r -> val(r, "code"))
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        List<String> dupInFile = codeCounts.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList();

        if (!dupInFile.isEmpty()) {
            throw new BadRequestAlertException(
                    "Duplicate CDT code(s) in CSV: " + String.join(", ", dupInFile),
                    "cdtcode",
                    "duplicate"
            );
        }

        // Map incoming rows
        List<CsvRow> rows = new ArrayList<>();
        for (CSVRecord rec : records) {
            rows.add(new CsvRow(
                    val(rec, "code").trim(),
                    val(rec, "description").trim(),
                    parseClass(val(rec, "class")),
                    parseBoolean(val(rec, "is active"))
            ));
        }
        int totalRows = rows.size();

        // Quick lookup by code
        Map<String, CsvRow> byCode = rows.stream().collect(Collectors.toMap(CsvRow::code, r -> r));

        // Load existing by code
        Map<String, CdtCode> existingMap = new HashMap<>();
        for (String code : byCode.keySet()) {
            repository.findByCode(code).ifPresent(e -> existingMap.put(code, e));
        }

        // Conflicts (if not overwriting)
        List<Conflict> conflicts = new ArrayList<>();
        for (Map.Entry<String, CdtCode> e : existingMap.entrySet()) {
            CsvRow incoming = byCode.get(e.getKey());
            CdtCode ex = e.getValue();
            conflicts.add(new Conflict(
                    incoming.code(),
                    incoming.description(),
                    incoming.cdtClass().name(),
                    incoming.isActive(),
                    ex.getDescription(),
                    ex.getCdtClass().name(),
                    ex.getIsActive()
            ));
        }

        if (!overwrite && !conflicts.isEmpty()) {
            LOG.info("CDT import aborted due to {} conflict(s).", conflicts.size());
            return new ImportResult(totalRows, 0, 0, conflicts);
        }

        // Inserts / updates
        int inserted = 0, updated = 0;
        for (CsvRow r : rows) {
            CdtCode ex = existingMap.get(r.code());
            if (ex == null) {
                repository.save(CdtCode.builder()
                        .code(r.code())
                        .description(r.description())
                        .cdtClass(r.cdtClass())
                        .isActive(r.isActive())
                        .lastUpdated(Instant.now())
                        .build());
                inserted++;
            } else if (overwrite) {
                ex.setDescription(r.description());
                ex.setCdtClass(r.cdtClass());
                ex.setIsActive(r.isActive());
                ex.setLastUpdated(Instant.now());
                repository.save(ex);
                updated++;
            }
        }

        LOG.info("CDT import done. Inserted={}, Updated={}, Conflicts={}", inserted, updated, conflicts.size());
        return new ImportResult(totalRows, inserted, updated, overwrite ? List.of() : conflicts);
    }

    // ====================== READ / FILTER ONLY ======================

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

    /**
     * Flexible filter based on which parameter is provided.
     * Priority:
     *  - class -> filter by CDT class
     *  - code -> filter by code fragment
     *  - description -> filter by description fragment
     *  - (optional) isActive -> filter by active flag
     *  - else -> return all
     */
    @Transactional(value = Transactional.TxType.SUPPORTS)
    public Page<CdtCode> filter(String code, String description, CdtClass cdtClass, Boolean isActive, Pageable pageable) {
        LOG.debug("CDT filter code='{}', description='{}', class='{}', isActive='{}', pageable={}",
                code, description, cdtClass, isActive, pageable);

        if (cdtClass != null) {
            return findByClass(cdtClass, pageable);
        }
        if (code != null && !code.isBlank()) {
            return findByCodeContainingIgnoreCase(code, pageable);
        }
        if (description != null && !description.isBlank()) {
            return findByDescriptionContainingIgnoreCase(description, pageable);
        }
        if (isActive != null) {
            return findByIsActive(isActive, pageable);
        }
        return findAll(pageable);
    }

    // ====================== Internal helpers ======================

    private record CsvRow(String code, String description, CdtClass cdtClass, Boolean isActive) {}

    private static final Set<String> REQ_HEADERS =
            Set.of("code", "description", "class", "is active");

    // Allow common aliases (case-insensitive)
    private static final Map<String, Set<String>> HEADER_ALIASES = Map.of(
            "code", Set.of("code"),
            "description", Set.of("description", "desc"),
            "class", Set.of("class", "category", "cdtclass", "cdt_class"),
            "is active", Set.of("is active", "is_active", "isActive", "active")
    );

    private List<CSVRecord> readCsv(MultipartFile file) {
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withDelimiter(',')
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            // Validate required headers up-front (with aliases and BOM stripping)
            ensureHeaders(parser);

            return parser.getRecords();
        } catch (IOException e) {
            LOG.error("Error reading CDT CSV file: {}", e.getMessage(), e);
            throw new BadRequestAlertException(
                    "Error reading CSV file: " + e.getMessage(),
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

        for (String required : REQ_HEADERS) {
            Set<String> aliases = HEADER_ALIASES.getOrDefault(required, Set.of(required));
            boolean present = headers.stream().anyMatch(h ->
                    aliases.stream().anyMatch(a -> a.equalsIgnoreCase(h)));
            if (!present) {
                throw new BadRequestAlertException(
                        "Missing required column header: '" + required + "'",
                        "cdtcode",
                        "missingheader"
                );
            }
        }
    }

    private String val(CSVRecord r, String canonicalCol) {
        // Try direct
        try {
            String direct = r.get(canonicalCol);
            if (direct != null && !direct.isBlank()) return direct;
        } catch (IllegalArgumentException ignored) {
            // fall through to alias search
        }

        // Try aliases (case-insensitive), handling BOM on first header
        Set<String> aliases = HEADER_ALIASES.getOrDefault(canonicalCol, Set.of(canonicalCol));
        for (String k : r.toMap().keySet()) {
            if (k == null) continue;
            String cleaned = cleanHeaderKey(k);
            if (aliases.stream().anyMatch(a -> a.equalsIgnoreCase(cleaned))) {
                String v = r.get(k);
                if (v == null || v.isBlank()) {
                    throw new BadRequestAlertException(
                            "Missing '" + canonicalCol + "' at line " + r.getRecordNumber(),
                            "cdtcode",
                            "missingfield"
                    );
                }
                return v;
            }
        }

        throw new BadRequestAlertException(
                "Missing '" + canonicalCol + "' at line " + r.getRecordNumber(),
                "cdtcode",
                "missingfield"
        );
    }

    private String cleanHeaderKey(String header) {
        // strip BOM + trim
        return header.replace("\uFEFF", "").trim();
    }

    private CdtClass parseClass(String raw) {
        try {
            // Normalize: replace '&' with AND, collapse non-alnum to underscores
            String normalized = raw.trim()
                    .toUpperCase()
                    .replace("&", " AND ")
                    .replaceAll("[^A-Z0-9]+", "_")
                    .replaceAll("_+", "_")
                    .replaceAll("^_|_$", "");
            return CdtClass.valueOf(normalized);
        } catch (Exception e) {
            throw new BadRequestAlertException(
                    "Invalid class: '" + raw + "'. Please match one of the defined CDT classes.",
                    "cdtcode",
                    "badclass"
            );
        }
    }

    private Boolean parseBoolean(String raw) {
        String s = raw.trim().toLowerCase();
        if (s.equals("true") || s.equals("t") || s.equals("yes") || s.equals("y") || s.equals("1")) return true;
        if (s.equals("false") || s.equals("f") || s.equals("no") || s.equals("n") || s.equals("0")) return false;

        throw new BadRequestAlertException(
                "Invalid 'is active' value: '" + raw + "'. Expected TRUE/FALSE, YES/NO, or 1/0.",
                "cdtcode",
                "badboolean"
        );
    }
}
