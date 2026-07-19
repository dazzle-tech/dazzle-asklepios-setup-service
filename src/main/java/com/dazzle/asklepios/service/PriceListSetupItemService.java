package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.PriceListSetupItem;
import com.dazzle.asklepios.repository.PriceListSetupItemRepository;
import com.dazzle.asklepios.repository.PriceListSetupRepository;
import com.dazzle.asklepios.service.dto.PriceListSetupItemDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PriceListSetupItemService {

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

        PriceListSetupItem entity =
                new PriceListSetupItem();

        entity.setPriceListSetupId(priceListSetupId);
        entity.setWaseelItemMappingId(
                dto.waseelItemMappingId()
        );
        entity.setSbsCatalogId(dto.sbsCatalogId());
        entity.setItemType(dto.itemType());
        entity.setSourceId(dto.sourceId());
        entity.setItemCode(dto.itemCode());
        entity.setItemName(dto.itemName());
        entity.setPricingMethod(dto.pricingMethod());
        entity.setUnitPrice(dto.unitPrice());
        entity.setDiscountPercentage(
                dto.discountPercentage()
        );

        entity.setIsActive(
                dto.isActive() == null
                        ? true
                        : dto.isActive()
        );

        PriceListSetupItem savedEntity =
                priceListSetupItemRepository.save(entity);

        return toDTO(savedEntity);
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

        entity.setWaseelItemMappingId(
                dto.waseelItemMappingId()
        );
        entity.setSbsCatalogId(dto.sbsCatalogId());
        entity.setItemType(dto.itemType());
        entity.setSourceId(dto.sourceId());
        entity.setItemCode(dto.itemCode());
        entity.setItemName(dto.itemName());
        entity.setPricingMethod(dto.pricingMethod());
        entity.setUnitPrice(dto.unitPrice());
        entity.setDiscountPercentage(
                dto.discountPercentage()
        );

        if (dto.isActive() != null) {
            entity.setIsActive(dto.isActive());
        }

        PriceListSetupItem savedEntity =
                priceListSetupItemRepository.save(entity);

        return toDTO(savedEntity);
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
    public Page<PriceListSetupItemDTO> findAll(
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
                entity.getPricingMethod(),
                entity.getUnitPrice(),
                entity.getDiscountPercentage(),
                entity.getIsActive()
        );
    }
}