package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupStatus;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupType;
import com.dazzle.asklepios.domain.enumeration.biling.PriceListStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceListSetupRepository
        extends JpaRepository<PriceListSetup, Long> {

    boolean existsByFacilityIdAndTypeAndPayerIdAndVersionNumber(
            Long facilityId,
            PriceListSetupType type,
            Long payerId,
            Integer versionNumber
    );

    List<PriceListSetup> findAllByFacilityIdAndIsActiveTrue(
            Long facilityId
    );

    Optional<PriceListSetup>
    findFirstByFacilityIdAndTypeAndPayerIdAndStatusAndIsActiveTrueAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqualOrderByVersionNumberDesc(
            Long facilityId,
            PriceListSetupType type,
            Long payerId,
            PriceListSetupStatus status,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    );


    List<PriceListSetup>
    findAllByFacilityIdAndCurrencyAndStatusAndIsActiveTrue(
            Long facilityId,
            Currency currency,
            PriceListSetupStatus status
    );

    
}