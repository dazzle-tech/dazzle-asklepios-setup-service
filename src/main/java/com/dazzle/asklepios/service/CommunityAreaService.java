package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.CommunityArea;
import com.dazzle.asklepios.domain.DistrictCommunity;
import com.dazzle.asklepios.repository.CommunityAreaRepository;
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
public class CommunityAreaService {

    private static final Logger LOG = LoggerFactory.getLogger(CommunityAreaService.class);

    private final CommunityAreaRepository areaRepository;
    private final EntityManager entityManager;

    public CommunityAreaService(CommunityAreaRepository areaRepository, EntityManager entityManager) {
        this.areaRepository = areaRepository;
        this.entityManager = entityManager;
    }

    public CommunityArea create(Long communityId, CommunityArea incoming) {
        LOG.info("[CREATE] Request to create CommunityArea for communityId={} payload={}", communityId, incoming);

        if (communityId == null) {
            throw new BadRequestAlertException("Community id is required", "communityArea", "community.required");
        }
        if (incoming == null) {
            throw new BadRequestAlertException("CommunityArea payload is required", "communityArea", "payload.required");
        }

        CommunityArea entity = CommunityArea.builder()
                .community(refCommunity(communityId))
                .name(incoming.getName().trim())
                .isActive(incoming.getIsActive() != null ? incoming.getIsActive() : Boolean.TRUE)
                .build();

        try {
            return areaRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            throw handleConstraintViolationOnCreateOrUpdate(ex, "create");
        }
    }

    public Optional<CommunityArea> update(Long id, CommunityArea incoming) {
        LOG.info("[UPDATE] Request to update CommunityArea id={} payload={}", id, incoming);

        if (id == null) {
            throw new BadRequestAlertException("CommunityArea id is required", "communityArea", "id.required");
        }
        if (incoming == null) {
            throw new BadRequestAlertException("CommunityArea payload is required", "communityArea", "payload.required");
        }

        CommunityArea existing = areaRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "CommunityArea not found with id " + id,
                        "communityArea",
                        "notfound"));

        existing.setName(incoming.getName().trim());
        existing.setIsActive(incoming.getIsActive());

        try {
            return Optional.of(areaRepository.saveAndFlush(existing));
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            throw handleConstraintViolationOnCreateOrUpdate(ex, "update");
        }
    }

    @Transactional(readOnly = true)
    public Page<CommunityArea> findAll(Pageable pageable) {
        return areaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<CommunityArea> findByCommunity(Long communityId, Pageable pageable) {
        if (communityId == null) {
            throw new BadRequestAlertException("Community id is required", "communityArea", "community.required");
        }
        return areaRepository.findByCommunity_Id(communityId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CommunityArea> findByName(Long communityId, String name, Pageable pageable) {
        if (communityId == null) {
            throw new BadRequestAlertException("Community id is required", "communityArea", "community.required");
        }

        if (name == null || name.trim().isEmpty()) {
            return areaRepository.findByCommunity_Id(communityId, pageable);
        }

        return areaRepository.findByCommunity_IdAndNameContainingIgnoreCase(communityId, name.trim(), pageable);
    }

    @Transactional
    public Optional<CommunityArea> toggleIsActive(Long id) {
        return areaRepository.findById(id)
                .map(entity -> {
                    entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
                    entity.setLastModifiedDate(Instant.now());
                    return areaRepository.save(entity);
                });
    }

    private BadRequestAlertException handleConstraintViolationOnCreateOrUpdate(Exception ex, String operation) {
        Throwable root = getRootCause(ex);
        String message = root != null ? root.getMessage() : ex.getMessage();
        String lower = message != null ? message.toLowerCase() : "";

        if (lower.contains("ux_community_areas_community_name")
                || lower.contains("unique constraint")
                || lower.contains("duplicate key")
                || lower.contains("duplicate entry")) {
            return new BadRequestAlertException(
                    "A community area with the same name already exists for this community.",
                    "communityArea",
                    "unique.communityArea.name"
            );
        }

        return new BadRequestAlertException(
                "Database constraint violated while trying to " + operation + " communityArea.",
                "communityArea",
                "db.constraint"
        );
    }

    private DistrictCommunity refCommunity(Long communityId) {
        return entityManager.getReference(DistrictCommunity.class, communityId);
    }
}
