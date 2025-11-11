package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Vaccine;
import com.dazzle.asklepios.domain.VaccineBrands;
import com.dazzle.asklepios.repository.VaccineBrandsRepository;
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
public class VaccineBrandsService {

    private static final Logger LOG = LoggerFactory.getLogger(VaccineBrandsService.class);

    private final VaccineBrandsRepository vaccineBrandsRepository;
    private final EntityManager entityManager;

    public VaccineBrandsService(VaccineBrandsRepository vaccineBrandsRepository, EntityManager entityManager) {
        this.vaccineBrandsRepository = vaccineBrandsRepository;
        this.entityManager = entityManager;
    }

    public VaccineBrands create(Long vaccineId, VaccineBrands incoming) {
        LOG.info("[CREATE] Request to create VaccineBrand for vaccineId={} payload={}", vaccineId, incoming);

        if (vaccineId == null) {
            throw new BadRequestAlertException("Vaccine id is required", "vaccineBrand", "vaccine.required");
        }
        if (incoming == null) {
            throw new BadRequestAlertException("Vaccine brand payload is required", "vaccineBrand", "payload.required");
        }
        VaccineBrands entity = VaccineBrands.builder()
                .vaccine(refVaccine(vaccineId))
                .name(incoming.getName())
                .manufacture(incoming.getManufacture())
                .volume(incoming.getVolume())
                .unit(incoming.getUnit())
                .marketingAuthorizationHolder(incoming.getMarketingAuthorizationHolder())
                .isActive(Boolean.TRUE.equals(incoming.getIsActive()))
                .build();

        try {
            VaccineBrands saved = vaccineBrandsRepository.saveAndFlush(entity);
            LOG.info("Successfully created vaccine brand id={} name='{}' for vaccineId={}", saved.getId(), saved.getName(), vaccineId);
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException constraintException) {
            handleConstraintViolation(constraintException);
            throw constraintException;
        }
    }


    public Optional<VaccineBrands> update(Long id, Long vaccineId, VaccineBrands incoming) {
        LOG.info("[UPDATE] Request to update VaccineBrand id={} vaccineId={} payload={}", id, vaccineId, incoming);

        if (incoming == null) {
            throw new BadRequestAlertException("Vaccine brand payload is required", "vaccineBrand", "payload.required");
        }

        VaccineBrands existing = vaccineBrandsRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Vaccine brand not found with id " + id, "vaccineBrand", "notfound"));

        existing.setName(incoming.getName());
        existing.setManufacture(incoming.getManufacture());
        existing.setVolume(incoming.getVolume());
        existing.setUnit(incoming.getUnit());
        existing.setMarketingAuthorizationHolder(incoming.getMarketingAuthorizationHolder());
        existing.setIsActive(incoming.getIsActive());

        try {
            VaccineBrands updated = vaccineBrandsRepository.saveAndFlush(existing);
            LOG.info("Successfully updated vaccine brand id={} (name='{}')", updated.getId(), updated.getName());
            return Optional.of(updated);
        } catch (DataIntegrityViolationException | JpaSystemException constraintException) {
            handleConstraintViolation(constraintException);
            throw constraintException;
        }
    }


    @Transactional(readOnly = true)
    public Page<VaccineBrands> findByVaccineId(Long vaccineId, Pageable pageable) {
        LOG.debug("Fetching VaccineBrands by vaccineId={} pageable={}", vaccineId, pageable);
        return vaccineBrandsRepository.findByVaccine_Id(vaccineId, pageable);
    }

    public Optional<VaccineBrands> toggleIsActive(Long id) {
        LOG.info("Toggling isActive for VaccineBrand id={}", id);
        return vaccineBrandsRepository.findById(id)
                .map(entity -> {
                    entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
                    entity.setLastModifiedDate(Instant.now());
                    VaccineBrands saved = vaccineBrandsRepository.save(entity);
                    LOG.info("VaccineBrand id={} active status changed to {}", id, saved.getIsActive());
                    return saved;
                });
    }

    private Vaccine refVaccine(Long vaccineId) {
        return entityManager.getReference(Vaccine.class, vaccineId);
    }

    private void handleConstraintViolation(RuntimeException constraintException) {
        Throwable root = getRootCause(constraintException);
        String message = (root != null ? root.getMessage() : constraintException.getMessage());
        String msgLower = message != null ? message.toLowerCase() : "";

        LOG.error("Database constraint violation while saving vaccine brand: {}", message, constraintException);

        if (msgLower.contains("ux_vaccine_brands_name_unit_volume")
                || msgLower.contains("unique constraint")
                || msgLower.contains("duplicate key")
                || msgLower.contains("duplicate entry")) {
            throw new BadRequestAlertException(
                    "A vaccine brand with the same (name, unit, volume) already exists.",
                    "vaccineBrand",
                    "unique.name.unit.volume"
            );
        }
        throw new BadRequestAlertException(
                "Database constraint violated while saving vaccine brand (check unique fields or required values).",
                "vaccineBrand",
                "db.constraint"
        );
    }
}
