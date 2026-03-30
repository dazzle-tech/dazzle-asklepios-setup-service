package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.PolicyDefinition;
import com.dazzle.asklepios.domain.enumeration.AllergenTypes;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.PolicyDefinitionRepository;
import com.dazzle.asklepios.service.dto.PolicyDefinition.PolicyDefinitionCreateDTO;
import com.dazzle.asklepios.service.dto.PolicyDefinition.PolicyDefinitionUpdateDTO;
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
public class PolicyDefinitionService {

    private static final Logger LOG = LoggerFactory.getLogger(PolicyDefinitionService.class);

    private final PolicyDefinitionRepository policyDefinitionRepository;
    private final FacilityRepository facilityRepository;

    public PolicyDefinition create(PolicyDefinitionCreateDTO policyDefinitionCreateDTO) {
        LOG.debug("Request to create PolicyDefinition : {}", policyDefinitionCreateDTO);

        Facility facility = facilityRepository.findById(policyDefinitionCreateDTO.facilityId())
                .orElseThrow(() -> new NotFoundAlertException(
                        "Facility not found",
                        "facility",
                        "notfound"
                ));

        PolicyDefinition policyDefinitionToCreate = PolicyDefinition.builder()
                .facility(facility)
                .name(policyDefinitionCreateDTO.name())
                .code(policyDefinitionCreateDTO.code())
                .description(policyDefinitionCreateDTO.description())
                .isActive(true)
                .build();

        try {
            PolicyDefinition saved = policyDefinitionRepository.save(policyDefinitionToCreate);


            LOG.debug("Created PolicyDefinition: {}", saved);
            return saved;
        } catch (DataIntegrityViolationException | JpaSystemException constraintException) {
            throw handleConstraintViolation(constraintException);

        }
    }

    public PolicyDefinition update(PolicyDefinitionUpdateDTO policyDefinitionUpdateDTO) {
        LOG.debug("Request to update PolicyDefinition: {}", policyDefinitionUpdateDTO);
        PolicyDefinition entity = policyDefinitionRepository.findById(policyDefinitionUpdateDTO.id())
                .orElseThrow(() -> new BadRequestAlertException(
                        "idNotFound",
                        "policy_definition",
                        "PolicyDefinition not found with id " + policyDefinitionUpdateDTO.id()
                ));
        Facility facility = facilityRepository.findById(policyDefinitionUpdateDTO.facilityId())
                .orElseThrow(() -> new NotFoundAlertException(
                        "Facility not found",
                        "facility",
                        "notfound"
                ));
        entity.setFacility(facility);
        entity.setCode(policyDefinitionUpdateDTO.code());
        entity.setName(policyDefinitionUpdateDTO.name());
        entity.setDescription(policyDefinitionUpdateDTO.description());

        try {
            PolicyDefinition updated = policyDefinitionRepository.saveAndFlush(entity);
            LOG.debug("Updated PolicyDefinition: {}", updated);
            return updated;
        } catch (DataIntegrityViolationException | JpaSystemException constraintException) {
            LOG.debug("Constraint violation caught during update");
            throw handleConstraintViolation(constraintException);
        }
    }

    @Transactional(readOnly = true)
    public Optional<PolicyDefinition> findOne(Long id) {
        LOG.debug("Request to get PolicyDefinition : {}", id);
        return policyDefinitionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<PolicyDefinition> findAll(Pageable pageable) {
        LOG.debug("Request to get all PolicyDefinition");
       return policyDefinitionRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<PolicyDefinition> findByFacilityId(Long facilityId, Pageable pageable) {
        LOG.debug("Request to get PolicyDefinition by facilityId={} pageable={}", facilityId, pageable);
        return policyDefinitionRepository.findByFacility_Id(facilityId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PolicyDefinition> findByCode(String code, Pageable pageable) {
        LOG.debug("Request to get PolicyDefinition by code='{}' pageable={}", code, pageable);
        return policyDefinitionRepository.findByCodeContainingIgnoreCase(code, pageable);
    }

    @Transactional(readOnly = true)
    public Page<PolicyDefinition> findByName(String name, Pageable pageable) {
        LOG.debug("Request to get PolicyDefinition by name='{}' pageable={}", name, pageable);
        return policyDefinitionRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public Optional<PolicyDefinition> toggleActive(Long id) {
        LOG.debug("Request to toggle active PolicyDefinition : {}", id);

        return policyDefinitionRepository.findById(id)
                .map(existing -> {
                    existing.setIsActive(!Boolean.TRUE.equals(existing.getIsActive()));
                    return policyDefinitionRepository.save(existing);
                });
    }

    private BadRequestAlertException handleConstraintViolation(RuntimeException constraintException) {
        Throwable root = getRootCause(constraintException);
        String message = (root != null ? root.getMessage() : constraintException.getMessage());
        String msgLower = message != null ? message.toLowerCase() : "";

        LOG.error("Database constraint violation while saving patient allergy: {}", message, constraintException);

        if (msgLower.contains("uk_policy_definition_code")) {

            return new BadRequestAlertException(
                    "code",
                    "policy_definition",
                    "This code already exists"
            );
        }

        return new BadRequestAlertException(
                "db.constraint",
                "policy_definition",
                "Database constraint violated while saving patient allergy"
        );
    }

}
