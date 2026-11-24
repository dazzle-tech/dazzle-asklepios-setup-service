package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.Department;
import com.dazzle.asklepios.domain.Discount;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.DiscountApplicableOn;
import com.dazzle.asklepios.domain.enumeration.DiscountType;
import com.dazzle.asklepios.repository.DepartmentsRepository;
import com.dazzle.asklepios.repository.DiscountRepository;
import com.dazzle.asklepios.repository.DiscountSpecifications;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.service.dto.DiscountDTO;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

@Service
@Transactional
public class DiscountService {

    private static final Logger LOG =
            LoggerFactory.getLogger(DiscountService.class);

    private static final String ENTITY_NAME = "discount";

    private final DiscountRepository discountRepository;
    private final FacilityRepository facilityRepository;
    private final DepartmentsRepository departmentsRepository;
    private final ServiceRepository serviceRepository;
    private final UserDepartmentService userDepartmentService;

    public DiscountService(
            DiscountRepository discountRepository,
            FacilityRepository facilityRepository,
            DepartmentsRepository departmentsRepository,
            ServiceRepository serviceRepository,
            UserDepartmentService userDepartmentService
    ) {
        this.discountRepository = discountRepository;
        this.facilityRepository = facilityRepository;
        this.departmentsRepository = departmentsRepository;
        this.serviceRepository = serviceRepository;
        this.userDepartmentService = userDepartmentService;
    }

    public Discount create(DiscountDTO discountDTO) {
        LOG.info(
                "[CREATE] Request to create Discount payload={}",
                discountDTO
        );

        if (discountDTO.id() != null) {
            throw new BadRequestAlertException(
                    "A new discount cannot already have an id",
                    ENTITY_NAME,
                    "id.exists"
            );
        }

        validateDTO(discountDTO);
        validateFacility(discountDTO.facilityId());

        validateUniqueCode(
                discountDTO.facilityId(),
                discountDTO.code(),
                null
        );

        validateDiscountValues(discountDTO);

        boolean active =
                discountDTO.active() == null
                        || discountDTO.active();

        boolean defaultDiscount =
                Boolean.TRUE.equals(
                        discountDTO.isDefault()
                );

        if (defaultDiscount && !active) {
            throw new BadRequestAlertException(
                    "A default discount must be active",
                    ENTITY_NAME,
                    "default.inactive"
            );
        }

        if (defaultDiscount) {
            clearExistingDefaultDiscount(
                    discountDTO.facilityId(),
                    null
            );
        }

        Discount discount = Discount.builder()
                .facilityId(discountDTO.facilityId())
                .code(normalizeCode(discountDTO.code()))
                .name(discountDTO.name().trim())
                .discountType(discountDTO.discountType())
                .percentage(resolvePercentage(discountDTO))
                .fixedAmount(resolveFixedAmount(discountDTO))
                .currency(resolveCurrency(discountDTO))
                .applicableOn(discountDTO.applicableOn())
                .departmentIds(
                        normalizeDepartmentIds(
                                discountDTO.facilityId(),
                                discountDTO.departmentIds()
                        )
                )
                .validFrom(discountDTO.validFrom())
                .validTo(discountDTO.validTo())
                .maximumDiscountAmount(
                        normalizeOptionalAmount(
                                discountDTO.maximumDiscountAmount(),
                                "Maximum discount amount"
                        )
                )
                .minimumInvoiceAmount(
                        normalizeOptionalAmount(
                                discountDTO.minimumInvoiceAmount(),
                                "Minimum invoice amount"
                        )
                )
                .maxUsagePerPatient(
                        normalizeMaxUsagePerPatient(
                                discountDTO.maxUsagePerPatient()
                        )
                )
                .requiresReason(
                        Boolean.TRUE.equals(
                                discountDTO.requiresReason()
                        )
                )
                .requiresApproval(
                        Boolean.TRUE.equals(
                                discountDTO.requiresApproval()
                        )
                )
                .combinable(
                        Boolean.TRUE.equals(
                                discountDTO.combinable()
                        )
                )
                .isDefault(defaultDiscount)
                .active(active)
                .description(
                        trimToNull(
                                discountDTO.description()
                        )
                )
                .build();

        try {
            Discount created =
                    discountRepository.saveAndFlush(discount);

            LOG.info(
                    "[CREATE] Successfully created Discount id={} facilityId={} code={}",
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
                    "Database constraint violated while saving discount",
                    ENTITY_NAME,
                    "db.constraint"
            );
        }
    }

