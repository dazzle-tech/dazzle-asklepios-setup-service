package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.Payor;
import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.PriceListSetupItem;
import com.dazzle.asklepios.domain.Tax;
import com.dazzle.asklepios.domain.enumeration.DiscountType;
import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupStatus;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupType;
import com.dazzle.asklepios.domain.enumeration.biling.BillingCoverageType;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.repository.FacilityRepository;
import com.dazzle.asklepios.repository.NphiesPayerRepository;
import com.dazzle.asklepios.repository.PayorRepository;
import com.dazzle.asklepios.repository.PriceListSetupItemRepository;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.repository.TaxRepository;
import com.dazzle.asklepios.security.SecurityUtils;
import com.dazzle.asklepios.service.dto.BillingPricingResolutionDTO;
import com.dazzle.asklepios.service.dto.BillingPricingResolutionRequest;
import com.dazzle.asklepios.service.dto.PriceListSetupCloneRequest;
import com.dazzle.asklepios.service.dto.PriceListSetupDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class PriceListSetupService {

    private static final Logger LOG =
            LoggerFactory.getLogger(PriceListSetupService.class);

    private static final String ENTITY_NAME = "priceListSetup";

    private static final Set<PriceListSetupType> CASH_TYPES = Set.of(
            PriceListSetupType.CASH,
            PriceListSetupType.SELF_PAY
    );

    private final PriceListSetupRepository priceListSetupRepository;

    private final PriceListSetupItemRepository priceListSetupItemRepository;

    private final FacilityRepository facilityRepository;

    private final PayorRepository payorRepository;

    private final NphiesPayerRepository nphiesPayerRepository;

    private final TaxRepository taxRepository;

    public PriceListSetupDTO create(PriceListSetupDTO dto) {
        validateDto(dto, true);
        validateNoOverlappingInterval(null, dto);
        validateSingleActiveList(null, dto);

        PriceListSetup entity = new PriceListSetup();
        applyCreateFields(entity, dto);
        entity.setIsActive(true);

        PriceListSetup savedEntity = priceListSetupRepository.save(entity);
        return toDTO(savedEntity);
    }

    public PriceListSetupDTO clonePriceList(
            Long sourceId,
            PriceListSetupCloneRequest request
    ) {
        PriceListSetup source = priceListSetupRepository.findById(sourceId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Price list setup not found with id: " + sourceId
                        )
                );

        boolean cloneItems = request == null || request.cloneItems() == null
                || Boolean.TRUE.equals(request.cloneItems());

        PriceListSetup clone = new PriceListSetup();
        clone.setFacilityId(
                request != null && request.facilityId() != null
                        ? request.facilityId()
                        : source.getFacilityId()
        );
        clone.setAppliesToAllFacilities(
                request != null && request.appliesToAllFacilities() != null
                        ? request.appliesToAllFacilities()
                        : Boolean.TRUE.equals(source.getAppliesToAllFacilities())
        );
        clone.setType(source.getType());
        clone.setPayerId(source.getPayerId());
        clone.setNphiesPayerId(source.getNphiesPayerId());
        clone.setName(
                request != null && request.name() != null && !request.name().isBlank()
                        ? request.name().trim()
                        : source.getName() + " (Copy)"
        );
        clone.setShortName(
                request != null && request.shortName() != null
                        ? request.shortName()
                        : source.getShortName()
        );
        clone.setDescription(
                request != null && request.description() != null
                        ? request.description()
                        : source.getDescription()
        );
        clone.setTaxId(
                request != null && request.taxId() != null
                        ? request.taxId()
                        : source.getTaxId()
        );
        clone.setVersionNumber(nextVersionNumber(source));
        clone.setEffectiveFrom(null);
        clone.setEffectiveTo(null);
        clone.setCurrency(source.getCurrency());
        clone.setStatus(PriceListSetupStatus.INACTIVE);
        clone.setIsActive(true);

        PriceListSetup saved = priceListSetupRepository.save(clone);

        if (cloneItems) {
            cloneItems(sourceId, saved.getId());
        }

        return toDTO(saved);
    }

    public PriceListSetupDTO activate(Long id) {
        PriceListSetup entity = requireEntity(id);
        PriceListSetupDTO dto = toDTO(entity);

        if (entity.getEffectiveFrom() == null) {
            throw new BadRequestAlertException(
                    "Effective start date is required before activating a price list.",
                    ENTITY_NAME,
                    "effectiveDate.required"
            );
        }

        validateNoOverlappingInterval(id, dto);
        validateSingleActiveList(id, dto);

        entity.setStatus(PriceListSetupStatus.ACTIVE);
        entity.setIsActive(true);

        return toDTO(priceListSetupRepository.save(entity));
    }

    public PriceListSetupDTO update(Long id, PriceListSetupDTO dto) {
        PriceListSetup entity = requireEntity(id);

        applyCreateFields(entity, dto);

        PriceListSetupDTO merged = toDTO(entity);
        validateDto(merged, false);
        validateNoOverlappingInterval(id, merged);
        validateSingleActiveList(id, merged);

        return toDTO(priceListSetupRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public PriceListSetupDTO findById(Long id) {
        return toDTO(requireEntity(id));
    }

    @Transactional(readOnly = true)
    public Page<PriceListSetupDTO> findAll(Pageable pageable) {
        return priceListSetupRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<PriceListSetupDTO> findAllBasedOnLoggedInFacility(Pageable pageable) {
        Long facilityId = getFacility();
        return priceListSetupRepository
                .findAllVisibleToFacility(facilityId, pageable)
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

        BillingCoverageType coverageType = resolveCoverageType(request);
        PriceListItemType itemType = mapItemType(request.billingItemType());

        MatchedPrice matched = findMatchingPrice(
                request,
                coverageType,
                itemType,
                pricingDate,
                true
        );

        List<PriceListSetup> candidatePriceLists =
                findCandidatePriceLists(
                        request,
                        coverageType,
                        pricingDate
                );

        if (candidatePriceLists.isEmpty()) {
            LOG.info(
                    "[RESOLVE] No {} price list found. "
                            + "Caller will fall back to Setup item price. "
                            + "facilityId={} itemType={} itemId={}",
                    coverageType,
                    request.facilityId(),
                    itemType,
                    request.itemId()
            );
            return null;
        }

        if (coverageType == BillingCoverageType.INSURANCE
                && matched != null
                && !Boolean.TRUE.equals(matched.item().getIsActive())) {

            MatchedPrice cashMatched = findMatchingPrice(
                    request,
                    BillingCoverageType.SELF_PAY,
                    itemType,
                    pricingDate,
                    true
            );

            if (cashMatched != null
                    && Boolean.TRUE.equals(cashMatched.item().getIsActive())) {
                return toResolutionDTO(
                        cashMatched.priceList(),
                        cashMatched.item(),
                        true
                );
            }

            return null;
        }

        if (matched == null
                || !Boolean.TRUE.equals(matched.item().getIsActive())) {
            return null;
        }

        return toResolutionDTO(
                matched.priceList(),
                matched.item(),
                false
        );
    }

    @Transactional(readOnly = true)
    public boolean requiresPreAuthorization(
            BillingPricingResolutionRequest request
    ) {
        BillingPricingResolutionDTO resolved = resolve(request);

        if (resolved != null
                && Boolean.TRUE.equals(resolved.requiresPreAuthorization())) {
            return true;
        }

        return requiresPreAuthorizationFromAnyMatchingInsuranceItem(request);
    }

    private boolean requiresPreAuthorizationFromAnyMatchingInsuranceItem(
            BillingPricingResolutionRequest request
    ) {
        if (resolveCoverageType(request) != BillingCoverageType.INSURANCE) {
            return false;
        }

        LocalDate pricingDate =
                request.pricingDate() != null
                        ? request.pricingDate()
                        : LocalDate.now();

        PriceListItemType itemType = mapItemType(request.billingItemType());

        return findCandidatePriceLists(
                request,
                BillingCoverageType.INSURANCE,
                pricingDate
        )
                .stream()
                .map(priceList ->
                        selectItem(
                                priceList.getId(),
                                itemType,
                                request.itemId(),
                                true
                        )
                                .filter(item ->
                                        Boolean.TRUE.equals(
                                                item.getRequiresPreAuthorization()
                                        )
                                )
                                .isPresent()
                )
                .anyMatch(Boolean.TRUE::equals);
    }

    private MatchedPrice findMatchingPrice(
            BillingPricingResolutionRequest request,
            BillingCoverageType coverageType,
            PriceListItemType itemType,
            LocalDate pricingDate,
            boolean activeItemsOnly
    ) {
        List<PriceListSetup> candidatePriceLists =
                findCandidatePriceLists(request, coverageType, pricingDate);

        for (PriceListSetup priceList : candidatePriceLists) {
            Optional<PriceListSetupItem> itemOptional = selectItem(
                    priceList.getId(),
                    itemType,
                    request.itemId(),
                    activeItemsOnly
            );

            if (itemOptional.isEmpty()
                    && coverageType == BillingCoverageType.INSURANCE) {
                itemOptional = selectItem(
                        priceList.getId(),
                        itemType,
                        request.itemId(),
                        false
                );
            }

            if (itemOptional.isPresent()) {
                return new MatchedPrice(priceList, itemOptional.get());
            }
        }

        return null;
    }

    private Optional<PriceListSetupItem> selectItem(
            Long priceListSetupId,
            PriceListItemType itemType,
            Long sourceId,
            boolean activeOnly
    ) {
        if (activeOnly) {
            return priceListSetupItemRepository
                    .findFirstByPriceListSetupIdAndItemTypeAndSourceIdAndIsActiveTrue(
                            priceListSetupId,
                            itemType,
                            sourceId
                    );
        }

        return priceListSetupItemRepository
                .findFirstByPriceListSetupIdAndItemTypeAndSourceId(
                        priceListSetupId,
                        itemType,
                        sourceId
                );
    }

    private boolean isEffective(PriceListSetup priceList, LocalDate pricingDate) {
        if (priceList.getEffectiveFrom() == null) {
            return false;
        }

        boolean effectiveFromValid = !pricingDate.isBefore(priceList.getEffectiveFrom());
        boolean effectiveToValid =
                priceList.getEffectiveTo() == null
                        || !pricingDate.isAfter(priceList.getEffectiveTo());

        return effectiveFromValid && effectiveToValid;
    }

    private int payerPriority(PriceListSetup priceList, Long requestedPayerId) {
        if (requestedPayerId != null
                && requestedPayerId.equals(priceList.getPayerId())) {
            return 0;
        }
        if (requestedPayerId != null
                && requestedPayerId.equals(priceList.getNphiesPayerId())) {
            return 0;
        }
        if (priceList.getPayerId() == null && priceList.getNphiesPayerId() == null) {
            return 1;
        }
        return 2;
    }

    private BillingPricingResolutionDTO toResolutionDTO(
            PriceListSetup priceList,
            PriceListSetupItem item,
            boolean cashFallback
    ) {
        Tax tax = priceList.getTaxId() == null
                ? null
                : taxRepository.findById(priceList.getTaxId()).orElse(null);

        return new BillingPricingResolutionDTO(
                priceList.getId(),
                item.getId(),
                priceList.getName(),
                item.getItemCode(),
                item.getItemName(),
                item.getUnitPrice(),
                priceList.getCurrency(),
                null,
                hasItemDiscount(item) ? DiscountType.PERCENTAGE.name() : null,
                hasItemDiscount(item) ? item.getDiscountPercentage() : null,
                null,
                tax == null ? null : tax.getId(),
                tax == null || tax.getTaxType() == null ? null : tax.getTaxType().name(),
                tax == null ? null : tax.getPercentage(),
                "DISCOUNT_THEN_TAX",
                "HALF_UP",
                4,
                resolveRequiresPreAuthorization(priceList, item),
                priceList.getType() != null ? priceList.getType().name() : null,
                cashFallback
        );
    }

    private List<PriceListSetup> findCandidatePriceLists(
            BillingPricingResolutionRequest request,
            BillingCoverageType coverageType,
            LocalDate pricingDate
    ) {
        List<PriceListSetup> candidates = new ArrayList<>(
                priceListSetupRepository
                        .findAllByFacilityIdAndCurrencyAndStatusAndIsActiveTrue(
                                request.facilityId(),
                                request.currency(),
                                PriceListSetupStatus.ACTIVE
                        )
        );
        candidates.addAll(
                priceListSetupRepository
                        .findAllByAppliesToAllFacilitiesTrueAndCurrencyAndStatusAndIsActiveTrue(
                                request.currency(),
                                PriceListSetupStatus.ACTIVE
                        )
        );

        return candidates.stream()
                .filter(priceList ->
                        matchesCoverageType(
                                priceList,
                                coverageType,
                                request.payerId()
                        )
                )
                .filter(priceList -> isEffective(priceList, pricingDate))
                .distinct()
                .sorted(
                        Comparator
                                .comparing((PriceListSetup priceList) ->
                                        Boolean.TRUE.equals(
                                                priceList.getAppliesToAllFacilities()
                                        )
                                )
                                .thenComparing(priceList ->
                                        payerPriority(priceList, request.payerId())
                                )
                                .thenComparing(
                                        PriceListSetup::getVersionNumber,
                                        Comparator.nullsLast(Comparator.reverseOrder())
                                )
                                .thenComparing(
                                        PriceListSetup::getId,
                                        Comparator.reverseOrder()
                                )
                )
                .toList();
    }

    private Boolean resolveRequiresPreAuthorization(
            PriceListSetup priceList,
            PriceListSetupItem item
    ) {
        if (priceList.getType() != PriceListSetupType.INSURANCE) {
            return false;
        }
        return Boolean.TRUE.equals(item.getRequiresPreAuthorization());
    }

    private BillingCoverageType resolveCoverageType(
            BillingPricingResolutionRequest request
    ) {
        if (request.coverageType() != null) {
            return request.coverageType();
        }
        if (request.patientInsuranceId() != null || request.payerId() != null) {
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
            return isCashType(priceList.getType());
        }

        if (coverageType == BillingCoverageType.INSURANCE) {
            if (priceList.getType() != PriceListSetupType.INSURANCE) {
                return false;
            }
            if (requestedPayerId == null) {
                return true;
            }
            return requestedPayerId.equals(priceList.getPayerId())
                    || requestedPayerId.equals(priceList.getNphiesPayerId());
        }

        return true;
    }

    private void applyCreateFields(PriceListSetup entity, PriceListSetupDTO dto) {
        entity.setFacilityId(dto.facilityId());
        entity.setAppliesToAllFacilities(
                Boolean.TRUE.equals(dto.appliesToAllFacilities())
        );
        entity.setType(dto.type());
        entity.setPayerId(dto.payerId());
        entity.setNphiesPayerId(dto.nphiesPayerId());
        entity.setName(dto.name().trim());
        entity.setShortName(blankToNull(dto.shortName()));
        entity.setDescription(blankToNull(dto.description()));
        entity.setTaxId(dto.taxId());
        entity.setVersionNumber(
                dto.versionNumber() != null
                        ? dto.versionNumber()
                        : nextVersionNumber(entity)
        );
        entity.setEffectiveFrom(dto.effectiveFrom());
        entity.setEffectiveTo(dto.effectiveTo());
        entity.setCurrency(dto.currency());
        entity.setStatus(
                dto.status() != null ? dto.status() : PriceListSetupStatus.ACTIVE
        );
    }

    private void validateDto(PriceListSetupDTO dto, boolean creating) {
        if (dto.effectiveFrom() != null
                && dto.effectiveTo() != null
                && !dto.effectiveTo().isAfter(dto.effectiveFrom())
                && !dto.effectiveTo().isEqual(dto.effectiveFrom())) {
            throw new BadRequestAlertException(
                    "Effective End Date must be later than the Start Date.",
                    ENTITY_NAME,
                    "effectiveDate.invalidRange"
            );
        }

        if (dto.effectiveFrom() != null
                && dto.effectiveTo() != null
                && dto.effectiveTo().isBefore(dto.effectiveFrom())) {
            throw new BadRequestAlertException(
                    "Effective End Date must be later than the Start Date.",
                    ENTITY_NAME,
                    "effectiveDate.invalidRange"
            );
        }

        if (dto.type() == PriceListSetupType.INSURANCE
                && dto.payerId() == null
                && dto.nphiesPayerId() == null) {
            throw new BadRequestAlertException(
                    "Insurance company is required for insurance price lists.",
                    ENTITY_NAME,
                    "payer.required"
            );
        }

        if (isActiveStatus(dto.status()) && dto.effectiveFrom() == null) {
            throw new BadRequestAlertException(
                    "Effective start date is required for an active price list.",
                    ENTITY_NAME,
                    "effectiveDate.required"
            );
        }

        if (creating
                && dto.effectiveFrom() != null
                && dto.effectiveFrom().isBefore(LocalDate.now())) {
            throw new BadRequestAlertException(
                    "The start date cannot be in the past.",
                    ENTITY_NAME,
                    "effectiveDate.startInPast"
            );
        }

        if (dto.taxId() != null && taxRepository.findById(dto.taxId()).isEmpty()) {
            throw new BadRequestAlertException(
                    "Selected tax was not found.",
                    ENTITY_NAME,
                    "tax.notFound"
            );
        }
    }

    private void validateSingleActiveList(Long excludeId, PriceListSetupDTO dto) {
        if (!isActiveStatus(dto.status())) {
            return;
        }

        List<PriceListSetup> candidates = findActiveSiblings(dto);

        boolean duplicate = candidates.stream()
                .filter(candidate ->
                        excludeId == null || !excludeId.equals(candidate.getId())
                )
                .anyMatch(candidate -> isActiveStatus(candidate.getStatus()));

        if (!duplicate) {
            return;
        }

        if (dto.type() == PriceListSetupType.INSURANCE) {
            throw new BadRequestAlertException(
                    "An active price list already exists for this insurance company.",
                    ENTITY_NAME,
                    "active.payer.duplicate"
            );
        }

        throw new BadRequestAlertException(
                "Only one active Cash Price List is allowed.",
                ENTITY_NAME,
                "active.cash.duplicate"
        );
    }

    private void validateNoOverlappingInterval(
            Long excludeId,
            PriceListSetupDTO dto
    ) {
        if (dto.effectiveFrom() == null) {
            return;
        }

        List<PriceListSetup> candidates = findActiveSiblings(dto);

        boolean hasOverlap = candidates.stream()
                .filter(candidate ->
                        excludeId == null || !excludeId.equals(candidate.getId())
                )
                .filter(candidate -> candidate.getEffectiveFrom() != null)
                .anyMatch(candidate ->
                        intervalsOverlap(
                                dto.effectiveFrom(),
                                dto.effectiveTo(),
                                candidate.getEffectiveFrom(),
                                candidate.getEffectiveTo()
                        )
                );

        if (!hasOverlap) {
            return;
        }

        if (dto.type() == PriceListSetupType.INSURANCE) {
            throw new BadRequestAlertException(
                    "An insurance price list already exists for this company in the selected date range.",
                    ENTITY_NAME,
                    "interval.payer.duplicate"
            );
        }

        throw new BadRequestAlertException(
                "A cash price list already exists in the selected date range.",
                ENTITY_NAME,
                "interval.type.duplicate"
        );
    }

    private List<PriceListSetup> findActiveSiblings(PriceListSetupDTO dto) {
        List<PriceListSetup> candidates = new ArrayList<>();

        if (dto.type() == PriceListSetupType.INSURANCE) {
            if (dto.payerId() != null) {
                candidates.addAll(
                        priceListSetupRepository.findAllByFacilityIdAndPayerIdAndIsActiveTrue(
                                dto.facilityId(),
                                dto.payerId()
                        )
                );
                candidates.addAll(
                        priceListSetupRepository
                                .findAllByAppliesToAllFacilitiesTrueAndPayerIdAndIsActiveTrue(
                                        dto.payerId()
                                )
                );
            }
            if (dto.nphiesPayerId() != null) {
                candidates.addAll(
                        priceListSetupRepository
                                .findAllByFacilityIdAndNphiesPayerIdAndIsActiveTrue(
                                        dto.facilityId(),
                                        dto.nphiesPayerId()
                                )
                );
                candidates.addAll(
                        priceListSetupRepository
                                .findAllByAppliesToAllFacilitiesTrueAndNphiesPayerIdAndIsActiveTrue(
                                        dto.nphiesPayerId()
                                )
                );
            }
            return candidates;
        }

        if (isCashType(dto.type())) {
            candidates.addAll(
                    priceListSetupRepository.findAllByFacilityIdAndTypeInAndIsActiveTrue(
                            dto.facilityId(),
                            CASH_TYPES
                    )
            );
            candidates.addAll(
                    priceListSetupRepository
                            .findAllByAppliesToAllFacilitiesTrueAndTypeInAndIsActiveTrue(
                                    CASH_TYPES
                            )
            );
            return candidates;
        }

        candidates.addAll(
                priceListSetupRepository.findAllByFacilityIdAndTypeAndIsActiveTrue(
                        dto.facilityId(),
                        dto.type()
                )
        );
        return candidates;
    }

    private boolean intervalsOverlap(
            LocalDate from1,
            LocalDate to1,
            LocalDate from2,
            LocalDate to2
    ) {
        LocalDate end1 = to1 != null ? to1 : LocalDate.MAX;
        LocalDate end2 = to2 != null ? to2 : LocalDate.MAX;
        return !from1.isAfter(end2) && !from2.isAfter(end1);
    }

    private PriceListItemType mapItemType(BillingItemTypes billingItemType) {
        return PriceListItemType.valueOf(billingItemType.name());
    }

    private boolean hasItemDiscount(PriceListSetupItem item) {
        return item.getDiscountPercentage() != null
                && item.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0;
    }

    private void cloneItems(Long sourcePriceListSetupId, Long targetPriceListSetupId) {
        List<PriceListSetupItem> sourceItems =
                priceListSetupItemRepository.findAllByPriceListSetupId(
                        sourcePriceListSetupId
                );

        if (sourceItems.isEmpty()) {
            return;
        }

        List<PriceListSetupItem> clonedItems = sourceItems.stream()
                .map(sourceItem -> {
                    PriceListSetupItem clonedItem = new PriceListSetupItem();
                    clonedItem.setPriceListSetupId(targetPriceListSetupId);
                    clonedItem.setWaseelItemMappingId(sourceItem.getWaseelItemMappingId());
                    clonedItem.setSbsCatalogId(sourceItem.getSbsCatalogId());
                    clonedItem.setItemType(sourceItem.getItemType());
                    clonedItem.setSourceId(sourceItem.getSourceId());
                    clonedItem.setItemCode(sourceItem.getItemCode());
                    clonedItem.setItemName(sourceItem.getItemName());
                    clonedItem.setCategory(sourceItem.getCategory());
                    clonedItem.setUnitPrice(sourceItem.getUnitPrice());
                    clonedItem.setDiscountPercentage(sourceItem.getDiscountPercentage());
                    clonedItem.setIsActive(sourceItem.getIsActive());
                    clonedItem.setRequiresPreAuthorization(
                            sourceItem.getRequiresPreAuthorization()
                    );
                    return clonedItem;
                })
                .toList();

        priceListSetupItemRepository.saveAll(clonedItems);
    }

    private Integer nextVersionNumber(PriceListSetup source) {
        Integer max = priceListSetupRepository.findMaxVersionNumber(
                source.getFacilityId(),
                source.getType(),
                source.getPayerId(),
                source.getNphiesPayerId()
        );
        return (max == null ? 0 : max) + 1;
    }

    private PriceListSetupDTO toDTO(PriceListSetup entity) {
        String facilityName = facilityRepository.findById(entity.getFacilityId())
                .map(facility -> facility.getName())
                .orElse(null);

        String payerName = entity.getPayerId() == null
                ? null
                : payorRepository.findById(entity.getPayerId())
                        .map(Payor::getName)
                        .orElse(null);

        String nphiesPayerName = entity.getNphiesPayerId() == null
                ? null
                : nphiesPayerRepository.findById(entity.getNphiesPayerId())
                        .map(NphiesPayer::getNameEn)
                        .orElse(null);

        String taxName = entity.getTaxId() == null
                ? null
                : taxRepository.findById(entity.getTaxId())
                        .map(Tax::getName)
                        .orElse(null);

        return new PriceListSetupDTO(
                entity.getId(),
                entity.getFacilityId(),
                Boolean.TRUE.equals(entity.getAppliesToAllFacilities())
                        ? "All"
                        : facilityName,
                entity.getAppliesToAllFacilities(),
                entity.getType(),
                entity.getPayerId(),
                payerName,
                entity.getNphiesPayerId(),
                nphiesPayerName,
                entity.getName(),
                entity.getShortName(),
                entity.getDescription(),
                entity.getVersionNumber(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getTaxId(),
                taxName,
                entity.getCreatedDate(),
                entity.getLastModifiedDate(),
                entity.getCreatedBy(),
                entity.getLastModifiedBy()
        );
    }

    private PriceListSetup requireEntity(Long id) {
        return priceListSetupRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Price list setup not found with id: " + id
                        )
                );
    }

    private Long getFacility() {
        return SecurityUtils.getCurrentUserFacility()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Missing mandatory claim 'tenant' in JWT."
                        )
                );
    }

    private boolean isCashType(PriceListSetupType type) {
        return type != null && CASH_TYPES.contains(type);
    }

    private boolean isActiveStatus(PriceListSetupStatus status) {
        return status == PriceListSetupStatus.ACTIVE;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private record MatchedPrice(
            PriceListSetup priceList,
            PriceListSetupItem item
    ) {
    }
}
