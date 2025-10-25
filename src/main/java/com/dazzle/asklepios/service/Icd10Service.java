package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Icd10Code;
import com.dazzle.asklepios.repository.Icd10Repository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Icd10Service {

    private final Icd10Repository repository;

    @Transactional
    public void importCsv(MultipartFile file) {
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withDelimiter(';')
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

       
            Set<String> csvCodes = new HashSet<>();

            for (CSVRecord record : parser) {
                String code = record.get("code");
                String description = record.get("description");
                String version = record.get("version");


                String activeValue = record.isMapped("is_active") ? record.get("is_active") : "true";
                boolean isActive = !"false".equalsIgnoreCase(activeValue) && !"0".equals(activeValue);

                csvCodes.add(code);

                Optional<Icd10Code> existing = repository.findByCode(code);

                if (existing.isPresent()) {
                    Icd10Code entity = existing.get();
                    boolean changed = false;

                    if (!Objects.equals(entity.getDescription(), description)) {
                        entity.setDescription(description);
                        changed = true;
                    }
                    if (!Objects.equals(entity.getVersion(), version)) {
                        entity.setVersion(version);
                        changed = true;
                    }
                    if (!Objects.equals(entity.getIsActive(), isActive)) {
                        entity.setIsActive(isActive);
                        changed = true;
                    }

                    if (changed) {
                        entity.setLastUpdated(Instant.now());
                        repository.save(entity);
                    }

                } else {
                    // سجل جديد
                    Icd10Code newCode = Icd10Code.builder()
                            .code(code)
                            .description(description)
                            .version(version)
                            .isActive(isActive)
                            .lastUpdated(Instant.now())
                            .build();

                    repository.save(newCode);
                }
            }

            // تعطيل الأكواد التي لم تعد موجودة في CSV
            List<Icd10Code> allExisting = repository.findAll();
            List<Icd10Code> toDeactivate = allExisting.stream()
                    .filter(code -> !csvCodes.contains(code.getCode()) && Boolean.TRUE.equals(code.getIsActive()))
                    .collect(Collectors.toList());

            for (Icd10Code code : toDeactivate) {
                code.setIsActive(false);
                code.setLastUpdated(Instant.now());
                repository.save(code);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file", e);
        }
    }

    public List<Icd10Code> findAll() {
        return repository.findAll();
    }

}