    public Discount update(
            Long id,
            DiscountDTO discountDTO
    ) {
        LOG.info(
                "[UPDATE] Request to update Discount id={} payload={}",
                id,
                discountDTO
        );

        if (discountDTO.id() == null) {
            throw new BadRequestAlertException(
                    "Discount id is required",
                    ENTITY_NAME,
                    "id.required"
            );
        }

        if (!id.equals(discountDTO.id())) {
            throw new BadRequestAlertException(
                    "Path id and payload id do not match",
                    ENTITY_NAME,
                    "id.mismatch"
            );
        }

        validateDTO(discountDTO);
        validateFacility(discountDTO.facilityId());

        Discount existing = findById(id);

        validateUniqueCode(
                discountDTO.facilityId(),
                discountDTO.code(),
                id
        );

        validateDiscountValues(discountDTO);

        boolean active =
                discountDTO.active() == null
                        ? Boolean.TRUE.equals(
                        existing.getActive()
                )
                        : discountDTO.active();

        boolean defaultDiscount =
                Boolean.TRUE.equals(
                        discountDTO.isDefault()
                );

        if (defaultDiscount && !active) {
            throw new BadRequestAlertException(
                    "A default discount must be active",
                    ENTITY_NAME,
                    "default.inactive"
            );
        }

        if (defaultDiscount) {
            clearExistingDefaultDiscount(
                    discountDTO.facilityId(),
                    id
            );
        }

        existing.setFacilityId(discountDTO.facilityId());
        existing.setCode(normalizeCode(discountDTO.code()));
        existing.setName(discountDTO.name().trim());
        existing.setDiscountType(discountDTO.discountType());
        existing.setPercentage(resolvePercentage(discountDTO));
        existing.setFixedAmount(resolveFixedAmount(discountDTO));
        existing.setCurrency(resolveCurrency(discountDTO));
        existing.setApplicableOn(discountDTO.applicableOn());
        replaceDepartmentIds(
                existing,
                normalizeDepartmentIds(
                        discountDTO.facilityId(),
                        discountDTO.departmentIds()
                )
        );
        existing.setValidFrom(discountDTO.validFrom());
        existing.setValidTo(discountDTO.validTo());

        existing.setMaximumDiscountAmount(
                normalizeOptionalAmount(
                        discountDTO.maximumDiscountAmount(),
                        "Maximum discount amount"
                )
        );

        existing.setMinimumInvoiceAmount(
                normalizeOptionalAmount(
                        discountDTO.minimumInvoiceAmount(),
                        "Minimum invoice amount"
                )
        );

        existing.setMaxUsagePerPatient(
                normalizeMaxUsagePerPatient(
                        discountDTO.maxUsagePerPatient()
                )
        );

        existing.setRequiresReason(
                Boolean.TRUE.equals(
                        discountDTO.requiresReason()
                )
        );

        existing.setRequiresApproval(
                Boolean.TRUE.equals(
                        discountDTO.requiresApproval()
                )
        );

        existing.setCombinable(
                Boolean.TRUE.equals(
                        discountDTO.combinable()
                )
        );

        existing.setIsDefault(defaultDiscount);
        existing.setActive(active);
        existing.setDescription(
                trimToNull(
                        discountDTO.description()
                )
        );

        try {
            Discount updated =
                    discountRepository.saveAndFlush(existing);

            LOG.info(
                    "[UPDATE] Successfully updated Discount id={} facilityId={} code={}",
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
                    "Database constraint violated while updating discount",
                    ENTITY_NAME,
                    "db.constraint"
            );
        }
    }

    public Discount toggleActive(Long id) {
        LOG.info(
                "[TOGGLE ACTIVE] Request to toggle Discount id={}",
                id
        );

        Discount discount = findById(id);

        boolean newActive =
                !Boolean.TRUE.equals(
                        discount.getActive()
                );

        discount.setActive(newActive);

        if (!newActive) {
            discount.setIsDefault(false);
        }

        return discountRepository.saveAndFlush(discount);
    }

