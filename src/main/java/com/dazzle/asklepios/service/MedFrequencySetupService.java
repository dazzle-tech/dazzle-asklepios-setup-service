package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.MedFrequencySetup;
import com.dazzle.asklepios.domain.enumeration.FrequencyList;
import com.dazzle.asklepios.domain.enumeration.MedFrequency;
import com.dazzle.asklepios.repository.MedFrequencySetupRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.dazzle.asklepios.service.dto.medFrequencySetup.MedFrequencySetupCreateDTO;
import com.dazzle.asklepios.service.dto.medFrequencySetup.MedFrequencySetupUpdateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class MedFrequencySetupService {

    private static final Logger LOG = LoggerFactory.getLogger(MedFrequencySetupService.class);
    private static final String ENTITY_NAME = "medFrequencySetup";

    private final MedFrequencySetupRepository repository;

    public MedFrequencySetupService(MedFrequencySetupRepository repository) {
        this.repository = repository;
    }

    public MedFrequencySetup create(MedFrequencySetupCreateDTO dto) {
        LOG.debug("Request to create MedFrequencySetup : {}", dto);
        validate(dto.frequency(), dto.frequencyNumber());

        if (repository.existsByFrequency(dto.frequency())) {
            throw new BadRequestAlertException(
                    "A setup already exists for this frequency", ENTITY_NAME, "frequencyexists");
        }

        MedFrequencySetup entity = MedFrequencySetup.builder()
                .frequency(dto.frequency())
                .frequencyNumber(dto.frequencyNumber())
                .frequencyUnit(dto.frequencyUnit())
                .isActive(true)
                .build();

        return repository.save(entity);
    }

    public MedFrequencySetup update(Long id, MedFrequencySetupUpdateDTO dto) {
        LOG.debug("Request to update MedFrequencySetup id={} with {}", id, dto);
        validate(dto.frequency(), dto.frequencyNumber());

        MedFrequencySetup entity = getById(id);

        if (repository.existsByFrequencyAndIdNot(dto.frequency(), id)) {
            throw new BadRequestAlertException(
                    "A setup already exists for this frequency", ENTITY_NAME, "frequencyexists");
        }

        entity.setFrequency(dto.frequency());
        entity.setFrequencyNumber(dto.frequencyNumber());
        entity.setFrequencyUnit(dto.frequencyUnit());

        return repository.save(entity);
    }

    public MedFrequencySetup toggleIsActive(Long id) {
        LOG.debug("Request to toggle MedFrequencySetup isActive id={}", id);
        MedFrequencySetup entity = getById(id);
        entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Page<MedFrequencySetup> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<MedFrequencySetup> findByStatus(Boolean isActive, Pageable pageable) {
        return repository.findByIsActive(isActive, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MedFrequencySetup> findByFrequencyNumber(Integer frequencyNumber, Pageable pageable) {
        return repository.findByFrequencyNumber(frequencyNumber, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MedFrequencySetup> findByFrequencyUnit(FrequencyList frequencyUnit, Pageable pageable) {
        return repository.findByFrequencyUnit(frequencyUnit, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<MedFrequencySetup> findByFrequency(MedFrequency frequency) {
        return repository.findByFrequency(frequency);
    }

    private MedFrequencySetup getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Frequency setup not found: " + id, ENTITY_NAME, "notfound"));
    }

    private static void validate(MedFrequency frequency, Integer frequencyNumber) {
        // STAT is a single immediate dose: it is never split
        if (frequency == MedFrequency.STAT) {
            throw new BadRequestAlertException(
                    "STAT is a single immediate dose and cannot be split", ENTITY_NAME, "statnotallowed");
        }
        if (frequencyNumber == null || frequencyNumber < 1) {
            throw new BadRequestAlertException(
                    "Frequency number must be at least 1", ENTITY_NAME, "frequencynumberinvalid");
        }
    }
}
