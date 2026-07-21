package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Tax;
import com.dazzle.asklepios.domain.enumeration.TaxCalculationType;
import com.dazzle.asklepios.domain.enumeration.TaxType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaxRepository
        extends JpaRepository<Tax, Long> {

    Page<Tax> findAllByFacilityId(
            Long facilityId,
            Pageable pageable
    );

    Page<Tax> findAllByFacilityIdAndActiveTrue(
            Long facilityId,
            Pageable pageable
    );

    Page<Tax> findAllByFacilityIdAndCodeContainingIgnoreCase(
            Long facilityId,
            String code,
            Pageable pageable
    );

    Page<Tax> findAllByFacilityIdAndNameContainingIgnoreCase(
            Long facilityId,
            String name,
            Pageable pageable
    );

    Page<Tax> findAllByFacilityIdAndTaxType(
            Long facilityId,
            TaxType taxType,
            Pageable pageable
    );

    Page<Tax> findAllByFacilityIdAndCalculationType(
            Long facilityId,
            TaxCalculationType calculationType,
            Pageable pageable
    );

    Optional<Tax> findByFacilityIdAndCodeIgnoreCase(
            Long facilityId,
            String code
    );

    Optional<Tax>
    findFirstByFacilityIdAndIsDefaultTrueAndActiveTrue(
            Long facilityId
    );

    List<Tax> findAllByFacilityIdAndIsDefaultTrue(
            Long facilityId
    );

    List<Tax>
    findAllByFacilityIdAndActiveTrueAndValidFromLessThanEqualAndValidToIsNull(
            Long facilityId,
            LocalDate date
    );

    List<Tax>
    findAllByFacilityIdAndActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqual(
            Long facilityId,
            LocalDate date,
            LocalDate sameDate
    );

    boolean existsByFacilityIdAndCodeIgnoreCase(
            Long facilityId,
            String code
    );

    boolean existsByFacilityIdAndCodeIgnoreCaseAndIdNot(
            Long facilityId,
            String code,
            Long id
    );
}