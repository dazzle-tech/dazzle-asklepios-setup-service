package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.PriceListSetupItem;
import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupStatus;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.repository.BrandMedicationRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.PriceListSetupItemRepository;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.repository.ProcedureRepository;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.service.dto.PriceListItemWaseelCodesDTO;
import com.dazzle.asklepios.service.dto.PriceListSetupItemDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class PriceListSetupItemService {

    private static final String ENTITY = "priceListSetupItem";

    private final PriceListSetupItemRepository priceListSetupItemRepository;

    private final PriceListSetupRepository priceListSetupRepository;

    private final ServiceRepository serviceRepository;

    private final ProcedureRepository procedureRepository;

    private final BrandMedicationRepository brandMedicationRepository;

    private final DiagnosticTestRepository diagnosticTestRepository;

    public PriceListSetupItemDTO create(
            Long priceListSetupId,
            PriceListSetupItemDTO dto
    ) {
        PriceListSetup priceListSetup = requirePriceList(priceListSetupId);

        EncounterType visitType = resolveVisitType(priceListSetup, dto);
        validateActiveCatalogItem(dto);
        validateUniqueWithinPriceList(priceListSetupId, dto, visitType, null);

        PriceListSetupItem entity = new PriceListSetupItem();
        entity.setId(null);
        entity.setPriceListSetupId(priceListSetupId);
        entity.setWaseelItemMappingId(dto.waseelItemMappingId());
        entity.setSbsCatalogId(dto.sbsCatalogId());
        entity.setItemType(dto.itemType());
        entity.setSourceId(dto.sourceId());
        entity.setItemCode(dto.itemCode());
        entity.setNonStandardCode(blankToNull(dto.nonStandardCode()));
        entity.setItemName(dto.itemName());
        entity.setCategory(blankToNull(dto.category()));
        entity.setVisitType(visitType);
        entity.setUnitPrice(dto.unitPrice());
        entity.setCost(dto.cost());
        entity.setDiscountPercentage(
                dto.discountPercentage() != null
                        ? dto.discountPercentage()
                        : BigDecimal.ZERO
        );
        entity.setIsActive(dto.isActive() == null ? true : dto.isActive());
        entity.setRequiresPreAuthorization(
                resolveRequiresPreAuthorization(
                        priceListSetup,
                        dto.requiresPreAuthorization()
                )
        );
        entity.setVisitTypeLocked(false);

        try {
            return toDTO(priceListSetupItemRepository.save(entity));
        } catch (DataIntegrityViolationException exception) {
            throw mapIntegrityViolation(exception);
        }
    }

    public PriceListSetupItemDTO update(
            Long priceListSetupId,
            Long itemId,
            PriceListSetupItemDTO dto
    ) {
        PriceListSetupItem entity =
                priceListSetupItemRepository
                        .findByIdAndPriceListSetupId(itemId, priceListSetupId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Price list item not found with id: " + itemId
                                )
                        );

        PriceListSetup priceListSetup = requirePriceList(priceListSetupId);
        EncounterType visitType = resolveVisitType(priceListSetup, dto);

        if (visitType != entity.getVisitType()) {
            if (Boolean.TRUE.equals(entity.getVisitTypeLocked())) {
                throw new BadRequestAlertException(
                        "Visit / encounter type cannot be changed after the service has been used for a patient.",
                        ENTITY,
                        "visitType.locked"
                );
            }
            validateUniqueWithinPriceList(
                    priceListSetupId,
                    dto,
                    visitType,
                    itemId
            );
        }

        entity.setCategory(blankToNull(dto.category()));
        entity.setVisitType(visitType);
        entity.setWaseelItemMappingId(
                dto.waseelItemMappingId()
        );
        entity.setSbsCatalogId(dto.sbsCatalogId());
        entity.setItemType(dto.itemType());
        entity.setSourceId(dto.sourceId());
        entity.setItemCode(dto.itemCode());
        entity.setNonStandardCode(blankToNull(dto.nonStandardCode()));
        entity.setItemName(dto.itemName());
        entity.setUnitPrice(dto.unitPrice());
        entity.setCost(dto.cost());
        if (dto.isActive() != null) {
            entity.setIsActive(dto.isActive());
        }

        try {
            return toDTO(priceListSetupItemRepository.save(entity));
        } catch (DataIntegrityViolationException exception) {
            throw mapIntegrityViolation(exception);
        }
    }

    @Transactional(readOnly = true)
    public PriceListSetupItemDTO findById(Long priceListSetupId, Long itemId) {
        PriceListSetupItem entity =
                priceListSetupItemRepository
                        .findByIdAndPriceListSetupId(itemId, priceListSetupId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Price list item not found with id: " + itemId
                                )
                        );

        return toDTO(entity);
    }

    @Transactional(readOnly = true)
    public Page<PriceListSetupItemDTO> search(
            Long priceListSetupId,
            String search,
            PriceListItemType itemType,
            Pageable pageable
    ) {
        String normalizedSearch =
                search == null || search.isBlank() ? null : search.trim();

        return priceListSetupItemRepository
                .findAllByPriceListSetupIdAndItemNameContaining(
                        priceListSetupId,
                        normalizedSearch,
                        itemType,
                        pageable
                )
                .map(this::toDTO);
    }

    public void delete(Long priceListSetupId, Long itemId) {
    @Transactional(readOnly = true)
    public java.util.Optional<PriceListItemWaseelCodesDTO> findInsuranceWaseelCodes(
            PriceListItemType itemType,
            Long sourceId,
            Long facilityId
    ) {
        if (itemType == null || sourceId == null) {
            return java.util.Optional.empty();
        }

        return priceListSetupItemRepository
                .findActiveInsuranceItemsByCatalog(
                        itemType,
                        sourceId,
                        facilityId,
                        PriceListSetupStatus.ACTIVE,
                        PriceListSetupType.INSURANCE
                )
                .stream()
                .findFirst()
                .map(item -> new PriceListItemWaseelCodesDTO(
                        item.getItemCode(),
                        blankToNull(item.getNonStandardCode())
                ));
    }

    public void delete(
            Long priceListSetupId,
            Long itemId
    ) {

        PriceListSetupItem entity =
                priceListSetupItemRepository
                        .findByIdAndPriceListSetupId(itemId, priceListSetupId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Price list item not found with id: " + itemId
                                )
                        );

        priceListSetupItemRepository.delete(entity);
    }

    private EncounterType resolveVisitType(
            PriceListSetup priceListSetup,
            PriceListSetupItemDTO dto
    ) {
        return dto.visitType();
    }

    private void validateActiveCatalogItem(PriceListSetupItemDTO dto) {
        boolean active = switch (dto.itemType()) {
            case SERVICE -> serviceRepository.findById(dto.sourceId())
                    .map(ServiceSetup::getIsActive)
                    .orElse(false);
            case PROCEDURE -> procedureRepository.findById(dto.sourceId())
                    .map(Procedure::getIsActive)
                    .orElse(false);
            case MEDICATION -> brandMedicationRepository.findById(dto.sourceId())
                    .map(BrandMedication::getIsActive)
                    .orElse(false);
            case LABORATORY, RADIOLOGY, PATHOLOGY ->
                    diagnosticTestRepository.findById(dto.sourceId())
                            .map(DiagnosticTest::getIsActive)
                            .orElse(false);
        };

        if (!Boolean.TRUE.equals(active)) {
            throw new BadRequestAlertException(
                    "Only active services from Service Definition can be added to a price list.",
                    ENTITY,
                    "catalog.inactive"
            );
        }
    }

    private void validateUniqueWithinPriceList(
            Long priceListSetupId,
            PriceListSetupItemDTO dto,
            EncounterType visitType,
            Long excludeItemId
    ) {
        if (dto.sourceId() == null || dto.itemType() == null) {
            return;
        }

        boolean duplicate = priceListSetupItemRepository
                .findAllByPriceListSetupIdAndItemTypeAndSourceId(
                        priceListSetupId,
                        dto.itemType(),
                        dto.sourceId()
                )
                .stream()
                .filter(item ->
                        excludeItemId == null || !excludeItemId.equals(item.getId())
                )
                .anyMatch(item -> sameVisitType(item.getVisitType(), visitType));

        if (duplicate) {
            throw new BadRequestAlertException(
                    "This service is already configured for the selected visit type on this price list.",
                    ENTITY,
                    "item.duplicateVisitType"
            );
        }
    }

    private boolean sameVisitType(
            EncounterType first,
            EncounterType second
    ) {
        return first == second;
    }

    private BadRequestAlertException mapIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        String message = exception.getMostSpecificCause() != null
                ? exception.getMostSpecificCause().getMessage()
                : exception.getMessage();

        if (message != null && message.contains("price_list_setup_item_pkey")) {
            return new BadRequestAlertException(
                    "Unable to allocate a new price-list item ID. "
                            + "The database sequence is out of sync — run the "
                            + "price_list_setup_item sequence fix migration "
                            + "or contact support.",
                    ENTITY,
                    "item.idSequenceOutOfSync"
            );
        }

        if (message != null
                && (message.contains("uk_price_list_item_code_visit")
                || message.contains("uk_price_list_item_code"))) {
            return new BadRequestAlertException(
                    "This service is already configured for the selected visit type on this price list.",
                    ENTITY,
                    "item.duplicateVisitType"
            );
        }

        return new BadRequestAlertException(
                "Unable to save the price-list item because of a data constraint.",
                ENTITY,
                "item.constraint"
        );
    }

    private PriceListSetup requirePriceList(Long priceListSetupId) {
        return priceListSetupRepository.findById(priceListSetupId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Price list setup not found with id: "
                                        + priceListSetupId
                        )
                );
    }

    private PriceListSetupItemDTO toDTO(PriceListSetupItem entity) {
        return new PriceListSetupItemDTO(
                entity.getId(),
                entity.getPriceListSetupId(),
                entity.getWaseelItemMappingId(),
                entity.getSbsCatalogId(),
                entity.getItemType(),
                entity.getSourceId(),
                entity.getItemCode(),
                entity.getNonStandardCode(),
                entity.getItemName(),
                entity.getCategory(),
                entity.getVisitType(),
                entity.getUnitPrice(),
                entity.getCost(),
                entity.getDiscountPercentage(),
                entity.getIsActive(),
                entity.getRequiresPreAuthorization(),
                entity.getVisitTypeLocked(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate(),
                entity.getCreatedBy(),
                entity.getLastModifiedBy()
        );
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Boolean resolveRequiresPreAuthorization(
            PriceListSetup priceListSetup,
            Boolean requestedValue
    ) {
        if (priceListSetup.getType() != PriceListSetupType.INSURANCE) {
            if (Boolean.TRUE.equals(requestedValue)) {
                throw new BadRequestAlertException(
                        "Requires Pre-Authorization is only allowed on insurance price list items.",
                        ENTITY,
                        "requiresPreAuthorization.insuranceOnly"
                );
            }

            return false;
        }

        return Boolean.TRUE.equals(requestedValue);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
