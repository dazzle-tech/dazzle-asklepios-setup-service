package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PriceListSetupItem;
import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PriceListSetupItemRepository
        extends JpaRepository<PriceListSetupItem, Long> {

    Page<PriceListSetupItem> findAllByPriceListSetupId(
            Long priceListSetupId,
            Pageable pageable
    );

    @Query("""
            SELECT item
            FROM PriceListSetupItem item
            WHERE item.priceListSetupId = :priceListSetupId
              AND (:itemType IS NULL OR item.itemType = :itemType)
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(COALESCE(item.itemName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(COALESCE(item.itemCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<PriceListSetupItem> findAllByPriceListSetupIdAndItemNameContaining(
            @Param("priceListSetupId") Long priceListSetupId,
            @Param("search") String search,
            @Param("itemType") PriceListItemType itemType,
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
            PriceListItemType type,
            Long sourceId
    );

    boolean existsByPriceListSetupIdAndItemCode(
            Long priceListSetupId,
            String itemCode
    );

}