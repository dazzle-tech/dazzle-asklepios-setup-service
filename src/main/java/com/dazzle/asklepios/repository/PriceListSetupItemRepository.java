package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PriceListSetupItem;
import com.dazzle.asklepios.domain.enumeration.PriceListItemType;
import com.dazzle.asklepios.domain.enumeration.EncounterType;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupStatus;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceListSetupItemRepository
        extends JpaRepository<PriceListSetupItem, Long> {

    Page<PriceListSetupItem> findAllByPriceListSetupId(
            Long priceListSetupId,
            Pageable pageable
    );

    List<PriceListSetupItem> findAllByPriceListSetupId(
            Long priceListSetupId
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
                    OR LOWER(COALESCE(item.nonStandardCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
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

    Optional<PriceListSetupItem>
    findByPriceListSetupIdAndItemCode(
            Long priceListSetupId,
            String itemCode
    );

    Optional<PriceListSetupItem>
    findFirstByPriceListSetupIdAndItemTypeAndSourceId(
            Long priceListSetupId,
            PriceListItemType type,
            Long sourceId
    );

    List<PriceListSetupItem>
    findAllByPriceListSetupIdAndItemTypeAndSourceId(
            Long priceListSetupId,
            PriceListItemType type,
            Long sourceId
    );

    Optional<PriceListSetupItem>
    findFirstByPriceListSetupIdAndItemTypeAndSourceIdAndVisitType(
            Long priceListSetupId,
            PriceListItemType type,
            Long sourceId,
            EncounterType visitType
    );

    List<PriceListSetupItem> findAllByItemTypeAndSourceId(
            PriceListItemType type,
            Long sourceId
    );

    List<PriceListSetupItem> findAllByPriceListSetupIdInAndItemTypeAndSourceId(
            Collection<Long> priceListSetupIds,
            PriceListItemType itemType,
            Long sourceId
    );

    List<PriceListSetupItem> findAllByPriceListSetupIdInAndItemTypeInAndSourceIdIn(
            Collection<Long> priceListSetupIds,
            Collection<PriceListItemType> itemTypes,
            Collection<Long> sourceIds
    );

    Page<PriceListSetupItem> findAllByPriceListSetupIdIn(
            Collection<Long> priceListSetupIds,
            Pageable pageable
    );

    Page<PriceListSetupItem> findAllByPriceListSetupIdInAndItemType(
            Collection<Long> priceListSetupIds,
            PriceListItemType itemType,
            Pageable pageable
    );

    Page<PriceListSetupItem> findAllByPriceListSetupIdInAndItemNameContainingIgnoreCase(
            Collection<Long> priceListSetupIds,
            String itemName,
            Pageable pageable
    );

    Page<PriceListSetupItem> findAllByPriceListSetupIdInAndItemCodeContainingIgnoreCase(
            Collection<Long> priceListSetupIds,
            String itemCode,
            Pageable pageable
    );

    Page<PriceListSetupItem> findAllByPriceListSetupIdInAndItemTypeAndItemNameContainingIgnoreCase(
            Collection<Long> priceListSetupIds,
            PriceListItemType itemType,
            String itemName,
            Pageable pageable
    );

    Page<PriceListSetupItem> findAllByPriceListSetupIdInAndItemTypeAndItemCodeContainingIgnoreCase(
            Collection<Long> priceListSetupIds,
            PriceListItemType itemType,
            String itemCode,
            Pageable pageable
    );

    long countByPriceListSetupIdIn(Collection<Long> priceListSetupIds);

    long countByPriceListSetupIdInAndItemType(
            Collection<Long> priceListSetupIds,
            PriceListItemType itemType
    );

    List<PriceListSetupItem> findAllBySourceId(Long sourceId);
    @Query("""
            SELECT item
            FROM PriceListSetupItem item
            WHERE item.itemType = :itemType
              AND item.sourceId = :sourceId
              AND item.isActive = true
              AND EXISTS (
                    SELECT 1
                    FROM PriceListSetup setup
                    WHERE setup.id = item.priceListSetupId
                      AND setup.isActive = true
                      AND setup.status = :status
                      AND setup.type = :setupType
                      AND (:facilityId IS NULL OR setup.facilityId = :facilityId)
              )
            ORDER BY item.id DESC
            """)
    List<PriceListSetupItem> findActiveInsuranceItemsByCatalog(
            @Param("itemType") PriceListItemType itemType,
            @Param("sourceId") Long sourceId,
            @Param("facilityId") Long facilityId,
            @Param("status") PriceListSetupStatus status,
            @Param("setupType") PriceListSetupType setupType
    );

}