package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.enumeration.Currency;
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

    List<PriceListSetup>
    findAllByFacilityIdAndCurrencyAndStatusAndIsActiveTrue(
            Long facilityId,
            Currency currency,
            PriceListSetupStatus status
    );

    List<PriceListSetup>
    findAllByAppliesToAllFacilitiesTrueAndCurrencyAndStatusAndIsActiveTrue(
            Currency currency,
            PriceListSetupStatus status
    );

    List<PriceListSetup>
    findAllByFacilityIdAndTypeAndIsActiveTrue(
            Long facilityId,
            PriceListSetupType type
    );

    List<PriceListSetup>
    findAllByFacilityIdAndTypeInAndIsActiveTrue(
            Long facilityId,
            Collection<PriceListSetupType> types
    );

    List<PriceListSetup>
    findAllByAppliesToAllFacilitiesTrueAndTypeInAndIsActiveTrue(
            Collection<PriceListSetupType> types
    );

    List<PriceListSetup>
    findAllByFacilityIdAndPayerIdAndIsActiveTrue(
            Long facilityId,
            Long payerId
    );

    List<PriceListSetup>
    findAllByAppliesToAllFacilitiesTrueAndPayerIdAndIsActiveTrue(
            Long payerId
    );

    List<PriceListSetup>
    findAllByFacilityIdAndNphiesPayerIdAndIsActiveTrue(
            Long facilityId,
            Long nphiesPayerId
    );

    List<PriceListSetup>
    findAllByAppliesToAllFacilitiesTrueAndNphiesPayerIdAndIsActiveTrue(
            Long nphiesPayerId
    );

    Page<PriceListSetup>
    findAllByFacilityId(
            Long facilityId,
            Pageable pageable
    );

    @Query("""
            SELECT pls FROM PriceListSetup pls
            WHERE pls.facilityId = :facilityId
               OR pls.appliesToAllFacilities = TRUE
            """)
    Page<PriceListSetup> findAllVisibleToFacility(
            @Param("facilityId") Long facilityId,
            Pageable pageable
    );

    @Query("""
            SELECT COALESCE(MAX(pls.versionNumber), 0)
            FROM PriceListSetup pls
            WHERE pls.facilityId = :facilityId
              AND pls.type = :type
              AND (
                    (:payerId IS NULL AND pls.payerId IS NULL)
                    OR pls.payerId = :payerId
              )
              AND (
                    (:nphiesPayerId IS NULL AND pls.nphiesPayerId IS NULL)
                    OR pls.nphiesPayerId = :nphiesPayerId
              )
            """)
    Integer findMaxVersionNumber(
            @Param("facilityId") Long facilityId,
            @Param("type") PriceListSetupType type,
            @Param("payerId") Long payerId,
            @Param("nphiesPayerId") Long nphiesPayerId
    );

    @Query("""
            SELECT pls FROM PriceListSetup pls
            WHERE (
                    pls.payerId = :payerId
                    OR pls.nphiesPayerId = :payerId
            )
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(pls.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(COALESCE(pls.shortName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            ORDER BY pls.name
            """)
    Page<PriceListSetup> searchByPayerId(
            @Param("payerId") Long payerId,
            @Param("search") String search,
            Pageable pageable
    );
}