    public Discount changeActivationStatus(
            Long id,
            boolean active
    ) {
        LOG.info(
                "[CHANGE ACTIVATION] Discount id={} active={}",
                id,
                active
        );

        Discount discount = findById(id);

        discount.setActive(active);

        if (!active) {
            discount.setIsDefault(false);
        }

        return discountRepository.saveAndFlush(discount);
    }

    public Discount setDefault(Long id) {
        LOG.info(
                "[SET DEFAULT] Request to set default Discount id={}",
                id
        );

        Discount discount = findById(id);

        if (!Boolean.TRUE.equals(discount.getActive())) {
            throw new BadRequestAlertException(
                    "An inactive discount cannot be set as default",
                    ENTITY_NAME,
                    "default.inactive"
            );
        }

        if (!isCurrentlyValid(discount, LocalDate.now())) {
            throw new BadRequestAlertException(
                    "A future or expired discount cannot be set as default",
                    ENTITY_NAME,
                    "default.invalid.period"
            );
        }

        clearExistingDefaultDiscount(
                discount.getFacilityId(),
                discount.getId()
        );

        discount.setIsDefault(true);

        return discountRepository.saveAndFlush(discount);
    }

    public Discount updateApplicableServices(
            Long id,
            List<Long> serviceIds
    ) {
        LOG.info(
                "[SERVICES] Discount id={} serviceIds={}",
                id,
                serviceIds
        );

        Discount discount = findById(id);

        replaceServiceIds(
                discount,
                normalizeServiceIds(
                        discount.getFacilityId(),
                        serviceIds
                )
        );

        return discountRepository.saveAndFlush(discount);
    }

    public Discount updateApprovalUsers(
            Long id,
            List<Long> approvalUserIds
    ) {
        LOG.info(
                "[APPROVAL USERS] Discount id={} approvalUserIds={}",
                id,
                approvalUserIds
        );

        Discount discount = findById(id);

        replaceApprovalUserIds(
                discount,
                normalizeApprovalUserIds(
                        discount.getFacilityId(),
                        approvalUserIds
                )
        );

        return discountRepository.saveAndFlush(discount);
    }

