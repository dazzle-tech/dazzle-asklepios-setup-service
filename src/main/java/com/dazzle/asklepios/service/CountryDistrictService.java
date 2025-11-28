package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Country;
import com.dazzle.asklepios.domain.CountryDistrict;
import com.dazzle.asklepios.repository.CountryDistrictRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import jakarta.persistence.EntityManager;
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
public class CountryDistrictService {

    private static final Logger LOG = LoggerFactory.getLogger(CountryDistrictService.class);

    private final CountryDistrictRepository districtRepository;
    private final EntityManager entityManager;

    public CountryDistrictService(CountryDistrictRepository districtRepository, EntityManager entityManager) {
        this.districtRepository = districtRepository;
        this.entityManager = entityManager;
    }

    public CountryDistrict create(Long countryId, CountryDistrict incoming) {
        LOG.info("[CREATE] Request to create CountryDistrict for countryId={} payload={}", countryId, incoming);

        if (countryId == null) {
            throw new BadRequestAlertException("Country id is required", "countryDistrict", "country.required");
        }
        if (incoming == null) {
            throw new BadRequestAlertException("CountryDistrict payload is required", "countryDistrict", "payload.required");
        }

        CountryDistrict entity = CountryDistrict.builder()
                .country(refCountry(countryId))
                .name(incoming.getName().trim())
                .code(incoming.getCode().trim())
                .isActive(incoming.getIsActive() != null ? incoming.getIsActive() : Boolean.TRUE)
                .build();

        try {
            CountryDistrict saved = districtRepository.saveAndFlush(entity);
            LOG.info("Successfully created CountryDistrict id={} name='{}' code='{}' countryId={}",
                    saved.getId(), saved.getName(), saved.getCode(), countryId);
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            throw handleConstraintViolationOnCreateOrUpdate(ex, "create");
        }
    }

    public Optional<CountryDistrict> update(Long id, CountryDistrict incoming) {
        LOG.info("[UPDATE] Request to update CountryDistrict id={} payload={}", id, incoming);

        if (id == null) {
            throw new BadRequestAlertException("CountryDistrict id is required", "countryDistrict", "id.required");
        }
        if (incoming == null) {
            throw new BadRequestAlertException("CountryDistrict payload is required", "countryDistrict", "payload.required");
        }

        CountryDistrict existing = districtRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("CountryDistrict not found with id " + id, "countryDistrict", "notfound"));

        existing.setName(incoming.getName().trim());
        existing.setCode(incoming.getCode().trim());
        existing.setIsActive(incoming.getIsActive());

        try {
            CountryDistrict updated = districtRepository.saveAndFlush(existing);
            LOG.info("Successfully updated CountryDistrict id={} name='{}' code='{}'",
                    updated.getId(), updated.getName(), updated.getCode());
            return Optional.of(updated);
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            throw handleConstraintViolationOnCreateOrUpdate(ex, "update");
        }
    }

    @Transactional(readOnly = true)
    public Page<CountryDistrict> findAll(Pageable pageable) {
        LOG.debug("Fetching paged CountryDistricts (all countries) pageable={}", pageable);
        return districtRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<CountryDistrict> findByCountry(Long countryId, Pageable pageable) {
        LOG.debug("Fetching CountryDistricts for countryId={} pageable={}", countryId, pageable);
        if (countryId == null) {
            throw new BadRequestAlertException("Country id is required", "countryDistrict", "country.required");
        }
        return districtRepository.findByCountry_Id(countryId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CountryDistrict> findByName(Long countryId, String name, Pageable pageable) {
        LOG.debug("Fetching CountryDistricts for countryId={} by name='{}' pageable={}", countryId, name, pageable);
        if (countryId == null) {
            throw new BadRequestAlertException("Country id is required", "countryDistrict", "country.required");
        }
        if (name == null || name.trim().isEmpty()) {
            return districtRepository.findByCountry_Id(countryId, pageable);
        }
        return districtRepository.findByCountry_IdAndNameContainingIgnoreCase(countryId, name.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<CountryDistrict> findByCode(Long countryId, String code, Pageable pageable) {
        LOG.debug("Fetching CountryDistricts for countryId={} by code='{}' pageable={}", countryId, code, pageable);
        if (countryId == null) {
            throw new BadRequestAlertException("Country id is required", "countryDistrict", "country.required");
        }
        if (code == null || code.trim().isEmpty()) {
            return districtRepository.findByCountry_Id(countryId, pageable);
        }
        return districtRepository.findByCountry_IdAndCodeContainingIgnoreCase(countryId, code.trim(), pageable);
    }

    @Transactional
    public Optional<CountryDistrict> toggleIsActive(Long id) {
        LOG.info("Toggling isActive for CountryDistrict id={}", id);

        return districtRepository.findById(id)
                .map(entity -> {
                    entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
                    entity.setLastModifiedDate(Instant.now());
                    CountryDistrict saved = districtRepository.save(entity);
                    LOG.info("CountryDistrict id={} active status changed to {}", id, saved.getIsActive());
                    return saved;
                });
    }

    private BadRequestAlertException handleConstraintViolationOnCreateOrUpdate(Exception ex, String operation) {
        Throwable root = getRootCause(ex);
        String message = (root != null ? root.getMessage() : ex.getMessage());
        String lower = message != null ? message.toLowerCase() : "";

        LOG.error("Database constraint violation while trying to {} CountryDistrict: {}", operation, message, ex);

        if (lower.contains("ux_country_districts_country_code")
                || lower.contains("ux_country_districts_country_name")
                || lower.contains("unique constraint")
                || lower.contains("duplicate key")
                || lower.contains("duplicate entry")) {

            return new BadRequestAlertException(
                    "A district with the same name or code already exists for this country.",
                    "countryDistrict",
                    "unique.countryDistrict.namecode"
            );
        }

        return new BadRequestAlertException(
                "Database constraint violated while trying to " + operation + " countryDistrict (check unique name/code or required fields).",
                "countryDistrict",
                "db.constraint"
        );
    }

    private Country refCountry(Long countryId) {
        return entityManager.getReference(Country.class, countryId);
    }
}
