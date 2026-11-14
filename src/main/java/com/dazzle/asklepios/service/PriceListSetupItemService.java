package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.BrandMedication;
import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.PriceListSetupItem;
import com.dazzle.asklepios.domain.Procedure;
import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.VaccineBrands;
import com.dazzle.asklepios.domain.WaseelSbsCatalog;
import com.dazzle.asklepios.repository.WaseelSbsCatalogRepository;
import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupStatus;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupType;
import com.dazzle.asklepios.repository.BrandMedicationRepository;
import com.dazzle.asklepios.repository.DiagnosticTestRepository;
import com.dazzle.asklepios.repository.PriceListSetupItemRepository;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.repository.ProcedureRepository;
import com.dazzle.asklepios.repository.ServiceRepository;
import com.dazzle.asklepios.repository.VaccineBrandsRepository;
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
import java.util.Objects;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PriceListSetupItemService {

    private static final String ENTITY = "priceListSetupItem";

    private final PriceListSetupItemRepository priceListSetupItemRepository;

    private final WaseelSbsCatalogRepository waseelSbsCatalogRepository;

    private final PriceListSetupRepository priceListSetupRepository;

    private final ServiceRepository serviceRepository;

    private final ProcedureRepository procedureRepository;

    private final BrandMedicationRepository brandMedicationRepository;

    private final DiagnosticTestRepository diagnosticTestRepository;

    private final VaccineBrandsRepository
            vaccineBrandsRepository;

    public PriceListSetupItemDTO create(
            Long priceListSetupId,
            PriceListSetupItemDTO dto
    ) {
        PriceListSetup priceListSetup = requirePriceList(priceListSetupId);

        validateActiveCatalogItem(dto);
        validateUniqueWithinPriceList(priceListSetupId, dto);

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
        entity.setUnitPrice(dto.unitPrice());
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

        boolean willBeActive =
                dto.isActive() != null
                        ? dto.isActive()
                        : Boolean.TRUE.equals(entity.getIsActive());

        if (willBeActive
                && dto.sourceId() != null
                && dto.itemType() != null
                && priceListSetupItemRepository
                        .existsByPriceListSetupIdAndItemTypeAndSourceIdAndIsActiveTrueAndIdNot(
                                priceListSetupId,
                                dto.itemType(),
                                dto.sourceId(),
                                entity.getId()
                        )) {
            throw duplicateItemException();
        }

        entity.setCategory(blankToNull(dto.category()));
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

        Page<PriceListSetupItem> page =
                priceListSetupItemRepository
                        .findAllByPriceListSetupIdAndItemNameContaining(
                                priceListSetupId,
                                normalizedSearch,
                                itemType,
                                pageable
                        );

        Map<PriceListItemType, Map<Long, String>> setupItemNames =
                resolveSetupItemNames(page.getContent());

        return page.map(item -> toDTO(item, setupItemNames));
    }

    /**
     * Active items of one catalog item (item type + source id) across all price lists.
     */
    @Transactional(readOnly = true)
    public List<PriceListSetupItemDTO> findActiveBySource(
            PriceListItemType itemType,
            Long sourceId
    ) {
        List<PriceListSetupItem> items =
                priceListSetupItemRepository
                        .findAllByItemTypeAndSourceIdAndIsActiveTrue(
                                itemType,
                                sourceId
                        );

        Map<PriceListItemType, Map<Long, String>> setupItemNames =
                resolveSetupItemNames(items);

        return items.stream()
                .map(item -> toDTO(item, setupItemNames))
                .toList();
    }

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
                .map(this::toWaseelCodes);
    }

    private PriceListItemWaseelCodesDTO toWaseelCodes(PriceListSetupItem item) {
        WaseelSbsCatalog catalog = item.getSbsCatalogId() == null
                ? null
                : waseelSbsCatalogRepository.findById(item.getSbsCatalogId()).orElse(null);
        return new PriceListItemWaseelCodesDTO(
                item.getItemCode(),
                blankToNull(item.getNonStandardCode()),
                catalog == null ? null : blankToNull(catalog.getSbsCode()),
                catalog == null ? null : firstNonBlank(catalog.getShortDescription(), catalog.getLongDescription()),
                catalog == null ? null : blankToNull(catalog.getWaseelItemType())
        );
    }

    private String firstNonBlank(String first, String second) {
        String value = blankToNull(first);
        return value != null ? value : blankToNull(second);
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
            case VACCINE -> vaccineBrandsRepository.findById(dto.sourceId())
                    .map(VaccineBrands::getIsActive)
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

    /**
     * The same item code may appear on several items (one SBS code is shared
     * by many services); the same catalog item may be active only once.
     */
    private void validateUniqueWithinPriceList(
            Long priceListSetupId,
            PriceListSetupItemDTO dto
    ) {
        boolean willBeActive =
                dto.isActive() == null || dto.isActive();

        if (willBeActive
                && dto.sourceId() != null
                && dto.itemType() != null
                && priceListSetupItemRepository
                        .findFirstByPriceListSetupIdAndItemTypeAndSourceIdAndIsActiveTrue(
                                priceListSetupId,
                                dto.itemType(),
                                dto.sourceId()
                        )
                        .isPresent()) {
            throw duplicateItemException();
        }
    }

    private BadRequestAlertException duplicateItemException() {
        return new BadRequestAlertException(
                "This catalog item is already on the selected price list. "
                        + "Use a different price list header (e.g. Cash vs Insurance) "
                        + "to price the same service separately.",
                ENTITY,
                "item.duplicateInPriceList"
        );
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

        if (message != null && message.contains("uk_price_list_item_source_active")) {
            return duplicateItemException();
        }
        if (message != null
                && (message.contains("uk_price_list_item_code_visit")
                || message.contains("uk_price_list_item_code"))) {
            return new BadRequestAlertException(
                    "This service is already configured on this price list.",
                    ENTITY,
                    "item.duplicate"
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

    private PriceListSetupItemDTO toDTO(
            PriceListSetupItem entity
    ) {
        return toDTO(
                entity,
                resolveSetupItemNames(List.of(entity))
        );
    }

    private PriceListSetupItemDTO toDTO(
            PriceListSetupItem entity,
            Map<PriceListItemType, Map<Long, String>> setupItemNames
    ) {

        String setupItemName =
                entity.getItemType() == null || entity.getSourceId() == null
                        ? null
                        : setupItemNames
                                .getOrDefault(entity.getItemType(), Collections.emptyMap())
                                .get(entity.getSourceId());

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
                entity.getUnitPrice(),
                entity.getDiscountPercentage(),
                entity.getIsActive(),
                entity.getRequiresPreAuthorization(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate(),
                entity.getCreatedBy(),
                entity.getLastModifiedBy(),
                setupItemName
        );
    }

    /**
     * Resolves the setup-definition name of each item (by item type + source id),
     * one query per item type present in the given items.
     */
    private Map<PriceListItemType, Map<Long, String>> resolveSetupItemNames(
            List<PriceListSetupItem> items
    ) {
        Map<PriceListItemType, Set<Long>> sourceIdsByType =
                items.stream()
                        .filter(item -> item.getItemType() != null
                                && item.getSourceId() != null)
                        .collect(Collectors.groupingBy(
                                PriceListSetupItem::getItemType,
                                () -> new EnumMap<>(PriceListItemType.class),
                                Collectors.mapping(
                                        PriceListSetupItem::getSourceId,
                                        Collectors.toSet()
                                )
                        ));

        Map<PriceListItemType, Map<Long, String>> namesByType =
                new EnumMap<>(PriceListItemType.class);

        sourceIdsByType.forEach((itemType, sourceIds) ->
                namesByType.put(
                        itemType,
                        findSetupItemNames(itemType, sourceIds)
                )
        );

        return namesByType;
    }

    private Map<Long, String> findSetupItemNames(
            PriceListItemType itemType,
            Set<Long> sourceIds
    ) {
        return switch (itemType) {
            case MEDICATION -> toNameMap(
                    brandMedicationRepository.findAllById(sourceIds),
                    BrandMedication::getId,
                    BrandMedication::getName
            );
            case LABORATORY, RADIOLOGY, PATHOLOGY -> toNameMap(
                    diagnosticTestRepository.findAllById(sourceIds),
                    DiagnosticTest::getId,
                    DiagnosticTest::getName
            );
            case SERVICE -> toNameMap(
                    serviceRepository.findAllById(sourceIds),
                    ServiceSetup::getId,
                    ServiceSetup::getName
            );
            case PROCEDURE -> toNameMap(
                    procedureRepository.findAllById(sourceIds),
                    Procedure::getId,
                    Procedure::getName
            );
            case VACCINE -> toNameMap(
                    vaccineBrandsRepository.findAllById(sourceIds),
                    VaccineBrands::getId,
                    VaccineBrands::getName
            );
        };
    }

    private <T> Map<Long, String> toNameMap(
            List<T> entities,
            Function<T, Long> idGetter,
            Function<T, String> nameGetter
    ) {
        return entities.stream()
                .filter(entity -> nameGetter.apply(entity) != null)
                .collect(Collectors.toMap(
                        idGetter,
                        nameGetter,
                        (first, second) -> first
                ));
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
}
