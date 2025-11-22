package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CountryDistrict;
import com.dazzle.asklepios.domain.DistrictCommunity;
import com.dazzle.asklepios.repository.DistrictCommunityRepository;
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
public class DistrictCommunityService {

    private static final Logger LOG = LoggerFactory.getLogger(DistrictCommunityService.class);

    private final DistrictCommunityRepository communityRepository;
    private final EntityManager entityManager;

    public DistrictCommunityService(DistrictCommunityRepository communityRepository, EntityManager entityManager) {
        this.communityRepository = communityRepository;
        this.entityManager = entityManager;
    }

    public DistrictCommunity create(Long districtId, DistrictCommunity incoming) {
        LOG.info("[CREATE] Request to create DistrictCommunity for districtId={} payload={}", districtId, incoming);

        if (districtId == null) {
            throw new BadRequestAlertException("District id is required", "districtCommunity", "district.required");
        }
        if (incoming == null) {
            throw new BadRequestAlertException("DistrictCommunity payload is required", "districtCommunity", "payload.required");
        }

        DistrictCommunity entity = DistrictCommunity.builder()
                .district(refDistrict(districtId))
                .name(incoming.getName().trim())
                .isActive(incoming.getIsActive() != null ? incoming.getIsActive() : Boolean.TRUE)
                .build();

        try {
            DistrictCommunity saved = communityRepository.saveAndFlush(entity);
            LOG.info("Successfully created DistrictCommunity id={} name='{}' districtId={}",
                    saved.getId(), saved.getName(), districtId);
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            throw handleConstraintViolationOnCreateOrUpdate(ex, "create");
        }
    }

    public Optional<DistrictCommunity> update(Long id, DistrictCommunity incoming) {
        LOG.info("[UPDATE] Request to update DistrictCommunity id={} payload={}", id, incoming);

        if (id == null) {
            throw new BadRequestAlertException("DistrictCommunity id is required", "districtCommunity", "id.required");
        }
        if (incoming == null) {
            throw new BadRequestAlertException("DistrictCommunity payload is required", "districtCommunity", "payload.required");
        }

        DistrictCommunity existing = communityRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundAlertException("DistrictCommunity not found with id " + id,
                                "districtCommunity", "notfound"));

        existing.setName(incoming.getName().trim());
        existing.setIsActive(incoming.getIsActive());

        try {
            DistrictCommunity updated = communityRepository.saveAndFlush(existing);
            LOG.info("Successfully updated DistrictCommunity id={} name='{}'",
                    updated.getId(), updated.getName());
            return Optional.of(updated);
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            throw handleConstraintViolationOnCreateOrUpdate(ex, "update");
        }
    }

    @Transactional(readOnly = true)
    public Page<DistrictCommunity> findAll(Pageable pageable) {
        LOG.debug("Fetching paged DistrictCommunities pageable={}", pageable);
        return communityRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<DistrictCommunity> findByDistrict(Long districtId, Pageable pageable) {
        LOG.debug("Fetching DistrictCommunities for districtId={} pageable={}", districtId, pageable);

        if (districtId == null) {
            throw new BadRequestAlertException("District id is required", "districtCommunity", "district.required");
        }

        return communityRepository.findByDistrict_Id(districtId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<DistrictCommunity> findByName(Long districtId, String name, Pageable pageable) {
        LOG.debug("Fetching DistrictCommunities for districtId={} by name='{}' pageable={}", districtId, name, pageable);

        if (districtId == null) {
            throw new BadRequestAlertException("District id is required", "districtCommunity", "district.required");
        }

        if (name == null || name.trim().isEmpty()) {
            return communityRepository.findByDistrict_Id(districtId, pageable);
        }

        return communityRepository.findByDistrict_IdAndNameContainingIgnoreCase(
                districtId, name.trim(), pageable
        );
    }


    @Transactional
    public Optional<DistrictCommunity> toggleIsActive(Long id) {
        LOG.info("Toggling isActive for DistrictCommunity id={}", id);

        return communityRepository.findById(id)
                .map(entity -> {
                    entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
                    entity.setLastModifiedDate(Instant.now());
                    DistrictCommunity saved = communityRepository.save(entity);

                    LOG.info("DistrictCommunity id={} active status changed to {}", id, saved.getIsActive());
                    return saved;
                });
    }

    private BadRequestAlertException handleConstraintViolationOnCreateOrUpdate(Exception ex, String operation) {
        Throwable root = getRootCause(ex);
        String message = (root != null ? root.getMessage() : ex.getMessage());
        String lower = message != null ? message.toLowerCase() : "";

        LOG.error("Database constraint violation while trying to {} DistrictCommunity: {}", operation, message, ex);

        if (lower.contains("ux_communities_district_name")
                || lower.contains("unique constraint")
                || lower.contains("duplicate key")
                || lower.contains("duplicate entry")) {

            return new BadRequestAlertException(
                    "A community with the same name already exists for this district.",
                    "districtCommunity",
                    "unique.community.name"
            );
        }

        return new BadRequestAlertException(
                "Database constraint violated while trying to " + operation + " districtCommunity.",
                "districtCommunity",
                "db.constraint"
        );
    }

    private CountryDistrict refDistrict(Long districtId) {
        return entityManager.getReference(CountryDistrict.class, districtId);
    }
}
