package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Icd10Code;
import com.dazzle.asklepios.repository.Icd10Repository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import jakarta.transaction.Transactional;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class Icd10Service {

    private static final Logger LOG = LoggerFactory.getLogger(Icd10Service.class);
    private final Icd10Repository repository;

    public Icd10Service(Icd10Repository repository) {
        this.repository = repository;
    }

    /**
     * Import ICD10 codes from a CSV file.
     */
    @Transactional
    public void importCsv(MultipartFile file) {
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withDelimiter(',')
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            Set<String> requiredHeaders = Set.of("code", "description", "version", "is_active");
            Set<String> fileHeaders = parser.getHeaderMap().keySet()
                    .stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            List<String> missingHeaders = requiredHeaders.stream()
                    .filter(h -> !fileHeaders.contains(h))
                    .toList();

            if (!missingHeaders.isEmpty()) {
                throw new BadRequestAlertException(
                        "icd10",
                        "missingheaders",
                        "Missing or incorrect column(s): " + String.join(", ", missingHeaders)
                );
            }


            List<CSVRecord> records = parser.getRecords();
            LOG.info("Starting ICD10 incremental CSV import. Total records: {}", records.size());


            Map<String, Long> codeCounts = records.stream()
                    .map(r -> r.get("code"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

            List<String> duplicates = codeCounts.entrySet().stream()
                    .filter(e -> e.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .toList();

            if (!duplicates.isEmpty()) {
                throw new BadRequestAlertException(
                        "Duplicate ICD10 code(s): " + String.join(", ", duplicates),
                        "icd10",
                        "Duplicate ICD10 code(s): " + String.join(", ", duplicates)
                );
            }


            List<Icd10Code> toSave = new ArrayList<>();

            for (CSVRecord record : records) {
                String code = record.get("code");
                if (code == null || code.isBlank()) {
                    throw new BadRequestAlertException(
                            "missingcode",
                            "icd10",
                            "Missing code at line " + record.getRecordNumber()
                    );
                }

                String description = record.get("description");
                String version = record.get("version");
                Boolean isActive = !"false".equalsIgnoreCase(record.get("is_active"));

                Optional<Icd10Code> existing = repository.findByCode(code);

                if (existing.isPresent()) {
                    Icd10Code entity = existing.get();


                    if (!Objects.equals(entity.getDescription(), description)
                            || !Objects.equals(entity.getVersion(), version)
                            || !Objects.equals(entity.getIsActive(), isActive)) {

                        entity.setDescription(description);
                        entity.setVersion(version);
                        entity.setIsActive(isActive);
                        entity.setLastUpdated(Instant.now());
                        toSave.add(entity);
                    }
                } else {

                    toSave.add(Icd10Code.builder()
                            .code(code)
                            .description(description)
                            .version(version)
                            .isActive(isActive)
                            .lastUpdated(Instant.now())
                            .build());
                }
            }

            repository.saveAll(toSave);

            LOG.info("ICD10 incremental import complete. {} records inserted/updated.", toSave.size());

        } catch (IOException e) {
            LOG.error("Error reading ICD10 CSV file: {}", e.getMessage(), e);
            throw new BadRequestAlertException("Error reading CSV file: " + e.getMessage(), "icd10", "filereaderror");

        } catch (BadRequestAlertException e) {
            LOG.warn("ICD10 CSV import validation failed: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            LOG.error("Unexpected error during ICD10 import: {}", e.getMessage(), e);
            throw new BadRequestAlertException("Unexpected error during import: " + e.getMessage(), "icd10", "unexpected");
        }
    }


    /**
     * Get paginated ICD10 codes.
     */
    public Page<Icd10Code> findAll(Pageable pageable) {
        LOG.debug("Fetching all ICD10 codes with pagination: {}", pageable);
        return repository.findAll(pageable);
    }
    
    /**
     * Find ICD10 code by its code value.
     */
    public Icd10Code findByCode(String code) {
        LOG.debug("Fetching ICD10 code: {}", code);
        return repository.findByCode(code)
                .orElseThrow(() -> new NotFoundAlertException(
                        "notfound",
                        "icd10",
                        "ICD10 code not found: " + code
                ));
    }

    public Page<Icd10Code> searchByCodeOrDescription(String keyword, Pageable pageable) {
        return repository.findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable);
    }


}
