package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.DiscountCode;
import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.DiscountType;
import com.dazzle.asklepios.repository.DiscountCodeRepository;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.service.dto.discountCode.DiscountCodeCreateDTO;
import com.dazzle.asklepios.service.dto.discountCode.DiscountCodeUpdateDTO;
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

import java.math.BigDecimal;
import java.util.Locale;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
@RequiredArgsConstructor
public class DiscountCodeService {

    private static final Logger LOG = LoggerFactory.getLogger(DiscountCodeService.class);

    private static final String ENTITY_NAME = "discountCode";

    private final DiscountCodeRepository discountCodeRepository;
    private final FacilityRepository facilityRepository;

    public DiscountCode create(DiscountCodeCreateDTO dto) {
        LOG.debug("Request to create DiscountCode : {}", dto);

        Facility facility = findFacility(dto.facilityId());
        String code = normalizeCode(dto.code());
        validateValues(dto.discountType(), dto.percentage(), dto.fixedAmount(), dto.currency());
        validateUniqueCode(facility.getId(), code, null);

        DiscountCode entity = DiscountCode.builder()
                .facility(facility)
                .code(code)
                .name(dto.name().trim())
                .description(trimToNull(dto.description()))
                .discountType(dto.discountType())
                .percentage(resolvePercentage(dto.discountType(), dto.percentage()))
                .fixedAmount(resolveFixedAmount(dto.discountType(), dto.fixedAmount()))
                .currency(resolveCurrency(dto.discountType(), dto.currency()))
                .isActive(true)
                .build();

        return save(entity);
    }

    public DiscountCode update(DiscountCodeUpdateDTO dto) {
        LOG.debug("Request to update DiscountCode : {}", dto);

        DiscountCode entity = discountCodeRepository.findById(dto.id())
                .orElseThrow(() -> new NotFoundAlertException(
                        "Discount code not found with id " + dto.id(),
                        ENTITY_NAME,
                        "notfound"
                ));

        Facility facility = findFacility(dto.facilityId());
        String code = normalizeCode(dto.code());
        validateValues(dto.discountType(), dto.percentage(), dto.fixedAmount(), dto.currency());
        validateUniqueCode(facility.getId(), code, entity.getId());

        entity.setFacility(facility);
        entity.setCode(code);
        entity.setName(dto.name().trim());
        entity.setDescription(trimToNull(dto.description()));
        entity.setDiscountType(dto.discountType());
        entity.setPercentage(resolvePercentage(dto.discountType(), dto.percentage()));
        entity.setFixedAmount(resolveFixedAmount(dto.discountType(), dto.fixedAmount()));
        entity.setCurrency(resolveCurrency(dto.discountType(), dto.currency()));

        return save(entity);
    }

