package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.ProcedureItems;
import com.dazzle.asklepios.domain.enumeration.ProcedureItemsType;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.ProcedureItemsRepository;
import com.dazzle.asklepios.repository.ProcedureRepository;
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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

/**
 * Links setup procedures to sources (department + specialty). Active
 * DEPARTMENTS links are the default procedures of a visit, mirroring
 * {@link ServiceItemsService} for services.
 */
@Service
@Transactional
public class ProcedureItemsService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcedureItemsService.class);

    private static final String ENTITY_NAME = "procedureItems";

    private final ProcedureItemsRepository procedureItemsRepository;
    private final ProcedureRepository procedureRepository;
    private final DepartmentsRepository departmentsRepository;

    public ProcedureItemsService(
            ProcedureItemsRepository procedureItemsRepository,
            ProcedureRepository procedureRepository,
            DepartmentsRepository departmentsRepository
    ) {
        this.procedureItemsRepository = procedureItemsRepository;
        this.procedureRepository = procedureRepository;
        this.departmentsRepository = departmentsRepository;
    }

    public ProcedureItems create(Long procedureId, ProcedureItems input) {
        LOG.debug("Request to create ProcedureItems for procedureId={} payload={}", procedureId, input);

        if (procedureId == null) {
            throw new BadRequestAlertException("Procedure ID is required", ENTITY_NAME, "procedureid.required");
        }
        if (input == null) {
            throw new BadRequestAlertException("ProcedureItems payload is required", ENTITY_NAME, "payload.required");
        }
        requireSpecialtyForDepartment(input.getType(), input.getSpecialty());

        ProcedureItems entity = ProcedureItems.builder()
                .type(input.getType())
                .sourceId(input.getSourceId())
                .specialty(normalizeSpecialty(input.getSpecialty()))
                .procedure(loadProcedure(procedureId))
                .isActive(input.getIsActive() != null ? input.getIsActive() : Boolean.TRUE)
                .build();

        return saveAndFlush(entity, "creating");
    }

    public Optional<ProcedureItems> update(Long id, Long procedureId, ProcedureItems patch) {
        LOG.debug("Request to update ProcedureItems id={} procedureId={} with {}", id, procedureId, patch);

        ProcedureItems existing = loadProcedureItem(id);

        ProcedureItemsType effectiveType = patch.getType() != null ? patch.getType() : existing.getType();
        requireSpecialtyForDepartment(effectiveType, patch.getSpecialty());

        if (procedureId != null) {
            existing.setProcedure(loadProcedure(procedureId));
        }
        if (patch.getType() != null) {
            existing.setType(patch.getType());
        }
        if (patch.getSourceId() != null) {
            existing.setSourceId(patch.getSourceId());
        }
        existing.setSpecialty(normalizeSpecialty(patch.getSpecialty()));
        if (patch.getIsActive() != null) {
            existing.setIsActive(patch.getIsActive());
        }

        return Optional.of(saveAndFlush(existing, "updating"));
    }

    @Transactional(readOnly = true)
    public Page<ProcedureItems> findByProcedureId(Long procedureId, Pageable pageable) {
        LOG.debug("Request to get ProcedureItems by procedureId={} pageable={}", procedureId, pageable);

        if (procedureId == null) {
            throw new BadRequestAlertException("Procedure ID is required", ENTITY_NAME, "procedureid.required");
        }
        return procedureItemsRepository.findByProcedure_Id(procedureId, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<ProcedureItems> findOne(Long id) {
        LOG.debug("Request to get ProcedureItems : {}", id);
        return procedureItemsRepository.findById(id);
    }

    /**
     * Ids of the procedures linked to a department. When a specialty is given,
     * only the links of that specialty are returned.
     */
    @Transactional(readOnly = true)
    public List<Long> findLinkedProcedureIds(Long departmentId, String specialty) {
        if (departmentId == null) {
            throw new BadRequestAlertException("Department sourceId is required", ENTITY_NAME, "sourceid.required");
        }

        List<ProcedureItems> items = specialty == null || specialty.isBlank()
                ? procedureItemsRepository.findByTypeAndSourceIdAndIsActiveTrue(
                        ProcedureItemsType.DEPARTMENTS,
                        departmentId
                )
                : procedureItemsRepository.findByTypeAndSourceIdAndSpecialtyAndIsActiveTrue(
                        ProcedureItemsType.DEPARTMENTS,
                        departmentId,
                        specialty.trim()
                );

        return items.stream()
                .map(ProcedureItems::getProcedure)
                .filter(Objects::nonNull)
                .map(Procedure::getId)
                .distinct()
                .toList();
    }

    public Optional<ProcedureItems> toggleIsActive(Long id) {
        LOG.debug("Request to toggle ProcedureItems isActive id={}", id);

        ProcedureItems item = loadProcedureItem(id);
        item.setIsActive(!Boolean.TRUE.equals(item.getIsActive()));

        return Optional.of(procedureItemsRepository.save(item));
    }

    public void delete(Long id) {
        LOG.debug("Request to delete ProcedureItems : {}", id);

        if (!procedureItemsRepository.existsById(id)) {
            throw new NotFoundAlertException("ProcedureItems not found with id " + id, ENTITY_NAME, "notfound");
        }
        procedureItemsRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Department> findSourcesByTypeAndFacility(ProcedureItemsType type, Long facilityId) {
        LOG.debug("Request(findSourcesByTypeAndFacility) type={} facilityId={}", type, facilityId);

        if (facilityId == null) {
            throw new BadRequestAlertException("Facility ID is required", ENTITY_NAME, "facilityid.required");
        }

        if (type == ProcedureItemsType.DEPARTMENTS) {
            return departmentsRepository.findByFacilityIdAndIsActiveTrue(facilityId);
        }

        return List.of();
    }

    private ProcedureItems saveAndFlush(ProcedureItems entity, String operation) {
        try {
            return procedureItemsRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            Throwable root = getRootCause(ex);
            String msg = (root != null && root.getMessage() != null ? root.getMessage() : ex.getMessage()).toLowerCase();

            LOG.warn("DB constraint violation while {} ProcedureItems (type={}, sourceId={}, specialty={}): {}",
                    operation, entity.getType(), entity.getSourceId(), entity.getSpecialty(), msg, ex);

            if (msg.contains("uk_procedure_items_no_specialty")
                    || msg.contains("uk_procedure_items_with_specialty")
                    || msg.contains("unique constraint")
                    || msg.contains("duplicate key")) {
                throw new BadRequestAlertException(
                        "This procedure is already linked to the same department and specialty.",
                        ENTITY_NAME,
                        "unique.procedureitem"
                );
            }
            throw new BadRequestAlertException(
                    "Database constraint violated while " + operation + " ProcedureItem.",
                    ENTITY_NAME,
                    "db.constraint"
            );
        }
    }

    private void requireSpecialtyForDepartment(ProcedureItemsType type, String specialty) {
        if (type == ProcedureItemsType.DEPARTMENTS && (specialty == null || specialty.isBlank())) {
            throw new BadRequestAlertException(
                    "Specialty is required when linking a department",
                    ENTITY_NAME,
                    "specialty.required"
            );
        }
    }

    private String normalizeSpecialty(String specialty) {
        return specialty == null || specialty.isBlank() ? null : specialty.trim();
    }

    private Procedure loadProcedure(Long procedureId) {
        return procedureRepository.findById(procedureId)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Procedure not found with id " + procedureId, "procedure", "notfound"));
    }

    private ProcedureItems loadProcedureItem(Long id) {
        return procedureItemsRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "ProcedureItems not found with id " + id, ENTITY_NAME, "notfound"));
    }
}
