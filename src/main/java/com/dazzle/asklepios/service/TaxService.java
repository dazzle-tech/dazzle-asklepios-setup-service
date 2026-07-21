package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Tax;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.TaxCalculationType;
import com.dazzle.asklepios.domain.enumeration.TaxType;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.TaxRepository;
import com.dazzle.asklepios.service.dto.TaxDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class TaxService {

    private static final Logger LOG =
            LoggerFactory.getLogger(TaxService.class);

    private static final String ENTITY_NAME = "tax";

    private final TaxRepository taxRepository;
    private final FacilityRepository facilityRepository;

    public TaxService(
            TaxRepository taxRepository,
            FacilityRepository facilityRepository
    ) {
        this.taxRepository = taxRepository;
        this.facilityRepository = facilityRepository;
    }

    public Tax create(TaxDTO taxDTO) {
        LOG.info(
                "[CREATE] Request to create Tax payload={}",
                taxDTO
        );

        if (taxDTO.id() != null) {
            throw new BadRequestAlertException(
                    "A new tax cannot already have an id",
                    ENTITY_NAME,
                    "id.exists"
            );
        }

        validateDTO(taxDTO);
        validateFacility(taxDTO.facilityId());
        validateUniqueCode(
                taxDTO.facilityId(),
                taxDTO.code(),
                null
        );

        normalizeTaxValues(taxDTO);


        Tax tax = Tax.builder()
                .facilityId(taxDTO.facilityId())
                .code(normalizeCode(taxDTO.code()))
                .name(taxDTO.name().trim())
                .taxType(taxDTO.taxType())
                .percentage(resolvePercentage(taxDTO))
                .fixedAmount(resolveFixedAmount(taxDTO))
                .currency(taxDTO.currency())
                .calculationType(taxDTO.calculationType())
                .applicableOn(taxDTO.applicableOn())
                .validFrom(taxDTO.validFrom())
                .validTo(taxDTO.validTo())
                .description(trimToNull(taxDTO.description()))
                .build();

        try {
            Tax created = taxRepository.saveAndFlush(tax);

            LOG.info(
                    "[CREATE] Successfully created Tax id={} facilityId={} code={}",
                    created.getId(),
                    created.getFacilityId(),
                    created.getCode()
            );

            return created;

        } catch (
                DataIntegrityViolationException
                | JpaSystemException exception
        ) {
            handleDatabaseConstraint(exception);

            throw new BadRequestAlertException(
                    "Database constraint violated while saving tax",
                    ENTITY_NAME,
                    "db.constraint"
            );
        }
    }

    public Tax update(
            Long id,
            TaxDTO taxDTO
    ) {
        LOG.info(
                "[UPDATE] Request to update Tax id={} payload={}",
                id,
                taxDTO
        );

        if (taxDTO.id() == null) {
            throw new BadRequestAlertException(
                    "Tax id is required",
                    ENTITY_NAME,
                    "id.required"
            );
        }

        if (!id.equals(taxDTO.id())) {
            throw new BadRequestAlertException(
                    "Path id and payload id do not match",
                    ENTITY_NAME,
                    "id.mismatch"
            );
        }

        validateDTO(taxDTO);
        validateFacility(taxDTO.facilityId());

        Tax existing = findById(id);

        validateUniqueCode(
                taxDTO.facilityId(),
                taxDTO.code(),
                id
        );

        normalizeTaxValues(taxDTO);

        if (Boolean.TRUE.equals(taxDTO.isDefault())) {
            clearExistingDefaultTax(
                    taxDTO.facilityId(),
                    id
            );
        }

        existing.setFacilityId(taxDTO.facilityId());
        existing.setCode(normalizeCode(taxDTO.code()));
        existing.setName(taxDTO.name().trim());
        existing.setTaxType(taxDTO.taxType());
        existing.setPercentage(resolvePercentage(taxDTO));
        existing.setFixedAmount(resolveFixedAmount(taxDTO));
        existing.setCurrency(taxDTO.currency());
        existing.setCalculationType(taxDTO.calculationType());
        existing.setApplicableOn(taxDTO.applicableOn());
        existing.setValidFrom(taxDTO.validFrom());
        existing.setValidTo(taxDTO.validTo());
        existing.setIsDefault(Boolean.TRUE.equals(taxDTO.isDefault()));
        existing.setActive(taxDTO.active() == null || taxDTO.active());
        existing.setDescription(trimToNull(taxDTO.description()));

        try {
            Tax updated = taxRepository.saveAndFlush(existing);

            LOG.info(
                    "[UPDATE] Successfully updated Tax id={} facilityId={} code={}",
                    updated.getId(),
                    updated.getFacilityId(),
                    updated.getCode()
            );

            return updated;

        } catch (
                DataIntegrityViolationException
                | JpaSystemException exception
        ) {
            handleDatabaseConstraint(exception);

            throw new BadRequestAlertException(
                    "Database constraint violated while updating tax",
                    ENTITY_NAME,
                    "db.constraint"
            );
        }
    }

    public Tax toggleActive(Long id) {
        LOG.info(
                "[TOGGLE ACTIVE] Request to toggle Tax id={}",
                id
        );

        Tax tax = findById(id);

        boolean newActive =
                !Boolean.TRUE.equals(tax.getActive());

        tax.setActive(newActive);

        if (!newActive) {
            tax.setIsDefault(false);
        }

        return taxRepository.saveAndFlush(tax);
    }

    public Tax changeActivationStatus(
            Long id,
            boolean active
    ) {
        LOG.info(
                "[CHANGE ACTIVATION] Tax id={} active={}",
                id,
                active
        );

        Tax tax = findById(id);

        tax.setActive(active);

        if (!active) {
            tax.setIsDefault(false);
        }

        return taxRepository.saveAndFlush(tax);
    }

    public Tax setDefault(Long id) {
        LOG.info(
                "[SET DEFAULT] Request to set default Tax id={}",
                id
        );

        Tax tax = findById(id);

        if (!Boolean.TRUE.equals(tax.getActive())) {
            throw new BadRequestAlertException(
                    "Inactive tax cannot be set as default",
                    ENTITY_NAME,
                    "default.inactive"
            );
        }

        if (!isCurrentlyValid(tax, LocalDate.now())) {
            throw new BadRequestAlertException(
                    "Expired or future tax cannot be set as default",
                    ENTITY_NAME,
                    "default.invalid.period"
            );
        }

        clearExistingDefaultTax(
                tax.getFacilityId(),
                tax.getId()
        );

        tax.setIsDefault(true);

        return taxRepository.saveAndFlush(tax);
    }

    @Transactional(readOnly = true)
    public Tax findById(Long id) {
        return taxRepository
                .findById(id)
                .orElseThrow(
                        () -> new BadRequestAlertException(
                                "Tax not found with id " + id,
                                ENTITY_NAME,
                                "notfound"
                        )
                );
    }


    @Transactional(readOnly = true)
    public Tax findDefaultTax(Long facilityId) {
        validateFacility(facilityId);

        return taxRepository
                .findFirstByFacilityIdAndIsDefaultTrueAndActiveTrue(
                        facilityId
                )
                .orElseThrow(
                        () -> new BadRequestAlertException(
                                "No active default tax exists for facility "
                                        + facilityId,
                                ENTITY_NAME,
                                "default.notfound"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Page<Tax> findAllByFacilityId(
            Long facilityId,
            Pageable pageable
    ) {
        validateFacility(facilityId);

        return taxRepository.findAllByFacilityId(
                facilityId,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<Tax> findAllActiveByFacilityId(
            Long facilityId,
            Pageable pageable
    ) {
        validateFacility(facilityId);

        return taxRepository
                .findAllByFacilityIdAndActiveTrue(
                        facilityId,
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public Page<Tax> findByCode(
            Long facilityId,
            String code,
            Pageable pageable
    ) {
        validateFacility(facilityId);

        return taxRepository
                .findAllByFacilityIdAndCodeContainingIgnoreCase(
                        facilityId,
                        code.trim(),
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public Page<Tax> findByName(
            Long facilityId,
            String name,
            Pageable pageable
    ) {
        validateFacility(facilityId);

        return taxRepository
                .findAllByFacilityIdAndNameContainingIgnoreCase(
                        facilityId,
                        name.trim(),
                        pageable
                );
    }


    @Transactional(readOnly = true)
    public Page<Tax> findByTaxType(
            Long facilityId,
            TaxType taxType,
            Pageable pageable
    ) {
        validateFacility(facilityId);

        return taxRepository.findAllByFacilityIdAndTaxType(
                facilityId,
                taxType,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<Tax> findByCalculationType(
            Long facilityId,
            TaxCalculationType calculationType,
            Pageable pageable
    ) {
        validateFacility(facilityId);

        return taxRepository
                .findAllByFacilityIdAndCalculationType(
                        facilityId,
                        calculationType,
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public List<Tax> findEffectiveTaxes(
            Long facilityId,
            LocalDate date
    ) {
        validateFacility(facilityId);

        LocalDate effectiveDate =
                date == null
                        ? LocalDate.now()
                        : date;

        List<Tax> result = new ArrayList<>();

        result.addAll(
                taxRepository
                        .findAllByFacilityIdAndActiveTrueAndValidFromLessThanEqualAndValidToIsNull(
                                facilityId,
                                effectiveDate
                        )
        );

        result.addAll(
                taxRepository
                        .findAllByFacilityIdAndActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                                facilityId,
                                effectiveDate,
                                effectiveDate
                        )
        );

        return result;
    }

    public boolean delete(Long id) {
        LOG.info(
                "[DELETE] Request to delete Tax id={}",
                id
        );

        Tax tax = taxRepository
                .findById(id)
                .orElse(null);

        if (tax == null) {
            return false;
        }

        if (Boolean.TRUE.equals(tax.getActive())) {
            throw new BadRequestAlertException(
                    "Active tax cannot be deleted. Deactivate it first",
                    ENTITY_NAME,
                    "active.delete"
            );
        }

        try {
            taxRepository.delete(tax);
            taxRepository.flush();

            return true;

        } catch (
                DataIntegrityViolationException
                | JpaSystemException exception
        ) {
            throw new BadRequestAlertException(
                    "Tax is already used and cannot be deleted",
                    ENTITY_NAME,
                    "tax.in.use"
            );
        }
    }

    private void validateDTO(TaxDTO dto) {
        if (dto.facilityId() == null) {
            throw new BadRequestAlertException(
                    "Facility id is required",
                    ENTITY_NAME,
                    "facility.required"
            );
        }

        if (dto.code() == null || dto.code().isBlank()) {
            throw new BadRequestAlertException(
                    "Tax code is required",
                    ENTITY_NAME,
                    "code.required"
            );
        }

        if (dto.name() == null || dto.name().isBlank()) {
            throw new BadRequestAlertException(
                    "tax name is required",
                    ENTITY_NAME,
                    "name.en.required"
            );
        }

        if (dto.taxType() == null) {
            throw new BadRequestAlertException(
                    "Tax type is required",
                    ENTITY_NAME,
                    "type.required"
            );
        }

        if (dto.calculationType() == null) {
            throw new BadRequestAlertException(
                    "Calculation type is required",
                    ENTITY_NAME,
                    "calculation.type.required"
            );
        }

        if (dto.applicableOn() == null) {
            throw new BadRequestAlertException(
                    "Applicable-on value is required",
                    ENTITY_NAME,
                    "applicable.on.required"
            );
        }

        if (dto.validFrom() == null) {
            throw new BadRequestAlertException(
                    "Valid-from date is required",
                    ENTITY_NAME,
                    "valid.from.required"
            );
        }

        if (
                dto.validTo() != null
                        && dto.validTo().isBefore(dto.validFrom())
        ) {
            throw new BadRequestAlertException(
                    "Valid-to date cannot be before valid-from date",
                    ENTITY_NAME,
                    "valid.period.invalid"
            );
        }
    }

    private void normalizeTaxValues(TaxDTO dto) {
        if (dto.taxType() == TaxType.PERCENTAGE) {
            validatePercentage(dto.percentage());
            return;
        }

        validateFixedAmount(
                dto.fixedAmount(),
                dto.currency()
        );
    }

    private void validatePercentage(BigDecimal percentage) {
        if (percentage == null) {
            throw new BadRequestAlertException(
                    "Percentage is required for percentage tax",
                    ENTITY_NAME,
                    "percentage.required"
            );
        }

        if (
                percentage.compareTo(BigDecimal.ZERO) < 0
                        || percentage.compareTo(
                        BigDecimal.valueOf(100)
                ) > 0
        ) {
            throw new BadRequestAlertException(
                    "Tax percentage must be between 0 and 100",
                    ENTITY_NAME,
                    "percentage.invalid"
            );
        }
    }

    private void validateFixedAmount(
            BigDecimal fixedAmount,
            Currency currencyId
    ) {
        if (fixedAmount == null) {
            throw new BadRequestAlertException(
                    "Fixed amount is required for fixed-amount tax",
                    ENTITY_NAME,
                    "fixed.amount.required"
            );
        }

        if (fixedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestAlertException(
                    "Fixed tax amount cannot be negative",
                    ENTITY_NAME,
                    "fixed.amount.invalid"
            );
        }

        if (currencyId == null) {
            throw new BadRequestAlertException(
                    "Currency is required for fixed-amount tax",
                    ENTITY_NAME,
                    "currency.required"
            );
        }

    }

    private BigDecimal resolvePercentage(TaxDTO dto) {
        return dto.taxType() == TaxType.PERCENTAGE
                ? dto.percentage()
                : null;
    }

    private BigDecimal resolveFixedAmount(TaxDTO dto) {
        return dto.taxType() == TaxType.FIXED_AMOUNT
                ? dto.fixedAmount()
                : null;
    }

    private void validateFacility(Long facilityId) {
        if (
                facilityId == null
                        || !facilityRepository.existsById(facilityId)
        ) {
            throw new BadRequestAlertException(
                    "Facility not found with id " + facilityId,
                    ENTITY_NAME,
                    "facility.notfound"
            );
        }
    }

    private void validateUniqueCode(
            Long facilityId,
            String code,
            Long excludedId
    ) {
        boolean exists = excludedId == null
                ? taxRepository
                .existsByFacilityIdAndCodeIgnoreCase(
                        facilityId,
                        code.trim()
                )
                : taxRepository
                .existsByFacilityIdAndCodeIgnoreCaseAndIdNot(
                        facilityId,
                        code.trim(),
                        excludedId
                );

        if (exists) {
            throw new BadRequestAlertException(
                    "Tax code already exists for this facility",
                    ENTITY_NAME,
                    "code.exists"
            );
        }
    }

    private void clearExistingDefaultTax(
            Long facilityId,
            Long excludedId
    ) {
        List<Tax> defaults =
                taxRepository
                        .findAllByFacilityIdAndIsDefaultTrue(
                                facilityId
                        );

        for (Tax existingDefault : defaults) {
            if (
                    excludedId != null
                            && excludedId.equals(
                            existingDefault.getId()
                    )
            ) {
                continue;
            }

            existingDefault.setIsDefault(false);
        }

        if (!defaults.isEmpty()) {
            taxRepository.saveAll(defaults);
            taxRepository.flush();
        }
    }

    private boolean isCurrentlyValid(
            Tax tax,
            LocalDate date
    ) {
        boolean started =
                !tax.getValidFrom().isAfter(date);

        boolean notExpired =
                tax.getValidTo() == null
                        || !tax.getValidTo().isBefore(date);

        return started && notExpired;
    }

    private String normalizeCode(String code) {
        return code
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private void handleDatabaseConstraint(
            RuntimeException exception
    ) {
        Throwable rootCause = getRootCause(exception);

        String message = rootCause != null
                ? rootCause.getMessage()
                : exception.getMessage();

        LOG.error(
                "Tax database root cause: {}",
                message,
                exception
        );

        String lowerMessage = message == null
                ? ""
                : message.toLowerCase();

        if (
                lowerMessage.contains("uk_tax_facility_code")
                        || lowerMessage.contains("duplicate")
                        || lowerMessage.contains("unique")
        ) {
            throw new BadRequestAlertException(
                    "Tax code already exists for this facility",
                    ENTITY_NAME,
                    "code.exists"
            );
        }

        if (
                lowerMessage.contains("fk_tax_facility")
                        || lowerMessage.contains("facility_id")
        ) {
            throw new BadRequestAlertException(
                    "Invalid facility reference",
                    ENTITY_NAME,
                    "facility.invalid"
            );
        }

        if (
                lowerMessage.contains("fk_tax_currency")
                        || lowerMessage.contains("currency_id")
        ) {
            throw new BadRequestAlertException(
                    "Invalid currency reference",
                    ENTITY_NAME,
                    "currency.invalid"
            );
        }

        throw new BadRequestAlertException(
                "Database constraint violated while saving tax",
                ENTITY_NAME,
                "db.constraint"
        );
    }
}