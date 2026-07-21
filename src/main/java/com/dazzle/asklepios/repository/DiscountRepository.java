package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Discount;
import com.dazzle.asklepios.domain.enumeration.DiscountApplicableOn;
import com.dazzle.asklepios.domain.enumeration.DiscountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository
        extends JpaRepository<Discount, Long> {

    Page<Discount> findAllByFacilityId(
            Long facilityId,
            Pageable pageable
    );

    Page<Discount> findAllByFacilityIdAndActiveTrue(
            Long facilityId,
            Pageable pageable
    );

    Page<Discount> findAllByFacilityIdAndCodeContainingIgnoreCase(
            Long facilityId,
            String code,
            Pageable pageable
    );

    Page<Discount> findAllByFacilityIdAndNameContainingIgnoreCase(
            Long facilityId,
            String name,
            Pageable pageable
    );

    Page<Discount> findAllByFacilityIdAndDiscountType(
            Long facilityId,
            DiscountType discountType,
            Pageable pageable
    );

    Page<Discount> findAllByFacilityIdAndApplicableOn(
            Long facilityId,
            DiscountApplicableOn applicableOn,
            Pageable pageable
    );

    Optional<Discount> findByFacilityIdAndCodeIgnoreCase(
            Long facilityId,
            String code
    );

    Optional<Discount>
    findFirstByFacilityIdAndIsDefaultTrueAndActiveTrue(
            Long facilityId
    );

    List<Discount> findAllByFacilityIdAndIsDefaultTrue(
            Long facilityId
    );

    List<Discount>
    findAllByFacilityIdAndActiveTrueAndValidFromLessThanEqualAndValidToIsNull(
            Long facilityId,
            LocalDate date
    );

    List<Discount>
    findAllByFacilityIdAndActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqual(
            Long facilityId,
            LocalDate validFrom,
            LocalDate validTo
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