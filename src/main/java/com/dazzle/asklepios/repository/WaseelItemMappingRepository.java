package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.WaseelItemMapping;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WaseelItemMappingRepository extends JpaRepository<WaseelItemMapping, Long> {

    @Query("""
            SELECT mapping
            FROM WaseelItemMapping mapping
            WHERE mapping.isActive = true
              AND (:itemType IS NULL OR mapping.itemType = :itemType)
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(COALESCE(mapping.itemName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(COALESCE(mapping.itemCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(COALESCE(mapping.sbsCatalog.sbsCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<WaseelItemMapping> searchActiveMappings(
            @Param("itemType") BillingItemTypes itemType,
            @Param("search") String search,
            Pageable pageable
    );

    Optional<WaseelItemMapping> findByItemTypeAndSourceIdAndIsActiveTrue(
            BillingItemTypes itemType,
            Long sourceId
    );
}