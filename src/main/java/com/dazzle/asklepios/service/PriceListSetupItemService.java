package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.PriceListSetupItem;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupType;
import com.dazzle.asklepios.repository.PriceListSetupItemRepository;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.service.dto.PriceListSetupItemDTO;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PriceListSetupItemService {

    private static final String ENTITY = "priceListSetupItem";

    private final PriceListSetupItemRepository
            priceListSetupItemRepository;

    private final PriceListSetupRepository
            priceListSetupRepository;

    public PriceListSetupItemDTO create(
            Long priceListSetupId,
            PriceListSetupItemDTO dto
    ) {

        if (!priceListSetupRepository.existsById(priceListSetupId)) {
            throw new EntityNotFoundException(
                    "Price list setup not found with id: "
                            + priceListSetupId
            );
        }

        PriceListSetup priceListSetup =
                priceListSetupRepository
                        .findById(priceListSetupId)
                        .orElseThrow();

        validateUniqueWithinPriceList(priceListSetupId, dto);

        PriceListSetupItem entity =
                new PriceListSetupItem();

        entity.setId(null);
        entity.setPriceListSetupId(priceListSetupId);
        entity.setWaseelItemMappingId(
                dto.waseelItemMappingId()
        );
        entity.setSbsCatalogId(dto.sbsCatalogId());
        entity.setItemType(dto.itemType());
        entity.setSourceId(dto.sourceId());
        entity.setItemCode(dto.itemCode());
        entity.setItemName(dto.itemName());
        entity.setUnitPrice(dto.unitPrice());
        entity.setDiscountPercentage(
                dto.discountPercentage()
        );

        entity.setIsActive(
                dto.isActive() == null
                        ? true
                        : dto.isActive()
        );
        entity.setRequiresPreAuthorization(
                resolveRequiresPreAuthorization(
                        priceListSetup,
                        dto.requiresPreAuthorization()
                )
        );

        try {
            PriceListSetupItem savedEntity =
                    priceListSetupItemRepository.save(entity);

            return toDTO(savedEntity);
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
                        .findByIdAndPriceListSetupId(
                                itemId,
                                priceListSetupId
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Price list item not found with id: "
                                                + itemId
                                )
                        );

        PriceListSetup priceListSetup =
                priceListSetupRepository
                        .findById(priceListSetupId)
                        .orElseThrow();

        if (dto.itemCode() != null
                && !dto.itemCode().equals(entity.getItemCode())
                && priceListSetupItemRepository.existsByPriceListSetupIdAndItemCode(
                        priceListSetupId,
                        dto.itemCode()
                )) {
            throw duplicateItemCodeException(dto.itemCode());
        }

        entity.setWaseelItemMappingId(
                dto.waseelItemMappingId()
        );
        entity.setSbsCatalogId(dto.sbsCatalogId());
        entity.setItemType(dto.itemType());
        entity.setSourceId(dto.sourceId());
        entity.setItemCode(dto.itemCode());
        entity.setItemName(dto.itemName());
        entity.setUnitPrice(dto.unitPrice());
        entity.setDiscountPercentage(
                dto.discountPercentage()
        );

        if (dto.isActive() != null) {
            entity.setIsActive(dto.isActive());
        }

        if (dto.requiresPreAuthorization() != null) {
            entity.setRequiresPreAuthorization(
                    resolveRequiresPreAuthorization(
                            priceListSetup,
                            dto.requiresPreAuthorization()
                    )
            );
        }

        try {
            PriceListSetupItem savedEntity =
                    priceListSetupItemRepository.save(entity);

            return toDTO(savedEntity);
        } catch (DataIntegrityViolationException exception) {
            throw mapIntegrityViolation(exception);
        }
    }

    @Transactional(readOnly = true)
    public PriceListSetupItemDTO findById(
            Long priceListSetupId,
            Long itemId
    ) {

        PriceListSetupItem entity =
                priceListSetupItemRepository
                        .findByIdAndPriceListSetupId(
                                itemId,
                                priceListSetupId
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Price list item not found with id: "
                                                + itemId
                                )
                        );

        return toDTO(entity);
    }

    @Transactional(readOnly = true)
    public Page<PriceListSetupItemDTO> search(
            Long priceListSetupId,
            Pageable pageable
    ) {

        return priceListSetupItemRepository
                .findAllByPriceListSetupId(
                        priceListSetupId,
                        pageable
                )
                .map(this::toDTO);
    }

    public void delete(
            Long priceListSetupId,
            Long itemId
    ) {

        PriceListSetupItem entity =
                priceListSetupItemRepository
                        .findByIdAndPriceListSetupId(
                                itemId,
                                priceListSetupId
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Price list item not found with id: "
                                                + itemId
                                )
                        );

        priceListSetupItemRepository.delete(entity);
    }

    private void validateUniqueWithinPriceList(
            Long priceListSetupId,
            PriceListSetupItemDTO dto
    ) {
        if (priceListSetupItemRepository.existsByPriceListSetupIdAndItemCode(
                priceListSetupId,
                dto.itemCode()
        )) {
            throw duplicateItemCodeException(dto.itemCode());
        }

        if (dto.sourceId() != null
                && dto.itemType() != null
                && priceListSetupItemRepository
                        .findFirstByPriceListSetupIdAndItemTypeAndSourceIdAndIsActiveTrue(
                                priceListSetupId,
                                dto.itemType(),
                                dto.sourceId()
                        )
                        .isPresent()) {
            throw new BadRequestAlertException(
                    "This catalog item is already on the selected price list. "
                            + "Use a different price list header (e.g. Cash vs Insurance) "
                            + "to price the same service separately.",
                    ENTITY,
                    "item.duplicateInPriceList"
            );
        }
    }

    private BadRequestAlertException duplicateItemCodeException(
            String itemCode
    ) {
        return new BadRequestAlertException(
                "Item code "
                        + itemCode
                        + " already exists on this price list.",
                ENTITY,
                "itemCode.duplicate"
        );
    }

    private RuntimeException mapIntegrityViolation(
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

        if (message != null && message.contains("uk_price_list_item_code")) {
            return new BadRequestAlertException(
                    "This item code already exists on the selected price list.",
                    ENTITY,
                    "itemCode.duplicate"
            );
        }

        return exception;
    }

    private PriceListSetupItemDTO toDTO(
            PriceListSetupItem entity
    ) {

        return new PriceListSetupItemDTO(
                entity.getId(),
                entity.getPriceListSetupId(),
                entity.getWaseelItemMappingId(),
                entity.getSbsCatalogId(),
                entity.getItemType(),
                entity.getSourceId(),
                entity.getItemCode(),
                entity.getItemName(),
                entity.getUnitPrice(),
                entity.getDiscountPercentage(),
                entity.getIsActive(),
                entity.getRequiresPreAuthorization()
        );
    }

    private Boolean resolveRequiresPreAuthorization(
            PriceListSetup priceListSetup,
            Boolean requestedValue
    ) {
        if (priceListSetup.getType()
                != PriceListSetupType.INSURANCE) {
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