    @Transactional(readOnly = true)
    public DiscountCode findOne(Long id) {
        return discountCodeRepository.findById(id)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Discount code not found with id " + id,
                        ENTITY_NAME,
                        "notfound"
                ));
    }

    @Transactional(readOnly = true)
    public Page<DiscountCode> findAll(Pageable pageable) {
        return discountCodeRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<DiscountCode> findByFacilityId(Long facilityId, Pageable pageable) {
        return discountCodeRepository.findByFacility_Id(facilityId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<DiscountCode> findByCode(String code, Pageable pageable) {
        return discountCodeRepository.findByCodeContainingIgnoreCase(code.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<DiscountCode> findByName(String name, Pageable pageable) {
        return discountCodeRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<DiscountCode> findByDiscountType(DiscountType discountType, Pageable pageable) {
        return discountCodeRepository.findByDiscountType(discountType, pageable);
    }

    public DiscountCode toggleActive(Long id) {
        DiscountCode entity = findOne(id);
        entity.setIsActive(!Boolean.TRUE.equals(entity.getIsActive()));
        return discountCodeRepository.save(entity);
    }

    private DiscountCode save(DiscountCode entity) {
        try {
            return discountCodeRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException | JpaSystemException exception) {
            throw handleConstraintViolation(exception);
        }
    }

    private Facility findFacility(Long facilityId) {
        return facilityRepository.findById(facilityId)
                .orElseThrow(() -> new NotFoundAlertException(
                        "Facility not found with id " + facilityId,
                        ENTITY_NAME,
                        "facility.notfound"
                ));
    }

    private void validateUniqueCode(Long facilityId, String code, Long excludedId) {
        boolean exists = excludedId == null
                ? discountCodeRepository.existsByFacility_IdAndCodeIgnoreCase(facilityId, code)
                : discountCodeRepository.existsByFacility_IdAndCodeIgnoreCaseAndIdNot(facilityId, code, excludedId);

        if (exists) {
            throw new BadRequestAlertException(
                    "Discount code already exists for this facility",
                    ENTITY_NAME,
                    "code.exists"
            );
        }
    }

    private void validateValues(
            DiscountType discountType,
            BigDecimal percentage,
            BigDecimal fixedAmount,
            Currency currency
    ) {
        if (discountType == DiscountType.PERCENTAGE) {
            if (percentage == null) {
                throw new BadRequestAlertException(
                        "Percentage is required for a percentage discount",
                        ENTITY_NAME,
                        "percentage.required"
                );
            }
            if (percentage.compareTo(BigDecimal.ZERO) < 0 || percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestAlertException(
                        "Discount percentage must be between 0 and 100",
                        ENTITY_NAME,
                        "percentage.invalid"
                );
            }
            if (fixedAmount != null) {
                throw new BadRequestAlertException(
                        "Fixed amount must be empty for a percentage discount",
                        ENTITY_NAME,
                        "fixed.amount.not.allowed"
                );
            }
        } else if (discountType == DiscountType.FIXED_AMOUNT) {
            if (fixedAmount == null) {
                throw new BadRequestAlertException(
                        "Fixed amount is required for a fixed-amount discount",
                        ENTITY_NAME,
                        "fixed.amount.required"
                );
            }
            if (fixedAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestAlertException(
                        "Fixed discount amount cannot be negative",
                        ENTITY_NAME,
                        "fixed.amount.invalid"
                );
            }
            if (currency == null) {
                throw new BadRequestAlertException(
                        "Currency is required for a fixed-amount discount",
                        ENTITY_NAME,
                        "currency.required"
                );
            }
            if (percentage != null) {
                throw new BadRequestAlertException(
                        "Percentage must be empty for a fixed-amount discount",
                        ENTITY_NAME,
                        "percentage.not.allowed"
                );
            }
        } else {
            throw new BadRequestAlertException(
                    "Unsupported discount type",
                    ENTITY_NAME,
                    "type.invalid"
            );
        }
    }

    private BigDecimal resolvePercentage(DiscountType discountType, BigDecimal percentage) {
        return discountType == DiscountType.PERCENTAGE ? percentage : null;
    }

    private BigDecimal resolveFixedAmount(DiscountType discountType, BigDecimal fixedAmount) {
        return discountType == DiscountType.FIXED_AMOUNT ? fixedAmount : null;
    }

    private Currency resolveCurrency(DiscountType discountType, Currency currency) {
        return discountType == DiscountType.FIXED_AMOUNT ? currency : null;
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BadRequestAlertException(
                    "Discount code is required",
                    ENTITY_NAME,
                    "code.required"
            );
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private BadRequestAlertException handleConstraintViolation(RuntimeException exception) {
        Throwable root = getRootCause(exception);
        String message = root != null ? root.getMessage() : exception.getMessage();
        String lowerMessage = message == null ? "" : message.toLowerCase(Locale.ROOT);

        LOG.error("Database constraint violation while saving discount code: {}", message, exception);

        if (lowerMessage.contains("uk_discount_code_facility_code")
                || lowerMessage.contains("duplicate")
                || lowerMessage.contains("unique")) {
            return new BadRequestAlertException(
                    "Discount code already exists for this facility",
                    ENTITY_NAME,
                    "code.exists"
            );
        }

        return new BadRequestAlertException(
                "Database constraint violated while saving discount code",
                ENTITY_NAME,
                "db.constraint"
        );
    }
}
