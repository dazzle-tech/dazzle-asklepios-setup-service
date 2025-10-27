package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Icd10Code;
import com.dazzle.asklepios.repository.Icd10Repository;
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


    @Transactional
    public void importCsv(MultipartFile file) {
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withDelimiter(';')
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            List<CSVRecord> records = parser.getRecords();
            LOG.info("Starting CSV import. Records found: {}", records.size());

            // 1. check duplicates
            Map<String, Long> codeCounts = records.stream()
                    .map(r -> r.get("code"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

            List<String> duplicates = codeCounts.entrySet().stream()
                    .filter(e -> e.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .toList();
            if (!duplicates.isEmpty()) {
                throw new IllegalStateException("Duplicate record(s): " + String.join(", ", duplicates));
            }

            // 2. process records
            Set<String> csvCodes = new HashSet<>();
            List<Icd10Code> toSave = new ArrayList<>();

            for (CSVRecord record : records) {
                String code = record.get("code");
                if (code == null || code.isBlank()) {
                    throw new IllegalArgumentException("Missing code at line " + record.getRecordNumber());
                }

                csvCodes.add(code);
                String description = record.get("description");
                String version = record.get("version");
                boolean isActive = !"false".equalsIgnoreCase(record.get("is_active"));

                Optional<Icd10Code> existing = repository.findByCode(code);

                if (existing.isPresent()) {
                    Icd10Code e = existing.get();
                    if (!Objects.equals(e.getDescription(), description)
                            || !Objects.equals(e.getVersion(), version)
                            || !Objects.equals(e.getIsActive(), isActive)) {
                        e.setDescription(description);
                        e.setVersion(version);
                        e.setIsActive(isActive);
                        e.setLastUpdated(Instant.now());
                        toSave.add(e);
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

            // 3. deactivate missing
            List<Icd10Code> toDeactivate = repository.findAll().stream()
                    .filter(c -> !csvCodes.contains(c.getCode()) && Boolean.TRUE.equals(c.getIsActive()))
                    .peek(c -> {
                        c.setIsActive(false);
                        c.setLastUpdated(Instant.now());
                    })
                    .toList();

            repository.saveAll(toDeactivate);
            LOG.info("Import complete. {} updated/inserted, {} deactivated.", toSave.size(), toDeactivate.size());

        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file", e);
        }
    }




    public Page<Icd10Code> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }


}
