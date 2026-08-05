package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.WaseelItemMapping;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WaseelItemMappingRepository extends JpaRepository<WaseelItemMapping, Long> {

    Optional<WaseelItemMapping> findByItemTypeAndSourceIdAndIsActiveTrue(
            BillingItemTypes itemType,
            Long sourceId
    );
}