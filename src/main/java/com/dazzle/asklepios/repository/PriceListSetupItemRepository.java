package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PriceListSetupItem;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PriceListSetupItemRepository
        extends JpaRepository<PriceListSetupItem, Long> {

    Page<PriceListSetupItem> findAllByPriceListSetupId(
            Long priceListSetupId,
            Pageable pageable
    );

    Optional<PriceListSetupItem>
    findByIdAndPriceListSetupId(
            Long id,
            Long priceListSetupId
    );
    Optional<PriceListSetupItem>
    findFirstByPriceListSetupIdAndItemTypeAndSourceIdAndIsActiveTrue(
            Long priceListSetupId,
            BillingItemTypes type,
            Long sourceId
    );

}