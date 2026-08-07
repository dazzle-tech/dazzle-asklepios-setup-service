package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.FinancialDocumentNumbering;
import com.dazzle.asklepios.domain.enumeration.BillingConfigurationStatus;
import com.dazzle.asklepios.domain.enumeration.BillingResetFrequency;
import com.dazzle.asklepios.domain.enumeration.biling.FinancialDocumentType;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.FinancialDocumentNumberingRepository;
import com.dazzle.asklepios.service.dto.FinancialDocumentNumberRequest;
import com.dazzle.asklepios.service.dto.FinancialDocumentNumberResponse;
import com.dazzle.asklepios.service.dto.FinancialDocumentNumberingBulkDTO;
import com.dazzle.asklepios.service.dto.FinancialDocumentNumberingDTO;
import com.dazzle.asklepios.service.dto.FinancialDocumentSequenceStatusDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.errors.NotFoundAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FinancialDocumentNumberingService {

    private static final Logger LOG =
            LoggerFactory.getLogger(
                    FinancialDocumentNumberingService.class
            );

    private static final String ENTITY_NAME =
            "financialDocumentNumbering";

    private static final Map<
            FinancialDocumentType,
            String
            > DEFAULT_PREFIXES =
            Map.of(
                    FinancialDocumentType.INVOICE,
                    "INV",

                    FinancialDocumentType.CREDIT_NOTE,
                    "CN",

                    FinancialDocumentType.DEBIT_NOTE,
                    "DN",

                    FinancialDocumentType.RECEIPT,
                    "RCP",

                    FinancialDocumentType.PAYMENT,
                    "PAY",

                    FinancialDocumentType.REFUND,
                    "REF"
            );

    private final FinancialDocumentNumberingRepository
            numberingRepository;

    private final FacilityRepository
            facilityRepository;

    public FinancialDocumentNumberingService(
            FinancialDocumentNumberingRepository numberingRepository,
            FacilityRepository facilityRepository
    ) {
        this.numberingRepository =
                numberingRepository;

        this.facilityRepository =
                facilityRepository;
    }

    public FinancialDocumentNumbering create(
            FinancialDocumentNumberingDTO dto
    ) {
        LOG.info(
                "[CREATE] FinancialDocumentNumbering payload={}",
                dto
        );

        validateCreatePayload(
                dto
        );

        validateFacility(
                dto.facilityId()
        );

        validateUniqueDocumentType(
                dto.facilityId(),
                dto.documentType(),
                null
        );

        try {
            FinancialDocumentNumbering created =
                    numberingRepository.saveAndFlush(
                            toEntity(
                                    dto,
                                    null
                            )
                    );

            LOG.info(
                    "Created FinancialDocumentNumbering id={} facilityId={} type={}",
                    created.getId(),
                    created.getFacilityId(),
                    created.getDocumentType()
            );

            return created;
        } catch (
                DataAccessException exception
        ) {
            LOG.error(
                    "Failed to create FinancialDocumentNumbering",
                    exception
            );

            throw new BadRequestAlertException(
                    "Financial document numbering tables are not available. Apply the gateway Liquibase changelog first.",
                    ENTITY_NAME,
                    "db.notready"
            );
        }
    }

    public FinancialDocumentNumbering update(
            Long id,
            FinancialDocumentNumberingDTO dto
    ) {
        LOG.info(
                "[UPDATE] FinancialDocumentNumbering id={} payload={}",
                id,
                dto
        );

        if (
                id == null
                        ||
                        dto.id() == null
                        ||
                        !id.equals(
                                dto.id()
                        )
        ) {
            throw new BadRequestAlertException(
                    "Invalid financial document numbering id",
                    ENTITY_NAME,
                    "id.invalid"
            );
        }

        validateCreatePayload(
                dto
        );

        validateFacility(
                dto.facilityId()
        );

        FinancialDocumentNumbering existing =
                findById(
                        id
                );

        validateUniqueDocumentType(
                dto.facilityId(),
                dto.documentType(),
                id
        );

        applyDto(
                existing,
                dto
        );

        return numberingRepository.saveAndFlush(
                existing
        );
    }

    public List<FinancialDocumentNumbering>
    saveBulk(
            FinancialDocumentNumberingBulkDTO bulkDto
    ) {
        LOG.info(
                "[SAVE BULK] FinancialDocumentNumbering facilityId={} count={}",
                bulkDto.facilityId(),
                bulkDto.configurations().size()
        );

        validateFacility(
                bulkDto.facilityId()
        );

        validateBulkPayload(
                bulkDto
        );

        Map<
                FinancialDocumentType,
                FinancialDocumentNumbering
                > existingByType =
                loadPersistedConfigurations(
                                bulkDto.facilityId()
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        FinancialDocumentNumbering::getDocumentType,
                                        item -> item,
                                        (
                                                left,
                                                right
                                        ) -> left,
                                        LinkedHashMap::new
                                )
                        );

        List<FinancialDocumentNumbering> saved =
                new ArrayList<>();

        for (
                FinancialDocumentNumberingDTO dto :
                        bulkDto.configurations()
        ) {
            FinancialDocumentNumberingDTO normalized =
                    withFacilityId(
                            dto,
                            bulkDto.facilityId()
                    );

            FinancialDocumentNumbering existing =
                    existingByType.get(
                            normalized.documentType()
                    );

            if (
                    existing == null
            ) {
                FinancialDocumentNumberingDTO createDto =
                        new FinancialDocumentNumberingDTO(
                                null,
                                normalized.facilityId(),
                                normalized.documentType(),
                                normalized.prefix(),
                                normalized.sequenceLength(),
                                normalized.includeYear(),
                                normalized.includeFacilityCode(),
                                normalized.numberSeparator(),
                                normalized.resetFrequency(),
                                normalized.startingNumber(),
                                normalized.active(),
                                normalized.status()
                        );

                saved.add(
                        create(
                                createDto
                        )
                );
            } else {
                FinancialDocumentNumberingDTO updateDto =
                        new FinancialDocumentNumberingDTO(
                                existing.getId(),
                                normalized.facilityId(),
                                normalized.documentType(),
                                normalized.prefix(),
                                normalized.sequenceLength(),
                                normalized.includeYear(),
                                normalized.includeFacilityCode(),
                                normalized.numberSeparator(),
                                normalized.resetFrequency(),
                                normalized.startingNumber(),
                                normalized.active(),
                                normalized.status()
                        );

                saved.add(
                        update(
                                existing.getId(),
                                updateDto
                        )
                );
            }
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public FinancialDocumentNumbering findById(
            Long id
    ) {
        if (
                id == null
        ) {
            throw new BadRequestAlertException(
                    "Financial document numbering id is required",
                    ENTITY_NAME,
                    "id.required"
            );
        }

        return numberingRepository
                .findById(
                        id
                )
                .orElseThrow(
                        () ->
                                new NotFoundAlertException(
                                        "Financial document numbering not found with id "
                                                + id,
                                        ENTITY_NAME,
                                        "notfound"
                                )
                );
    }

    @Transactional(readOnly = true)
    public List<FinancialDocumentNumbering>
    findByFacilityId(
            Long facilityId
    ) {
        validateFacility(
                facilityId
        );

        return loadPersistedConfigurations(
                facilityId
        );
    }

    @Transactional(readOnly = true)
    public FinancialDocumentNumbering
    findByFacilityIdAndDocumentType(
            Long facilityId,
            FinancialDocumentType documentType
    ) {
        validateFacility(
                facilityId
        );

        if (
                documentType == null
        ) {
            throw new BadRequestAlertException(
                    "Document type is required",
                    ENTITY_NAME,
                    "document.type.required"
            );
        }

        return findPersistedConfiguration(
                        facilityId,
                        documentType
                )
                .orElseThrow(
                        () ->
                                new NotFoundAlertException(
                                        "Financial document numbering is not configured for facility "
                                                + facilityId
                                                + " and document type "
                                                + documentType,
                                        ENTITY_NAME,
                                        "notfound"
                                )
                );
    }

    private List<FinancialDocumentNumbering>
    loadPersistedConfigurations(
            Long facilityId
    ) {
        try {
            return numberingRepository
                    .findByFacilityIdOrderByDocumentTypeAsc(
                            facilityId
                    );
        } catch (
                RuntimeException exception
        ) {
            throw mapDatabaseAccessException(
                    exception
            );
        }
    }

    private Optional<FinancialDocumentNumbering>
    findPersistedConfiguration(
            Long facilityId,
            FinancialDocumentType documentType
    ) {
        try {
            return numberingRepository
                    .findByFacilityIdAndDocumentType(
                            facilityId,
                            documentType
                    );
        } catch (
                RuntimeException exception
        ) {
            throw mapDatabaseAccessException(
                    exception
            );
        }
    }

    private RuntimeException mapDatabaseAccessException(
            RuntimeException exception
    ) {
        String message =
                exception.getMessage() != null
                        ? exception.getMessage().toLowerCase(
                                Locale.ROOT
                        )
                        : "";

        if (
                message.contains(
                        "current_period_key does not exist"
                )
                        || message.contains(
                        "last_number does not exist"
                )
                        || message.contains(
                        "version does not exist"
                )
                        || message.contains(
                        "financial_document_numbering"
                )
                                && message.contains(
                                "does not exist"
                        )
        ) {
            LOG.error(
                    "Financial document numbering schema is out of date",
                    exception
            );

            return new BadRequestAlertException(
                    "Financial document numbering columns are missing. "
                            + "Deploy the gateway Liquibase changelog "
                            + "(1786080700_B_financial_document_numbering_counter.xml), "
                            + "then restart the setup service.",
                    ENTITY_NAME,
                    "db.schema.outdated"
            );
        }

        LOG.warn(
                "Unable to access financial document numbering tables: {}",
                exception.getMessage()
        );

        return new BadRequestAlertException(
                "Financial document numbering tables are not available. "
                        + "Apply the database migration first.",
                ENTITY_NAME,
                "db.notready"
        );
    }

    public FinancialDocumentNumbering changeActivationStatus(
            Long id,
            boolean active
    ) {
        FinancialDocumentNumbering existing =
                findById(
                        id
                );

        existing.setActive(
                active
        );

        if (
                !active
        ) {
            existing.setStatus(
                    BillingConfigurationStatus.INACTIVE
            );
        }

        return numberingRepository.saveAndFlush(
                existing
        );
    }

    public FinancialDocumentNumbering changeStatus(
            Long id,
            BillingConfigurationStatus status
    ) {
        if (
                status == null
        ) {
            throw new BadRequestAlertException(
                    "Status is required",
                    ENTITY_NAME,
                    "status.required"
            );
        }

        FinancialDocumentNumbering existing =
                findById(
                        id
                );

        if (
                status
                        == BillingConfigurationStatus.ACTIVE
        ) {
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

        return numberingRepository.saveAndFlush(
                existing
        );
    }

    public boolean delete(
            Long id
    ) {
        FinancialDocumentNumbering existing =
                numberingRepository
                        .findById(
                                id
                        )
                        .orElse(
                                null
                        );

        if (
                existing == null
        ) {
            return false;
        }

        if (
                existing.getStatus()
                        == BillingConfigurationStatus.ACTIVE
        ) {
            throw new BadRequestAlertException(
                    "Active financial document numbering cannot be deleted",
                    ENTITY_NAME,
                    "active.delete"
            );
        }

        numberingRepository.delete(
                existing
        );

        numberingRepository.flush();

        return true;
    }

    public FinancialDocumentNumberResponse
    generateNextNumber(
            FinancialDocumentNumberRequest request
    ) {
        if (
                request == null
                        ||
                        request.facilityId() == null
                        ||
                        request.documentType() == null
        ) {
            throw new BadRequestAlertException(
                    "Facility id and document type are required",
                    ENTITY_NAME,
                    "payload.required"
            );
        }

        FinancialDocumentNumbering numbering =
                numberingRepository
                        .findForUpdate(
                                request.facilityId(),
                                request.documentType()
                        )
                        .orElseThrow(
                                () ->
                                        new NotFoundAlertException(
                                                "Financial document numbering is not configured for facility "
                                                        + request.facilityId()
                                                        + " and type "
                                                        + request.documentType(),
                                                ENTITY_NAME,
                                                "notfound"
                                        )
                        );

        if (
                !Boolean.TRUE.equals(
                        numbering.getActive()
                )
                        ||
                        numbering.getStatus()
                                != BillingConfigurationStatus.ACTIVE
        ) {
            throw new BadRequestAlertException(
                    "Financial document numbering is not active",
                    ENTITY_NAME,
                    "configuration.inactive"
            );
        }

        LocalDate documentDate =
                request.documentDate() != null
                        ?
                        request.documentDate()
                        :
                        LocalDate.now();

        String periodKey =
                resolvePeriodKey(
                        numbering.getResetFrequency(),
                        documentDate
                );

        long nextSequence =
                reserveNextSequenceOnConfiguration(
                        numbering,
                        periodKey,
                        request.minimumUsedSequence()
                );

        numberingRepository.saveAndFlush(
                numbering
        );

        String documentNumber =
                formatDocumentNumber(
                        numbering,
                        nextSequence,
                        documentDate
                );

        LOG.info(
                "Generated financial document number facilityId={} type={} number={}",
                request.facilityId(),
                request.documentType(),
                documentNumber
        );

        return new FinancialDocumentNumberResponse(
                documentNumber,
                nextSequence,
                periodKey,
                request.documentType(),
                request.facilityId()
        );
    }

    @Transactional(readOnly = true)
    public List<FinancialDocumentSequenceStatusDTO>
    getSequenceStatus(
            Long facilityId,
            FinancialDocumentType documentType
    ) {
        validateFacility(
                facilityId
        );

        FinancialDocumentNumbering numbering =
                findPersistedConfiguration(
                                facilityId,
                                documentType
                        )
                        .orElseThrow(
                                () ->
                                        new NotFoundAlertException(
                                                "Financial document numbering is not configured for facility "
                                                        + facilityId
                                                        + " and document type "
                                                        + documentType,
                                                ENTITY_NAME,
                                                "notfound"
                                        )
                        );

        LocalDate today =
                LocalDate.now();

        String currentPeriodKey =
                resolvePeriodKey(
                        numbering.getResetFrequency(),
                        today
                );

        long lastAssigned =
                resolveLastAssignedSequence(
                        numbering,
                        currentPeriodKey
                );

        long nextNumber =
                lastAssigned + 1;

        return List.of(
                new FinancialDocumentSequenceStatusDTO(
                        documentType,
                        currentPeriodKey,
                        lastAssigned,
                        nextNumber,
                        formatDocumentNumber(
                                numbering,
                                nextNumber,
                                today
                        )
                )
        );
    }

    @Transactional(readOnly = true)
    public FinancialDocumentSequenceStatusDTO
    previewNextNumber(
            Long facilityId,
            FinancialDocumentType documentType,
            LocalDate documentDate
    ) {
        FinancialDocumentNumbering numbering =
                findPersistedConfiguration(
                                facilityId,
                                documentType
                        )
                        .orElseThrow(
                                () ->
                                        new NotFoundAlertException(
                                                "Financial document numbering is not configured for facility "
                                                        + facilityId
                                                        + " and document type "
                                                        + documentType,
                                                ENTITY_NAME,
                                                "notfound"
                                        )
                        );

        LocalDate effectiveDate =
                documentDate != null
                        ? documentDate
                        : LocalDate.now();

        String periodKey =
                resolvePeriodKey(
                        numbering.getResetFrequency(),
                        effectiveDate
                );

        long lastAssigned =
                resolveLastAssignedSequence(
                        numbering,
                        periodKey
                );

        long nextNumber =
                lastAssigned + 1;

        return new FinancialDocumentSequenceStatusDTO(
                documentType,
                periodKey,
                lastAssigned,
                nextNumber,
                formatDocumentNumber(
                        numbering,
                        nextNumber,
                        effectiveDate
                )
        );
    }

    public String previewDocumentNumber(
            FinancialDocumentNumbering numbering,
            Long sequenceNumber,
            LocalDate documentDate
    ) {
        return formatDocumentNumber(
                numbering,
                sequenceNumber,
                documentDate
        );
    }

    public FinancialDocumentNumberingDTO toDto(
            FinancialDocumentNumbering entity
    ) {
        return new FinancialDocumentNumberingDTO(
                entity.getId(),
                entity.getFacilityId(),
                entity.getDocumentType(),
                entity.getPrefix(),
                entity.getSequenceLength(),
                entity.getIncludeYear(),
                entity.getIncludeFacilityCode(),
                entity.getNumberSeparator(),
                entity.getResetFrequency(),
                entity.getStartingNumber(),
                entity.getActive(),
                entity.getStatus()
        );
    }

    private FinancialDocumentNumbering defaultTemplate(
            Long facilityId,
            FinancialDocumentType documentType
    ) {
        return FinancialDocumentNumbering
                .builder()
                .facilityId(
                        facilityId
                )
                .documentType(
                        documentType
                )
                .prefix(
                        DEFAULT_PREFIXES.get(
                                documentType
                        )
                )
                .sequenceLength(
                        6
                )
                .includeYear(
                        true
                )
                .includeFacilityCode(
                        false
                )
                .numberSeparator(
                        "-"
                )
                .resetFrequency(
                        BillingResetFrequency.YEARLY
                )
                .startingNumber(
                        1L
                )
                .active(
                        true
                )
                .status(
                        BillingConfigurationStatus.DRAFT
                )
                .build();
    }

    private FinancialDocumentNumbering toEntity(
            FinancialDocumentNumberingDTO dto,
            FinancialDocumentNumbering existing
    ) {
        FinancialDocumentNumbering entity =
                existing != null
                        ?
                        existing
                        :
                        new FinancialDocumentNumbering();

        applyDto(
                entity,
                dto
        );

        return entity;
    }

    private void applyDto(
            FinancialDocumentNumbering entity,
            FinancialDocumentNumberingDTO dto
    ) {
        entity.setFacilityId(
                dto.facilityId()
        );

        entity.setDocumentType(
                dto.documentType()
        );

        entity.setPrefix(
                normalizePrefix(
                        dto.prefix()
                )
        );

        entity.setSequenceLength(
                dto.sequenceLength()
        );

        entity.setIncludeYear(
                dto.includeYear()
        );

        entity.setIncludeFacilityCode(
                dto.includeFacilityCode()
        );

        entity.setNumberSeparator(
                normalizeSeparator(
                        dto.numberSeparator()
                )
        );

        entity.setResetFrequency(
                dto.resetFrequency()
        );

        entity.setStartingNumber(
                dto.startingNumber()
        );

        entity.setActive(
                dto.active() == null
                        ||
                        dto.active()
        );

        entity.setStatus(
                dto.status() == null
                        ?
                        BillingConfigurationStatus.DRAFT
                        :
                        dto.status()
        );
    }

    private void validateCreatePayload(
            FinancialDocumentNumberingDTO dto
    ) {
        if (
                dto == null
        ) {
            throw new BadRequestAlertException(
                    "Financial document numbering payload is required",
                    ENTITY_NAME,
                    "payload.required"
            );
        }

        if (
                dto.facilityId() == null
        ) {
            throw new BadRequestAlertException(
                    "Facility id is required",
                    ENTITY_NAME,
                    "facility.required"
            );
        }

        if (
                dto.documentType() == null
        ) {
            throw new BadRequestAlertException(
                    "Document type is required",
                    ENTITY_NAME,
                    "document.type.required"
            );
        }

        if (
                dto.prefix() == null
                        ||
                        dto.prefix().isBlank()
        ) {
            throw new BadRequestAlertException(
                    "Prefix is required",
                    ENTITY_NAME,
                    "prefix.required"
            );
        }

        if (
                dto.sequenceLength() == null
                        ||
                        dto.sequenceLength() < 1
                        ||
                        dto.sequenceLength() > 12
        ) {
            throw new BadRequestAlertException(
                    "Sequence length must be between 1 and 12",
                    ENTITY_NAME,
                    "sequence.length.invalid"
            );
        }

        if (
                dto.startingNumber() == null
                        ||
                        dto.startingNumber() < 1
        ) {
            throw new BadRequestAlertException(
                    "Starting number must be at least 1",
                    ENTITY_NAME,
                    "starting.number.invalid"
            );
        }

        if (
                dto.resetFrequency() == null
        ) {
            throw new BadRequestAlertException(
                    "Reset frequency is required",
                    ENTITY_NAME,
                    "reset.frequency.required"
            );
        }
    }

    private void validateBulkPayload(
            FinancialDocumentNumberingBulkDTO bulkDto
    ) {
        if (
                bulkDto.configurations() == null
                        ||
                        bulkDto.configurations().isEmpty()
        ) {
            throw new BadRequestAlertException(
                    "At least one document numbering configuration is required",
                    ENTITY_NAME,
                    "configurations.required"
            );
        }

        long distinctTypes =
                bulkDto
                        .configurations()
                        .stream()
                        .map(
                                FinancialDocumentNumberingDTO::documentType
                        )
                        .distinct()
                        .count();

        if (
                distinctTypes
                        != bulkDto
                                .configurations()
                                .size()
        ) {
            throw new BadRequestAlertException(
                    "Duplicate document types are not allowed in bulk save",
                    ENTITY_NAME,
                    "document.type.duplicate"
            );
        }

        for (
                FinancialDocumentNumberingDTO dto :
                        bulkDto.configurations()
        ) {
            validateCreatePayload(
                    withFacilityId(
                            dto,
                            bulkDto.facilityId()
                    )
            );
        }
    }

    private FinancialDocumentNumberingDTO withFacilityId(
            FinancialDocumentNumberingDTO dto,
            Long facilityId
    ) {
        return new FinancialDocumentNumberingDTO(
                dto.id(),
                facilityId,
                dto.documentType(),
                dto.prefix(),
                dto.sequenceLength(),
                dto.includeYear(),
                dto.includeFacilityCode(),
                dto.numberSeparator(),
                dto.resetFrequency(),
                dto.startingNumber(),
                dto.active(),
                dto.status()
        );
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

    private void validateUniqueDocumentType(
            Long facilityId,
            FinancialDocumentType documentType,
            Long excludedId
    ) {
        boolean exists =
                excludedId == null
                        ?
                        numberingRepository
                                .existsByFacilityIdAndDocumentType(
                                        facilityId,
                                        documentType
                                )
                        :
                        numberingRepository
                                .existsByFacilityIdAndDocumentTypeAndIdNot(
                                        facilityId,
                                        documentType,
                                        excludedId
                                );

        if (
                exists
        ) {
            numberingRepository
                    .findByFacilityIdAndDocumentType(
                            facilityId,
                            documentType
                    )
                    .ifPresentOrElse(
                            existing ->
                                    LOG.warn(
                                            "Duplicate financial document numbering blocked facilityId={} type={} existingId={}",
                                            facilityId,
                                            documentType,
                                            existing.getId()
                                    ),
                            () -> {}
                    );

            throw new BadRequestAlertException(
                    "Document numbering already exists for facility "
                            + facilityId
                            + " and document type "
                            + documentType
                            + ". Edit the existing configuration instead of creating a new one.",
                    ENTITY_NAME,
                    "document.type.exists"
            );
        }
    }

    private String normalizePrefix(
            String prefix
    ) {
        return prefix
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }

    private String normalizeSeparator(
            String separator
    ) {
        if (
                separator == null
                        ||
                        separator.isBlank()
        ) {
            return "";
        }

        return separator.trim();
    }

    private long reserveNextSequenceOnConfiguration(
            FinancialDocumentNumbering numbering,
            String periodKey,
            Long minimumUsedSequence
    ) {
        long lastAssigned =
                resolveLastAssignedSequence(
                        numbering,
                        periodKey
                );

        if (
                minimumUsedSequence != null
                        && minimumUsedSequence > lastAssigned
        ) {
            LOG.warn(
                    "Advancing financial document counter from setup value {} to issued value {} for facility {} type {} period {}",
                    lastAssigned,
                    minimumUsedSequence,
                    numbering.getFacilityId(),
                    numbering.getDocumentType(),
                    periodKey
            );
            lastAssigned = minimumUsedSequence;
        }

        long nextSequence = lastAssigned + 1;

        numbering.setCurrentPeriodKey(
                periodKey
        );
        numbering.setLastNumber(
                nextSequence
        );

        return nextSequence;
    }

    private long resolveLastAssignedSequence(
            FinancialDocumentNumbering numbering,
            String periodKey
    ) {
        if (
                periodKey.equals(
                        numbering.getCurrentPeriodKey()
                )
                        && numbering.getLastNumber() != null
        ) {
            return numbering.getLastNumber();
        }

        return numbering.getStartingNumber() - 1;
    }

    private String resolvePeriodKey(
            BillingResetFrequency resetFrequency,
            LocalDate documentDate
    ) {
        return switch (
                resetFrequency
        ) {
            case NEVER ->
                    "ALL";

            case YEARLY ->
                    String.valueOf(
                            documentDate.getYear()
                    );

            case MONTHLY ->
                    documentDate.format(
                            DateTimeFormatter.ofPattern(
                                    "yyyy-MM"
                            )
                    );

            case DAILY ->
                    documentDate.format(
                            DateTimeFormatter.ISO_LOCAL_DATE
                    );
        };
    }

    private String formatDocumentNumber(
            FinancialDocumentNumbering numbering,
            long sequenceNumber,
            LocalDate documentDate
    ) {
        String separator =
                numbering.getNumberSeparator();

        List<String> parts =
                new ArrayList<>();

        parts.add(
                numbering.getPrefix()
        );

        if (
                Boolean.TRUE.equals(
                        numbering.getIncludeFacilityCode()
                )
        ) {
            Facility facility =
                    facilityRepository
                            .findById(
                                    numbering.getFacilityId()
                            )
                            .orElseThrow(
                                    () ->
                                            new NotFoundAlertException(
                                                    "Facility not found with id "
                                                            + numbering.getFacilityId(),
                                                    ENTITY_NAME,
                                                    "facility.notfound"
                                            )
                            );

            parts.add(
                    facility.getCode()
            );
        }

        if (
                Boolean.TRUE.equals(
                        numbering.getIncludeYear()
                )
        ) {
            parts.add(
                    String.valueOf(
                            documentDate.getYear()
                    )
            );
        }

        parts.add(
                String.format(
                        Locale.ROOT,
                        "%0"
                                + numbering.getSequenceLength()
                                + "d",
                        sequenceNumber
                )
        );

        return String.join(
                separator,
                parts
        );
    }
}
