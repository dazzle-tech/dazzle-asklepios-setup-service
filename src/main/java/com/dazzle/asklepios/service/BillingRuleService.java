package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.BillingRule;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.repository.BillingRuleRepository;
import com.dazzle.asklepios.service.dto.BillingRuleDTO;
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

import java.util.Locale;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class BillingRuleService {

    private static final Logger LOG =
            LoggerFactory.getLogger(BillingRuleService.class);

    private static final String ENTITY_NAME =
            "billingRule";

    private final BillingRuleRepository billingRuleRepository;

    public BillingRuleService(
            BillingRuleRepository billingRuleRepository
    ) {
        this.billingRuleRepository = billingRuleRepository;
    }

    public BillingRule create(
            BillingRuleDTO dto
    ) {
        LOG.info(
                "[CREATE] Request to create BillingRule payload={}",
                dto
        );

        if (dto == null) {
            throw new BadRequestAlertException(
                    "Billing rule payload is required",
                    ENTITY_NAME,
                    "payload.required"
            );
        }

        if (dto.id() != null) {
            throw new BadRequestAlertException(
                    "A new billing rule cannot already have an id",
                    ENTITY_NAME,
                    "id.exists"
            );
        }

        validateRequiredFields(dto);
        validateUniqueName(dto.name(), null);

        BillingRule billingRule =
                BillingRule.builder()
                        .name(normalizeName(dto.name()))
                        .billingItemType(dto.billingItemType())
                        .billingTrigger(dto.billingTrigger())
                        .isDefault(Boolean.TRUE.equals(dto.isDefault()))
                        .build();

        /*
         * Only one rule can be the default for each billing item type.
         */
        if (Boolean.TRUE.equals(billingRule.getIsDefault())) {
            removeCurrentDefault(
                    billingRule.getBillingItemType(),
                    null
            );
        }

        try {
            BillingRule created =
                    billingRuleRepository.saveAndFlush(
                            billingRule
                    );

            LOG.info(
                    "Successfully created BillingRule id={} itemType={} trigger={} default={}",
                    created.getId(),
                    created.getBillingItemType(),
                    created.getBillingTrigger(),
                    created.getIsDefault()
            );

            return created;

        } catch (
                DataIntegrityViolationException
                | JpaSystemException exception
        ) {
            handleConstraintsOnCreateOrUpdate(exception);

            throw new BadRequestAlertException(
                    "Database constraint violated while saving billing rule",
                    ENTITY_NAME,
                    "db.constraint"
            );
        }
    }

    public BillingRule update(
            Long id,
            BillingRuleDTO dto
    ) {
        LOG.info(
                "[UPDATE] Request to update BillingRule id={} payload={}",
                id,
                dto
        );

        if (id == null) {
            throw new BadRequestAlertException(
                    "Billing rule id is required",
                    ENTITY_NAME,
                    "id.required"
            );
        }

        if (dto == null) {
            throw new BadRequestAlertException(
                    "Billing rule payload is required",
                    ENTITY_NAME,
                    "payload.required"
            );
        }

        if (dto.id() == null) {
            throw new BadRequestAlertException(
                    "Billing rule id is required in the payload",
                    ENTITY_NAME,
                    "id.required"
            );
        }

        if (!id.equals(dto.id())) {
            throw new BadRequestAlertException(
                    "Path id and payload id do not match",
                    ENTITY_NAME,
                    "id.mismatch"
            );
        }

        validateRequiredFields(dto);
        validateUniqueName(dto.name(), id);

        BillingRule existing = getById(id);

        boolean makeDefault =
                dto.isDefault() != null
                        ? dto.isDefault()
                        : Boolean.TRUE.equals(existing.getIsDefault());

        /*
         * When the billing item type changes or the rule becomes default,
         * remove the previous default of the selected item type.
         */
        if (makeDefault) {
            removeCurrentDefault(
                    dto.billingItemType(),
                    id
            );
        }

        existing.setName(
                normalizeName(dto.name())
        );

        existing.setBillingItemType(
                dto.billingItemType()
        );

        existing.setBillingTrigger(
                dto.billingTrigger()
        );

        existing.setIsDefault(
                makeDefault
        );

        try {
            BillingRule updated =
                    billingRuleRepository.saveAndFlush(
                            existing
                    );

            LOG.info(
                    "Successfully updated BillingRule id={} itemType={} trigger={} default={}",
                    updated.getId(),
                    updated.getBillingItemType(),
                    updated.getBillingTrigger(),
                    updated.getIsDefault()
            );

            return updated;

        } catch (
                DataIntegrityViolationException
                | JpaSystemException exception
        ) {
            handleConstraintsOnCreateOrUpdate(exception);

            throw new BadRequestAlertException(
                    "Database constraint violated while updating billing rule",
                    ENTITY_NAME,
                    "db.constraint"
            );
        }
    }

    @Transactional(readOnly = true)
    public BillingRule getById(
            Long id
    ) {
        LOG.debug(
                "[GET BY ID] Fetching BillingRule id={}",
                id
        );

        if (id == null) {
            throw new BadRequestAlertException(
                    "Billing rule id is required",
                    ENTITY_NAME,
                    "id.required"
            );
        }

        return billingRuleRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new NotFoundAlertException(
                                        "Billing rule not found with id " + id,
                                        ENTITY_NAME,
                                        "notfound"
                                )
                );
    }

    @Transactional(readOnly = true)
    public BillingRule getDefault(
            BillingItemTypes billingItemType
    ) {
        LOG.debug(
                "[GET DEFAULT] Fetching default BillingRule itemType={}",
                billingItemType
        );

        if (billingItemType == null) {
            throw new BadRequestAlertException(
                    "Billing item type is required",
                    ENTITY_NAME,
                    "item.type.required"
            );
        }

        return billingRuleRepository
                .findFirstByBillingItemTypeAndIsDefaultTrue(
                        billingItemType
                )
                .orElseThrow(
                        () ->
                                new NotFoundAlertException(
                                        "Default billing rule was not found for billing item type "
                                                + billingItemType,
                                        ENTITY_NAME,
                                        "default.notfound"
                                )
                );
    }

    /**
     * Use this method only when you need a single global default,
     * regardless of billing item type.
     */
    @Transactional(readOnly = true)
    public BillingRule getGlobalDefault() {
        LOG.debug(
                "[GET GLOBAL DEFAULT] Fetching default BillingRule"
        );

        return billingRuleRepository
                .findFirstByIsDefaultTrue()
                .orElseThrow(
                        () ->
                                new NotFoundAlertException(
                                        "Default billing rule was not found",
                                        ENTITY_NAME,
                                        "default.notfound"
                                )
                );
    }

    @Transactional(readOnly = true)
    public Page<BillingRule> findAll(
            Pageable pageable
    ) {
        LOG.debug(
                "[FIND ALL] Fetching BillingRules pageable={}",
                pageable
        );

        if (pageable == null) {
            throw new BadRequestAlertException(
                    "Pageable is required",
                    ENTITY_NAME,
                    "pageable.required"
            );
        }

        return billingRuleRepository
                .findAllByOrderByNameAsc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<BillingRule> findByBillingItemType(
            BillingItemTypes billingItemType,
            Pageable pageable
    ) {
        LOG.debug(
                "[FIND BY ITEM TYPE] itemType={} pageable={}",
                billingItemType,
                pageable
        );

        if (billingItemType == null) {
            throw new BadRequestAlertException(
                    "Billing item type is required",
                    ENTITY_NAME,
                    "item.type.required"
            );
        }

        return billingRuleRepository
                .findByBillingItemType(
                        billingItemType,
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public Page<BillingRule> findByName(
            String name,
            BillingItemTypes billingItemType,
            Pageable pageable
    ) {
        LOG.debug(
                "[FIND BY NAME] name={} itemType={} pageable={}",
                name,
                billingItemType,
                pageable
        );

        if (pageable == null) {
            throw new BadRequestAlertException(
                    "Pageable is required",
                    ENTITY_NAME,
                    "pageable.required"
            );
        }

        String normalizedName =
                name == null ? "" : name.trim();

        if (normalizedName.isBlank()) {
            throw new BadRequestAlertException(
                    "Billing rule name is required",
                    ENTITY_NAME,
                    "name.required"
            );
        }

        if (billingItemType != null) {
            return billingRuleRepository
                    .findByNameContainingIgnoreCaseAndBillingItemType(
                            normalizedName,
                            billingItemType,
                            pageable
                    );
        }

        return billingRuleRepository
                .findByNameContainingIgnoreCase(
                        normalizedName,
                        pageable
                );
    }

    public BillingRule setDefault(
            Long id
    ) {
        LOG.info(
                "[SET DEFAULT] BillingRule id={}",
                id
        );

        BillingRule billingRule =
                getById(id);

        removeCurrentDefault(
                billingRule.getBillingItemType(),
                billingRule.getId()
        );

        billingRule.setIsDefault(true);

        return billingRuleRepository
                .saveAndFlush(billingRule);
    }

    public boolean delete(
            Long id
    ) {
        LOG.info(
                "[DELETE] Request to delete BillingRule id={}",
                id
        );

        if (id == null) {
            throw new BadRequestAlertException(
                    "Billing rule id is required",
                    ENTITY_NAME,
                    "id.required"
            );
        }

        BillingRule existing =
                billingRuleRepository
                        .findById(id)
                        .orElse(null);

        if (existing == null) {
            return false;
        }

        if (Boolean.TRUE.equals(existing.getIsDefault())) {
            throw new BadRequestAlertException(
                    "Default billing rule cannot be deleted",
                    ENTITY_NAME,
                    "default.delete"
            );
        }

        try {
            billingRuleRepository.delete(existing);
            billingRuleRepository.flush();

            return true;

        } catch (
                DataIntegrityViolationException
                | JpaSystemException exception
        ) {
            handleConstraintsOnDelete(exception);

            return false;
        }
    }

    private void validateRequiredFields(
            BillingRuleDTO dto
    ) {
        if (dto == null) {
            throw new BadRequestAlertException(
                    "Billing rule payload is required",
                    ENTITY_NAME,
                    "payload.required"
            );
        }

        if (
                dto.name() == null
                        || dto.name().isBlank()
        ) {
            throw new BadRequestAlertException(
                    "Billing rule name is required",
                    ENTITY_NAME,
                    "name.required"
            );
        }

        if (dto.name().trim().length() > 150) {
            throw new BadRequestAlertException(
                    "Billing rule name cannot exceed 150 characters",
                    ENTITY_NAME,
                    "name.size"
            );
        }

        if (dto.billingItemType() == null) {
            throw new BadRequestAlertException(
                    "Billing item type is required",
                    ENTITY_NAME,
                    "item.type.required"
            );
        }

        if (dto.billingTrigger() == null) {
            throw new BadRequestAlertException(
                    "Billing trigger is required",
                    ENTITY_NAME,
                    "trigger.required"
            );
        }
    }

    private void validateUniqueName(
            String name,
            Long excludedId
    ) {
        String normalizedName =
                normalizeName(name);

        boolean exists =
                excludedId == null
                        ? billingRuleRepository
                        .existsByNameIgnoreCase(normalizedName)
                        : billingRuleRepository
                        .existsByNameIgnoreCaseAndIdNot(
                                normalizedName,
                                excludedId
                        );

        if (exists) {
            throw new BadRequestAlertException(
                    "A billing rule with the same name already exists",
                    ENTITY_NAME,
                    "name.exists"
            );
        }
    }

    private void removeCurrentDefault(
            BillingItemTypes billingItemType,
            Long excludedId
    ) {
        billingRuleRepository
                .findFirstByBillingItemTypeAndIsDefaultTrue(
                        billingItemType
                )
                .ifPresent(
                        currentDefault -> {
                            if (
                                    excludedId == null
                                            || !currentDefault
                                            .getId()
                                            .equals(excludedId)
                            ) {
                                currentDefault.setIsDefault(false);

                                billingRuleRepository.saveAndFlush(
                                        currentDefault
                                );
                            }
                        }
                );
    }

    private String normalizeName(
            String name
    ) {
        if (
                name == null
                        || name.isBlank()
        ) {
            throw new BadRequestAlertException(
                    "Billing rule name is required",
                    ENTITY_NAME,
                    "name.required"
            );
        }

        return name.trim();
    }

    private void handleConstraintsOnCreateOrUpdate(
            RuntimeException exception
    ) {
        Throwable rootCause =
                getRootCause(exception);

        String errorMessage =
                rootCause != null
                        ? rootCause.getMessage()
                        : exception.getMessage();

        LOG.error(
                "BillingRule DB ROOT CAUSE: {}",
                errorMessage,
                exception
        );

        String lowerCaseErrorMessage =
                errorMessage != null
                        ? errorMessage.toLowerCase(Locale.ROOT)
                        : "";

        if (
                lowerCaseErrorMessage.contains("name")
                        && (
                        lowerCaseErrorMessage.contains("duplicate")
                                || lowerCaseErrorMessage.contains("unique")
                )
        ) {
            throw new BadRequestAlertException(
                    "A billing rule with the same name already exists",
                    ENTITY_NAME,
                    "name.exists"
            );
        }

        throw new BadRequestAlertException(
                "Database constraint violated while saving billing rule",
                ENTITY_NAME,
                "db.constraint"
        );
    }

    private void handleConstraintsOnDelete(
            RuntimeException exception
    ) {
        Throwable rootCause =
                getRootCause(exception);

        String errorMessage =
                rootCause != null
                        ? rootCause.getMessage()
                        : exception.getMessage();

        LOG.error(
                "BillingRule DELETE DB ROOT CAUSE: {}",
                errorMessage,
                exception
        );

        throw new BadRequestAlertException(
                "Billing rule cannot be deleted because it is used by another record",
                ENTITY_NAME,
                "rule.in.use"
        );
    }
}