package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.BillingConfiguration;
import com.dazzle.asklepios.domain.enumeration.BillingConfigurationKey;
import com.dazzle.asklepios.domain.enumeration.BillingConfigurationStatus;
import com.dazzle.asklepios.domain.enumeration.BillingConfigurationValueType;
import com.dazzle.asklepios.repository.BillingConfigurationRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.service.dto.BillingConfigurationDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class BillingConfigurationService {

    private static final Logger LOG =
            LoggerFactory.getLogger(
                    BillingConfigurationService.class
            );

    private static final String ENTITY_NAME =
            "billingConfiguration";

    /*
     * Only the enum name/code is stored in the database.
     *
     * Example:
     *
     * BillingRoundingMethod
     *
     * The full Java class path is created internally only when
     * validating or resolving the enum value.
     */
    private static final String ENUM_PACKAGE =
            "com.dazzle.asklepios.domain.enumeration.";

    private final BillingConfigurationRepository
            billingConfigurationRepository;

    private final FacilityRepository
            facilityRepository;

    private final ObjectMapper
            objectMapper;

    public BillingConfigurationService(
            BillingConfigurationRepository billingConfigurationRepository,
            FacilityRepository facilityRepository,
            ObjectMapper objectMapper
    ) {
        this.billingConfigurationRepository =
                billingConfigurationRepository;

        this.facilityRepository =
                facilityRepository;

        this.objectMapper =
                objectMapper;
    }

    public BillingConfiguration create(
            BillingConfigurationDTO dto
    ) {
        LOG.info(
                "[CREATE] Request to create BillingConfiguration payload={}",
                dto
        );

        if (dto == null) {
            throw new BadRequestAlertException(
                    "Billing configuration payload is required",
                    ENTITY_NAME,
                    "payload.required"
            );
        }

        if (dto.id() != null) {
            throw new BadRequestAlertException(
                    "A new billing configuration cannot already have an id",
                    ENTITY_NAME,
                    "id.exists"
            );
        }

        validateRequiredFields(
                dto
        );

        validateFacility(
                dto.facilityId()
        );

        validateUniqueConfigurationKey(
                dto.facilityId(),
                dto.configurationKey(),
                null
        );

        String normalizedEnumCode =
                resolveEnumCode(
                        dto.valueType(),
                        dto.enumCode()
                );

        String normalizedValue =
                normalizeAndValidateValue(
                        dto.valueType(),
                        dto.configurationValue(),
                        normalizedEnumCode
                );

        BillingConfiguration billingConfiguration =
                BillingConfiguration.builder()
                        .facilityId(
                                dto.facilityId()
                        )
                        .configurationKey(
                                dto.configurationKey()
                        )
                        .valueType(
                                dto.valueType()
                        )
                        .configurationValue(
                                normalizedValue
                        )
                        .enumCode(
                                normalizedEnumCode
                        )
                        .description(
                                normalizeNullableText(
                                        dto.description()
                                )
                        )
                        .active(
                                dto.active() == null
                                        || dto.active()
                        )
                        .status(
                                dto.status() == null
                                        ? BillingConfigurationStatus.DRAFT
                                        : dto.status()
                        )
                        .build();

        try {
            BillingConfiguration created =
                    billingConfigurationRepository
                            .saveAndFlush(
                                    billingConfiguration
                            );

            LOG.info(
                    "Successfully created BillingConfiguration id={} facilityId={} key={}",
                    created.getId(),
                    created.getFacilityId(),
                    created.getConfigurationKey()
            );

            return created;

        } catch (
                DataIntegrityViolationException
                | JpaSystemException exception
        ) {
            handleConstraintsOnCreateOrUpdate(
                    exception
            );

            throw new BadRequestAlertException(
                    "Database constraint violated while saving billing configuration.",
                    ENTITY_NAME,
                    "db.constraint"
            );
        }
    }

    public BillingConfiguration update(
            Long id,
            BillingConfigurationDTO dto
    ) {
        LOG.info(
                "[UPDATE] Request to update BillingConfiguration id={} payload={}",
                id,
                dto
        );

        if (id == null) {
            throw new BadRequestAlertException(
                    "Billing configuration id is required",
                    ENTITY_NAME,
                    "id.required"
            );
        }

        if (dto == null) {
            throw new BadRequestAlertException(
                    "Billing configuration payload is required",
                    ENTITY_NAME,
                    "payload.required"
            );
        }

        if (dto.id() == null) {
            throw new BadRequestAlertException(
                    "Billing configuration id is required",
                    ENTITY_NAME,
                    "id.required"
            );
        }

        if (!id.equals(dto.id())) {
            throw new BadRequestAlertException(
                    "Invalid billing configuration id",
                    ENTITY_NAME,
                    "id.invalid"
            );
        }

        validateRequiredFields(
                dto
        );

        validateFacility(
                dto.facilityId()
        );

        BillingConfiguration existing =
                billingConfigurationRepository
                        .findById(
                                id
                        )
                        .orElseThrow(
                                () ->
                                        new NotFoundAlertException(
                                                "Billing configuration not found with id "
                                                        + id,
                                                ENTITY_NAME,
                                                "notfound"
                                        )
                        );

        validateUniqueConfigurationKey(
                dto.facilityId(),
                dto.configurationKey(),
                id
        );

        String normalizedEnumCode =
                resolveEnumCode(
                        dto.valueType(),
                        dto.enumCode()
                );

        String normalizedValue =
                normalizeAndValidateValue(
                        dto.valueType(),
                        dto.configurationValue(),
                        normalizedEnumCode
                );

        existing.setFacilityId(
                dto.facilityId()
        );

        existing.setConfigurationKey(
                dto.configurationKey()
        );

        existing.setValueType(
                dto.valueType()
        );

        existing.setConfigurationValue(
                normalizedValue
        );

        existing.setEnumCode(
                normalizedEnumCode
        );

        existing.setDescription(
                normalizeNullableText(
                        dto.description()
                )
        );

        if (dto.active() != null) {
            existing.setActive(
                    dto.active()
            );
        }

        if (dto.status() != null) {
            existing.setStatus(
                    dto.status()
            );
        }

        try {
            BillingConfiguration updated =
                    billingConfigurationRepository
                            .saveAndFlush(
                                    existing
                            );

            LOG.info(
                    "Successfully updated BillingConfiguration id={} facilityId={} key={}",
                    updated.getId(),
                    updated.getFacilityId(),
                    updated.getConfigurationKey()
            );

            return updated;

        } catch (
                DataIntegrityViolationException
                | JpaSystemException exception
        ) {
            handleConstraintsOnCreateOrUpdate(
                    exception
            );

            throw new BadRequestAlertException(
                    "Database constraint violated while updating billing configuration.",
                    ENTITY_NAME,
                    "db.constraint"
            );
        }
    }

    public BillingConfiguration changeActivationStatus(
            Long id,
            boolean active
    ) {
        LOG.info(
                "[CHANGE ACTIVATION STATUS] BillingConfiguration id={} active={}",
                id,
                active
        );

        BillingConfiguration existing =
                findById(
                        id
                );

        existing.setActive(
                active
        );

        if (!active) {
            existing.setStatus(
                    BillingConfigurationStatus.INACTIVE
            );
        }

        return billingConfigurationRepository
                .saveAndFlush(
                        existing
                );
    }

    public BillingConfiguration changeStatus(
            Long id,
            BillingConfigurationStatus status
    ) {
        LOG.info(
                "[CHANGE STATUS] BillingConfiguration id={} status={}",
                id,
                status
        );

        if (status == null) {
            throw new BadRequestAlertException(
                    "Billing configuration status is required",
                    ENTITY_NAME,
                    "status.required"
            );
        }

        BillingConfiguration existing =
                findById(
                        id
                );

        if (
                status
                        == BillingConfigurationStatus.ACTIVE
        ) {
            normalizeAndValidateValue(
                    existing.getValueType(),
                    existing.getConfigurationValue(),
                    existing.getEnumCode()
            );

            existing.setActive(
                    true
            );
        }

        if (
                status
                        == BillingConfigurationStatus.INACTIVE
        ) {
            existing.setActive(
                    false
            );
        }

        existing.setStatus(
                status
        );

        return billingConfigurationRepository
                .saveAndFlush(
                        existing
                );
    }

    @Transactional(readOnly = true)
    public BillingConfiguration findById(
            Long id
    ) {
        LOG.debug(
                "[FIND BY ID] Fetching BillingConfiguration id={}",
                id
        );

        if (id == null) {
            throw new BadRequestAlertException(
                    "Billing configuration id is required",
                    ENTITY_NAME,
                    "id.required"
            );
        }

        return billingConfigurationRepository
                .findById(
                        id
                )
                .orElseThrow(
                        () ->
                                new NotFoundAlertException(
                                        "Billing configuration not found with id "
                                                + id,
                                        ENTITY_NAME,
                                        "notfound"
                                )
                );
    }

    @Transactional(readOnly = true)
    public Page<BillingConfiguration> findByFacilityId(
            Long facilityId,
            Pageable pageable
    ) {
        LOG.debug(
                "[FIND BY FACILITY] facilityId={} pageable={}",
                facilityId,
                pageable
        );

        validateFacility(
                facilityId
        );

        return billingConfigurationRepository
                .findByFacilityId(
                        facilityId,
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public Page<BillingConfiguration> findActiveByFacilityId(
            Long facilityId,
            Pageable pageable
    ) {
        LOG.debug(
                "[FIND ACTIVE BY FACILITY] facilityId={} pageable={}",
                facilityId,
                pageable
        );

        validateFacility(
                facilityId
        );

        return billingConfigurationRepository
                .findByFacilityIdAndActiveTrue(
                        facilityId,
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public Page<BillingConfiguration> findByFacilityIdAndStatus(
            Long facilityId,
            BillingConfigurationStatus status,
            Pageable pageable
    ) {
        LOG.debug(
                "[FIND BY FACILITY AND STATUS] facilityId={} status={} pageable={}",
                facilityId,
                status,
                pageable
        );

        validateFacility(
                facilityId
        );

        if (status == null) {
            throw new BadRequestAlertException(
                    "Billing configuration status is required",
                    ENTITY_NAME,
                    "status.required"
            );
        }

        return billingConfigurationRepository
                .findByFacilityIdAndStatus(
                        facilityId,
                        status,
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public BillingConfiguration
    findByFacilityIdAndConfigurationKey(
            Long facilityId,
            BillingConfigurationKey configurationKey
    ) {
        LOG.debug(
                "[FIND BY FACILITY AND KEY] facilityId={} key={}",
                facilityId,
                configurationKey
        );

        validateFacility(
                facilityId
        );

        if (configurationKey == null) {
            throw new BadRequestAlertException(
                    "Configuration key is required",
                    ENTITY_NAME,
                    "key.required"
            );
        }

        return billingConfigurationRepository
                .findByFacilityIdAndConfigurationKey(
                        facilityId,
                        configurationKey
                )
                .orElseThrow(
                        () ->
                                new NotFoundAlertException(
                                        "Billing configuration not found for facility "
                                                + facilityId
                                                + " and key "
                                                + configurationKey,
                                        ENTITY_NAME,
                                        "notfound"
                                )
                );
    }

    @Transactional(readOnly = true)
    public Object getResolvedValue(
            Long facilityId,
            BillingConfigurationKey configurationKey
    ) {
        BillingConfiguration configuration =
                findByFacilityIdAndConfigurationKey(
                        facilityId,
                        configurationKey
                );

        if (
                !Boolean.TRUE.equals(
                        configuration.getActive()
                )
                        ||
                        configuration.getStatus()
                                != BillingConfigurationStatus.ACTIVE
        ) {
            throw new BadRequestAlertException(
                    "Billing configuration is not active",
                    ENTITY_NAME,
                    "configuration.inactive"
            );
        }

        return convertValue(
                configuration.getValueType(),
                configuration.getConfigurationValue(),
                configuration.getEnumCode()
        );
    }

    public boolean delete(
            Long id
    ) {
        LOG.info(
                "[DELETE] Request to delete BillingConfiguration id={}",
                id
        );

        if (id == null) {
            throw new BadRequestAlertException(
                    "Billing configuration id is required",
                    ENTITY_NAME,
                    "id.required"
            );
        }

        BillingConfiguration existing =
                billingConfigurationRepository
                        .findById(
                                id
                        )
                        .orElse(
                                null
                        );

        if (existing == null) {
            return false;
        }

        if (
                existing.getStatus()
                        == BillingConfigurationStatus.ACTIVE
        ) {
            throw new BadRequestAlertException(
                    "Active billing configuration cannot be deleted",
                    ENTITY_NAME,
                    "active.delete"
            );
        }

        billingConfigurationRepository.delete(
                existing
        );

        billingConfigurationRepository.flush();

        return true;
    }

    private void validateRequiredFields(
            BillingConfigurationDTO dto
    ) {
        if (dto == null) {
            throw new BadRequestAlertException(
                    "Billing configuration payload is required",
                    ENTITY_NAME,
                    "payload.required"
            );
        }

        if (dto.facilityId() == null) {
            throw new BadRequestAlertException(
                    "Facility id is required",
                    ENTITY_NAME,
                    "facility.required"
            );
        }

        if (dto.configurationKey() == null) {
            throw new BadRequestAlertException(
                    "Configuration key is required",
                    ENTITY_NAME,
                    "key.required"
            );
        }

        if (dto.valueType() == null) {
            throw new BadRequestAlertException(
                    "Configuration value type is required",
                    ENTITY_NAME,
                    "value.type.required"
            );
        }

        if (
                dto.configurationValue() == null
                        ||
                        dto.configurationValue()
                                .isBlank()
        ) {
            throw new BadRequestAlertException(
                    "Default value is required",
                    ENTITY_NAME,
                    "value.required"
            );
        }

        if (
                dto.valueType()
                        == BillingConfigurationValueType.ENUM
                        &&
                        (
                                dto.enumCode() == null
                                        ||
                                        dto.enumCode()
                                                .isBlank()
                        )
        ) {
            throw new BadRequestAlertException(
                    "Enum code is required when value type is ENUM",
                    ENTITY_NAME,
                    "enum.code.required"
            );
        }
    }

    private void validateFacility(
            Long facilityId
    ) {
        if (
                facilityId == null
                        ||
                        !facilityRepository.existsById(
                                facilityId
                        )
        ) {
            throw new NotFoundAlertException(
                    "Facility not found with id "
                            + facilityId,
                    ENTITY_NAME,
                    "facility.notfound"
            );
        }
    }

    private void validateUniqueConfigurationKey(
            Long facilityId,
            BillingConfigurationKey configurationKey,
            Long excludedId
    ) {
        boolean exists =
                excludedId == null
                        ?
                        billingConfigurationRepository
                                .existsByFacilityIdAndConfigurationKey(
                                        facilityId,
                                        configurationKey
                                )
                        :
                        billingConfigurationRepository
                                .existsByFacilityIdAndConfigurationKeyAndIdNot(
                                        facilityId,
                                        configurationKey,
                                        excludedId
                                );

        if (exists) {
            throw new BadRequestAlertException(
                    "Billing configuration key already exists for this facility",
                    ENTITY_NAME,
                    "key.exists"
            );
        }
    }

    private String normalizeAndValidateValue(
            BillingConfigurationValueType valueType,
            String value,
            String enumCode
    ) {
        if (valueType == null) {
            throw new BadRequestAlertException(
                    "Configuration value type is required",
                    ENTITY_NAME,
                    "value.type.required"
            );
        }

        if (
                value == null
                        ||
                        value.isBlank()
        ) {
            throw new BadRequestAlertException(
                    "Default value is required",
                    ENTITY_NAME,
                    "value.required"
            );
        }

        String normalizedValue =
                value.trim();

        try {
            return switch (valueType) {
                case STRING ->
                        normalizedValue;

                case BOOLEAN ->
                        normalizeBoolean(
                                normalizedValue
                        );

                case INTEGER ->
                        String.valueOf(
                                Integer.parseInt(
                                        normalizedValue
                                )
                        );

                case LONG ->
                        String.valueOf(
                                Long.parseLong(
                                        normalizedValue
                                )
                        );

                case DECIMAL ->
                        new BigDecimal(
                                normalizedValue
                        )
                                .stripTrailingZeros()
                                .toPlainString();

                case ENUM ->
                        normalizeEnum(
                                enumCode,
                                normalizedValue
                        );

                case DATE ->
                        LocalDate.parse(
                                        normalizedValue
                                )
                                .toString();

                case DATETIME ->
                        Instant.parse(
                                        normalizedValue
                                )
                                .toString();

                case JSON ->
                        objectMapper.writeValueAsString(
                                objectMapper.readTree(
                                        normalizedValue
                                )
                        );
            };

        } catch (
                BadRequestAlertException exception
        ) {
            throw exception;

        } catch (
                NumberFormatException exception
        ) {
            throw new BadRequestAlertException(
                    "Default value must be numeric for value type "
                            + valueType,
                    ENTITY_NAME,
                    "value.numeric.invalid"
            );

        } catch (
                Exception exception
        ) {
            LOG.warn(
                    "Invalid billing configuration value. valueType={} value={} enumCode={}",
                    valueType,
                    normalizedValue,
                    enumCode,
                    exception
            );

            throw new BadRequestAlertException(
                    "Invalid default value for selected value type "
                            + valueType,
                    ENTITY_NAME,
                    "value.invalid"
            );
        }
    }

    private Object convertValue(
            BillingConfigurationValueType valueType,
            String value,
            String enumCode
    ) {
        if (value == null) {
            return null;
        }

        if (valueType == null) {
            throw new BadRequestAlertException(
                    "Configuration value type is required",
                    ENTITY_NAME,
                    "value.type.required"
            );
        }

        try {
            return switch (valueType) {
                case STRING ->
                        value;

                case BOOLEAN ->
                        Boolean.valueOf(
                                value
                        );

                case INTEGER ->
                        Integer.valueOf(
                                value
                        );

                case LONG ->
                        Long.valueOf(
                                value
                        );

                case DECIMAL ->
                        new BigDecimal(
                                value
                        );

                case ENUM ->
                        convertEnum(
                                enumCode,
                                value
                        );

                case DATE ->
                        LocalDate.parse(
                                value
                        );

                case DATETIME ->
                        Instant.parse(
                                value
                        );

                case JSON ->
                        objectMapper.readTree(
                                value
                        );
            };

        } catch (
                BadRequestAlertException exception
        ) {
            throw exception;

        } catch (
                Exception exception
        ) {
            LOG.error(
                    "Unable to resolve billing configuration value. valueType={} value={} enumCode={}",
                    valueType,
                    value,
                    enumCode,
                    exception
            );

            throw new BadRequestAlertException(
                    "Unable to resolve billing configuration value",
                    ENTITY_NAME,
                    "value.resolve.failed"
            );
        }
    }

    private String normalizeBoolean(
            String value
    ) {
        if (
                "true".equalsIgnoreCase(
                        value
                )
        ) {
            return "true";
        }

        if (
                "false".equalsIgnoreCase(
                        value
                )
        ) {
            return "false";
        }

        throw new BadRequestAlertException(
                "Boolean value must be true or false",
                ENTITY_NAME,
                "boolean.invalid"
        );
    }

    @SuppressWarnings({
            "unchecked",
            "rawtypes"
    })
    private String normalizeEnum(
            String enumCode,
            String value
    ) {
        String normalizedEnumCode =
                normalizeEnumCode(
                        enumCode
                );

        String enumClassName =
                buildEnumClassName(
                        normalizedEnumCode
                );

        try {
            Class<?> loadedClass =
                    Class.forName(
                            enumClassName
                    );

            if (!loadedClass.isEnum()) {
                throw new BadRequestAlertException(
                        "Provided enum code does not refer to a valid enum",
                        ENTITY_NAME,
                        "enum.code.invalid"
                );
            }

            String normalizedValue =
                    value.trim()
                            .toUpperCase(
                                    Locale.ROOT
                            );

            Enum.valueOf(
                    (Class<? extends Enum>)
                            loadedClass,
                    normalizedValue
            );

            return normalizedValue;

        } catch (
                ClassNotFoundException exception
        ) {
            throw new BadRequestAlertException(
                    "Enum was not found for code: "
                            + normalizedEnumCode,
                    ENTITY_NAME,
                    "enum.code.notfound"
            );

        } catch (
                IllegalArgumentException exception
        ) {
            throw new BadRequestAlertException(
                    "Invalid enum value "
                            + value
                            + " for enum "
                            + normalizedEnumCode,
                    ENTITY_NAME,
                    "enum.value.invalid"
            );
        }
    }

    @SuppressWarnings({
            "unchecked",
            "rawtypes"
    })
    private Object convertEnum(
            String enumCode,
            String value
    ) throws ClassNotFoundException {
        String normalizedEnumCode =
                normalizeEnumCode(
                        enumCode
                );

        String enumClassName =
                buildEnumClassName(
                        normalizedEnumCode
                );

        Class<?> loadedClass =
                Class.forName(
                        enumClassName
                );

        if (!loadedClass.isEnum()) {
            throw new BadRequestAlertException(
                    "Provided enum code does not refer to a valid enum",
                    ENTITY_NAME,
                    "enum.code.invalid"
            );
        }

        return Enum.valueOf(
                (Class<? extends Enum>)
                        loadedClass,
                value.trim()
                        .toUpperCase(
                                Locale.ROOT
                        )
        );
    }

    /*
     * Returns a normalized enum code only when Value Type is ENUM.
     *
     * For all other value types, enumCode is stored as null.
     */
    private String resolveEnumCode(
            BillingConfigurationValueType valueType,
            String enumCode
    ) {
        if (
                valueType
                        != BillingConfigurationValueType.ENUM
        ) {
            return null;
        }

        String normalizedEnumCode =
                normalizeEnumCode(
                        enumCode
                );

        validateEnumCodeExists(
                normalizedEnumCode
        );

        return normalizedEnumCode;
    }

    /*
     * Converts:
     *
     * BillingRoundingMethod
     *
     * into:
     *
     * com.dazzle.asklepios.domain.enumeration.BillingRoundingMethod
     */
    private String buildEnumClassName(
            String enumCode
    ) {
        return ENUM_PACKAGE
                + enumCode;
    }

    /*
     * The database stores only the short enum name.
     *
     * If the frontend accidentally sends a full class name,
     * this method extracts and stores only the last part.
     */
    private String normalizeEnumCode(
            String enumCode
    ) {
        if (
                enumCode == null
                        ||
                        enumCode.isBlank()
        ) {
            throw new BadRequestAlertException(
                    "Enum code is required",
                    ENTITY_NAME,
                    "enum.code.required"
            );
        }

        String normalizedEnumCode =
                enumCode.trim();

        int lastDotIndex =
                normalizedEnumCode.lastIndexOf(
                        '.'
                );

        if (
                lastDotIndex >= 0
                        &&
                        lastDotIndex
                                <
                                normalizedEnumCode.length()
                                        - 1
        ) {
            normalizedEnumCode =
                    normalizedEnumCode.substring(
                            lastDotIndex + 1
                    );
        }

        if (
                normalizedEnumCode.isBlank()
        ) {
            throw new BadRequestAlertException(
                    "Enum code is invalid",
                    ENTITY_NAME,
                    "enum.code.invalid"
            );
        }

        /*
         * Prevent invalid class names and prevent another package
         * from being loaded through the enumCode field.
         */
        if (
                !normalizedEnumCode.matches(
                        "[A-Za-z_$][A-Za-z0-9_$]*"
                )
        ) {
            throw new BadRequestAlertException(
                    "Enum code contains invalid characters",
                    ENTITY_NAME,
                    "enum.code.invalid"
            );
        }

        return normalizedEnumCode;
    }

    private void validateEnumCodeExists(
            String enumCode
    ) {
        String enumClassName =
                buildEnumClassName(
                        enumCode
                );

        try {
            Class<?> loadedClass =
                    Class.forName(
                            enumClassName
                    );

            if (!loadedClass.isEnum()) {
                throw new BadRequestAlertException(
                        "Provided enum code does not refer to an enum",
                        ENTITY_NAME,
                        "enum.code.invalid"
                );
            }

        } catch (
                ClassNotFoundException exception
        ) {
            throw new BadRequestAlertException(
                    "Enum was not found for code: "
                            + enumCode,
                    ENTITY_NAME,
                    "enum.code.notfound"
            );
        }
    }

    private String normalizeNullableText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalizedValue =
                value.trim();

        return normalizedValue.isEmpty()
                ? null
                : normalizedValue;
    }

    private void handleConstraintsOnCreateOrUpdate(
            RuntimeException exception
    ) {
        Throwable rootCause =
                getRootCause(
                        exception
                );

        String errorMessage =
                rootCause != null
                        ?
                        rootCause.getMessage()
                        :
                        exception.getMessage();

        LOG.error(
                "BillingConfiguration DB ROOT CAUSE: {}",
                errorMessage,
                exception
        );

        String lowerCaseErrorMessage =
                errorMessage != null
                        ?
                        errorMessage.toLowerCase(
                                Locale.ROOT
                        )
                        :
                        "";

        if (
                lowerCaseErrorMessage.contains(
                        "uk_billing_configuration_facility_key"
                )
                        ||
                        lowerCaseErrorMessage.contains(
                                "duplicate"
                        )
                        ||
                        lowerCaseErrorMessage.contains(
                                "unique"
                        )
        ) {
            throw new BadRequestAlertException(
                    "A billing configuration with the same key already exists for this facility.",
                    ENTITY_NAME,
                    "key.exists"
            );
        }

        if (
                lowerCaseErrorMessage.contains(
                        "fk_billing_configuration_facility"
                )
                        ||
                        lowerCaseErrorMessage.contains(
                                "facility_id"
                        )
        ) {
            throw new BadRequestAlertException(
                    "Invalid facility reference.",
                    ENTITY_NAME,
                    "facility.invalid"
            );
        }

        throw new BadRequestAlertException(
                "Database constraint violated while saving billing configuration.",
                ENTITY_NAME,
                "db.constraint"
        );
    }
}