package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.PriceListSetupItem;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupStatus;
import com.dazzle.asklepios.repository.PriceListSetupItemRepository;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.service.dto.BillingPricingResolutionDTO;
import com.dazzle.asklepios.service.dto.BillingPricingResolutionRequest;
import com.dazzle.asklepios.service.dto.PriceListSetupDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PriceListSetupService {

    private final PriceListSetupRepository priceListSetupRepository;

    private final PriceListSetupItemRepository
            priceListSetupItemRepository;

    public PriceListSetupDTO create(PriceListSetupDTO dto) {

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

        entity.setStatus(PriceListSetupStatus.DRAFT);
        entity.setIsActive(true);

        PriceListSetup savedEntity =
                priceListSetupRepository.save(entity);

        return toDTO(savedEntity);
    }

    public PriceListSetupDTO update(
            Long id,
            PriceListSetupDTO dto
    ) {

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

        List<PriceListSetup> candidatePriceLists =
                priceListSetupRepository
                        .findAllByFacilityIdAndCurrencyAndStatusAndIsActiveTrue(
                                request.facilityId(),
                                request.currency(),
                                PriceListSetupStatus.ACTIVE
                        )
                        .stream()
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
                                    request.billingItemType(),
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

                /*
                 * Add these values when discount and tax are resolved.
                 * For now, they are returned as null/default values.
                 */
                null,
                null,
                null,
                null,

                null,
                null,
                null,

                "DISCOUNT_THEN_TAX",
                "HALF_UP",
                4
        );
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
                entity.getCurrency()
        );
    }
}