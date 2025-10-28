package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Allergens;
import com.dazzle.asklepios.domain.enumeration.AllergenType;
import com.dazzle.asklepios.repository.AllergensRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
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
public class AllergensService {

    private static final Logger LOG = LoggerFactory.getLogger(AllergensService.class);
    public static final String ALLERGENS = "allergens";

    private final AllergensRepository allergenRepository;

    public AllergensService(AllergensRepository allergenRepository) {
        this.allergenRepository = allergenRepository;
    }

    public Allergens create(Allergens incoming) {
        LOG.info("[CREATE] Request to create Allergen payload={}", incoming);

        if (incoming == null) {
            throw new BadRequestAlertException("Allergen payload is required", "allergen", "payload.required");
        }
        Allergens entity = Allergens.builder()
                .code(incoming.getCode())
                .name(incoming.getName())
                .type(incoming.getType())
                .description(incoming.getDescription())
                .isActive(Boolean.TRUE.equals(incoming.getIsActive()))
                .build();
        try {
            Allergens saved = allergenRepository.saveAndFlush(entity);
            LOG.info("Successfully created allergen id={} code='{}' name='{}'", saved.getId(), saved.getCode(), saved.getName());
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage());
            String lower = message != null ? message.toLowerCase() : "";
            LOG.error("Database constraint violation while creating allergen: {}", message, ex);
            if (lower.contains("ux_allergens_name") ||
                    lower.contains("unique") && lower.contains("name") ||
                    lower.contains("duplicate") && lower.contains("name")) {
                throw new BadRequestAlertException(
                        "An allergen with the same name already exists.",
                        "allergen",
                        "unique.name"
                );
            }
            if (lower.contains("ux_allergens_code") ||
                    lower.contains("unique") && lower.contains("code") ||
                    lower.contains("duplicate") && lower.contains("code")) {
                throw new BadRequestAlertException(
                        "An allergen with the same code already exists.",
                        "allergen",
                        "unique.code"
                );
            }

            throw new BadRequestAlertException(
                    "Database constraint violated while creating allergen (check unique name/code or required fields).",
                    "allergen",
                    "db.constraint"
            );
        }
    }

    // ====================== UPDATE ======================

    public Optional<Allergens> update(Long id, Allergens incoming) {
        LOG.info("[UPDATE] Request to update Allergen id={} payload={}", id, incoming);

        if (incoming == null) {
            throw new BadRequestAlertException("Allergen payload is required", "allergen", "payload.required");
        }

        Allergens existing = allergenRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException("Allergen not found with id " + id, "allergen", "notfound"));
        existing.setCode(incoming.getCode());
        existing.setName(incoming.getName());
        existing.setType(incoming.getType());
        existing.setDescription(incoming.getDescription());
        existing.setIsActive(incoming.getIsActive());
        existing.setLastModifiedDate(Instant.now());
        existing.setLastModifiedBy(incoming.getLastModifiedBy());
        try {
            Allergens updated = allergenRepository.saveAndFlush(existing);
            LOG.info("Successfully updated allergen id={} (code='{}', name='{}')", updated.getId(), updated.getCode(), updated.getName());
            return Optional.of(updated);
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String message = (root != null ? root.getMessage() : ex.getMessage());
            String lower = message != null ? message.toLowerCase() : "";

            LOG.error("Database constraint violation while updating allergen: {}", message, ex);

            if (lower.contains("ux_allergens_name") ||
                    lower.contains("unique") && lower.contains("name") ||
                    lower.contains("duplicate") && lower.contains("name")) {
                throw new BadRequestAlertException(
                        "Another allergen with the same name already exists.",
                        "allergen",
                        "unique.name"
                );
            }
            if (lower.contains("ux_allergens_code") ||
                    lower.contains("unique") && lower.contains("code") ||
                    lower.contains("duplicate") && lower.contains("code")) {
                throw new BadRequestAlertException(
                        "Another allergen with the same code already exists.",
                        "allergen",
                        "unique.code"
                );
            }

            throw new BadRequestAlertException(
                    "Database constraint violated while updating allergen (check unique name/code or required fields).",
                    "allergen",
                    "db.constraint"
            );
        }
    }


    @Transactional(readOnly = true)
    public List<Allergens> findAll() {
        LOG.debug("Fetching all Allergens");
        return allergenRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Allergens> findAll(Pageable pageable) {
        LOG.debug("Fetching paged Allergens pageable={}", pageable);
        return allergenRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Allergens> findByType(AllergenType type, Pageable pageable) {
        LOG.debug("Fetching Allergens by type={} pageable={}", type, pageable);
        return allergenRepository.findByType(type, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Allergens> findByCodeContainingIgnoreCase(String code, Pageable pageable) {
        LOG.debug("Fetching Allergens by code like='{}' pageable={}", code, pageable);
        return allergenRepository.findByCodeContainingIgnoreCase(code, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Allergens> findByNameContainingIgnoreCase(String name, Pageable pageable) {
        LOG.debug("Fetching Allergens by name like='{}' pageable={}", name, pageable);
        return allergenRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Allergens> findOne(Long id) {
        LOG.debug("Fetching single Allergen by id={}", id);
        return allergenRepository.findById(id);
    }

    public Optional<Allergens> toggleIsActive(Long id) {
        LOG.info("Toggling isActive for Allergen id={}", id);
        return allergenRepository.findById(id)
                .map(entity -> {
                    entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
                    entity.setLastModifiedDate(Instant.now());
                    Allergens saved = allergenRepository.save(entity);
                    LOG.info("Allergen id={} active status changed to {}", id, saved.getIsActive());
                    return saved;
                });
    }


    public void delete(Long id) {
        LOG.debug("Request to delete Allergen : {}", id);
        allergenRepository.deleteById(id);
    }
}
