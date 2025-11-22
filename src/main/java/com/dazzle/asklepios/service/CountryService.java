package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Country;
import com.dazzle.asklepios.repository.CountryRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class CountryService {

    private static final Logger LOG = LoggerFactory.getLogger(CountryService.class);

    private final CountryRepository countryRepository;

    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    public Country create(Country incoming) {
        LOG.info("[CREATE] Request to create Country payload={}", incoming);

        if (incoming == null) {
            throw new BadRequestAlertException("Country payload is required", "country", "payload.required");
        }

        Country entity = Country.builder()
                .name(incoming.getName().trim())
                .code(incoming.getCode().trim())
                .isActive(incoming.getIsActive() != null ? incoming.getIsActive() : Boolean.TRUE)
                .build();

        try {
            Country saved = countryRepository.saveAndFlush(entity);
            LOG.info("Successfully created Country id={} name='{}' code='{}'",
                    saved.getId(), saved.getName(), saved.getCode());
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            throw handleConstraintViolationOnCreateOrUpdate(ex, "create");
        }
    }

    public Optional<Country> update(Long id, Country incoming) {
        LOG.info("[UPDATE] Request to update Country id={} payload={}", id, incoming);

        if (id == null) {
            throw new BadRequestAlertException("Country id is required", "country", "id.required");
        }
        if (incoming == null) {
            throw new BadRequestAlertException("Country payload is required", "country", "payload.required");
        }

        Country existing = countryRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Country not found with id " + id, "country", "notfound"));

            existing.setName(incoming.getName().trim());
            existing.setCode(incoming.getCode().trim());
            existing.setIsActive(incoming.getIsActive());

        try {
            Country updated = countryRepository.saveAndFlush(existing);
            LOG.info("Successfully updated Country id={} name='{}' code='{}'",
                    updated.getId(), updated.getName(), updated.getCode());
            return Optional.of(updated);
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            throw handleConstraintViolationOnCreateOrUpdate(ex, "update");
        }
    }

    @Transactional(readOnly = true)
    public Page<Country> findAll(Pageable pageable) {
        LOG.debug("Fetching paged Countries pageable={}", pageable);
        return countryRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Country> findByName(String name, Pageable pageable) {
        LOG.debug("Fetching Countries by name like='{}' pageable={}", name, pageable);
        if (name == null || name.trim().isEmpty()) {
            return countryRepository.findAll(pageable);
        }
        return countryRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Country> findByCode(String code, Pageable pageable) {
        LOG.debug("Fetching Countries by code like='{}' pageable={}", code, pageable);
        if (code == null || code.trim().isEmpty()) {
            return countryRepository.findAll(pageable);
        }
        return countryRepository.findByCodeContainingIgnoreCase(code.trim(), pageable);
    }

    @Transactional
    public Optional<Country> toggleIsActive(Long id) {
        LOG.info("Toggling isActive for Country id={}", id);

        return countryRepository.findById(id)
                .map(entity -> {
                    entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
                    entity.setLastModifiedDate(Instant.now());
                    Country saved = countryRepository.save(entity);
                    LOG.info("Country id={} active status changed to {}", id, saved.getIsActive());
                    return saved;
                });
    }

    private BadRequestAlertException handleConstraintViolationOnCreateOrUpdate(Exception ex, String operation) {
        Throwable root = getRootCause(ex);
        String message = (root != null ? root.getMessage() : ex.getMessage());
        String lower = message != null ? message.toLowerCase() : "";

        LOG.error("Database constraint violation while trying to {} Country: {}", operation, message, ex);

        if (lower.contains("ux_country_name")
                || lower.contains("ux_country_code")
                || lower.contains("unique constraint")
                || lower.contains("duplicate key")
                || lower.contains("duplicate entry")) {

            return new BadRequestAlertException(
                    "A country with the same name or code already exists.",
                    "country",
                    "unique.country.namecode"
            );
        }

        return new BadRequestAlertException(
                "Database constraint violated while trying to " + operation + " country (check unique name/code or required fields).",
                "country",
                "db.constraint"
        );
    }
}
