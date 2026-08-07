package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.PriceListSetupItem;
import com.dazzle.asklepios.domain.enumeration.DiscountType;
import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupStatus;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupType;
import com.dazzle.asklepios.domain.enumeration.biling.BillingCoverageType;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.repository.PriceListSetupItemRepository;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.service.dto.BillingPricingResolutionDTO;
import com.dazzle.asklepios.service.dto.BillingPricingResolutionRequest;
import com.dazzle.asklepios.service.dto.PriceListSetupDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PriceListSetupService {

    private static final String ENTITY_NAME = "priceListSetup";

    private final PriceListSetupRepository priceListSetupRepository;

    private final PriceListSetupItemRepository
            priceListSetupItemRepository;

    public PriceListSetupDTO create(PriceListSetupDTO dto) {
        validateDto(dto);
        validateNoOverlappingInterval(null, dto);

        PriceListSetup entity = new PriceListSetup();

        entity.setFacilityId(dto.facilityId());
        entity.setType(dto.type());
        entity.setPayerId(dto.payerId());
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setVersionNumber(dto.versionNumber());
        entity.setEffectiveFrom(dto.effectiveFrom());
        entity.setEffectiveTo(dto.effectiveTo());
        entity.setCurrency(dto.currency());

        entity.setStatus(
                dto.status() != null
                        ? dto.status()
                        : PriceListSetupStatus.ACTIVE
        );
        entity.setIsActive(true);

        PriceListSetup savedEntity =
                priceListSetupRepository.save(entity);

        return toDTO(savedEntity);
    }

    public PriceListSetupDTO activate(Long id) {
        PriceListSetup entity =
                priceListSetupRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Price list setup not found with id: "
                                                + id
                                )
                        );

        entity.setStatus(PriceListSetupStatus.ACTIVE);
        entity.setIsActive(true);

        return toDTO(
                priceListSetupRepository.save(entity)
        );
    }

    public PriceListSetupDTO update(
            Long id,
            PriceListSetupDTO dto
    ) {
        validateDto(dto);
        validateNoOverlappingInterval(id, dto);

        PriceListSetup entity =
                priceListSetupRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Price list setup not found with id: "
                                                + id
                                )
                        );

        entity.setFacilityId(dto.facilityId());
        entity.setType(dto.type());
        entity.setPayerId(dto.payerId());
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setVersionNumber(dto.versionNumber());
        entity.setEffectiveFrom(dto.effectiveFrom());
        entity.setEffectiveTo(dto.effectiveTo());
        entity.setCurrency(dto.currency());

        if (dto.status() != null) {
            entity.setStatus(dto.status());
        }

        PriceListSetup savedEntity =
                priceListSetupRepository.save(entity);

        return toDTO(savedEntity);
    }

    @Transactional(readOnly = true)
    public PriceListSetupDTO findById(Long id) {

        PriceListSetup entity =
                priceListSetupRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Price list setup not found with id: "
                                                + id
                                )
                        );

        return toDTO(entity);
    }

    @Transactional(readOnly = true)
    public Page<PriceListSetupDTO> findAll(
            Pageable pageable
    ) {

        return priceListSetupRepository
                .findAll(pageable)
                .map(this::toDTO);
    }

    public void delete(Long id) {

        if (!priceListSetupRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "Price list setup not found with id: " + id
            );
        }

        priceListSetupRepository.deleteById(id);
    }

    /**
     * Runtime price resolution used by the patient/billing service.
     *
     * This method does not update the price list. It only resolves the
     * applicable active price-list header and item.
     */
    @Transactional(readOnly = true)
    public BillingPricingResolutionDTO resolve(
            BillingPricingResolutionRequest request
    ) {
        Objects.requireNonNull(
                request,
                "Billing pricing resolution request is required"
        );

        LocalDate pricingDate =
                request.pricingDate() != null
                        ? request.pricingDate()
                        : LocalDate.now();

        BillingCoverageType coverageType =
                resolveCoverageType(request);

        PriceListItemType itemType =
                mapItemType(
                        request.billingItemType()
                );

        List<PriceListSetup> candidatePriceLists =
                priceListSetupRepository
                        .findAllByFacilityIdAndCurrencyAndStatusAndIsActiveTrue(
                                request.facilityId(),
                                request.currency(),
                                PriceListSetupStatus.ACTIVE
                        )
                        .stream()
                        .filter(priceList ->
                                matchesCoverageType(
                                        priceList,
                                        coverageType,
                                        request.payerId()
                                )
                        )
                        .filter(priceList ->
                                isEffective(
                                        priceList,
                                        pricingDate
                                )
                        )
                        .sorted(
                                Comparator
                                        .comparing(
                                                (PriceListSetup priceList) ->
                                                        payerPriority(
                                                                priceList,
                                                                request.payerId()
                                                        )
                                        )
                                        .thenComparing(
                                                PriceListSetup::getVersionNumber,
                                                Comparator.nullsLast(
                                                        Comparator.reverseOrder()
                                                )
                                        )
                                        .thenComparing(
                                                PriceListSetup::getId,
                                                Comparator.reverseOrder()
                                        )
                        )
                        .toList();

        if (candidatePriceLists.isEmpty()) {
            return null;
        }
        for (PriceListSetup priceList : candidatePriceLists) {

            Optional<PriceListSetupItem> itemOptional =
                    priceListSetupItemRepository
                            .findFirstByPriceListSetupIdAndItemTypeAndSourceIdAndIsActiveTrue(
                                    priceList.getId(),
                                    itemType,
                                    request.itemId()
                            );

            if (itemOptional.isPresent()) {
                return toResolutionDTO(
                        priceList,
                        itemOptional.get()
                );
            }
        }

        return null;
    }

    private boolean isEffective(
            PriceListSetup priceList,
            LocalDate pricingDate
    ) {
        boolean effectiveFromValid =
                priceList.getEffectiveFrom() == null
                        || !pricingDate.isBefore(
                        priceList.getEffectiveFrom()
                );

        boolean effectiveToValid =
                priceList.getEffectiveTo() == null
                        || !pricingDate.isAfter(
                        priceList.getEffectiveTo()
                );

        return effectiveFromValid && effectiveToValid;
    }

    /**
     * Lower value means higher priority.
     *
     * Priority:
     * 0 = exact payer-specific price list
     * 1 = general price list where payer_id is null
     * 2 = another payer's price list, which should normally not be selected
     */
    private int payerPriority(
            PriceListSetup priceList,
            Long requestedPayerId
    ) {
        if (requestedPayerId != null
                && requestedPayerId.equals(
                priceList.getPayerId()
        )) {
            return 0;
        }

        if (priceList.getPayerId() == null) {
            return 1;
        }

        return 2;
    }

    private BillingPricingResolutionDTO toResolutionDTO(
            PriceListSetup priceList,
            PriceListSetupItem item
    ) {
        return new BillingPricingResolutionDTO(
                priceList.getId(),
                item.getId(),
                priceList.getName(),
                item.getItemCode(),
                item.getItemName(),
                item.getUnitPrice(),
                priceList.getCurrency(),

                null,
                hasItemDiscount(item)
                        ? DiscountType.PERCENTAGE.name()
                        : null,
                hasItemDiscount(item)
                        ? item.getDiscountPercentage()
                        : null,
                null,

                null,
                null,
                null,

                "DISCOUNT_THEN_TAX",
                "HALF_UP",
                4
        );
    }

    private BillingCoverageType resolveCoverageType(
            BillingPricingResolutionRequest request
    ) {
        if (request.coverageType() != null) {
            return request.coverageType();
        }

        if (request.patientInsuranceId() != null
                || request.payerId() != null) {
            return BillingCoverageType.INSURANCE;
        }

        return BillingCoverageType.SELF_PAY;
    }

    private boolean matchesCoverageType(
            PriceListSetup priceList,
            BillingCoverageType coverageType,
            Long requestedPayerId
    ) {
        if (coverageType == BillingCoverageType.SELF_PAY) {
            return priceList.getPayerId() == null
                    && isSelfPayPriceListType(
                            priceList.getType()
                    );
        }

        if (coverageType == BillingCoverageType.INSURANCE) {
            if (priceList.getType()
                    != PriceListSetupType.INSURANCE) {
                return false;
            }

            if (requestedPayerId == null) {
                return true;
            }

            return requestedPayerId.equals(
                    priceList.getPayerId()
            );
        }

        return true;
    }

    private boolean isSelfPayPriceListType(
            PriceListSetupType type
    ) {
        return type == PriceListSetupType.SELF_PAY;
    }

    private void validateDto(PriceListSetupDTO dto) {
        if (dto.effectiveTo() != null
                && dto.effectiveTo().isBefore(dto.effectiveFrom())) {
            throw new BadRequestAlertException(
                    "Effective To must be on or after Effective From.",
                    ENTITY_NAME,
                    "effectiveDate.invalidRange"
            );
        }

        if (dto.type() == PriceListSetupType.INSURANCE
                && dto.payerId() == null) {
            throw new BadRequestAlertException(
                    "Payer is required for insurance price lists.",
                    ENTITY_NAME,
                    "payer.required"
            );
        }
    }

    private void validateNoOverlappingInterval(
            Long excludeId,
            PriceListSetupDTO dto
    ) {
        List<PriceListSetup> candidates =
                dto.type() == PriceListSetupType.INSURANCE
                        ? priceListSetupRepository
                        .findAllByFacilityIdAndPayerIdAndIsActiveTrue(
                                dto.facilityId(),
                                dto.payerId()
                        )
                        : priceListSetupRepository
                        .findAllByFacilityIdAndTypeAndIsActiveTrue(
                                dto.facilityId(),
                                dto.type()
                        );

        boolean hasOverlap = candidates.stream()
                .filter(candidate ->
                        excludeId == null
                                || !excludeId.equals(candidate.getId())
                )
                .anyMatch(candidate ->
                        intervalsOverlap(
                                dto.effectiveFrom(),
                                dto.effectiveTo(),
                                candidate.getEffectiveFrom(),
                                candidate.getEffectiveTo()
                        )
                );

        if (hasOverlap) {
            if (dto.type() == PriceListSetupType.INSURANCE) {
                throw new BadRequestAlertException(
                        "An active insurance price list already exists for this payer in the selected date range.",
                        ENTITY_NAME,
                        "interval.payer.duplicate"
                );
            }

            throw new BadRequestAlertException(
                    "An active price list already exists for this type in the selected date range.",
                    ENTITY_NAME,
                    "interval.type.duplicate"
            );
        }
    }

    private boolean intervalsOverlap(
            LocalDate from1,
            LocalDate to1,
            LocalDate from2,
            LocalDate to2
    ) {
        LocalDate end1 =
                to1 != null ? to1 : LocalDate.MAX;
        LocalDate end2 =
                to2 != null ? to2 : LocalDate.MAX;

        return !from1.isAfter(end2)
                && !from2.isAfter(end1);
    }

    private PriceListItemType mapItemType(
            BillingItemTypes billingItemType
    ) {
        return PriceListItemType.valueOf(
                billingItemType.name()
        );
    }

    private boolean hasItemDiscount(
            PriceListSetupItem item
    ) {
        return item.getDiscountPercentage() != null
                && item.getDiscountPercentage()
                .compareTo(BigDecimal.ZERO) > 0;
    }

    private PriceListSetupDTO toDTO(
            PriceListSetup entity
    ) {
        return new PriceListSetupDTO(
                entity.getId(),
                entity.getFacilityId(),
                entity.getType(),
                entity.getPayerId(),
                entity.getName(),
                entity.getDescription(),
                entity.getVersionNumber(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                entity.getCurrency(),
                entity.getStatus()
        );
    }
}