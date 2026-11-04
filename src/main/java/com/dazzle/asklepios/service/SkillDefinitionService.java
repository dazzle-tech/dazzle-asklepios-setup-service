package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.SkillDefinition;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.SkilDefinitionRepository;
import com.dazzle.asklepios.service.dto.skillDefinition.SkillDefinitionCreateDTO;
import com.dazzle.asklepios.service.dto.skillDefinition.SkillDefinitionUpdateDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
@RequiredArgsConstructor
public class SkillDefinitionService {

    private static final Logger LOG = LoggerFactory.getLogger(SkillDefinitionService.class);

    private final SkilDefinitionRepository skillDefinitionRepository;
    private final FacilityRepository facilityRepository;

    public SkillDefinition create(SkillDefinitionCreateDTO skillDefinitionCreateDTO) {
        LOG.debug("Request to create SkillDefinition : {}", skillDefinitionCreateDTO);

        Facility facility = facilityRepository.findById(skillDefinitionCreateDTO.facilityId())
                .orElseThrow(() -> new NotFoundAlertException(
                        "Facility not found",
                        "facility",
                        "notfound"
                ));

        SkillDefinition skillDefinitionToCreate = SkillDefinition.builder()
                .facility(facility)
                .name(skillDefinitionCreateDTO.name())
                .code(skillDefinitionCreateDTO.code())
                .description(skillDefinitionCreateDTO.description())
                .isActive(true)
                .type(skillDefinitionCreateDTO.type())
                .build();

        try {
            SkillDefinition saved = skillDefinitionRepository.save(skillDefinitionToCreate);


            LOG.debug("Created SkillDefinition: {}", saved);
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException constraintException) {
            throw handleConstraintViolation(constraintException);

        }
    }

    public SkillDefinition update(SkillDefinitionUpdateDTO skillDefinitionUpdateDTO) {
        LOG.debug("Request to update SkillDefinition: {}", skillDefinitionUpdateDTO);
        SkillDefinition entity = skillDefinitionRepository.findById(skillDefinitionUpdateDTO.id())
                .orElseThrow(() -> new BadRequestAlertException(
                        "idNotFound",
                        "skill_definition",
                        "SkillDefinition not found with id " + skillDefinitionUpdateDTO.id()
                ));
        Facility facility = facilityRepository.findById(skillDefinitionUpdateDTO.facilityId())
                .orElseThrow(() -> new NotFoundAlertException(
                        "Facility not found",
                        "facility",
                        "notfound"
                ));
        entity.setFacility(facility);
        entity.setCode(skillDefinitionUpdateDTO.code());
        entity.setName(skillDefinitionUpdateDTO.name());
        entity.setDescription(skillDefinitionUpdateDTO.description());
        entity.setType(skillDefinitionUpdateDTO.type());

        try {
            SkillDefinition updated = skillDefinitionRepository.saveAndFlush(entity);
            LOG.debug("Updated SkillDefinition: {}", updated);
            return updated;
        } catch (DataIntegrityViolationException | JpaSystemException constraintException) {
            LOG.debug("Constraint violation caught during update");
            throw handleConstraintViolation(constraintException);
        }
    }

    @Transactional(readOnly = true)
    public Optional<SkillDefinition> findOne(Long id) {
        LOG.debug("Request to get SkillDefinition : {}", id);
        return skillDefinitionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<SkillDefinition> findAll(Pageable pageable) {
        LOG.debug("Request to get all SkillDefinition");
       return skillDefinitionRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<SkillDefinition> findByFacilityId(Long facilityId, Pageable pageable) {
        LOG.debug("Request to get SkillDefinition by facilityId={} pageable={}", facilityId, pageable);
        return skillDefinitionRepository.findByFacility_Id(facilityId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<SkillDefinition> findByCode(String code, Pageable pageable) {
        LOG.debug("Request to get SkillDefinition by code='{}' pageable={}", code, pageable);
        return skillDefinitionRepository.findByCodeContainingIgnoreCase(code, pageable);
    }

    @Transactional(readOnly = true)
    public Page<SkillDefinition> findByName(String name, Pageable pageable) {
        LOG.debug("Request to get SkillDefinition by name='{}' pageable={}", name, pageable);
        return skillDefinitionRepository.findByNameContainingIgnoreCase(name, pageable);
    }
    @Transactional(readOnly = true)
    public Page<SkillDefinition> findByType(String type, Pageable pageable) {
        LOG.debug("Request to get SkillDefinition by type='{}' pageable={}", type, pageable);
        return skillDefinitionRepository.findByTypeContainingIgnoreCase(type, pageable);
    }

    public Optional<SkillDefinition> toggleActive(Long id) {
        LOG.debug("Request to toggle active SkillDefinition : {}", id);

        return skillDefinitionRepository.findById(id)
                .map(existing -> {
                    existing.setIsActive(!Boolean.TRUE.equals(existing.getIsActive()));
                    return skillDefinitionRepository.save(existing);
                });
    }

    private BadRequestAlertException handleConstraintViolation(RuntimeException constraintException) {
        Throwable root = getRootCause(constraintException);
        String message = (root != null ? root.getMessage() : constraintException.getMessage());
        String msgLower = message != null ? message.toLowerCase() : "";

        LOG.error("Database constraint violation while saving skill definition: {}", message, constraintException);

        if (msgLower.contains("skill_definition_code_key")) {

            return new BadRequestAlertException(
                    "code",
                    "skill_definition",
                    "This code already exists"
            );
        }

        return new BadRequestAlertException(
                "db.constraint",
                "skill_definition",
                "Database constraint violated while saving skill definition"
        );
    }

}