    @Transactional(readOnly = true)
    public Discount findById(Long id) {
        return discountRepository
                .findById(id)
                .orElseThrow(
                        () -> new NotFoundAlertException(
                                "Discount not found with id " + id,
                                ENTITY_NAME,
                                "notfound"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Discount findByFacilityIdAndCode(
            Long facilityId,
            String code
    ) {
        validateFacility(facilityId);

        if (code == null || code.isBlank()) {
            throw new BadRequestAlertException(
                    "Discount code is required",
                    ENTITY_NAME,
                    "code.required"
            );
        }

        return discountRepository
                .findByFacilityIdAndCodeIgnoreCase(
                        facilityId,
                        code.trim()
                )
                .orElseThrow(
                        () -> new NotFoundAlertException(
                                "Discount was not found for facility "
                                        + facilityId
                                        + " and code "
                                        + code,
                                ENTITY_NAME,
                                "notfound"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Discount findDefaultDiscount(
            Long facilityId
    ) {
        validateFacility(facilityId);

        return discountRepository
                .findFirstByFacilityIdAndIsDefaultTrueAndActiveTrue(
                        facilityId
                )
                .orElseThrow(
                        () -> new NotFoundAlertException(
                                "No active default discount exists for facility "
                                        + facilityId,
                                ENTITY_NAME,
                                "default.notfound"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Page<Discount> findAllByFacilityId(
            Long facilityId,
            Pageable pageable
    ) {
        validateFacility(facilityId);

        return discountRepository.findAllByFacilityId(
                facilityId,
                pageable
        );
    }

    /** Discounts of a facility with optional filters combined with AND; nulls are ignored. */
    @Transactional(readOnly = true)
    public Page<Discount> findAllByFacilityId(
            Long facilityId,
            String code,
            String name,
            DiscountType discountType,
            BigDecimal value,
            DiscountApplicableOn applicableOn,
            Boolean requiresApproval,
            Boolean isDefault,
            Boolean active,
            Pageable pageable
    ) {
        validateFacility(facilityId);

        return discountRepository.findAll(
                DiscountSpecifications.search(
                        facilityId,
                        code,
                        name,
                        discountType,
                        value,
                        applicableOn,
                        requiresApproval,
                        isDefault,
                        active
                ),
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Page<Discount> findAllActiveByFacilityId(
            Long facilityId,
            Pageable pageable
    ) {
        validateFacility(facilityId);

        return discountRepository
                .findAllByFacilityIdAndActiveTrue(
                        facilityId,
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public Page<Discount> findByCode(
            Long facilityId,
            String code,
            Pageable pageable
    ) {
        validateFacility(facilityId);

        return discountRepository
                .findAllByFacilityIdAndCodeContainingIgnoreCase(
                        facilityId,
                        code == null ? "" : code.trim(),
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public Page<Discount> findByName(
            Long facilityId,
            String name,
            Pageable pageable
    ) {
        validateFacility(facilityId);

        return discountRepository
                .findAllByFacilityIdAndNameContainingIgnoreCase(
                        facilityId,
                        name == null ? "" : name.trim(),
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public Page<Discount> findByDiscountType(
            Long facilityId,
            DiscountType discountType,
            Pageable pageable
    ) {
        validateFacility(facilityId);

        return discountRepository
                .findAllByFacilityIdAndDiscountType(
                        facilityId,
                        discountType,
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public Page<Discount> findByApplicableOn(
            Long facilityId,
            DiscountApplicableOn applicableOn,
            Pageable pageable
    ) {
        validateFacility(facilityId);

        return discountRepository
                .findAllByFacilityIdAndApplicableOn(
                        facilityId,
                        applicableOn,
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public Page<Discount> findByValue(
            Long facilityId,
            BigDecimal value,
            Pageable pageable
    ) {
        validateFacility(facilityId);

        return discountRepository
                .findAllByFacilityIdAndPercentageOrFacilityIdAndFixedAmount(
                        facilityId,
                        value,
                        facilityId,
                        value,
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public Page<Discount> findByRequiresApproval(
            Long facilityId,
            Boolean requiresApproval,
            Pageable pageable
    ) {
        validateFacility(facilityId);

        return discountRepository
                .findAllByFacilityIdAndRequiresApproval(
                        facilityId,
                        requiresApproval,
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public Page<Discount> findByIsDefault(
            Long facilityId,
            Boolean isDefault,
            Pageable pageable
    ) {
        validateFacility(facilityId);

        return discountRepository
                .findAllByFacilityIdAndIsDefault(
                        facilityId,
                        isDefault,
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public Page<Discount> findByActive(
            Long facilityId,
            Boolean active,
            Pageable pageable
    ) {
        validateFacility(facilityId);

        return discountRepository
                .findAllByFacilityIdAndActive(
                        facilityId,
                        active,
                        pageable
                );
    }

    @Transactional(readOnly = true)
    public List<Discount> findEffectiveDiscounts(
            Long facilityId,
            LocalDate date
    ) {
        validateFacility(facilityId);

        LocalDate effectiveDate =
                date == null
                        ? LocalDate.now()
                        : date;

        List<Discount> discounts = new ArrayList<>();

        discounts.addAll(
                discountRepository
                        .findAllByFacilityIdAndActiveTrueAndValidFromLessThanEqualAndValidToIsNull(
                                facilityId,
                                effectiveDate
                        )
        );

        discounts.addAll(
                discountRepository
                        .findAllByFacilityIdAndActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                                facilityId,
                                effectiveDate,
                                effectiveDate
                        )
        );

        return discounts;
    }

    public boolean delete(Long id) {
        LOG.info(
                "[DELETE] Request to delete Discount id={}",
                id
        );

        Discount discount =
                discountRepository
                        .findById(id)
                        .orElse(null);

        if (discount == null) {
            return false;
        }

        if (Boolean.TRUE.equals(discount.getActive())) {
            throw new BadRequestAlertException(
                    "An active discount cannot be deleted. Deactivate it first",
                    ENTITY_NAME,
                    "active.delete"
            );
        }

        try {
            discountRepository.delete(discount);
            discountRepository.flush();

            return true;

        } catch (
                DataIntegrityViolationException
                | JpaSystemException exception
        ) {
            throw new BadRequestAlertException(
                    "The discount is already used and cannot be deleted",
                    ENTITY_NAME,
                    "discount.in.use"
            );
        }
    }

    private void validateDTO(DiscountDTO dto) {
        if (dto.facilityId() == null) {
            throw new BadRequestAlertException(
                    "Facility id is required",
                    ENTITY_NAME,
                    "facility.required"
            );
        }

        if (dto.code() == null || dto.code().isBlank()) {
            throw new BadRequestAlertException(
                    "Discount code is required",
                    ENTITY_NAME,
                    "code.required"
            );
        }

        if (dto.name() == null || dto.name().isBlank()) {
            throw new BadRequestAlertException(
                    "Discount name is required",
                    ENTITY_NAME,
                    "name.required"
            );
        }

        if (dto.discountType() == null) {
            throw new BadRequestAlertException(
                    "Discount type is required",
                    ENTITY_NAME,
                    "type.required"
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

    private void validateDiscountValues(
            DiscountDTO dto
    ) {
        if (dto.discountType() == DiscountType.PERCENTAGE) {
            validatePercentage(dto.percentage());

            if (dto.fixedAmount() != null) {
                throw new BadRequestAlertException(
                        "Fixed amount must be empty for a percentage discount",
                        ENTITY_NAME,
                        "fixed.amount.not.allowed"
                );
            }

            return;
        }

        if (dto.discountType() == DiscountType.FIXED_AMOUNT) {
            validateFixedAmount(
                    dto.fixedAmount(),
                    dto.currency()
            );

            if (dto.percentage() != null) {
                throw new BadRequestAlertException(
                        "Percentage must be empty for a fixed-amount discount",
                        ENTITY_NAME,
                        "percentage.not.allowed"
                );
            }

            return;
        }

        throw new BadRequestAlertException(
                "Unsupported discount type",
                ENTITY_NAME,
                "type.invalid"
        );
    }

    private void validatePercentage(
            BigDecimal percentage
    ) {
        if (percentage == null) {
            throw new BadRequestAlertException(
                    "Percentage is required for a percentage discount",
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
                    "Discount percentage must be between 0 and 100",
                    ENTITY_NAME,
                    "percentage.invalid"
            );
        }
    }

    private void validateFixedAmount(
            BigDecimal fixedAmount,
            Object currency
    ) {
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
    }

    private BigDecimal normalizeOptionalAmount(
            BigDecimal amount,
            String fieldName
    ) {
        if (amount == null) {
            return null;
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestAlertException(
                    fieldName + " cannot be negative",
                    ENTITY_NAME,
                    "amount.invalid"
            );
        }

        return amount;
    }

    private Set<Long> normalizeDepartmentIds(
            Long facilityId,
            List<Long> departmentIds
    ) {
        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>();

        if (departmentIds != null) {
            for (Long departmentId : departmentIds) {
                if (departmentId != null) {
                    uniqueIds.add(departmentId);
                }
            }
        }

        if (uniqueIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        List<Department> departments =
                departmentsRepository.findByIdIn(
                        new ArrayList<>(uniqueIds)
                );

        if (departments.size() != uniqueIds.size()) {
            throw new BadRequestAlertException(
                    "One or more selected departments were not found",
                    ENTITY_NAME,
                    "department.notfound"
            );
        }

        for (Department department : departments) {
            Long departmentFacilityId =
                    department.getFacility() == null
                            ? null
                            : department.getFacility().getId();

            if (!facilityId.equals(departmentFacilityId)) {
                throw new BadRequestAlertException(
                        "Selected departments must belong to the discount facility",
                        ENTITY_NAME,
                        "department.facility.invalid"
                );
            }
        }

        return uniqueIds;
    }

    private Set<Long> normalizeServiceIds(
            Long facilityId,
            List<Long> serviceIds
    ) {
        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>();

        if (serviceIds != null) {
            for (Long serviceId : serviceIds) {
                if (serviceId != null) {
                    uniqueIds.add(serviceId);
                }
            }
        }

        if (uniqueIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        List<ServiceSetup> services =
                serviceRepository.findAllById(uniqueIds);

        if (services.size() != uniqueIds.size()) {
            throw new BadRequestAlertException(
                    "One or more selected services were not found",
                    ENTITY_NAME,
                    "service.notfound"
            );
        }

        for (ServiceSetup service : services) {
            Long serviceFacilityId =
                    service.getFacility() == null
                            ? null
                            : service.getFacility().getId();

            if (!facilityId.equals(serviceFacilityId)) {
                throw new BadRequestAlertException(
                        "Selected services must belong to the discount facility",
                        ENTITY_NAME,
                        "service.facility.invalid"
                );
            }
        }

        return uniqueIds;
    }

    private Set<Long> normalizeApprovalUserIds(
            Long facilityId,
            List<Long> approvalUserIds
    ) {
        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>();

        if (approvalUserIds != null) {
            for (Long approvalUserId : approvalUserIds) {
                if (approvalUserId != null) {
                    uniqueIds.add(approvalUserId);
                }
            }
        }

        if (uniqueIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        Set<Long> facilityUserIds =
                userDepartmentService
                        .getActiveUsersByFacility(facilityId)
                        .stream()
                        .map(user -> user.getId())
                        .collect(Collectors.toSet());

        for (Long approvalUserId : uniqueIds) {
            if (!facilityUserIds.contains(approvalUserId)) {
                throw new BadRequestAlertException(
                        "Approval users must be active users of the discount facility",
                        ENTITY_NAME,
                        "approval.user.invalid"
                );
            }
        }

        return uniqueIds;
    }

    private void replaceApprovalUserIds(
            Discount discount,
            Set<Long> approvalUserIds
    ) {
        if (discount.getApprovalUserIds() == null) {
            discount.setApprovalUserIds(new LinkedHashSet<>());
        }

        discount.getApprovalUserIds().clear();
        discount.getApprovalUserIds().addAll(approvalUserIds);
    }

    private void replaceServiceIds(
            Discount discount,
            Set<Long> serviceIds
    ) {
        if (discount.getServiceIds() == null) {
            discount.setServiceIds(new LinkedHashSet<>());
        }

        discount.getServiceIds().clear();
        discount.getServiceIds().addAll(serviceIds);
    }

    private void replaceDepartmentIds(
            Discount discount,
            Set<Long> departmentIds
    ) {
        if (discount.getDepartmentIds() == null) {
            discount.setDepartmentIds(new LinkedHashSet<>());
        }

        discount.getDepartmentIds().clear();
        discount.getDepartmentIds().addAll(departmentIds);
    }

    private Integer normalizeMaxUsagePerPatient(Integer times) {
        if (times == null) {
            throw new BadRequestAlertException(
                    "How many times a patient can use this discount is required",
                    ENTITY_NAME,
                    "max.usage.per.patient.required"
            );
        }

        if (times < 1) {
            throw new BadRequestAlertException(
                    "How many times a patient can use this discount must be at least 1",
                    ENTITY_NAME,
                    "max.usage.per.patient.invalid"
            );
        }

        return times;
    }

    private BigDecimal resolvePercentage(
            DiscountDTO dto
    ) {
        return dto.discountType() == DiscountType.PERCENTAGE
                ? dto.percentage()
                : null;
    }

    private BigDecimal resolveFixedAmount(
            DiscountDTO dto
    ) {
        return dto.discountType() == DiscountType.FIXED_AMOUNT
                ? dto.fixedAmount()
                : null;
    }

    private com.dazzle.asklepios.domain.enumeration.Currency
    resolveCurrency(DiscountDTO dto) {
        return dto.discountType() == DiscountType.FIXED_AMOUNT
                ? dto.currency()
                : null;
    }

    private void validateFacility(Long facilityId) {
        if (
                facilityId == null
                        || !facilityRepository.existsById(facilityId)
        ) {
            throw new NotFoundAlertException(
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
        boolean exists =
                excludedId == null
                        ? discountRepository
                        .existsByFacilityIdAndCodeIgnoreCase(
                                facilityId,
                                code.trim()
                        )
                        : discountRepository
                        .existsByFacilityIdAndCodeIgnoreCaseAndIdNot(
                                facilityId,
                                code.trim(),
                                excludedId
                        );

        if (exists) {
            throw new BadRequestAlertException(
                    "Discount code already exists for this facility",
                    ENTITY_NAME,
                    "code.exists"
            );
        }
    }

    private void clearExistingDefaultDiscount(
            Long facilityId,
            Long excludedId
    ) {
        List<Discount> currentDefaults =
                discountRepository
                        .findAllByFacilityIdAndIsDefaultTrue(
                                facilityId
                        );

        List<Discount> discountsToUpdate =
                new ArrayList<>();

        for (Discount currentDefault : currentDefaults) {
            if (
                    excludedId != null
                            && excludedId.equals(
                            currentDefault.getId()
                    )
            ) {
                continue;
            }

            currentDefault.setIsDefault(false);
            discountsToUpdate.add(currentDefault);
        }

        if (!discountsToUpdate.isEmpty()) {
            discountRepository.saveAll(discountsToUpdate);
            discountRepository.flush();
        }
    }

    private boolean isCurrentlyValid(
            Discount discount,
            LocalDate date
    ) {
        boolean started =
                discount.getValidFrom() != null
                        && !discount.getValidFrom().isAfter(date);

        boolean notExpired =
                discount.getValidTo() == null
                        || !discount.getValidTo().isBefore(date);

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

        String message =
                rootCause != null
                        ? rootCause.getMessage()
                        : exception.getMessage();

        LOG.error(
                "Discount database root cause: {}",
                message,
                exception
        );

        String lowerMessage =
                message == null
                        ? ""
                        : message.toLowerCase();

        if (
                lowerMessage.contains(
                        "uk_discount_facility_code"
                )
                        || lowerMessage.contains("duplicate")
                        || lowerMessage.contains("unique")
        ) {
            throw new BadRequestAlertException(
                    "Discount code already exists for this facility",
                    ENTITY_NAME,
                    "code.exists"
            );
        }

        if (
                lowerMessage.contains(
                        "fk_discount_facility"
                )
                        || lowerMessage.contains("facility_id")
        ) {
            throw new BadRequestAlertException(
                    "Invalid facility reference",
                    ENTITY_NAME,
                    "facility.invalid"
            );
        }

        if (
                lowerMessage.contains(
                        "fk_discount_department_department"
                )
                        || lowerMessage.contains("department_id")
        ) {
            throw new BadRequestAlertException(
                    "One or more selected departments are invalid",
                    ENTITY_NAME,
                    "department.invalid"
            );
        }

        throw new BadRequestAlertException(
                "Database constraint violated while saving discount",
                ENTITY_NAME,
                "db.constraint"
        );
    }


    @Transactional(readOnly = true)
    public Discount resolveApplicableDiscount(
            Long facilityId,
            DiscountApplicableOn applicableOn,
            LocalDate pricingDate
    ) {
        LocalDate effectiveDate =
                pricingDate == null
                        ? LocalDate.now()
                        : pricingDate;

        List<Discount> effectiveDiscounts =
                new ArrayList<>();

        effectiveDiscounts.addAll(
                discountRepository
                        .findAllByFacilityIdAndActiveTrueAndValidFromLessThanEqualAndValidToIsNull(
                                facilityId,
                                effectiveDate
                        )
        );

        effectiveDiscounts.addAll(
                discountRepository
                        .findAllByFacilityIdAndActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                                facilityId,
                                effectiveDate,
                                effectiveDate
                        )
        );

        return effectiveDiscounts.stream()
                .filter(discount ->
                        applicableOn == null
                                || discount.getApplicableOn()
                                == applicableOn
                )
                .sorted(
                        Comparator
                                .comparing(
                                        Discount::getIsDefault,
                                        Comparator.nullsLast(
                                                Comparator.reverseOrder()
                                        )
                                )
                                .thenComparing(
                                        Discount::getId,
                                        Comparator.reverseOrder()
                                )
                )
                .findFirst()
                .orElse(null);
    }
}