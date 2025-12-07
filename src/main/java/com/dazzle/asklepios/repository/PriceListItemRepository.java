package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PriceListItem;
import com.dazzle.asklepios.domain.enumeration.biling.PriceListItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceListItemRepository extends JpaRepository<PriceListItem, Long> {

    Page<PriceListItem> findByIsActiveTrue(Pageable pageable);

    Page<PriceListItem> findByPriceListId(Long priceListId, Pageable pageable);

    Page<PriceListItem> findByPriceListIdAndIsActiveTrue(Long priceListId, Pageable pageable);

    Page<PriceListItem> findByPriceListIdAndItemType(Long priceListId, PriceListItemType itemType, Pageable pageable);
}
