package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Vaccine;
import com.dazzle.asklepios.domain.enumeration.RouteOfAdministration;
import com.dazzle.asklepios.domain.enumeration.VaccineType;
import com.dazzle.asklepios.repository.VaccineRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class VaccineService {

    private static final Logger LOG = LoggerFactory.getLogger(VaccineService.class);

    private final VaccineRepository vaccineRepository;

    public VaccineService(VaccineRepository vaccineRepository) {
        this.vaccineRepository = vaccineRepository;
    }

    public Vaccine create(Vaccine incoming) {
        LOG.info("[CREATE] Request to create Vaccine payload={}", incoming);

        if (incoming == null) {
            throw new BadRequestAlertException("Vaccine payload is required", "vaccine", "payload.required");
        }

        Vaccine entity = Vaccine.builder()
                .name(incoming.getName())
                .atcCode(incoming.getAtcCode())
                .type(incoming.getType())
                .roa(incoming.getRoa())
                .siteOfAdministration(incoming.getSiteOfAdministration())
                .postOpeningDuration(incoming.getPostOpeningDuration())
                .numberOfDoses(incoming.getNumberOfDoses())
                .durationUnit(incoming.getDurationUnit())
                .indications(incoming.getIndications())
                .possibleReactions(incoming.getPossibleReactions())
                .contraindicationsAndPrecautions(incoming.getContraindicationsAndPrecautions())
                .storageAndHandling(incoming.getStorageAndHandling())
                .isActive(Boolean.TRUE.equals(incoming.getIsActive()))
                .build();

        try {
            Vaccine saved = vaccineRepository.saveAndFlush(entity);
            LOG.info("Successfully created vaccine id={} name='{}'", saved.getId(), saved.getName());
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException constraintException) {
            handleConstraintsOnCreateOrUpdate(constraintException);
            throw constraintException;
        }
    }

    public Optional<Vaccine> update(Long id, Vaccine incoming) {
        LOG.info("[UPDATE] Request to update Vaccine id={} payload={}", id, incoming);

        if (incoming == null) {
            throw new BadRequestAlertException("Vaccine payload is required", "vaccine", "payload.required");
        }

        Vaccine existing = vaccineRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Vaccine not found with id " + id, "vaccine", "notfound"));

        existing.setName(incoming.getName());
        existing.setAtcCode(incoming.getAtcCode());
        existing.setType(incoming.getType());
        existing.setRoa(incoming.getRoa());
        existing.setSiteOfAdministration(incoming.getSiteOfAdministration());
        existing.setNumberOfDoses(incoming.getNumberOfDoses());
        existing.setPostOpeningDuration(incoming.getPostOpeningDuration());
        existing.setDurationUnit(incoming.getDurationUnit());
        existing.setIndications(incoming.getIndications());
        existing.setPossibleReactions(incoming.getPossibleReactions());
        existing.setContraindicationsAndPrecautions(incoming.getContraindicationsAndPrecautions());
        existing.setStorageAndHandling(incoming.getStorageAndHandling());
        existing.setIsActive(incoming.getIsActive());

        try {
            Vaccine updated = vaccineRepository.saveAndFlush(existing);
            LOG.info("Successfully updated vaccine id={} (name='{}')", updated.getId(), updated.getName());
            return Optional.of(updated);
        } catch (DataIntegrityViolationException | JpaSystemException constraintException) {
            handleConstraintsOnCreateOrUpdate(constraintException);
            throw constraintException;
        }
    }

    @Transactional(readOnly = true)
    public Page<Vaccine> findAll(Pageable pageable) {
        LOG.debug("Fetching paged Vaccines pageable={}", pageable);
        return vaccineRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Vaccine> findByName(String name, Pageable pageable) {
        LOG.debug("Fetching Vaccines by name='{}'", name);
        return vaccineRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Vaccine> findByRoa(RouteOfAdministration roa, Pageable pageable) {
        LOG.debug("Fetching Vaccines by roa={}", roa);
        return vaccineRepository.findByRoa(roa, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Vaccine> findByType(VaccineType type, Pageable pageable) {
        LOG.debug("Fetching Vaccines by type={}", type);
        return vaccineRepository.findByType(type, pageable);
    }

    public Optional<Vaccine> toggleIsActive(Long id) {
        LOG.info("Toggling isActive for Vaccine id={}", id);
        return vaccineRepository.findById(id)
                .map(entity -> {
                    entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
                    entity.setLastModifiedDate(Instant.now());
                    Vaccine saved = vaccineRepository.save(entity);
                    LOG.info("Vaccine id={} active status changed to {}", id, saved.getIsActive());
                    return saved;
                });
    }

    private void handleConstraintsOnCreateOrUpdate(RuntimeException constraintException) {
        Throwable root = getRootCause(constraintException);
        String message = (root != null ? root.getMessage() : constraintException.getMessage());
        String lower = (message != null ? message.toLowerCase() : "");

        LOG.error("Database constraint violation while saving vaccine: {}", message, constraintException);

        if (lower.contains("ux_vaccine_name_type_roa")
                || lower.contains("unique constraint")
                || lower.contains("duplicate key")
                || lower.contains("duplicate entry")) {
            throw new BadRequestAlertException(
                    "A vaccine with the same (name, type, roa) already exists.",
                    "vaccine",
                    "unique.name.type.roa"
            );
        }

        throw new BadRequestAlertException(
                "Database constraint violated while saving vaccine (check required fields or unique constraints).",
                "vaccine",
                "db.constraint"
        );
    }
}
