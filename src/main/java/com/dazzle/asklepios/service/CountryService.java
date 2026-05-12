package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Country;
import com.dazzle.asklepios.domain.enumeration.CountryName;
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
import java.util.List;
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

    public Country create(Country countryRequest) {
        LOG.info("[CREATE] Request to create Country payload={}", countryRequest);

        if (countryRequest == null) {
            throw new BadRequestAlertException("Country payload is required", "country", "payload.required");
        }

        Country entity = Country.builder()
                .name(countryRequest.getName())
                .code(countryRequest.getCode() != null ? countryRequest.getCode().trim() : null)
                .isActive(countryRequest.getIsActive() != null ? countryRequest.getIsActive() : Boolean.TRUE)
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

    public Optional<Country> update(Long id, Country countryRequest) {
        LOG.info("[UPDATE] Request to update Country id={} payload={}", id, countryRequest);

        if (id == null) {
            throw new BadRequestAlertException("Country id is required", "country", "id.required");
        }
        if (countryRequest == null) {
            throw new BadRequestAlertException("Country payload is required", "country", "payload.required");
        }

        Country existing = countryRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Country not found with id " + id, "country", "notfound"));

        existing.setName(countryRequest.getName());
        existing.setCode(countryRequest.getCode() != null ? countryRequest.getCode().trim() : null);
        existing.setIsActive(countryRequest.getIsActive());

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
    public Page<Country> findActive(Pageable pageable) {
        LOG.debug("Fetching active Countries pageable={}", pageable);
        return countryRepository.findByIsActiveTrue(pageable);
    }


    @Transactional(readOnly = true)
    public Page<Country> findByName(CountryName name, Pageable pageable) {
        LOG.debug("Fetching Countries by name='{}' pageable={}", name, pageable);
        if (name == null) {
            return countryRepository.findAll(pageable);
        }
        return countryRepository.findByName(name, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Country> findByCode(String code, Pageable pageable) {
        LOG.debug("Fetching Countries by code like='{}' pageable={}", code, pageable);
        if (code == null || code.trim().isEmpty()) {
            return countryRepository.findAll(pageable);
        }
        return countryRepository.findByCode(code.trim(), pageable);
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
    @Transactional(readOnly = true)
    public Country findById(Long id) {
        LOG.debug("[GET country BY ID] id={}", id);

        return countryRepository.findById(id)
                .orElseThrow(() -> {
                    LOG.warn("[GET country BY ID] country not found id={}", id);
                    return new NotFoundAlertException(
                            "country not found with id " + id,
                            "country",
                            "country.notfound"
                    );
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
    @Transactional(readOnly = true)
    public List<Country> findByIds(List<Long> ids) {
        LOG.debug("[COUNTRY][FIND BY IDS] Request to find countries by ids={}", ids);

        if (ids == null || ids.isEmpty()) {
            LOG.warn("[COUNTRY][FIND BY IDS] Empty or null ids list received, returning empty list");
            return java.util.Collections.emptyList();
        }

        List<Country> result = countryRepository.findAllById(ids);
        LOG.info("[COUNTRY][FIND BY IDS] Found {} countries for {} requested ids",
                result.size(), ids.size());

        return result;
    }

}
