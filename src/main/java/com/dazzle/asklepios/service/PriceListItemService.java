package com.dazzle.asklepios.service;

import com.dazzle.asklepios.domain.PriceListItem;
import com.dazzle.asklepios.domain.enumeration.biling.PriceListItemType;
import com.dazzle.asklepios.repository.PriceListItemRepository;
import com.dazzle.asklepios.web.rest.errors.BadRequestAlertException;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemSaveVM;
import com.dazzle.asklepios.web.rest.vm.pricelist.PriceListItemUpdateVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class PriceListItemService {

    private static final Logger LOG = LoggerFactory.getLogger(PriceListItemService.class);

    private final PriceListItemRepository repo;

    public PriceListItemService(PriceListItemRepository repo) {
        this.repo = repo;
    }

    // ------------ CREATE ------------
    public PriceListItem create(PriceListItemSaveVM vm) {
        LOG.debug("Create PriceListItem payload={}", vm);

        validatePolymorphicTarget(vm.itemType(), vm.serviceId(), vm.productId());

        PriceListItem item = PriceListItem.builder()
                .priceListId(vm.priceListId())
                .itemType(vm.itemType())
                .productType(vm.productType())
                .serviceId(vm.serviceId())
                .productId(vm.productId())
                .price(vm.price())
                .discountAllowed(vm.discountAllowed() != null ? vm.discountAllowed() : false)
                .isActive(vm.isActive() != null ? vm.isActive() : true)
                .build();

        return repo.save(item);
    }

    // ------------ UPDATE ------------
    public PriceListItem update(PriceListItemUpdateVM vm) {
        LOG.debug("Update PriceListItem payload={}", vm);

        PriceListItem existing = repo.findById(vm.id())
                .orElseThrow(() -> new BadRequestAlertException(
                        "notFound",
                        "priceListItem",
                        "Price list item not found."
                ));

        validatePolymorphicTarget(vm.itemType(), vm.serviceId(), vm.productId());

        existing.setPriceListId(vm.priceListId());
        existing.setItemType(vm.itemType());
        existing.setProductType(vm.productType());
        existing.setServiceId(vm.serviceId());
        existing.setProductId(vm.productId());

        existing.setPrice(vm.price());
        existing.setDiscountAllowed(vm.discountAllowed() != null ? vm.discountAllowed() : existing.getDiscountAllowed());
        existing.setIsActive(vm.isActive() != null ? vm.isActive() : existing.getIsActive());

        return repo.save(existing);
    }

    private void validatePolymorphicTarget(
            PriceListItemType itemType,
            Long serviceId,
            Long productId
    ) {
        if (itemType == PriceListItemType.SERVICE) {
            if (serviceId == null || productId != null) {
                throw new BadRequestAlertException(
                        "invalidTarget",
                        "priceListItem",
                        "SERVICE item must have serviceId only."
                );
            }
        }

        if (itemType == PriceListItemType.PRODUCT) {
            if (productId == null || serviceId != null) {
                throw new BadRequestAlertException(
                        "invalidTarget",
                        "priceListItem",
                        "PRODUCT item must have productId only."
                );
            }
        }
    }

    // ------------ READ ------------
    @Transactional(readOnly = true)
    public Page<PriceListItem> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<PriceListItem> findAllActive(Pageable pageable) {
        return repo.findByIsActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<PriceListItem> findByPriceList(Long priceListId, Pageable pageable) {
        return repo.findByPriceListId(priceListId, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<PriceListItem> findOne(Long id) {
        return repo.findById(id);
    }

    public Optional<PriceListItem> toggleIsActive(Long id) {
        return repo.findById(id)
                .map(it -> {
                    it.setIsActive(!Boolean.TRUE.equals(it.getIsActive()));
                    return repo.save(it);
                });
    }
}
